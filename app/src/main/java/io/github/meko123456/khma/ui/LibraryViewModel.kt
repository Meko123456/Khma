package io.github.meko123456.khma.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.meko123456.khma.data.PodcastRepository
import io.github.meko123456.khma.data.db.EpisodeEntity
import io.github.meko123456.khma.data.db.KhmaDatabase
import io.github.meko123456.khma.data.db.PodcastEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryViewModel(app: Application) : AndroidViewModel(app) {

    private val db = KhmaDatabase.get(app)
    private val repo = PodcastRepository(db.podcastDao(), db.episodeDao())

    val podcasts: StateFlow<List<PodcastEntity>> =
        repo.podcasts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    var status by mutableStateOf<String?>(null); private set
    var busy by mutableStateOf(false); private set

    fun podcast(feedUrl: String): Flow<PodcastEntity?> = repo.podcast(feedUrl)
    fun episodes(feedUrl: String): Flow<List<EpisodeEntity>> = repo.episodes(feedUrl)

    fun subscribe(url: String) {
        val feedUrl = url.trim()
        if (feedUrl.isBlank()) return
        viewModelScope.launch {
            busy = true
            status = "Adding…"
            repo.subscribe(feedUrl)
                .onSuccess { status = "Added" }
                .onFailure { status = it.message ?: "Couldn't add that feed" }
            busy = false
        }
    }

    fun unsubscribe(feedUrl: String) {
        viewModelScope.launch { repo.unsubscribe(feedUrl) }
    }

    fun clearStatus() { status = null }
}
