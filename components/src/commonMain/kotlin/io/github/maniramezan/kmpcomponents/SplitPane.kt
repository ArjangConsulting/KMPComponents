package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/** Localizable accessibility strings for [HorizontalSplitPane]'s divider. */
@Immutable
public data class SplitPaneLabels(
    public val handleDescription: String = "Resize panes",
    public val splitDescription: (split: Float) -> String = { "${(it * PERCENT).roundToInt()}% leading pane" },
)

/** Default bounds and keyboard step for [HorizontalSplitPane], as fractions of the width. */
public object SplitPaneDefaults {
    public const val MIN_SPLIT: Float = 0.2f
    public const val MAX_SPLIT: Float = 0.8f
    public const val KEYBOARD_STEP: Float = 0.05f
}

/**
 * Two panes side by side with a draggable divider. [split] is the leading pane's share of the
 * width, kept between [minSplit] and [maxSplit]; persist it if the layout should survive restarts.
 *
 * The divider can be dragged, moved with the Left/Right arrow keys by [keyboardStep] when focused,
 * and adjusted by screen readers like a slider. The handle reserves Material's minimum touch
 * target width. Dragging and arrow keys move the divider in the
 * indicated direction in both left-to-right and right-to-left layouts. Drag deltas accumulate
 * locally so callbacks stay consistent when the caller applies updates late; the visible panes
 * follow [split]. On desktop it shows a resize cursor.
 */
@Composable
public fun HorizontalSplitPane(
    split: Float,
    onSplitChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    minSplit: Float = SplitPaneDefaults.MIN_SPLIT,
    maxSplit: Float = SplitPaneDefaults.MAX_SPLIT,
    keyboardStep: Float = SplitPaneDefaults.KEYBOARD_STEP,
    labels: SplitPaneLabels = SplitPaneLabels(),
    leading: @Composable () -> Unit,
    trailing: @Composable () -> Unit,
) {
    require(minSplit in 0f..maxSplit && maxSplit <= 1f) { "Need 0 <= minSplit <= maxSplit <= 1" }
    val shown = coerceSplit(split, minSplit, maxSplit)
    val currentSplit by rememberUpdatedState(shown)
    val currentOnSplitChange by rememberUpdatedState(onSplitChange)
    val reverseDirection = LocalLayoutDirection.current == LayoutDirection.Rtl
    fun change(value: Float) {
        val next = coerceSplit(value, minSplit, maxSplit)
        if (next != currentSplit) currentOnSplitChange(next)
    }
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val dividerColor = if (focused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant

    val handleWidth = MaterialTheme.kmpSpacing.small.coerceAtLeast(minimumTouchTargetSize())
    val paneExtent = remember { SplitPaneExtent() }
    var dragSplit by remember { mutableFloatStateOf(shown) }
    val dragState = rememberDraggableState { delta ->
        val next = coerceSplit(dragSplit + delta / paneExtent.px, minSplit, maxSplit)
        if (next != dragSplit) {
            dragSplit = next
            currentOnSplitChange(next)
        }
    }
    SplitPaneLayout(
        orientation = Orientation.Horizontal,
        split = shown,
        handleThickness = handleWidth,
        paneExtent = paneExtent,
        modifier = modifier,
        first = leading,
        second = trailing,
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .pointerHoverIcon(resizeHorizontalPointerIcon)
                .draggable(
                    state = dragState,
                    orientation = Orientation.Horizontal,
                    interactionSource = interactionSource,
                    reverseDirection = reverseDirection,
                    onDragStarted = { dragSplit = currentSplit },
                )
                .onKeyEvent { event ->
                    val direction = when (event.key) {
                        Key.DirectionLeft -> -1
                        Key.DirectionRight -> 1
                        else -> 0
                    }
                    if (event.type != KeyEventType.KeyDown || direction == 0) return@onKeyEvent false
                    val layoutDirectionMultiplier = if (reverseDirection) -1 else 1
                    change(currentSplit + direction * layoutDirectionMultiplier * keyboardStep)
                    true
                }
                .semantics {
                    contentDescription = labels.handleDescription
                    stateDescription = labels.splitDescription(shown)
                    progressBarRangeInfo = ProgressBarRangeInfo(shown, minSplit..maxSplit)
                    setProgress { target ->
                        change(target)
                        true
                    }
                }
                .focusable(interactionSource = interactionSource),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .width(if (focused) DIVIDER_FOCUSED_WIDTH else DIVIDER_WIDTH)
                    .fillMaxHeight()
                    .padding(vertical = MaterialTheme.kmpSpacing.small)
                    .background(dividerColor),
            )
        }
    }
}

/** The pane space along the split axis, in pixels, written by the measure pass and read by drags. */
internal class SplitPaneExtent {
    var px: Float = 1f
}

/**
 * Lays out [first], [handle], and [second] along [orientation]. The panes are sized in the measure
 * pass, so moving the divider re-measures them instead of recomposing them.
 */
@Composable
internal fun SplitPaneLayout(
    orientation: Orientation,
    split: Float,
    handleThickness: Dp,
    paneExtent: SplitPaneExtent,
    modifier: Modifier,
    first: @Composable () -> Unit,
    second: @Composable () -> Unit,
    handle: @Composable () -> Unit,
) {
    val horizontal = orientation == Orientation.Horizontal
    Layout(
        contents = listOf({ Box { first() } }, handle, { Box { second() } }),
        modifier = modifier,
    ) { (firstMeasurables, handleMeasurables, secondMeasurables), constraints ->
        val mainMax = if (horizontal) constraints.maxWidth else constraints.maxHeight
        val crossMax = if (horizontal) constraints.maxHeight else constraints.maxWidth
        val crossBounded = if (horizontal) constraints.hasBoundedHeight else constraints.hasBoundedWidth
        val handlePx = handleThickness.roundToPx()
        val paneSpace = mainMax - handleThickness.toPx()
        paneExtent.px = paneSpace.coerceAtLeast(1f)
        val firstMain = (paneSpace * split).roundToInt().coerceIn(0, mainMax)
        val secondMain = (mainMax - firstMain - handlePx).coerceAtLeast(0)
        fun measure(measurables: List<Measurable>, main: Int) = measurables.map { measurable ->
            val crossMin = if (crossBounded) crossMax else 0
            measurable.measure(
                if (horizontal) {
                    Constraints(minWidth = main, maxWidth = main, minHeight = crossMin, maxHeight = crossMax)
                } else {
                    Constraints(minWidth = crossMin, maxWidth = crossMax, minHeight = main, maxHeight = main)
                },
            )
        }
        val firstPlaceables = measure(firstMeasurables, firstMain)
        val handlePlaceables = measure(handleMeasurables, handlePx)
        val secondPlaceables = measure(secondMeasurables, secondMain)
        val all = firstPlaceables + handlePlaceables + secondPlaceables
        val crossSize = all.maxOfOrNull { if (horizontal) it.height else it.width } ?: 0
        val width = (if (horizontal) mainMax else crossSize).coerceIn(constraints.minWidth, constraints.maxWidth)
        val height = (if (horizontal) crossSize else mainMax).coerceIn(constraints.minHeight, constraints.maxHeight)
        layout(width, height) {
            var offset = 0
            val slots = listOf(
                firstPlaceables to firstMain,
                handlePlaceables to handlePx,
                secondPlaceables to secondMain,
            )
            for ((placeables, main) in slots) {
                placeables.forEach { if (horizontal) it.placeRelative(offset, 0) else it.placeRelative(0, offset) }
                offset += main
            }
        }
    }
}

/** [split] clamped to `minSplit..maxSplit`; NaN (e.g. from a corrupt preference) becomes the midpoint. */
public fun coerceSplit(split: Float, minSplit: Float, maxSplit: Float): Float =
    if (split.isNaN()) (minSplit + maxSplit) / 2f else split.coerceIn(minSplit, maxSplit)

/** Desktop shows a horizontal resize cursor; touch platforms have no hover cursor. */
internal expect val resizeHorizontalPointerIcon: PointerIcon

private const val PERCENT = 100

// Hairline divider, the same accepted exception as a 1.dp border; it thickens when focused.
private val DIVIDER_WIDTH = 1.dp
private val DIVIDER_FOCUSED_WIDTH = 2.dp
