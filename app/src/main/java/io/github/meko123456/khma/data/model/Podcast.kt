package io.github.meko123456.khma.data.model

/** A podcast parsed from an RSS feed, with its episodes. */
data class Podcast(
    val feedUrl: String,
    val title: String,
    val author: String = "",
    val description: String = "",
    val imageUrl: String? = null,
    val episodes: List<Episode> = emptyList(),
)

/** One episode. [audioUrl] is the enclosure media URL; [durationSeconds] is 0 when unknown. */
data class Episode(
    val guid: String,
    val title: String,
    val audioUrl: String,
    val description: String = "",
    val imageUrl: String? = null,
    val durationSeconds: Int = 0,
    val pubDateMillis: Long = 0,
)
