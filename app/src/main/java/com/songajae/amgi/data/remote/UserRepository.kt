package com.songajae.amgi.data.remote

import com.songajae.amgi.util.Result
import com.songajae.amgi.R
import com.songajae.amgi.util.AppStringProvider
import kotlinx.coroutines.tasks.await

object UserRepository {
    suspend fun createProfile(name: String, email: String): Result<Unit> = try {
        val uid = AuthService.uid()
            ?: return Result.Error(AppStringProvider.get(R.string.error_login_required_short))
        val now = System.currentTimeMillis()
        val db = FirebaseServiceProvider.firestoreOrNull()
            ?: return Result.Error(AppStringProvider.get(R.string.error_firebase_service_unavailable))
        db.collection("users").document(uid).set(
            mapOf(
                "userId" to uid, "name" to name, "email" to email,
                "isActive" to true, "createdAt" to now, "updatedAt" to now
            )
        ).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: AppStringProvider.get(R.string.error_profile_creation_failure))
    }
}
