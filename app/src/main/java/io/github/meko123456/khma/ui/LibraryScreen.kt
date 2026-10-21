package io.github.meko123456.khma.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import io.github.meko123456.khma.data.db.PodcastEntity

/** A link shared to Khma, numbered so the same link shared twice still opens the dialog twice. */
data class SharedAddress(val address: String, val number: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onOpenPodcast: (String) -> Unit,
    shared: SharedAddress? = null,
    onSharedOpened: () -> Unit = {},
    vm: LibraryViewModel = viewModel(),
) {
    val podcasts by vm.podcasts.collectAsState()
    // What the add dialog opened with: null while it is closed, "" from the + button, or a shared link.
    var adding by rememberSaveable { mutableStateOf<String?>(null) }
    // Counts the openings, so a link shared while the dialog is open replaces what was typed in it.
    var openings by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(shared) {
        if (shared != null) {
            adding = shared.address
            openings++
            onSharedOpened()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Read as the app's name and as the screen's heading. TalkBack read the emoji
                    // too: "Khma, headphone".
                    Text(
                        "Khma 🎧",
                        modifier = Modifier.clearAndSetSemantics { contentDescription = "Khma"; heading() },
                    )
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { adding = ""; openings++ }) {
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
                        "Tap + and paste a podcast's RSS feed or Apple Podcasts link, or share the link to Khma.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            } else {
                // 88dp at the bottom: the FAB's 56 and its margins. Without it the list ended under the
                // add button, which hid the end of the last podcast's title and took taps meant for it.
                LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 88.dp)) {
                    items(podcasts, key = { it.feedUrl }) { p ->
                        PodcastRow(p, onClick = { onOpenPodcast(p.feedUrl) })
                    }
                }
            }
        }
    }

    adding?.let { initial ->
        key(openings) {
            AddFeedDialog(
                initial = initial,
                busy = vm.busy,
                onAdd = { vm.subscribe(it); adding = null },
                onDismiss = { adding = null },
            )
        }
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
private fun AddFeedDialog(initial: String, busy: Boolean, onAdd: (String) -> Unit, onDismiss: () -> Unit) {
    var url by rememberSaveable { mutableStateOf(initial) }
    val canAdd = url.isNotBlank() && !busy
    val field = remember { FocusRequester() }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add a podcast") },
        text = {
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                // It takes more than feeds now (4590793), and a longer label would be cut off at the
                // larger text sizes, so the field says what it takes underneath.
                label = { Text("Podcast link") },
                supportingText = { Text("An RSS feed or an Apple Podcasts link") },
                singleLine = true,
                // A link keyboard with nothing to autocorrect, and Go adds the feed as Add does.
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    autoCorrectEnabled = false,
                    imeAction = ImeAction.Go,
                ),
                keyboardActions = KeyboardActions(onGo = { if (canAdd) onAdd(url) }),
                modifier = Modifier.fillMaxWidth().focusRequester(field),
            )
            // Opened empty, from + or the launcher shortcut, the next thing is pasting a link, so the
            // field has the keyboard; a shared link is already in it and needs only Add.
            LaunchedEffect(Unit) { if (initial.isEmpty()) field.requestFocus() }
        },
        confirmButton = { TextButton(enabled = canAdd, onClick = { onAdd(url) }) { Text("Add") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
