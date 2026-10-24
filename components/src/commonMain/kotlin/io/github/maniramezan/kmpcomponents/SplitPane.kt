package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
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

    BoxWithConstraints(modifier) {
        val totalWidth = maxWidth
        val handleWidth = MaterialTheme.kmpSpacing.small.coerceAtLeast(minimumTouchTargetSize())
        val paneWidth = totalWidth - handleWidth
        val paneWidthPx = with(LocalDensity.current) { paneWidth.toPx() }.coerceAtLeast(1f)
        var dragSplit by remember { mutableFloatStateOf(shown) }
        val dragState = rememberDraggableState { delta ->
            val next = coerceSplit(dragSplit + delta / paneWidthPx, minSplit, maxSplit)
            if (next != dragSplit) {
                dragSplit = next
                currentOnSplitChange(next)
            }
        }
        Row {
            Box(Modifier.width(paneWidth * shown).fillMaxHeight()) { leading() }
            Box(
                modifier = Modifier
                    .width(handleWidth)
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
            Box(Modifier.weight(1f).fillMaxHeight()) { trailing() }
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
