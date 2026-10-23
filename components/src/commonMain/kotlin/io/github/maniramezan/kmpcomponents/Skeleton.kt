package io.github.maniramezan.kmpcomponents

import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp

/**
 * A full-width placeholder block one line of body text tall, for a list or card that is loading.
 * It is decorative and hidden from screen readers; announce loading with a [ProgressIndicator] or
 * [LoadingState] label instead.
 */
@Composable
public fun Skeleton(modifier: Modifier = Modifier) {
    SkeletonBlock(height = glyphSize(), modifier = modifier.fillMaxWidth())
}

/**
 * A placeholder block of [height] (and [width], or the width [modifier] gives it) for skeleton
 * layouts. It is decorative and hidden from screen readers.
 */
@Composable
public fun SkeletonBlock(
    height: Dp,
    modifier: Modifier = Modifier,
    width: Dp? = null,
    shape: Shape = MaterialTheme.shapes.small,
    color: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    Box(
        modifier = modifier
            .then(if (width != null) Modifier.width(width) else Modifier)
            .height(height)
            .clip(shape)
            .background(color)
            .clearAndSetSemantics {},
    )
}

/**
 * Turns [skeletonShimmer] off for the subtree, for example in screenshot tests. It is also off when
 * the system reduces motion and in previews.
 */
public val LocalSkeletonShimmerEnabled: ProvidableCompositionLocal<Boolean> = staticCompositionLocalOf { true }

/**
 * Sweeps a soft highlight band across the placeholders in this subtree so a skeleton reads as
 * loading rather than frozen. Apply it once at the root of a skeleton layout. Every shimmering
 * subtree uses the same frame clock, so separate cards sweep in unison.
 *
 * The highlight only lands on drawn placeholders, not the gaps between them. The modifier adds no
 * input handling or semantics, and does nothing when [LocalSkeletonShimmerEnabled] is `false`, the
 * system reduces motion, or in previews.
 *
 * @param highlight the swept color; defaults to the lighter of surface and on-surface.
 */
@Composable
public fun Modifier.skeletonShimmer(highlight: Color = MaterialTheme.colorScheme.shimmerHighlight()): Modifier {
    val animate = LocalSkeletonShimmerEnabled.current && decorativeAnimationsEnabled()
    val sweep by produceState(initialValue = 0f, animate) {
        if (!animate) {
            value = 0f
            return@produceState
        }
        while (true) {
            withInfiniteAnimationFrameMillis { frameMillis -> value = skeletonShimmerSweep(frameMillis) }
        }
    }
    if (!animate) return this
    return this
        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithContent {
            drawContent()
            val bandWidth = size.width * SHIMMER_BAND_WIDTH_FRACTION
            val bandStart = -bandWidth + (size.width + 2f * bandWidth) * sweep
            drawRect(
                brush = Brush.linearGradient(
                    0f to highlight.copy(alpha = 0f),
                    HALF to highlight.copy(alpha = SHIMMER_PEAK_ALPHA),
                    1f to highlight.copy(alpha = 0f),
                    start = Offset(bandStart, 0f),
                    end = Offset(bandStart + bandWidth, 0f),
                ),
                blendMode = BlendMode.SrcAtop,
            )
        }
}

/**
 * The band position at [frameMillis], from `0f` (entering at the start edge) to `1f` (parked past
 * the end edge). It ramps up over the first [SHIMMER_SWEEP_FRACTION] of each
 * [SHIMMER_PERIOD_MILLIS] cycle, then rests at `1f`.
 */
internal fun skeletonShimmerSweep(frameMillis: Long): Float {
    val progress = frameMillis.mod(SHIMMER_PERIOD_MILLIS).toFloat() / SHIMMER_PERIOD_MILLIS
    return (progress / SHIMMER_SWEEP_FRACTION).coerceAtMost(1f)
}

internal const val SHIMMER_PERIOD_MILLIS = 1_800L
internal const val SHIMMER_SWEEP_FRACTION = 0.65f
private const val SHIMMER_BAND_WIDTH_FRACTION = 0.45f
private const val SHIMMER_PEAK_ALPHA = 0.10f
private const val HALF = 0.5f
