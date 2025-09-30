package com.songajae.amgi.data.packs

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.songajae.amgi.core.io.EncryptedIO
import com.songajae.amgi.data.remote.AuthService
import com.songajae.amgi.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

data class ContentPack(
    val id: String,
    val title: String,
    val description: String,
    val chapterCount: Int
)

object PackRepository {
    private const val CACHE_FILE = "packs_cache.json"

    private fun firestoreOrNull(): FirebaseFirestore? = try {
        Firebase.firestore
    } catch (_: IllegalStateException) {
        null
    }


    suspend fun fetchRemotePacks(): Result<List<ContentPack>> = withContext(Dispatchers.IO) {
        val uid = AuthService.uid() ?: return@withContext Result.Error("로그인이 필요합니다.")
        val db = firestoreOrNull()
            ?: return@withContext Result.Error("Firebase 서비스를 사용할 수 없습니다.")
        return@withContext runCatching {
            val snap = db.collection("users").document(uid).collection("packs").get().await()
            val packs = snap.documents.map { doc ->
                ContentPack(
                    id = doc.id,
                    title = doc.getString("title") ?: "제목 미정",
                    description = doc.getString("description") ?: "",
                    chapterCount = (doc.getLong("chapterCount") ?: 0L).toInt()
                )
            }
            Result.Success(packs)
        }.getOrElse { throwable ->
            Result.Error(throwable.message ?: "콘텐츠를 불러오지 못했습니다.")
        }
    }

    suspend fun cachePacks(ctx: Context, packs: List<ContentPack>) = withContext(Dispatchers.IO) {
        val json = JSONArray().apply {
            packs.forEach { pack ->
                put(
                    JSONObject().apply {
                        put("id", pack.id)
                        put("title", pack.title)
                        put("description", pack.description)
                        put("chapterCount", pack.chapterCount)
                    }
                )
            }
        }
        EncryptedIO.writeString(ctx, CACHE_FILE, json.toString())
    }

    suspend fun loadCachedPacks(ctx: Context): List<ContentPack> = withContext(Dispatchers.IO) {
        val raw = EncryptedIO.readString(ctx, CACHE_FILE) ?: return@withContext emptyList()
        return@withContext runCatching {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    add(
                        ContentPack(
                            id = obj.optString("id"),
                            title = obj.optString("title"),
                            description = obj.optString("description"),
                            chapterCount = obj.optInt("chapterCount")
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }
}