package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight

/** A horizontally scrolling row of [TabChip]s that forms one selection group. */
@Composable
public fun TabChipRow(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

/**
 * A compact tab, for switching between documents or variants inside a pane.
 *
 * With [onClose], the chip shows a close control described as [closeDescription] (for example
 * "Close Success"). The close control is a separate accessibility element from the tab itself.
 */
@Composable
public fun TabChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClose: (() -> Unit)? = null,
    closeDescription: String = "Close $label",
) {
    val colors = MaterialTheme.colorScheme
    val container = if (selected) colors.primaryContainer else colors.surfaceVariant
    val content = if (selected) colors.onPrimaryContainer else colors.onSurfaceVariant
    val spacing = MaterialTheme.kmpSpacing
    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(container)
            .selectable(selected = selected, enabled = enabled, role = Role.Tab, onClick = onClick)
            .padding(
                start = spacing.medium,
                end = if (onClose != null) spacing.small else spacing.medium,
                top = spacing.small,
                bottom = spacing.small,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = content,
            fontWeight = if (selected) FontWeight.SemiBold else null,
        )
        if (onClose != null) {
            GlyphIcon(
                glyph = Glyph.CLOSE,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .clickable(enabled = enabled, role = Role.Button, onClick = onClose)
                    .semantics { contentDescription = closeDescription },
                size = glyphSize(MaterialTheme.typography.labelLarge),
                color = content,
            )
        }
    }
}
