package io.github.meko123456.khma.playback

import android.app.Application
import android.content.ComponentName
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import io.github.meko123456.khma.data.db.EpisodeEntity
import io.github.meko123456.khma.data.db.KhmaDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlayerUi(
    val hasItem: Boolean = false,
    val isPlaying: Boolean = false,
    val title: String = "",
    val positionMs: Long = 0,
    val durationMs: Long = 0,
)

/** Connects a MediaController to [PlaybackService] and exposes play + transport controls. */
class PlayerViewModel(app: Application) : AndroidViewModel(app) {

    private val episodeDao = KhmaDatabase.get(app).episodeDao()
    private var currentFeed: String? = null
    private var currentGuid: String? = null

    private var controller: MediaController? = null
    private val _state = MutableStateFlow(PlayerUi())
    val state: StateFlow<PlayerUi> = _state.asStateFlow()

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) = pushState()
    }

    init {
        val token = SessionToken(app, ComponentName(app, PlaybackService::class.java))
        val future = MediaController.Builder(app, token).buildAsync()
        future.addListener({
            controller = future.get().also { it.addListener(listener) }
            pushState()
        }, ContextCompat.getMainExecutor(app))

        // Position ticker while playing; also persist progress every few seconds.
        viewModelScope.launch {
            var ticks = 0
            while (true) {
                delay(500)
                if (controller?.isPlaying == true) {
                    pushState()
                    if (++ticks % 8 == 0) saveProgress()
                }
            }
        }
    }

    fun play(episode: EpisodeEntity) {
        val c = controller ?: return
        if (episode.feedUrl != currentFeed || episode.guid != currentGuid) saveProgress() // save the outgoing one
        currentFeed = episode.feedUrl
        currentGuid = episode.guid
        val item = MediaItem.Builder()
            .setUri(episode.downloadPath ?: episode.audioUrl)
            .setMediaId("${episode.feedUrl}|${episode.guid}")
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(episode.title)
                    .setArtworkUri(episode.imageUrl?.toUri())
                    .build(),
            )
            .build()
        c.setMediaItem(item)
        c.prepare()
        if (episode.positionMillis > 0) c.seekTo(episode.positionMillis) // resume where we left off
        c.play()
    }

    fun togglePlayPause() {
        val c = controller ?: return
        if (c.isPlaying) {
            c.pause()
            saveProgress()
        } else {
            c.play()
        }
    }

    /** Persists the current episode's position (and marks it finished near the end). */
    private fun saveProgress() {
        val c = controller ?: return
        val feed = currentFeed ?: return
        val guid = currentGuid ?: return
        val position = c.currentPosition.coerceAtLeast(0)
        val duration = c.duration
        val finished = duration > 0 && position >= duration - 5_000
        viewModelScope.launch { episodeDao.updateProgress(feed, guid, position, finished) }
    }

    fun seekTo(ms: Long) { controller?.seekTo(ms.coerceAtLeast(0)) }

    fun skip(deltaMs: Long) {
        val c = controller ?: return
        c.seekTo((c.currentPosition + deltaMs).coerceAtLeast(0))
    }

    private fun pushState() {
        val c = controller ?: return
        _state.value = PlayerUi(
            hasItem = c.currentMediaItem != null,
            isPlaying = c.isPlaying,
            title = c.mediaMetadata.title?.toString().orEmpty(),
            positionMs = c.currentPosition.coerceAtLeast(0),
            durationMs = c.duration.takeIf { it > 0 } ?: 0,
        )
    }

    override fun onCleared() {
        controller?.release()
        controller = null
    }
}
