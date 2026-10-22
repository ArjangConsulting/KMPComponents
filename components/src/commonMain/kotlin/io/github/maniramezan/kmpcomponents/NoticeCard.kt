package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * A full-width notice with an optional action and dismiss button, such as "Device disconnected ·
 * Retry" or "Provider not configured · Open settings". It uses the same [StatusTone] colors as
 * [StatusMessage]; use [StatusMessage] instead when there is nothing to act on.
 *
 * The action shows when both [actionLabel] and [onAction] are set; the dismiss button shows when
 * [onDismiss] is set. Warning and error notices are announced politely when they appear.
 */
@Composable
public fun NoticeCard(
    message: String,
    modifier: Modifier = Modifier,
    tone: StatusTone = StatusTone.INFO,
    title: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    dismissLabel: String = "Dismiss",
) {
    val accent = statusAccent(tone)
    val announce = tone == StatusTone.WARNING || tone == StatusTone.ERROR
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = tonalContainerColor(accent),
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, accent),
    ) {
        Row(
            modifier = Modifier.padding(
                start = MaterialTheme.kmpSpacing.medium,
                top = MaterialTheme.kmpSpacing.small,
                bottom = MaterialTheme.kmpSpacing.small,
                end = MaterialTheme.kmpSpacing.small,
            ),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = MaterialTheme.kmpSpacing.extraSmall)
                    .semantics(mergeDescendants = true) { if (announce) liveRegion = LiveRegionMode.Polite },
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.extraSmall),
            ) {
                if (title != null) {
                    Text(title, style = MaterialTheme.typography.titleSmall, color = accent)
                }
                Text(message, style = MaterialTheme.typography.bodyMedium)
            }
            if (actionLabel != null && onAction != null) {
                TextButton(onClick = onAction) { Text(actionLabel) }
            }
            if (onDismiss != null) {
                GlyphIconButton(glyph = Glyph.CLOSE, label = dismissLabel, onClick = onDismiss)
            }
        }
    }
}
