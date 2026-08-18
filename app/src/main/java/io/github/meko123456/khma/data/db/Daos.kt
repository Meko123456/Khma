package io.github.meko123456.khma.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PodcastDao {
    @Query("SELECT * FROM podcasts ORDER BY title COLLATE NOCASE")
    fun observeAll(): Flow<List<PodcastEntity>>

    @Query("SELECT * FROM podcasts WHERE feedUrl = :feedUrl")
    fun observe(feedUrl: String): Flow<PodcastEntity?>

    @Upsert
    suspend fun upsert(podcast: PodcastEntity)

    @Query("DELETE FROM podcasts WHERE feedUrl = :feedUrl")
    suspend fun delete(feedUrl: String)
}

@Dao
interface EpisodeDao {
    @Query("SELECT * FROM episodes WHERE feedUrl = :feedUrl ORDER BY pubDateMillis DESC")
    fun observeForPodcast(feedUrl: String): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM episodes WHERE feedUrl = :feedUrl AND guid = :guid")
    suspend fun byId(feedUrl: String, guid: String): EpisodeEntity?

    @Upsert
    suspend fun upsertAll(episodes: List<EpisodeEntity>)

    @Query("UPDATE episodes SET positionMillis = :positionMillis, finished = :finished WHERE feedUrl = :feedUrl AND guid = :guid")
    suspend fun updateProgress(feedUrl: String, guid: String, positionMillis: Long, finished: Boolean)

    @Query("UPDATE episodes SET downloadPath = :path WHERE feedUrl = :feedUrl AND guid = :guid")
    suspend fun setDownloadPath(feedUrl: String, guid: String, path: String?)
}
