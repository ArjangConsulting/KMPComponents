package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.focusable
import androidx.compose.material3.ExperimentalMaterial3Api
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

/**
 * A small "i" glyph that shows [text] in a tooltip on hover, long-press, or keyboard focus. Use it
 * next to a label whose meaning needs a short explanation.
 *
 * Screen readers read [text] directly, so the explanation is not hidden behind the tooltip.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun InfoTooltip(text: String, modifier: Modifier = Modifier) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = { PlainTooltip { Text(text) } },
        state = rememberTooltipState(),
        modifier = modifier,
    ) {
        GlyphIcon(
            glyph = Glyph.INFO,
            modifier = Modifier
                .semantics { contentDescription = text }
                .focusable(),
            size = glyphSize(MaterialTheme.typography.bodyMedium),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
