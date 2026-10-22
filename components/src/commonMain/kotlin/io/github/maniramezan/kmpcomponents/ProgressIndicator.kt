package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle

/**
 * A centered progress indicator with a readable [label], for a pane or screen that is loading.
 *
 * `null` [progress] shows an indeterminate spinner; a value from 0 to 1 shows a determinate bar.
 * The label is announced politely, so changes such as "Installing app…" are read out.
 */
@Composable
public fun ProgressIndicator(
    label: String,
    modifier: Modifier = Modifier,
    progress: Float? = null,
) {
    Column(
        modifier = modifier.semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (progress == null) {
            CircularProgressIndicator()
        } else {
            LinearProgressIndicator(progress = { progress.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
        }
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

/**
 * A small spinner followed by [label], for work in progress inside a row, toolbar, or card
 * ("Checking…", "Booting device…").
 *
 * The spinner is sized from [style] so it matches the text at every font scale. The label is
 * announced politely.
 */
@Composable
public fun InlineProgressIndicator(
    label: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Row(
        modifier = modifier.semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        InlineSpinner(style = style)
        Text(label, style = style, color = color)
    }
}

/** An indeterminate spinner as tall as one line of [style], for use next to text. */
@Composable
internal fun InlineSpinner(
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = LocalContentColor.current,
) {
    val size = glyphSize(style)
    CircularProgressIndicator(
        modifier = modifier.size(size),
        color = color,
        strokeWidth = size / SPINNER_STROKE_DIVISOR,
    )
}

private const val SPINNER_STROKE_DIVISOR = 8
