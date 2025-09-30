package com.songajae.amgi.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.songajae.amgi.util.Result
import kotlinx.coroutines.tasks.await

object AuthService {
    private fun authOrNull(): FirebaseAuth? = try {
        Firebase.auth
    } catch (_: IllegalStateException) {
        null
    }

    fun isLoggedIn() = authOrNull()?.currentUser != null
    fun uid(): String? = authOrNull()?.currentUser?.uid

    suspend fun login(email: String, pw: String): Result<Unit> = try {
        val auth = authOrNull() ?: return Result.Error("Firebase 인증을 사용할 수 없습니다.")
        auth.signInWithEmailAndPassword(email, pw).await()
        Result.Success(Unit)
    } catch (e: Exception) { Result.Error(e.message ?: "로그인 실패") }

    suspend fun signup(email: String, pw: String): Result<Unit> = try {
        val auth = authOrNull() ?: return Result.Error("Firebase 인증을 사용할 수 없습니다.")
        auth.createUserWithEmailAndPassword(email, pw).await()
        Result.Success(Unit)
    } catch (e: Exception) { Result.Error(e.message ?: "가입 실패") }

    fun logout() = authOrNull()?.signOut()
}
