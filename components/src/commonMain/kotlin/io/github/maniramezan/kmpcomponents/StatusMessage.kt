package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/** Semantic intent of a [StatusMessage]; selects its accent color. */
public enum class StatusTone { INFO, SUCCESS, WARNING, ERROR }

private const val STATUS_CONTAINER_TINT = 0.12f

/**
 * An inline, non-dismissible message such as "Connection verified" or "API key rejected".
 *
 * Warning and error messages are announced by screen readers when they appear (polite live
 * region). The message sizes to its content; pass `Modifier.fillMaxWidth()` for a full-width
 * banner. For transient feedback prefer a snackbar.
 */
@Composable
public fun StatusMessage(
    message: String,
    modifier: Modifier = Modifier,
    tone: StatusTone = StatusTone.INFO,
    title: String? = null,
) {
    val accent = statusAccent(tone)
    val announce = tone == StatusTone.WARNING || tone == StatusTone.ERROR
    Surface(
        modifier = modifier.semantics(mergeDescendants = true) {
            if (announce) liveRegion = LiveRegionMode.Polite
        },
        shape = MaterialTheme.shapes.medium,
        color = tonalContainerColor(accent),
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, accent),
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.kmpSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.extraSmall),
        ) {
            if (title != null) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = accent)
            }
            Text(message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/** The accent color for [tone], shared by [StatusMessage] and [TonalBadge]. */
@Composable
@ReadOnlyComposable
internal fun statusAccent(tone: StatusTone): Color = when (tone) {
    StatusTone.INFO -> MaterialTheme.colorScheme.primary
    StatusTone.SUCCESS -> MaterialTheme.kmpColors.success
    StatusTone.WARNING -> MaterialTheme.kmpColors.warning
    StatusTone.ERROR -> MaterialTheme.colorScheme.error
}

/** A container for content tinted with [accent]: the surface blended slightly toward [accent]. */
@Composable
@ReadOnlyComposable
internal fun tonalContainerColor(
    accent: Color,
): Color = lerp(MaterialTheme.colorScheme.surface, accent, STATUS_CONTAINER_TINT)
