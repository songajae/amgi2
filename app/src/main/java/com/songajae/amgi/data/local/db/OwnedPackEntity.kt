package com.songajae.amgi.data.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.songajae.amgi.data.packs.ContentPack

@Entity(
    tableName = "owned_packs",
    primaryKeys = ["pack_id", "user_id", "device_id"]
)
data class OwnedPackEntity(
    @ColumnInfo(name = "pack_id") val packId: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "device_id") val deviceId: String,
    val title: String,
    val description: String,
    @ColumnInfo(name = "chapter_count") val chapterCount: Int
) {
    fun toContentPack(): ContentPack = ContentPack(
        id = packId,
        title = title,
        description = description,
        chapterCount = chapterCount
    )

    companion object {
        fun from(userId: String, deviceId: String, pack: ContentPack): OwnedPackEntity =
            OwnedPackEntity(
                packId = pack.id,
                userId = userId,
                deviceId = deviceId,
                title = pack.title,
                description = pack.description,
                chapterCount = pack.chapterCount
            )
    }
}