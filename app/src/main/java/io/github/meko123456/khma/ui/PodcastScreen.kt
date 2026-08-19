package io.github.meko123456.khma.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.meko123456.khma.data.db.EpisodeEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFmt = SimpleDateFormat("d MMM yyyy", Locale.getDefault())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PodcastScreen(feedUrl: String, onBack: () -> Unit, onPlay: (EpisodeEntity) -> Unit, vm: LibraryViewModel = viewModel()) {
    val podcast by vm.podcast(feedUrl).collectAsState(initial = null)
    val episodes by vm.episodes(feedUrl).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(podcast?.title ?: "Podcast", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { vm.unsubscribe(feedUrl); onBack() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Unsubscribe")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding)) {
            items(episodes, key = { it.guid }) { e ->
                EpisodeRow(e, vm, onClick = { onPlay(e) })
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun EpisodeRow(e: EpisodeEntity, vm: LibraryViewModel, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            Modifier.weight(1f).clickable(onClick = onClick).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(e.title, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(
                listOfNotNull(
                    e.pubDateMillis.takeIf { it > 0 }?.let { dateFmt.format(Date(it)) },
                    formatDuration(e.durationSeconds),
                ).joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        DownloadControl(e, vm)
    }
}

@Composable
private fun DownloadControl(e: EpisodeEntity, vm: LibraryViewModel) {
    when {
        e.downloadPath != null -> {
            IconButton(onClick = { vm.deleteDownload(e) }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete download",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
        else -> {
            val progress by vm.downloadProgress(e.feedUrl, e.guid).collectAsState(initial = -1)
            if (progress >= 0) {
                Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { progress / 100f },
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                    )
                }
            } else {
                IconButton(onClick = { vm.download(e) }) {
                    Text("⬇", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

private fun formatDuration(seconds: Int): String? {
    if (seconds <= 0) return null
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    return if (h > 0) "${h}h ${m}m" else "$m min"
}
