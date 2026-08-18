package io.github.meko123456.khma.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** A subscribed podcast, keyed by its feed URL. */
@Entity(tableName = "podcasts")
data class PodcastEntity(
    @PrimaryKey val feedUrl: String,
    val title: String,
    val author: String,
    val description: String,
    val imageUrl: String?,
)

/**
 * One episode of a subscribed podcast. Keyed by (feedUrl, guid) and cascade-deleted
 * with its podcast. Carries playback + download state used by later features.
 */
@Entity(
    tableName = "episodes",
    primaryKeys = ["feedUrl", "guid"],
    foreignKeys = [
        ForeignKey(
            entity = PodcastEntity::class,
            parentColumns = ["feedUrl"],
            childColumns = ["feedUrl"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("feedUrl")],
)
data class EpisodeEntity(
    val feedUrl: String,
    val guid: String,
    val title: String,
    val audioUrl: String,
    val description: String,
    val imageUrl: String?,
    val durationSeconds: Int,
    val pubDateMillis: Long,
    val positionMillis: Long = 0,
    val finished: Boolean = false,
    val downloadPath: String? = null,
)
