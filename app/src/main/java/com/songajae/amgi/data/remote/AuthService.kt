package com.songajae.amgi.data.remote

import com.songajae.amgi.util.Result
import com.songajae.amgi.R
import com.songajae.amgi.util.AppStringProvider
import kotlinx.coroutines.tasks.await

object AuthService {
    fun isLoggedIn() = FirebaseServiceProvider.authOrNull()?.currentUser != null
    fun uid(): String? = FirebaseServiceProvider.authOrNull()?.currentUser?.uid

    suspend fun login(email: String, pw: String): Result<Unit> = try {
        val auth = FirebaseServiceProvider.authOrNull()
            ?: return Result.Error(AppStringProvider.get(R.string.error_firebase_auth_unavailable))
        auth.signInWithEmailAndPassword(email, pw).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: AppStringProvider.get(R.string.error_login_failure))
    }

    suspend fun signup(email: String, pw: String): Result<Unit> = try {
        val auth = FirebaseServiceProvider.authOrNull()
            ?: return Result.Error(AppStringProvider.get(R.string.error_firebase_auth_unavailable))
        auth.createUserWithEmailAndPassword(email, pw).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: AppStringProvider.get(R.string.error_signup_failure))
    }

    fun logout() = FirebaseServiceProvider.authOrNull()?.signOut()
}
