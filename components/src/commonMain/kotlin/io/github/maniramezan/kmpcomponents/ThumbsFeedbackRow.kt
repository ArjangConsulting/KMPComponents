package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.stateDescription

/** The rating shown by a [ThumbsFeedbackRow]. */
public enum class ThumbsRating {
    /** Neither choice is selected. */
    NONE,

    /** The positive (thumbs up) choice is selected. */
    POSITIVE,

    /** The negative (thumbs down) choice is selected. */
    NEGATIVE,
}

/**
 * The rating that results from tapping [tapped] while the row shows [current], or `null` when the
 * tap changes nothing and should not be reported.
 */
internal fun resolveThumbsRatingTap(
    current: ThumbsRating,
    tapped: ThumbsRating,
    allowClearing: Boolean,
): ThumbsRating? =
    when {
        tapped == ThumbsRating.NONE -> null
        tapped != current -> tapped
        allowClearing -> ThumbsRating.NONE
        else -> null
    }

/**
 * Two icon buttons, thumbs up and thumbs down, for rating a piece of content such as a generated
 * answer.
 *
 * The row only displays [rating] and reports taps. It does not submit anything, ask a follow-up
 * question, or retry failed submissions; make those decisions in [onRatingChange]. The buttons form
 * a single-choice group: at most one is selected, each exposes its selected state to accessibility
 * services, and a selected button also gets a filled container, so selection is not shown by color
 * alone. Each button shows its description as a tooltip on hover, focus, or long-press.
 *
 * Tapping the unselected choice reports it. Tapping the selected choice reports
 * [ThumbsRating.NONE] when [allowClearing] is `true`, and does nothing when it is `false`.
 * [onRatingChange] is never called with the rating already shown.
 *
 * @param rating the current selection.
 * @param onRatingChange called with the new rating when the user changes the selection.
 * @param positiveContentDescription accessible name and tooltip of the thumbs up button.
 * @param negativeContentDescription accessible name and tooltip of the thumbs down button.
 * @param enabled whether the buttons respond to taps. Disabled buttons are dimmed and keep showing
 *   [rating].
 * @param allowClearing whether tapping the selected choice clears the rating. When `false`, only the
 *   caller can return the row to [ThumbsRating.NONE].
 * @param selectedStateDescription optional state announced for the selected button, such as
 *   "Selected". `null` or blank keeps the platform's default selected announcement.
 */
@Composable
public fun ThumbsFeedbackRow(
    rating: ThumbsRating,
    onRatingChange: (ThumbsRating) -> Unit,
    positiveContentDescription: String,
    negativeContentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    allowClearing: Boolean = true,
    selectedStateDescription: String? = null,
) {
    Row(
        modifier = modifier.selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.extraSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ThumbsFeedbackButton(
            glyph = Glyph.THUMB_UP,
            selected = rating == ThumbsRating.POSITIVE,
            enabled = enabled,
            label = positiveContentDescription,
            selectedStateDescription = selectedStateDescription,
            onClick = { resolveThumbsRatingTap(rating, ThumbsRating.POSITIVE, allowClearing)?.let(onRatingChange) },
        )
        ThumbsFeedbackButton(
            glyph = Glyph.THUMB_DOWN,
            selected = rating == ThumbsRating.NEGATIVE,
            enabled = enabled,
            label = negativeContentDescription,
            selectedStateDescription = selectedStateDescription,
            onClick = { resolveThumbsRatingTap(rating, ThumbsRating.NEGATIVE, allowClearing)?.let(onRatingChange) },
        )
    }
}

@Composable
private fun ThumbsFeedbackButton(
    glyph: Glyph,
    selected: Boolean,
    enabled: Boolean,
    label: String,
    selectedStateDescription: String?,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    // A filled primary container, not a tint, so the selection keeps strong contrast in light and dark.
    val container = when {
        !selected -> null
        enabled -> colors.primary
        else -> colors.onSurface.copy(alpha = DISABLED_CONTAINER_ALPHA)
    }
    val glyphColor = when {
        !enabled -> colors.onSurface.copy(alpha = DISABLED_ALPHA)
        selected -> colors.onPrimary
        else -> colors.onSurfaceVariant
    }
    val iconSize = glyphSize()
    TooltipIconButton(
        label = label,
        onClick = onClick,
        iconSize = iconSize,
        modifier = if (container != null) Modifier.background(container, CircleShape) else Modifier,
        enabled = enabled,
        extraSemantics = {
            this.selected = selected
            if (selected && !selectedStateDescription.isNullOrBlank()) stateDescription = selectedStateDescription
        },
    ) {
        GlyphIcon(glyph = glyph, size = iconSize, color = glyphColor)
    }
}

private const val DISABLED_ALPHA = 0.38f
private const val DISABLED_CONTAINER_ALPHA = 0.12f
