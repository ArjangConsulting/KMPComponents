package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TooltipState
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.withTimeoutOrNull

/**
 * A small "i" glyph that shows [text] in a tooltip on hover, long-press, or keyboard focus. Use it
 * next to a label whose meaning needs a short explanation.
 *
 * Screen readers read [text] directly, so the explanation is not hidden behind the tooltip.
 * The anchor reserves the Material minimum interactive size and shows a border when focused.
 *
 * [displayDurationMillis] limits mouse-hover display time; `null` keeps the tooltip visible until
 * the pointer leaves or it is dismissed. Keyboard focus keeps the explanation visible until focus
 * leaves. Touch users can long-press to show it. A finite duration must be positive.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun InfoTooltip(
    text: String,
    modifier: Modifier = Modifier,
    displayDurationMillis: Long? = 1_500L,
) {
    require(displayDurationMillis == null || displayDurationMillis > 0) {
        "displayDurationMillis must be positive or null"
    }
    val persistentState = rememberTooltipState(isPersistent = true)
    val tooltipState = remember(persistentState, displayDurationMillis) {
        HoverDurationTooltipState(persistentState, displayDurationMillis)
    }
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val colors = MaterialTheme.colorScheme
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = { PlainTooltip { Text(text) } },
        state = tooltipState,
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .semantics { contentDescription = text }
                .focusable(interactionSource = interactionSource)
                .background(if (focused) colors.secondaryContainer else Color.Transparent, MaterialTheme.shapes.small)
                .border(1.dp, if (focused) colors.primary else Color.Transparent, MaterialTheme.shapes.small)
                .minimumInteractiveComponentSize()
                .padding(MaterialTheme.kmpSpacing.extraSmall),
            contentAlignment = Alignment.Center,
        ) {
            GlyphIcon(
                glyph = Glyph.INFO,
                size = glyphSize(MaterialTheme.typography.bodyMedium),
                color = if (focused) colors.onSecondaryContainer else colors.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private class HoverDurationTooltipState(private val delegate: TooltipState, private val displayDurationMillis: Long?) :
    TooltipState by delegate {
    override suspend fun show(mutatePriority: MutatePriority) {
        if (displayDurationMillis == null) {
            delegate.show(mutatePriority)
        } else {
            withTimeoutOrNull(displayDurationMillis) { delegate.show(mutatePriority) }
        }
    }
}

/** Retained for binary compatibility; use the overload with a configurable hover duration. */
@Deprecated("Retained for binary compatibility", level = DeprecationLevel.HIDDEN)
@Composable
public fun InfoTooltip(text: String, modifier: Modifier = Modifier): Unit = InfoTooltip(text, modifier, 1_500L)
