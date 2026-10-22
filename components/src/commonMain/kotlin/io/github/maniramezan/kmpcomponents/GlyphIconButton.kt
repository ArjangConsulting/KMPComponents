package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle

/**
 * An icon-only button drawing [glyph], with [label] as its accessible name and hover/focus tooltip.
 * The glyph follows [style]'s line height, so the button grows with the user's font scale.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GlyphIconButton(
    glyph: Glyph,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
) {
    val iconSize = glyphSize(style)
    val buttonSize = iconSize + MaterialTheme.kmpSpacing.small * 2
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = { PlainTooltip { Text(label) } },
        state = rememberTooltipState(),
        modifier = modifier,
    ) {
        IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .sizeIn(minWidth = buttonSize, minHeight = buttonSize)
                .semantics { contentDescription = label },
        ) {
            GlyphIcon(glyph = glyph, size = iconSize)
        }
    }
}
