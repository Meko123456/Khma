package io.github.meko123456.khma.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import io.github.meko123456.khma.data.PodcastRepository
import io.github.meko123456.khma.data.db.EpisodeEntity
import io.github.meko123456.khma.data.db.KhmaDatabase
import io.github.meko123456.khma.data.db.PodcastEntity
import io.github.meko123456.khma.data.download.DownloadWorker
import io.github.meko123456.khma.data.download.Downloads
import io.github.meko123456.khma.data.download.MissingDownloads
import io.github.meko123456.khma.data.download.OrphanDownloads
import io.github.meko123456.khma.data.rss.FeedFailure
import io.github.meko123456.khma.domain.FeedAddress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LibraryViewModel(app: Application) : AndroidViewModel(app) {

    private val db = KhmaDatabase.get(app)
    private val repo = PodcastRepository(db.podcastDao(), db.episodeDao())

    val podcasts: StateFlow<List<PodcastEntity>> =
        repo.podcasts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    var status by mutableStateOf<String?>(null); private set
    var busy by mutableStateOf(false); private set
    var refreshing by mutableStateOf(false); private set

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val episodes = db.episodeDao()
            // Downloads a restore didn't bring back, since backup leaves the audio out: those
            // episodes stream instead of failing to play a file that isn't there.
            MissingDownloads.find(episodes.downloadPaths()).forEach { episodes.forgetDownload(it) }
            // Audio that unsubscribes left behind before they deleted it themselves (d56c3d2).
            val referenced = episodes.downloadPaths().map { it.substringAfterLast('/') }.toSet()
            OrphanDownloads.sweep(Downloads.dir(app), referenced, System.currentTimeMillis())
        }
    }

    fun podcast(feedUrl: String): Flow<PodcastEntity?> = repo.podcast(feedUrl)
    fun episodes(feedUrl: String): Flow<List<EpisodeEntity>> = repo.episodes(feedUrl)

    fun subscribe(url: String) {
        if (url.isBlank()) return
        val feedUrl = FeedAddress.normalise(url)
        viewModelScope.launch {
            busy = true
            status = "Adding…"
            repo.subscribe(feedUrl)
                .onSuccess { status = "Added" }
                .onFailure { status = FeedFailure.describe(it) }
            busy = false
        }
    }

    /**
     * Removes the podcast, its episodes and their downloads. The downloads used to stay behind:
     * files in files/downloads that no screen listed any more, so nothing could ever delete them.
     */
    fun unsubscribe(feedUrl: String) {
        val app = getApplication<Application>()
        viewModelScope.launch {
            val guids = repo.episodeGuids(feedUrl)
            // Running downloads stop first, so none of them lands a file after the sweep below.
            guids.forEach { Downloads.cancel(app, feedUrl, it) }
            repo.unsubscribe(feedUrl)
            withContext(Dispatchers.IO) { guids.forEach { Downloads.delete(app, feedUrl, it) } }
        }
    }

    /** Toggles an episode's played state: mark played keeps position; mark unplayed resets it. */
    fun toggleFinished(e: EpisodeEntity) {
        viewModelScope.launch {
            if (e.finished) db.episodeDao().updateProgress(e.feedUrl, e.guid, 0, false)
            else db.episodeDao().updateProgress(e.feedUrl, e.guid, e.positionMillis, true)
        }
    }

    /** Re-fetches the feed to pull in new episodes; insert-ignore keeps existing progress. */
    fun refresh(feedUrl: String) {
        viewModelScope.launch {
            refreshing = true
            repo.subscribe(feedUrl)
            refreshing = false
        }
    }

    /** Enqueues a background download of the episode's audio. */
    fun download(e: EpisodeEntity) {
        Downloads.enqueue(getApplication(), e)
    }

    /** Cancels any in-flight download, removes the local file, and clears the stored path. */
    fun deleteDownload(e: EpisodeEntity) {
        val app = getApplication<Application>()
        Downloads.cancel(app, e.feedUrl, e.guid)
        viewModelScope.launch {
            withContext(Dispatchers.IO) { Downloads.delete(app, e.feedUrl, e.guid) }
            db.episodeDao().setDownloadPath(e.feedUrl, e.guid, null)
        }
    }

    /** Emits download progress percent (0..100) while running, or -1 when idle/finished. */
    fun downloadProgress(feedUrl: String, guid: String): Flow<Int> =
        WorkManager.getInstance(getApplication())
            .getWorkInfosForUniqueWorkFlow(Downloads.uniqueName(feedUrl, guid))
            .map { infos ->
                val active = infos.firstOrNull { !it.state.isFinished }
                active?.progress?.getInt(DownloadWorker.KEY_PROGRESS, 0) ?: -1
            }

    fun clearStatus() { status = null }
}
