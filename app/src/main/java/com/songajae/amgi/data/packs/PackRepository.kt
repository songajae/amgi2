package com.songajae.amgi.data.packs

import android.content.Context
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.songajae.amgi.R
import com.songajae.amgi.util.AppStringProvider
import com.songajae.amgi.core.io.EncryptedIO
import com.songajae.amgi.data.remote.AuthService
import com.songajae.amgi.data.remote.FirebaseServiceProvider
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
    private val cacheKeySanitizer = Regex("[^A-Za-z0-9_.-]")

    private fun cacheFile(uid: String, deviceId: String): String {
        fun sanitize(value: String) = value.replace(cacheKeySanitizer, "_")
        return "packs_cache_${sanitize(uid)}_${sanitize(deviceId)}.json"
    }

    suspend fun fetchRemotePacks(): Result<List<ContentPack>> = withContext(Dispatchers.IO) {
        val uid = AuthService.uid()
            ?: return@withContext Result.Error(AppStringProvider.get(R.string.error_login_required_long))
        val db = FirebaseServiceProvider.firestoreOrNull()
            ?: return@withContext Result.Error(AppStringProvider.get(R.string.error_firebase_service_unavailable))
        return@withContext try {
            val userDoc = db.collection("users").document(uid)
            val packCollections = listOf("language_packs", "word_packs", "packs")
            val documents = mutableListOf<DocumentSnapshot>()
            for (collection in packCollections) {
                val snapshot = userDoc.collection(collection).get().await()
                if (snapshot.isEmpty) {
                    continue
                }
                documents.addAll(snapshot.documents)
                break
            }
            val packs = documents.map { doc ->
                ContentPack(
                    id = doc.id,
                    title = doc.getString("title") ?: AppStringProvider.get(R.string.pack_title_unknown),
                    description = doc.getString("description") ?: "",
                    chapterCount = (doc.getLong("chapterCount") ?: 0L).toInt()
                )
            }.toList()
            Result.Success(packs)
        } catch (throwable: Throwable) {
            Result.Error(
                throwable.message ?: AppStringProvider.get(R.string.error_content_fetch_failed)
            )
        }
    }

    suspend fun cachePacks(
        ctx: Context,
        uid: String,
        deviceId: String,
        packs: List<ContentPack>
    ) = withContext(Dispatchers.IO) {
        if (packs.isEmpty()) {
            EncryptedIO.delete(ctx, cacheFile(uid, deviceId))
            return@withContext
        }
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
        EncryptedIO.writeString(ctx, cacheFile(uid, deviceId), json.toString())
    }

    suspend fun loadCachedPacks(
        ctx: Context,
        uid: String,
        deviceId: String
    ): List<ContentPack> = withContext(Dispatchers.IO) {
        val raw = EncryptedIO.readString(ctx, cacheFile(uid, deviceId))
            ?: return@withContext emptyList()
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