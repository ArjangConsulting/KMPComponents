package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp

/**
 * An icon-only button with [label] as its accessible name and hover/focus tooltip. The button grows
 * with [iconSize], so icons sized from text follow the user's font scale.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TooltipIconButton(
    label: String,
    onClick: () -> Unit,
    iconSize: Dp,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    extraSemantics: SemanticsPropertyReceiver.() -> Unit = {},
    icon: @Composable () -> Unit,
) {
    val buttonSize = (iconSize + MaterialTheme.kmpSpacing.small * 2).coerceAtLeast(minimumTouchTargetSize())
    val tooltipState = rememberTooltipState()
    val interactionSource = remember { MutableInteractionSource() }
    TooltipFocusTrigger(tooltipState, interactionSource)
    // The caller's modifier goes on an outer Box: TooltipBox wraps it in an inner container, which
    // would hide a parent's align/weight from the parent.
    Box(modifier = modifier) {
        TooltipBox(
            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
            tooltip = { PlainTooltip { Text(label) } },
            state = tooltipState,
            // See TooltipTriggers.kt: Material's own input handling adds an unnamed node on iOS.
            modifier = Modifier.tooltipPointerTriggers(tooltipState),
            enableUserInput = false,
        ) {
            IconButton(
                onClick = onClick,
                enabled = enabled,
                interactionSource = interactionSource,
                modifier = Modifier
                    .keyboardFocusOutline(IconButtonDefaults.standardShape)
                    .sizeIn(minWidth = buttonSize, minHeight = buttonSize)
                    .semantics {
                        contentDescription = label
                        extraSemantics()
                    },
                content = icon,
            )
        }
    }
}

/** A [TooltipIconButton] drawing [glyph], sized from [style]'s line height. */
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
    TooltipIconButton(label = label, onClick = onClick, iconSize = iconSize, modifier = modifier, enabled = enabled) {
        GlyphIcon(glyph = glyph, size = iconSize)
    }
}
