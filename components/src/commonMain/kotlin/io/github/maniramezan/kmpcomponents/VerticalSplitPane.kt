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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Two panes stacked vertically with a draggable divider, such as an editor above a console.
 * [split] is the top pane's share of the height, kept between [minSplit] and [maxSplit].
 *
 * The divider can be dragged, moved with the Up/Down arrow keys by [keyboardStep] when focused,
 * and adjusted by screen readers like a slider. The drag position is internal state until the
 * gesture ends. On desktop it shows a vertical resize cursor.
 */
@Composable
public fun VerticalSplitPane(
    split: Float,
    onSplitChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    minSplit: Float = SplitPaneDefaults.MIN_SPLIT,
    maxSplit: Float = SplitPaneDefaults.MAX_SPLIT,
    keyboardStep: Float = SplitPaneDefaults.KEYBOARD_STEP,
    labels: SplitPaneLabels = SplitPaneLabels(splitDescription = { "${(it * PERCENT).roundToInt()}% top pane" }),
    top: @Composable () -> Unit,
    bottom: @Composable () -> Unit,
) {
    require(minSplit in 0f..maxSplit && maxSplit <= 1f) { "Need 0 <= minSplit <= maxSplit <= 1" }
    val shown = coerceSplit(split, minSplit, maxSplit)
    val currentSplit by rememberUpdatedState(shown)
    val currentOnSplitChange by rememberUpdatedState(onSplitChange)
    fun change(value: Float) {
        val next = coerceSplit(value, minSplit, maxSplit)
        if (next != currentSplit) currentOnSplitChange(next)
    }
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val dividerColor = if (focused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

    BoxWithConstraints(modifier) {
        val handleHeight = MaterialTheme.kmpSpacing.small
        val paneHeight = maxHeight - handleHeight
        val paneHeightPx = with(LocalDensity.current) { paneHeight.toPx() }.coerceAtLeast(1f)
        // Track the drag locally so a parent that applies changes late cannot make the divider jump.
        var dragSplit by remember { mutableFloatStateOf(shown) }
        val dragState = rememberDraggableState { delta ->
            val next = coerceSplit(dragSplit + delta / paneHeightPx, minSplit, maxSplit)
            if (next != dragSplit) {
                dragSplit = next
                currentOnSplitChange(next)
            }
        }
        Column {
            Box(Modifier.height(paneHeight * shown).fillMaxWidth()) { top() }
            Box(
                modifier = Modifier
                    .height(handleHeight)
                    .fillMaxWidth()
                    .pointerHoverIcon(resizeVerticalPointerIcon)
                    .draggable(
                        state = dragState,
                        orientation = Orientation.Vertical,
                        interactionSource = interactionSource,
                        onDragStarted = { dragSplit = currentSplit },
                    )
                    .onKeyEvent { event ->
                        val direction = when (event.key) {
                            Key.DirectionUp -> -1
                            Key.DirectionDown -> 1
                            else -> 0
                        }
                        if (event.type != KeyEventType.KeyDown || direction == 0) return@onKeyEvent false
                        change(currentSplit + direction * keyboardStep)
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
                        .height(if (focused) DIVIDER_FOCUSED_THICKNESS else DIVIDER_THICKNESS)
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.kmpSpacing.small)
                        .background(dividerColor),
                )
            }
            Box(Modifier.weight(1f).fillMaxWidth()) { bottom() }
        }
    }
}

/** Desktop shows a vertical resize cursor; touch platforms have no hover cursor. */
internal expect val resizeVerticalPointerIcon: PointerIcon

private const val PERCENT = 100

// Hairline divider, the same accepted exception as a 1.dp border; it thickens when focused.
private val DIVIDER_THICKNESS = 1.dp
private val DIVIDER_FOCUSED_THICKNESS = 2.dp
