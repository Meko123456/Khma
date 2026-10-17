package io.github.meko123456.khma.data

import io.github.meko123456.khma.data.db.EpisodeDao
import io.github.meko123456.khma.data.db.EpisodeEntity
import io.github.meko123456.khma.data.db.PodcastDao
import io.github.meko123456.khma.data.db.PodcastEntity
import io.github.meko123456.khma.data.model.Episode
import io.github.meko123456.khma.data.model.Podcast
import io.github.meko123456.khma.data.rss.FeedFetcher
import kotlinx.coroutines.flow.Flow

/**
 * The single source of truth for subscriptions. Fetches feeds via [FeedFetcher]
 * and persists them; refresh adds new episodes without clobbering existing
 * playback/download state (insert-ignore).
 */
class PodcastRepository(
    private val podcastDao: PodcastDao,
    private val episodeDao: EpisodeDao,
    private val fetcher: FeedFetcher = FeedFetcher(),
) {
    val podcasts: Flow<List<PodcastEntity>> = podcastDao.observeAll()

    fun podcast(feedUrl: String): Flow<PodcastEntity?> = podcastDao.observe(feedUrl)

    fun episodes(feedUrl: String): Flow<List<EpisodeEntity>> = episodeDao.observeForPodcast(feedUrl)

    /** Subscribes to (or refreshes) a feed: fetch, upsert the podcast, add new episodes. */
    suspend fun subscribe(feedUrl: String): Result<Unit> = fetcher.fetch(feedUrl).mapCatching { podcast ->
        podcastDao.upsert(podcast.toEntity())
        episodeDao.insertNew(podcast.episodes.map { it.toEntity(podcast.feedUrl) })
    }

    /** Removes the podcast; its episodes go with it through the foreign key. */
    suspend fun unsubscribe(feedUrl: String) = podcastDao.delete(feedUrl)

    /** Every episode guid stored for [feedUrl] — what an unsubscribe has to clear off the disk too. */
    suspend fun episodeGuids(feedUrl: String): List<String> = episodeDao.guidsFor(feedUrl)

    private fun Podcast.toEntity() = PodcastEntity(feedUrl, title, author, description, imageUrl)

    private fun Episode.toEntity(feedUrl: String) =
        EpisodeEntity(feedUrl, guid, title, audioUrl, description, imageUrl, durationSeconds, pubDateMillis)
}
