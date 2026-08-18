package io.github.meko123456.khma

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.meko123456.khma.ui.LibraryScreen
import io.github.meko123456.khma.ui.PodcastScreen
import io.github.meko123456.khma.ui.theme.KhmaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KhmaTheme {
                var openFeed by remember { mutableStateOf<String?>(null) }

                BackHandler(enabled = openFeed != null) { openFeed = null }

                val feed = openFeed
                if (feed == null) {
                    LibraryScreen(onOpenPodcast = { openFeed = it })
                } else {
                    PodcastScreen(feedUrl = feed, onBack = { openFeed = null })
                }
            }
        }
    }
}
