package com.songajae.amgi.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface OwnedPackDao {
    @Query(
        "SELECT * FROM owned_packs WHERE user_id = :userId AND device_id = :deviceId ORDER BY title"
    )
    suspend fun getOwnedPacks(userId: String, deviceId: String): List<OwnedPackEntity>

    @Query(
        "SELECT pack_id FROM owned_packs WHERE user_id = :userId AND device_id = :deviceId"
    )
    suspend fun getOwnedPackIds(userId: String, deviceId: String): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(packs: List<OwnedPackEntity>)

    @Query("DELETE FROM owned_packs WHERE user_id = :userId AND device_id = :deviceId")
    suspend fun clear(userId: String, deviceId: String)
}