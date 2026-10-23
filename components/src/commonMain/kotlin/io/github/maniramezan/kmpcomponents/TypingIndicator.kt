package io.github.maniramezan.kmpcomponents

import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics

/**
 * Three dots pulsing in turn, for "a reply is being written", usually inside a chat bubble. It
 * reads as composing rather than loading. The wave runs in the draw phase without recomposing,
 * and the dots hold still when the system reduces motion and in previews.
 *
 * @param contentDescription announced politely to screen readers; blank hides the indicator from
 *   them, for example when the enclosing bubble already announces the pending reply.
 */
@Composable
public fun TypingIndicator(
    modifier: Modifier = Modifier,
    contentDescription: String = "",
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    val animate = decorativeAnimationsEnabled()
    val frameMillis by produceState(initialValue = 0L, animate) {
        if (!animate) return@produceState
        while (true) {
            withInfiniteAnimationFrameMillis { value = it }
        }
    }
    val semantics = if (contentDescription.isNotBlank()) {
        Modifier.semantics {
            this.contentDescription = contentDescription
            liveRegion = LiveRegionMode.Polite
        }
    } else {
        Modifier.clearAndSetSemantics {}
    }
    Row(
        modifier = modifier.then(semantics).padding(vertical = MaterialTheme.kmpSpacing.extraSmall),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.extraSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(TYPING_DOT_COUNT) { index ->
            Box(
                Modifier
                    .size(MaterialTheme.kmpSpacing.small)
                    .graphicsLayer {
                        alpha =
                            if (animate) typingDotAlpha(index, frameMillis) else TYPING_DOT_BRIGHT_ALPHA
                    }
                    .background(color, CircleShape),
            )
        }
    }
}

/**
 * Alpha of dot [index] at [frameMillis]. Each dot ramps up over one stagger step, starting a step
 * after the previous dot, ramps back down over the next step, and rests until the cycle repeats:
 * a travelling wave rather than a synchronized blink.
 */
internal fun typingDotAlpha(index: Int, frameMillis: Long): Float {
    val t = frameMillis.mod(TYPING_CYCLE_MILLIS)
    val rampStart = index * TYPING_DOT_STAGGER_MILLIS
    val crest = rampStart + TYPING_DOT_STAGGER_MILLIS
    val rampEnd = crest + TYPING_DOT_STAGGER_MILLIS
    val range = TYPING_DOT_BRIGHT_ALPHA - TYPING_DOT_DIM_ALPHA
    return when {
        t <= rampStart || t >= rampEnd -> TYPING_DOT_DIM_ALPHA
        t < crest -> TYPING_DOT_DIM_ALPHA + range * (t - rampStart) / TYPING_DOT_STAGGER_MILLIS
        else -> TYPING_DOT_BRIGHT_ALPHA - range * (t - crest) / TYPING_DOT_STAGGER_MILLIS
    }
}

private const val TYPING_DOT_COUNT = 3
internal const val TYPING_CYCLE_MILLIS = 900L
internal const val TYPING_DOT_STAGGER_MILLIS = 150L
internal const val TYPING_DOT_DIM_ALPHA = 0.3f
internal const val TYPING_DOT_BRIGHT_ALPHA = 1f
