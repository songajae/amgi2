package com.songajae.amgi.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.songajae.amgi.data.remote.AuthService
import com.songajae.amgi.data.remote.UserRepository
import com.songajae.amgi.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val _state = MutableStateFlow<Result<Unit>>(Result.Idle)
    val state: StateFlow<Result<Unit>> = _state

    fun login(email: String, pw: String) = viewModelScope.launch {
        _state.value = Result.Loading
        _state.value = AuthService.login(email, pw)
    }

    fun signup(name: String, email: String, pw: String) = viewModelScope.launch {
        _state.value = Result.Loading
        val r = AuthService.signup(email, pw)
        _state.value = if (r is Result.Success) UserRepository.createProfile(name, email) else r
    }

    fun resetState() {
        _state.value = Result.Idle
    }
}
