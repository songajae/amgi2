package com.songajae.amgi.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.songajae.amgi.util.Result
import kotlinx.coroutines.tasks.await

object UserRepository {
    private fun firestoreOrNull(): FirebaseFirestore? = try {
        Firebase.firestore
    } catch (_: IllegalStateException) {
        null
    }

    suspend fun createProfile(name: String, email: String): Result<Unit> = try {
        val uid = AuthService.uid() ?: return Result.Error("로그인 필요")
        val now = System.currentTimeMillis()
        val db = firestoreOrNull() ?: return Result.Error("Firebase 서비스를 사용할 수 없습니다.")
        db.collection("users").document(uid).set(
            mapOf(
                "userId" to uid, "name" to name, "email" to email,
                "isActive" to true, "createdAt" to now, "updatedAt" to now
            )
        ).await()
        Result.Success(Unit)
    } catch (e: Exception) { Result.Error(e.message ?: "프로필 생성 실패") }
}
