package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow

/**
 * A short label on a tinted background, such as an HTTP method, a status code, or a severity.
 *
 * [color] tints the background and supplies the preferred text color; unreadable text falls back
 * to the on-surface role. Pass an accent role (`primary`, `error`,
 * `kmpColors.success`, ...), not an `on*` role. Use the [StatusTone] overload for semantic states.
 */
@Composable
public fun TonalBadge(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    BadgeText(text, color, MaterialTheme.shapes.small, modifier)
}

/** A [TonalBadge] colored like a [StatusMessage] of the same [tone]. */
@Composable
public fun TonalBadge(text: String, tone: StatusTone, modifier: Modifier = Modifier) {
    TonalBadge(text = text, modifier = modifier, color = statusAccent(tone))
}

/**
 * A pill showing [count], capped as "[maxCount]+" so it stays compact.
 *
 * Screen readers read [contentDescription] when given (e.g. "3 variants"); otherwise the visible
 * text.
 */
@Composable
public fun CountBadge(
    count: Int,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.secondary,
    maxCount: Int = DEFAULT_MAX_BADGE_COUNT,
    contentDescription: String? = null,
) {
    val semanticsModifier = if (contentDescription != null) {
        Modifier.clearAndSetSemantics { this.contentDescription = contentDescription }
    } else {
        Modifier
    }
    BadgeText(badgeCountText(count, maxCount), color, PILL, modifier.then(semanticsModifier))
}

/** The text a [CountBadge] shows: [count], or "[maxCount]+" above it. Negative counts show 0. */
public fun badgeCountText(count: Int, maxCount: Int = DEFAULT_MAX_BADGE_COUNT): String {
    require(maxCount > 0) { "maxCount must be positive" }
    val shown = count.coerceAtLeast(0)
    return if (shown > maxCount) "$maxCount+" else shown.toString()
}

private const val DEFAULT_MAX_BADGE_COUNT = 99
private const val PILL_PERCENT = 50
private val PILL = RoundedCornerShape(percent = PILL_PERCENT)

@Composable
private fun BadgeText(text: String, color: Color, shape: Shape, modifier: Modifier) {
    val spacing = MaterialTheme.kmpSpacing
    Text(
        text = text,
        modifier = modifier
            .background(tonalContainerColor(color), shape)
            .padding(horizontal = spacing.small, vertical = spacing.extraSmall / 2),
        color = tonalContentColor(color),
        style = MaterialTheme.typography.labelMedium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
