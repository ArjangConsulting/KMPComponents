package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics

/**
 * A list row with a [title], optional [secondaryText] beside it, optional [supportingText] under
 * it, and leading and trailing slots. Use it for recent projects, endpoints, steps, and settings
 * entries.
 *
 * The whole row is one click target when [onClick] is set; buttons in [trailingContent] (Remove,
 * Edit) stay separately focusable. Pass [selected] for rows in a single-choice list. The selected
 * row uses the secondary container color and reports its selected state; `null` means the row is
 * not part of a selection and reports nothing. The row fills the width its parent gives it.
 */
@Composable
public fun ContentRow(
    title: String,
    modifier: Modifier = Modifier,
    secondaryText: String? = null,
    supportingText: String? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    selected: Boolean? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    val isSelected = selected == true
    val contentColor = if (isSelected) colors.onSecondaryContainer else colors.onSurface
    val supportingColor = if (isSelected) colors.onSecondaryContainer else colors.onSurfaceVariant
    val rowModifier = modifier
        .clip(MaterialTheme.shapes.small)
        .background(if (isSelected) colors.secondaryContainer else Color.Transparent)
        .let { base ->
            if (onClick != null) {
                base.keyboardFocusOutline(MaterialTheme.shapes.small, contentColor)
                    .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            } else {
                base
            }
        }
        .semantics(mergeDescendants = onClick != null) {
            if (selected != null) this.selected = selected
        }
        .heightIn(min = minimumTouchTargetSize())
        .padding(horizontal = MaterialTheme.kmpSpacing.medium, vertical = MaterialTheme.kmpSpacing.small)

    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Row(
            modifier = rowModifier,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingContent?.invoke()
            Column(
                // A clickable row already merges its text; a nested merge would leave the row unlabeled.
                modifier = Modifier.weight(1f).semantics(mergeDescendants = onClick == null) {},
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.extraSmall),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(title, style = MaterialTheme.typography.titleSmall)
                    if (secondaryText != null) {
                        Text(secondaryText, style = MaterialTheme.typography.bodySmall, color = supportingColor)
                    }
                }
                if (supportingText != null) {
                    Text(supportingText, style = MaterialTheme.typography.bodySmall, color = supportingColor)
                }
            }
            if (isSelected) GlyphIcon(Glyph.CHECK, color = contentColor)
            trailingContent?.invoke()
        }
    }
}
