package com.songajae.amgi.data.remote

import android.content.Context
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.songajae.amgi.data.local.LeaseCache
import kotlinx.coroutines.tasks.await

sealed class DevicePolicyResult {
    data object Approved : DevicePolicyResult()
    data object LimitReached : DevicePolicyResult()
    data class Error(val message: String, val throwable: Throwable? = null) : DevicePolicyResult()
}


object AdminApi {

    private const val MAX_DEVICE_COUNT = 5
    private fun firestoreOrNull(): FirebaseFirestore? = try {
        Firebase.firestore
    } catch (_: IllegalStateException) {
        null
    }


    suspend fun listDevicesPretty(): List<String> {
        val uid = AuthService.uid() ?: return emptyList()
        val db = firestoreOrNull() ?: return emptyList()
        val snap = db.collection("users").document(uid)
            .collection("device_registry").get().await()
        return snap.documents.map { it.id }
    }

    suspend fun deleteDeviceByIndex(index: Int) {
        val uid = AuthService.uid() ?: return
        val db = firestoreOrNull() ?: return
        val docs = db.collection("users").document(uid)
            .collection("device_registry").get().await().documents
        docs.getOrNull(index)?.reference?.delete()?.await()
    }

    suspend fun processDevicePolicyAndPing(ctx: Context, deviceId: String): DevicePolicyResult {
        val uid = AuthService.uid() ?: return DevicePolicyResult.Error("로그인이 필요합니다.")
        val db = firestoreOrNull()
            ?: return DevicePolicyResult.Error("Firebase 서비스를 사용할 수 없습니다.")
        val regCol = db.collection("users").document(uid).collection("device_registry")
        return try {
            val docs = regCol.get().await().documents
            val exists = docs.any { it.id == deviceId }
            if (!exists) {
                if (docs.size >= MAX_DEVICE_COUNT) {
                    return DevicePolicyResult.LimitReached
                }
                regCol.document(deviceId).set(
                    mapOf(
                        "model" to android.os.Build.MODEL,
                        "os" to "Android",
                        "firstSeenAt" to FieldValue.serverTimestamp(),
                        "lastSeenAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                regCol.document(deviceId)
                    .update("lastSeenAt", FieldValue.serverTimestamp())
                    .await()
            }
            LeaseCache.save(ctx, deviceId, System.currentTimeMillis())
            DevicePolicyResult.Approved
        } catch (t: Throwable) {
            DevicePolicyResult.Error(t.message ?: "기기 정책 확인 중 문제가 발생했습니다.", t)
        }
    }
}
