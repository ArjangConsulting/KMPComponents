package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * An outlined card in a list the user picks from (providers, reports, devices). A leading selection
 * box gains a check when selected, in addition to the secondary container color. The whole card
 * reports its selected state to accessibility services and shows an outline on keyboard focus.
 *
 * The whole card is one click target. Put secondary actions (Edit, Remove) inside [content] as
 * buttons; they stay separately focusable.
 */
@Composable
public fun SelectableCard(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val contentColor = if (selected) colors.onSecondaryContainer else colors.onSurface
    val outlineColor = if (selected) contentColor else colors.outline
    Card(
        onClick = onClick,
        modifier = modifier
            .keyboardFocusOutline(CardDefaults.shape, contentColor)
            .heightIn(min = minimumTouchTargetSize())
            .semantics { this.selected = selected },
        enabled = enabled,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) colors.secondaryContainer else colors.surfaceContainerLow,
            contentColor = contentColor,
        ),
        border = BorderStroke(1.dp, outlineColor),
    ) {
        Row(
            modifier = Modifier.padding(MaterialTheme.kmpSpacing.medium),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(glyphSize(MaterialTheme.typography.bodyLarge))
                    .border(1.dp, outlineColor, MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) GlyphIcon(Glyph.CHECK, color = contentColor)
            }
            Column(
                modifier = Modifier.weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.extraSmall),
                content = content,
            )
        }
    }
}
