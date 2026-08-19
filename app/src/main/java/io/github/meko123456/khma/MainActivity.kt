package io.github.meko123456.khma

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.meko123456.khma.playback.PlayerViewModel
import io.github.meko123456.khma.ui.LibraryScreen
import io.github.meko123456.khma.ui.NowPlayingBar
import io.github.meko123456.khma.ui.NowPlayingScreen
import io.github.meko123456.khma.ui.PodcastScreen
import io.github.meko123456.khma.ui.theme.KhmaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KhmaTheme {
                val playerVm: PlayerViewModel = viewModel()
                val playerState by playerVm.state.collectAsState()
                var openFeed by remember { mutableStateOf<String?>(null) }
                var showNowPlaying by remember { mutableStateOf(false) }

                BackHandler(enabled = showNowPlaying) { showNowPlaying = false }
                BackHandler(enabled = openFeed != null && !showNowPlaying) { openFeed = null }

                if (showNowPlaying && playerState.hasItem) {
                    NowPlayingScreen(
                        state = playerState,
                        onCollapse = { showNowPlaying = false },
                        onToggle = playerVm::togglePlayPause,
                        onSeek = playerVm::seekTo,
                        onSkip = playerVm::skip,
                        onSetSpeed = playerVm::setSpeed,
                    )
                } else {
                    Column(Modifier.fillMaxSize()) {
                        Box(Modifier.weight(1f)) {
                            val feed = openFeed
                            if (feed == null) {
                                LibraryScreen(onOpenPodcast = { openFeed = it })
                            } else {
                                PodcastScreen(feedUrl = feed, onBack = { openFeed = null }, onPlay = playerVm::play)
                            }
                        }
                        if (playerState.hasItem) {
                            NowPlayingBar(
                                state = playerState,
                                onToggle = playerVm::togglePlayPause,
                                onExpand = { showNowPlaying = true },
                            )
                        }
                    }
                }
            }
        }
    }
}
