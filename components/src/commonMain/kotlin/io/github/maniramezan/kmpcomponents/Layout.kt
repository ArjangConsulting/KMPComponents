package io.github.maniramezan.kmpcomponents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.HorizontalDivider as MaterialHorizontalDivider
import androidx.compose.material3.VerticalDivider as MaterialVerticalDivider

/** Width caps for [AdaptiveContentContainer], taken from Material's window size class breakpoints. */
public object AdaptiveContentContainerDefaults {
    /** Forms and reading content: the compact/medium window breakpoint. */
    public val FormMaxWidth: Dp = CompactWindowMaxWidth

    /** Lists and wider content: the medium/expanded window breakpoint. */
    public val ListMaxWidth: Dp = 840.dp
}

/**
 * Centers [content] and caps its width at [maxWidth], so forms and lists do not stretch across
 * tablet and desktop windows. In narrow windows it simply fills the width.
 */
@Composable
public fun AdaptiveContentContainer(
    modifier: Modifier = Modifier,
    maxWidth: Dp = AdaptiveContentContainerDefaults.FormMaxWidth,
    contentAlignment: Alignment = Alignment.TopCenter,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = contentAlignment) {
        // Cap before filling: fillMaxWidth first would fix the width so widthIn could not shrink it.
        Box(modifier = Modifier.widthIn(max = maxWidth).fillMaxWidth(), content = content)
    }
}

/**
 * A rounded, translucent container for labels and controls drawn over images or video. Content
 * defaults to the inverse on-surface color so it reads on the dark overlay.
 */
@Composable
public fun OverlayCard(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    background: Color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = OVERLAY_ALPHA),
    contentColor: Color = MaterialTheme.colorScheme.inverseOnSurface,
    content: @Composable BoxScope.() -> Unit,
) {
    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Box(
            modifier = modifier
                .clip(MaterialTheme.shapes.large)
                .background(background)
                .padding(MaterialTheme.kmpSpacing.medium),
            contentAlignment = contentAlignment,
            content = content,
        )
    }
}

private const val OVERLAY_ALPHA = 0.72f

/**
 * A card that always shows [summary] and reveals [detail] when [expanded]. The summary row is the
 * toggle; it announces [expandedLabel] or [collapsedLabel] as its state.
 */
@Composable
public fun DisclosureCard(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    expandedLabel: String = "Expanded",
    collapsedLabel: String = "Collapsed",
    summary: @Composable RowScope.() -> Unit,
    detail: @Composable () -> Unit,
) {
    val spacing = MaterialTheme.kmpSpacing
    val duration = MaterialTheme.kmpMotion.standardMillis
    val rotation by animateFloatAsState(if (expanded) HALF_TURN else 0f, tween(duration), label = "chevron")
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minimumTouchTargetSize())
                    .clickable(role = Role.Button) { onExpandedChange(!expanded) }
                    .semantics { stateDescription = if (expanded) expandedLabel else collapsedLabel }
                    .padding(spacing.medium),
                horizontalArrangement = Arrangement.spacedBy(spacing.small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(spacing.small),
                    verticalAlignment = Alignment.CenterVertically,
                    content = summary,
                )
                GlyphIcon(glyph = Glyph.CHEVRON_DOWN, modifier = Modifier.graphicsLayer { rotationZ = rotation })
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(tween(duration)),
                exit = shrinkVertically(tween(duration)),
            ) {
                Box(Modifier.padding(start = spacing.medium, end = spacing.medium, bottom = spacing.medium)) {
                    detail()
                }
            }
        }
    }
}

private const val HALF_TURN = 180f

/** A full-width horizontal rule in the outline variant color. */
@Composable
public fun ThemeHorizontalDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = DividerDefaults.Thickness,
    color: Color = MaterialTheme.colorScheme.outlineVariant,
) {
    MaterialHorizontalDivider(modifier = modifier, thickness = thickness, color = color)
}

/** A full-height vertical rule in the outline variant color. */
@Composable
public fun ThemeVerticalDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = DividerDefaults.Thickness,
    color: Color = MaterialTheme.colorScheme.outlineVariant,
) {
    MaterialVerticalDivider(modifier = modifier, thickness = thickness, color = color)
}

/**
 * Lays [content] out from start to end, wrapping onto new rows when the width runs out, with the
 * standard small gap between items and rows. Use it for chips, tags, and button groups that must
 * not clip at large font sizes.
 */
@Composable
public fun FlowLayout(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small),
    content: @Composable FlowRowScope.() -> Unit,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        itemVerticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}
