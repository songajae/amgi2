package com.songajae.amgi.data.packs

import android.content.Context
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.songajae.amgi.R
import com.songajae.amgi.data.remote.AuthService
import com.songajae.amgi.data.remote.FirebaseServiceProvider
import com.songajae.amgi.util.AppStringProvider
import com.songajae.amgi.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
object PackRepository {
    private const val MAX_IN_QUERY = 10
    private val userPackCollections = listOf("language_packs", "word_packs", "packs")

    suspend fun fetchRemotePacks(): Result<List<ContentPack>> = withContext(Dispatchers.IO) {
        val uid = AuthService.uid()
            ?: return@withContext Result.Error(
                AppStringProvider.get(R.string.error_login_required_long)
            )
        val db = FirebaseServiceProvider.firestoreOrNull()
            ?: return@withContext Result.Error(
                AppStringProvider.get(R.string.error_firebase_service_unavailable)
            )
        return@withContext try {
            val userDoc = db.collection("users").document(uid)
            val ownedPackIds = fetchOwnedPackIds(userDoc)
            if (ownedPackIds.isEmpty()) {
                Result.Success(emptyList())
            } else {
                val packs = fetchPacksForIds(userDoc, db, ownedPackIds)
                Result.Success(packs)
            }

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
        OwnedPackLocalDataSource.replaceOwnedPacks(ctx, uid, deviceId, packs)
    }

    suspend fun loadCachedPacks(
        ctx: Context,
        uid: String,
        deviceId: String
    ): List<ContentPack> = withContext(Dispatchers.IO) {
        OwnedPackLocalDataSource.loadOwnedPacks(ctx, uid, deviceId)
    }

    private suspend fun fetchOwnedPackIds(userDoc: DocumentReference): Set<String> {
        val snapshot = userDoc.get().await()
        val raw = snapshot.get("ownedPacks")
        return extractOwnedPackIds(raw)
    }

    private fun extractOwnedPackIds(raw: Any?): Set<String> = when (raw) {
        is Map<*, *> -> raw.keys.mapNotNull { it as? String }.toSet()
        is List<*> -> raw.mapNotNull { element ->
            when (element) {
                is String -> element
                is Map<*, *> -> mapToPackId(element)
                else -> null
            }
        }.toSet()
        is String -> setOf(raw)
        else -> emptySet()
    }

    private fun mapToPackId(raw: Map<*, *>): String? {
        val direct = listOf("id", "packId", "pack_id")
            .firstNotNullOfOrNull { key -> raw[key] as? String }
        if (!direct.isNullOrBlank()) return direct
        raw.entries.forEach { (key, value) ->
            if (key is String && key.isNotBlank()) {
                when (value) {
                    true, 1, 1L, null -> return key
                    is String -> if (value.equals("true", ignoreCase = true)) return key
                }
            }
        }
        return null
    }

    private suspend fun fetchPacksForIds(
        userDoc: DocumentReference,
        db: FirebaseFirestore,
        ownedPackIds: Set<String>
    ): List<ContentPack> {
        if (ownedPackIds.isEmpty()) return emptyList()
        val remaining = ownedPackIds.toMutableSet()
        val results = mutableListOf<ContentPack>()

        for (collection in userPackCollections) {
            if (remaining.isEmpty()) break
            collectFromCollection(remaining, results) { chunk ->
                userDoc.collection(collection)
                    .whereIn(FieldPath.documentId(), chunk)
                    .get()
                    .await()
                    .documents
            }
        }

        if (remaining.isNotEmpty()) {
            for (collection in userPackCollections) {
                if (remaining.isEmpty()) break
                collectFromCollection(remaining, results) { chunk ->
                    db.collection(collection)
                        .whereIn(FieldPath.documentId(), chunk)
                        .get()
                        .await()
                        .documents
                }
            }
        }

        return results
    }
}
private suspend fun collectFromCollection(
    remaining: MutableSet<String>,
    results: MutableList<ContentPack>,
    fetcher: suspend (List<String>) -> List<DocumentSnapshot>
) {
    val chunks = remaining.chunked(MAX_IN_QUERY)
    for (chunk in chunks) {
        if (chunk.isEmpty()) continue
        val documents = runCatching { fetcher(chunk) }.getOrDefault(emptyList())
        documents.forEach { document ->
            if (!document.exists()) return@forEach
            results.add(document.toContentPack())
            remaining.remove(document.id)
        }
    }
}

private fun DocumentSnapshot.toContentPack(): ContentPack = ContentPack(
    id = id,
    title = getString("title") ?: AppStringProvider.get(R.string.pack_title_unknown),
    description = getString("description") ?: "",
    chapterCount = (getLong("chapterCount") ?: 0L).toInt()
)
}