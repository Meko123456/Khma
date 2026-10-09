package io.github.meko123456.khma.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import io.github.meko123456.khma.domain.TimeFormat
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
        BoxWithConstraints(Modifier.fillMaxSize().padding(padding)) {
            val screenHeight = maxHeight
            if (maxWidth > maxHeight) {
                // Landscape: the artwork beside the controls, which scroll if the height runs out.
                Row(Modifier.fillMaxSize().padding(24.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Column {
                        CollapseButton(onCollapse)
                        Artwork(state.artworkUri, min(260.dp, (screenHeight - 96.dp).coerceAtLeast(0.dp)))
                    }
                    Column(
                        Modifier.weight(1f).verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        PlayerControls(state, onToggle, onSeek, onSkip, onSetSpeed, onSetSleep, onCancelSleep)
                    }
                }
            } else {
                // Portrait: the artwork takes the height the controls leave, up to 260dp. When the
                // controls need more than the screen (big text on a small phone), it gives way and
                // the screen scrolls.
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .heightIn(min = screenHeight)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                        CollapseButton(onCollapse)
                    }
                    BoxWithConstraints(Modifier.weight(1f, fill = false)) {
                        Artwork(state.artworkUri, min(260.dp, min(maxWidth, maxHeight)))
                    }
                    PlayerControls(state, onToggle, onSeek, onSkip, onSetSpeed, onSetSleep, onCancelSleep)
                }
            }
        }
    }
}

@Composable
private fun CollapseButton(onCollapse: () -> Unit) {
    IconButton(onClick = onCollapse) {
        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Collapse")
    }
}

/** Title, seek bar, transport, speed and sleep timer, laid out into the caller's column. */
@Composable
private fun PlayerControls(
    state: PlayerUi,
    onToggle: () -> Unit,
    onSeek: (Long) -> Unit,
    onSkip: (Long) -> Unit,
    onSetSpeed: (Float) -> Unit,
    onSetSleep: (Int) -> Unit,
    onCancelSleep: () -> Unit,
) {
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
        Text(TimeFormat.clock(position.toLong()), style = MaterialTheme.typography.bodySmall)
        Text(TimeFormat.clock(state.durationMs), style = MaterialTheme.typography.bodySmall)
    }

    Row(
        Modifier.fillMaxWidth().padding(top = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedButton(onClick = { onSkip(-10_000) }) { SkipLabel("−10s") }
        IconButton(onClick = onToggle, modifier = Modifier.size(72.dp)) {
            if (state.isPlaying) {
                Text(
                    "❚❚",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.clearAndSetSemantics { contentDescription = "Pause" },
                )
            } else {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play", modifier = Modifier.size(48.dp))
            }
        }
        OutlinedButton(onClick = { onSkip(30_000) }) { SkipLabel("+30s") }
    }

    // Flow rows: five chips are wider than a 360dp phone, and the last one was squeezed until its
    // label stood one letter per line. They wrap onto a second line instead.
    FlowRow(
        Modifier.fillMaxWidth().padding(top = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp),
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
    FlowRow(
        Modifier.fillMaxWidth().padding(top = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        val remaining = state.sleepRemainingMs
        if (remaining != null) {
            Text("😴 ${TimeFormat.clock(remaining)}", style = MaterialTheme.typography.bodyMedium)
            AssistChip(onClick = onCancelSleep, label = { Text("Cancel") })
        } else {
            Text("😴", style = MaterialTheme.typography.bodyMedium)
            listOf(15, 30, 45, 60).forEach { m ->
                AssistChip(onClick = { onSetSleep(m) }, label = { Text("${m}m") })
            }
        }
    }
}

/** One line, shrinking if it must: at large text sizes "+30s" otherwise broke after "+30". */
@Composable
private fun SkipLabel(text: String) {
    Text(text, maxLines = 1, autoSize = TextAutoSize.StepBased(minFontSize = 8.sp, maxFontSize = LocalTextStyle.current.fontSize))
}
