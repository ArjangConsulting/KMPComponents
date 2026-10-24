package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

// Material's TooltipBox handles hover, focus, and long-press itself, but to do so it wraps the
// anchor in an unnamed node that merges descendants. When the anchor is a button (its own merge
// boundary) that node stays empty, and iOS exposes it as an element with no description. Tooltip
// anchors here pass `enableUserInput = false` and use these triggers instead, which handle the same
// hover, focus, and long-press input without adding semantics.

/** Shows [state] while [interactionSource] is focused, until focus leaves. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TooltipFocusTrigger(state: TooltipState, interactionSource: InteractionSource) {
    val focused by interactionSource.collectIsFocusedAsState()
    LaunchedEffect(state, focused) {
        if (focused) state.show(MutatePriority.PreventUserInput) else state.dismiss()
    }
}

/**
 * Shows [state] while a mouse hovers the anchor, and while a touch or stylus press is held past the
 * long-press timeout; the release after a long press is swallowed so it does not also click the
 * anchor. Hover from other pointer types is ignored, as in Material.
 */
@OptIn(ExperimentalMaterial3Api::class)
internal fun Modifier.tooltipPointerTriggers(state: TooltipState): Modifier = pointerInput(state) {
    coroutineScope {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Main)
                if (event.changes.firstOrNull()?.type != PointerType.Mouse) continue
                when (event.type) {
                    PointerEventType.Enter -> launch { state.show(MutatePriority.UserInput) }
                    PointerEventType.Exit -> state.dismiss()
                }
            }
        }
    }
}.pointerInput(state) {
    coroutineScope {
        awaitEachGesture {
            val pass = PointerEventPass.Initial
            val down = awaitFirstDown(requireUnconsumed = false, pass = pass)
            if (down.type != PointerType.Touch && down.type != PointerType.Stylus) return@awaitEachGesture
            // Null only on timeout; a release or a cancelled gesture before then ends the wait.
            val endedEarly = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
                waitForUpOrCancellation(pass)
                true
            }
            if (endedEarly == null) {
                val showing =
                    launch(start = CoroutineStart.UNDISPATCHED) { state.show(MutatePriority.PreventUserInput) }
                waitForUpOrCancellation(pass)?.consume()
                showing.cancel()
            }
        }
    }
}
