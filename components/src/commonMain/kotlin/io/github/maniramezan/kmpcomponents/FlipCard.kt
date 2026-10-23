package io.github.maniramezan.kmpcomponents

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription

/** The axis a [FlipCard] turns around. */
public enum class FlipAxis {
    /** Around the vertical axis, like turning a page. */
    HORIZONTAL,

    /** Around the horizontal axis, top over bottom. */
    VERTICAL,
}

/**
 * A two-sided card that turns in 3D between [front] and [back], for flash cards and reveals. Size
 * it with [modifier]; both faces fill it.
 *
 * Pass [flipped] to control it, or leave it `null` and the card keeps its own state (saved across
 * recreation). Either way a tap requests the other face through [onFlippedChange]. The face turned
 * away leaves composition and keyboard traversal; its saveable state is restored when shown again.
 * The back is never mirrored. [frontLabel] and [backLabel]
 * are announced as the state, and [flipLabel] names the tap action.
 */
@Composable
public fun FlipCard(
    front: @Composable BoxScope.() -> Unit,
    back: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    flipped: Boolean? = null,
    onFlippedChange: ((Boolean) -> Unit)? = null,
    axis: FlipAxis = FlipAxis.HORIZONTAL,
    animationSpec: AnimationSpec<Float> = tween(MaterialTheme.kmpMotion.deliberateMillis),
    enabled: Boolean = true,
    shape: Shape = MaterialTheme.shapes.large,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    frontLabel: String? = null,
    backLabel: String? = null,
    flipLabel: String? = null,
) {
    var internalFlipped by rememberSaveable { mutableStateOf(false) }
    val isFlipped = flipped ?: internalFlipped
    val faceStateHolder = rememberSaveableStateHolder()
    // Read only in the faces' graphics layers, so the flip animates without recomposing the card.
    val rotation = animateFloatAsState(if (isFlipped) HALF_TURN else 0f, animationSpec, label = "flip")
    val density = LocalDensity.current.density
    // Each face is shown for half the turn; at the edge-on midpoint the swap is invisible.
    val frontVisible by remember { derivedStateOf { rotation.value <= QUARTER_TURN } }
    val state = if (isFlipped) backLabel else frontLabel
    val toggle = if (enabled) {
        Modifier.toggleable(value = isFlipped, role = Role.Switch) { requested ->
            if (flipped == null) internalFlipped = requested
            onFlippedChange?.invoke(requested)
        }
    } else {
        Modifier
    }
    Box(
        modifier = modifier
            .keyboardFocusOutline(shape)
            .then(toggle)
            .semantics {
                if (state != null) stateDescription = state
                if (enabled && flipLabel != null) onClick(label = flipLabel, action = null)
            },
    ) {
        faceStateHolder.SaveableStateProvider(frontVisible) {
            if (frontVisible) {
                FlipFace({ rotation.value }, axis, density, shape, containerColor, front)
            } else {
                FlipFace({ rotation.value - HALF_TURN }, axis, density, shape, containerColor, back)
            }
        }
    }
}

@Composable
private fun BoxScope.FlipFace(
    rotation: () -> Float,
    axis: FlipAxis,
    density: Float,
    shape: Shape,
    containerColor: Color,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = Modifier
            .matchParentSize()
            .graphicsLayer {
                when (axis) {
                    FlipAxis.HORIZONTAL -> rotationY = rotation()
                    FlipAxis.VERTICAL -> rotationX = rotation()
                }
                cameraDistance = CAMERA_DISTANCE_MULTIPLIER * density
            }
            .clip(shape)
            .background(containerColor),
        content = content,
    )
}

private const val HALF_TURN = 180f
private const val QUARTER_TURN = 90f

// A small camera distance deepens the perspective so the turn reads as 3D, not a flat squish.
private const val CAMERA_DISTANCE_MULTIPLIER = 8f
