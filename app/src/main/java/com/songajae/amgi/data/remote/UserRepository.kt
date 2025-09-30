package com.songajae.amgi.data.remote

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.songajae.amgi.util.Result
import kotlinx.coroutines.tasks.await

object UserRepository {
    private val db get() = Firebase.firestore

    suspend fun createProfile(name: String, email: String): Result<Unit> = try {
        val uid = AuthService.uid() ?: return Result.Error("로그인 필요")
        val now = System.currentTimeMillis()
        db.collection("users").document(uid).set(
            mapOf(
                "userId" to uid, "name" to name, "email" to email,
                "isActive" to true, "createdAt" to now, "updatedAt" to now
            )
        ).await()
        Result.Success(Unit)
    } catch (e: Exception) { Result.Error(e.message ?: "프로필 생성 실패") }
}
