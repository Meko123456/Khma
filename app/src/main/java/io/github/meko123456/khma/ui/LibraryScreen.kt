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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import io.github.meko123456.khma.data.db.PodcastEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(onOpenPodcast: (String) -> Unit, vm: LibraryViewModel = viewModel()) {
    val podcasts by vm.podcasts.collectAsState()
    var adding by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Khma 🎧") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { adding = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add a podcast")
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (vm.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            vm.status?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().clickable { vm.clearStatus() }.padding(horizontal = 16.dp, vertical = 6.dp),
                )
            }

            if (podcasts.isEmpty()) {
                Column(
                    Modifier.fillMaxSize().padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("No podcasts yet", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Tap + and paste a podcast's RSS feed URL to subscribe.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(podcasts, key = { it.feedUrl }) { p ->
                        PodcastRow(p, onClick = { onOpenPodcast(p.feedUrl) })
                    }
                }
            }
        }
    }

    if (adding) {
        AddFeedDialog(
            busy = vm.busy,
            onAdd = { vm.subscribe(it); adding = false },
            onDismiss = { adding = false },
        )
    }
}

@Composable
private fun PodcastRow(podcast: PodcastEntity, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Artwork(podcast.imageUrl, 56.dp)
        Column(Modifier.padding(start = 12.dp)) {
            Text(podcast.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (podcast.author.isNotBlank()) {
                Text(
                    podcast.author,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun Artwork(url: String?, size: androidx.compose.ui.unit.Dp) {
    Box(Modifier.size(size).clip(RoundedCornerShape(8.dp))) {
        if (url != null) {
            AsyncImage(model = url, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        } else {
            Box(Modifier.fillMaxSize()) {
                Text("🎧", modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
private fun AddFeedDialog(busy: Boolean, onAdd: (String) -> Unit, onDismiss: () -> Unit) {
    var url by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add a podcast") },
        text = {
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("RSS feed URL") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = { TextButton(enabled = url.isNotBlank() && !busy, onClick = { onAdd(url) }) { Text("Add") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
