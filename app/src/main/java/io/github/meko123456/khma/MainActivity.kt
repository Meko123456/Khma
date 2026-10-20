package io.github.meko123456.khma

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.meko123456.khma.domain.FeedAddress
import io.github.meko123456.khma.playback.PlayerViewModel
import io.github.meko123456.khma.ui.LibraryScreen
import io.github.meko123456.khma.ui.NowPlayingBar
import io.github.meko123456.khma.ui.NowPlayingScreen
import io.github.meko123456.khma.ui.PodcastScreen
import io.github.meko123456.khma.ui.SharedAddress
import io.github.meko123456.khma.ui.theme.KhmaTheme

class MainActivity : ComponentActivity() {
    /** A link shared to Khma that the library hasn't opened the add dialog with yet. */
    private var shared by mutableStateOf<SharedAddress?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Only a fresh start takes the intent: recreated after a rotation, the activity still holds
        // the share it was opened with, and the dialog keeps whatever was typed over it since.
        if (savedInstanceState == null) take(intent)
        setContent {
            KhmaTheme {
                val playerVm: PlayerViewModel = viewModel()
                val playerState by playerVm.state.collectAsState()
                var openFeed by rememberSaveable { mutableStateOf<String?>(null) }
                var showNowPlaying by rememberSaveable { mutableStateOf(false) }

                // The flag survives the process being killed, but the episode doesn't. Expanded with
                // nothing to play, the hidden player still took the first Back press, and the next
                // episode opened expanded; so the flag clears whenever the player is empty.
                val expanded = showNowPlaying && playerState.hasItem
                LaunchedEffect(playerState.hasItem) { if (!playerState.hasItem) showNowPlaying = false }

                // The add dialog belongs to the library, so a share closes what was open over it.
                val incoming = shared
                LaunchedEffect(incoming) {
                    if (incoming != null) {
                        openFeed = null
                        showNowPlaying = false
                    }
                }

                BackHandler(enabled = expanded) { showNowPlaying = false }
                BackHandler(enabled = openFeed != null && !expanded) { openFeed = null }

                if (expanded) {
                    NowPlayingScreen(
                        state = playerState,
                        onCollapse = { showNowPlaying = false },
                        onToggle = playerVm::togglePlayPause,
                        onSeek = playerVm::seekTo,
                        onSkip = playerVm::skip,
                        onSetSpeed = playerVm::setSpeed,
                        onSetSleep = playerVm::setSleepTimer,
                        onCancelSleep = playerVm::cancelSleepTimer,
                    )
                } else {
                    Column(Modifier.fillMaxSize()) {
                        Box(Modifier.weight(1f)) {
                            val feed = openFeed
                            if (feed == null) {
                                LibraryScreen(
                                    onOpenPodcast = { openFeed = it },
                                    shared = shared,
                                    onSharedOpened = { shared = null },
                                )
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        take(intent)
    }

    private fun take(intent: Intent?) {
        if (intent?.action != Intent.ACTION_SEND) return
        val text = intent.getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString()?.takeIf { it.isNotBlank() } ?: return
        shared = SharedAddress(FeedAddress.fromShared(text), (shared?.number ?: 0) + 1)
    }
}
