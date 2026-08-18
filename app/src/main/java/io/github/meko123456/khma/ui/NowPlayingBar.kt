package io.github.meko123456.khma.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import io.github.meko123456.khma.playback.PlayerUi

/** Persistent mini-player shown while something is loaded. Tap play/pause; a thin
 *  progress line shows position. The full now-playing screen and skip/speed land in #7. */
@Composable
fun NowPlayingBar(state: PlayerUi, onToggle: () -> Unit) {
    Surface(tonalElevation = 3.dp, modifier = Modifier.fillMaxWidth()) {
        Column {
            val progress = if (state.durationMs > 0) (state.positionMs.toFloat() / state.durationMs).coerceIn(0f, 1f) else 0f
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    state.title.ifBlank { "Loading…" },
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onToggle) {
                    // material-icons-core has no Pause glyph; overlay a pause using two bars via text.
                    if (state.isPlaying) {
                        Text("❚❚", style = MaterialTheme.typography.titleMedium)
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                    }
                }
            }
        }
    }
}
