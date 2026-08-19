package io.github.meko123456.khma.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.meko123456.khma.playback.PlayerUi

private val SPEEDS = listOf(0.8f, 1.0f, 1.25f, 1.5f, 2.0f)

@Composable
fun NowPlayingScreen(
    state: PlayerUi,
    onCollapse: () -> Unit,
    onToggle: () -> Unit,
    onSeek: (Long) -> Unit,
    onSkip: (Long) -> Unit,
    onSetSpeed: (Float) -> Unit,
    onSetSleep: (Int) -> Unit,
    onCancelSleep: () -> Unit,
) {
    Scaffold { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                IconButton(onClick = onCollapse) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Collapse")
                }
            }

            Artwork(state.artworkUri, 260.dp)

            Text(
                state.title.ifBlank { "Loading…" },
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            )

            // Seek bar (local drag state so the ticker doesn't fight the thumb).
            var dragging by remember { mutableStateOf(false) }
            var dragValue by remember { mutableFloatStateOf(0f) }
            val duration = state.durationMs.toFloat().coerceAtLeast(1f)
            val position = if (dragging) dragValue else state.positionMs.toFloat().coerceIn(0f, duration)
            Slider(
                value = position,
                onValueChange = { dragging = true; dragValue = it },
                onValueChangeFinished = { onSeek(dragValue.toLong()); dragging = false },
                valueRange = 0f..duration,
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatMs(position.toLong()), style = MaterialTheme.typography.bodySmall)
                Text(formatMs(state.durationMs), style = MaterialTheme.typography.bodySmall)
            }

            Row(
                Modifier.fillMaxWidth().padding(top = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(onClick = { onSkip(-10_000) }) { Text("−10s") }
                IconButton(onClick = onToggle, modifier = Modifier.size(72.dp)) {
                    if (state.isPlaying) {
                        Text("❚❚", style = MaterialTheme.typography.headlineMedium)
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play", modifier = Modifier.size(48.dp))
                    }
                }
                OutlinedButton(onClick = { onSkip(30_000) }) { Text("+30s") }
            }

            Row(
                Modifier.fillMaxWidth().padding(top = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            ) {
                SPEEDS.forEach { s ->
                    FilterChip(
                        selected = kotlin.math.abs(state.speed - s) < 0.01f,
                        onClick = { onSetSpeed(s) },
                        label = { Text("${s}x") },
                    )
                }
            }

            // Sleep timer: minute chips when off; countdown + cancel when running.
            Row(
                Modifier.fillMaxWidth().padding(top = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val remaining = state.sleepRemainingMs
                if (remaining != null) {
                    Text("😴 ${formatMs(remaining)}", style = MaterialTheme.typography.bodyMedium)
                    AssistChip(onClick = onCancelSleep, label = { Text("Cancel") })
                } else {
                    Text("😴", style = MaterialTheme.typography.bodyMedium)
                    listOf(15, 30, 45, 60).forEach { m ->
                        AssistChip(onClick = { onSetSleep(m) }, label = { Text("${m}m") })
                    }
                }
            }
        }
    }
}

private fun formatMs(ms: Long): String {
    val total = ms / 1000
    val h = total / 3600
    val m = (total % 3600) / 60
    val s = total % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}
