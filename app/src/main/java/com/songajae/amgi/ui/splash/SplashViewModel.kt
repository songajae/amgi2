package com.songajae.amgi.ui.splash

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.songajae.amgi.core.license.LeaseManager
import com.songajae.amgi.core.net.NetworkGate
import com.songajae.amgi.core.sync.SyncManager
import com.songajae.amgi.data.local.DeviceIdStore
import com.songajae.amgi.data.packs.ContentPack
import com.songajae.amgi.data.packs.PackRepository
import com.songajae.amgi.data.remote.AuthService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SplashViewModel(
    private val networkGate: NetworkGate = NetworkGate,
    private val leaseManager: LeaseManager = LeaseManager,
    private val syncManager: SyncManager = SyncManager
) : ViewModel() {

    sealed class SplashState {
        data object Idle : SplashState()
        data object Loading : SplashState()
        data object NavigateLogin : SplashState()
        data class NavigateHome(val packs: List<ContentPack>) : SplashState()
        data object NavigateDeviceLimit : SplashState()
        data class Error(val message: String) : SplashState()
    }

    private val _state = MutableStateFlow<SplashState>(SplashState.Idle)
    val state: StateFlow<SplashState> = _state

    fun bootstrap(ctx: Context) {
        viewModelScope.launch {
            _state.value = SplashState.Loading
            val deviceId = DeviceIdStore.getOrCreate(ctx)
            val online = networkGate.isOnline(ctx)

            if (!online) {
                val uid = AuthService.uid()
                if (uid != null && leaseManager.isLeaseValidOffline(ctx, deviceId)) {
                    val cached = PackRepository.loadCachedPacks(ctx, uid, deviceId)
                    _state.value = SplashState.NavigateHome(cached)
                } else {
                    _state.value = SplashState.NavigateLogin
                }
                return@launch
            }

            if (!AuthService.isLoggedIn()) {
                _state.value = SplashState.NavigateLogin
                return@launch
            }

            when (val result = syncManager.sync(ctx, deviceId)) {
                is SyncManager.SyncResult.Success -> _state.value = SplashState.NavigateHome(result.packs)
                SyncManager.SyncResult.DeviceLimitReached -> _state.value = SplashState.NavigateDeviceLimit
                is SyncManager.SyncResult.Failure -> _state.value = SplashState.Error(result.message)
            }
        }
    }

    fun markHandled() {
        _state.value = SplashState.Idle
    }
}
