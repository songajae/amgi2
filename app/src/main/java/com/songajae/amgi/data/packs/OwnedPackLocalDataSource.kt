package com.songajae.amgi.data.packs

import android.content.Context
import androidx.room.withTransaction
import com.songajae.amgi.data.local.db.OwnedPackDatabase
import com.songajae.amgi.data.local.db.OwnedPackEntity

/**
 * Handles persistence of owned packs using Room.
 */
object OwnedPackLocalDataSource {
    private fun db(ctx: Context) = OwnedPackDatabase.getInstance(ctx.applicationContext)

    suspend fun replaceOwnedPacks(
        ctx: Context,
        userId: String,
        deviceId: String,
        packs: List<ContentPack>
    ) {
        val database = db(ctx)
        database.withTransaction {
            val dao = database.ownedPackDao()
            dao.clear(userId, deviceId)
            if (packs.isNotEmpty()) {
                dao.insertAll(packs.map { OwnedPackEntity.from(userId, deviceId, it) })
            }
        }
    }

    suspend fun loadOwnedPacks(
        ctx: Context,
        userId: String,
        deviceId: String
    ): List<ContentPack> {
        return db(ctx).ownedPackDao()
            .getOwnedPacks(userId, deviceId)
            .map { it.toContentPack() }
    }

    suspend fun loadOwnedPackIds(
        ctx: Context,
        userId: String,
        deviceId: String
    ): Set<String> {
        return db(ctx).ownedPackDao()
            .getOwnedPackIds(userId, deviceId)
            .toSet()
    }
}