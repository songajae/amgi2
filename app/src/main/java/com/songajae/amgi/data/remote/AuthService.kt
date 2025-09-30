package com.songajae.amgi.data.remote

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.songajae.amgi.util.Result
import kotlinx.coroutines.tasks.await

object AuthService {
    fun isLoggedIn() = Firebase.auth.currentUser != null
    fun uid(): String? = Firebase.auth.currentUser?.uid

    suspend fun login(email: String, pw: String): Result<Unit> = try {
        Firebase.auth.signInWithEmailAndPassword(email, pw).await()
        Result.Success(Unit)
    } catch (e: Exception) { Result.Error(e.message ?: "로그인 실패") }

    suspend fun signup(email: String, pw: String): Result<Unit> = try {
        Firebase.auth.createUserWithEmailAndPassword(email, pw).await()
        Result.Success(Unit)
    } catch (e: Exception) { Result.Error(e.message ?: "가입 실패") }

    fun logout() = Firebase.auth.signOut()
}
