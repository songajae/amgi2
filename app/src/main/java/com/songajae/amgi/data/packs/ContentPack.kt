package com.songajae.amgi.data.packs

/**
 * Represents a downloadable language/word pack owned by the user.
 */
data class ContentPack(
    val id: String,
    val title: String,
    val description: String,
    val chapterCount: Int
)