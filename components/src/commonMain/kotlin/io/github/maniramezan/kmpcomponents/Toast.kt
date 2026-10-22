package io.github.maniramezan.kmpcomponents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume

/**
 * A transient message on an inverse surface with an optional leading [icon] and action, announced
 * politely. Usually shown through a [ToastHost]; it can also be placed directly.
 *
 * @param iconTint tint for [icon]; `null` uses the message color, [Color.Unspecified] keeps a
 *   multicolor icon's own colors.
 * @param iconContentDescription leave `null` when the message already says what the icon shows.
 */
@Composable
public fun Toast(
    message: String,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    iconContentDescription: String? = null,
    iconTint: Color? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(colors.inverseSurface)
            .padding(horizontal = MaterialTheme.kmpSpacing.medium, vertical = MaterialTheme.kmpSpacing.small)
            .heightIn(min = minimumTouchTargetSize())
            .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                painter = icon,
                contentDescription = iconContentDescription,
                modifier = Modifier.size(glyphSize()),
                tint = iconTint ?: colors.inverseOnSurface,
            )
        }
        Text(
            text = message,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.inverseOnSurface,
        )
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) { Text(actionLabel, color = colors.inversePrimary) }
        }
    }
}

/** How long a toast stays before dismissing itself. */
public enum class ToastDuration {
    /** About four seconds; the default without an action. */
    SHORT,

    /** About ten seconds; the default with an action, so there is time to use it. */
    LONG,

    /** Until the user or the caller dismisses it. */
    INDEFINITE,
}

/** The screen edge a [ToastHost] anchors its toast to. */
public enum class ToastPosition { TOP, BOTTOM }

/** How a [ToastHostState.showToast] call ended. */
public enum class ToastResult {
    /** The user tapped the action. */
    ACTION_PERFORMED,

    /** The toast timed out or was dismissed without the action. */
    DISMISSED,
}

/** The auto-dismiss timeout in milliseconds, or `null` for [ToastDuration.INDEFINITE]. */
internal val ToastDuration.timeoutMillis: Long?
    get() = when (this) {
        ToastDuration.SHORT -> SHORT_TOAST_MILLIS
        ToastDuration.LONG -> LONG_TOAST_MILLIS
        ToastDuration.INDEFINITE -> null
    }

private const val SHORT_TOAST_MILLIS = 4_000L
private const val LONG_TOAST_MILLIS = 10_000L

/** The toast a [ToastHost] is showing, for custom `toast` slots. */
@Stable
public interface ToastData {
    public val message: String
    public val icon: Painter?
    public val iconContentDescription: String?
    public val iconTint: Color?
    public val actionLabel: String?
    public val duration: ToastDuration

    /** Ends the originating [ToastHostState.showToast] with [ToastResult.ACTION_PERFORMED]. */
    public fun performAction()

    /** Ends the originating [ToastHostState.showToast] with [ToastResult.DISMISSED]. */
    public fun dismiss()
}

/**
 * Shows one toast at a time, like `SnackbarHostState`: [showToast] suspends until the toast is
 * dismissed and reports how it ended; concurrent calls wait their turn. Pair it with a [ToastHost]
 * and create it with [rememberToastHostState].
 */
@Stable
public class ToastHostState {
    /** The toast being shown, or `null`. */
    public var currentToastData: ToastData? by mutableStateOf(null)
        private set

    private val mutex = Mutex()

    /**
     * Shows [message] and suspends until the toast is dismissed.
     *
     * @param duration defaults to [ToastDuration.SHORT] without an action and [ToastDuration.LONG]
     *   with one.
     */
    public suspend fun showToast(
        message: String,
        icon: Painter? = null,
        iconContentDescription: String? = null,
        iconTint: Color? = null,
        actionLabel: String? = null,
        duration: ToastDuration = if (actionLabel == null) ToastDuration.SHORT else ToastDuration.LONG,
    ): ToastResult = mutex.withLock {
        try {
            suspendCancellableCoroutine { continuation ->
                currentToastData = ToastDataImpl(
                    message = message,
                    icon = icon,
                    iconContentDescription = iconContentDescription,
                    iconTint = iconTint,
                    actionLabel = actionLabel,
                    duration = duration,
                    continuation = continuation,
                )
            }
        } finally {
            currentToastData = null
        }
    }
}

private class ToastDataImpl(
    override val message: String,
    override val icon: Painter?,
    override val iconContentDescription: String?,
    override val iconTint: Color?,
    override val actionLabel: String?,
    override val duration: ToastDuration,
    private val continuation: CancellableContinuation<ToastResult>,
) : ToastData {
    override fun performAction() {
        if (continuation.isActive) continuation.resume(ToastResult.ACTION_PERFORMED)
    }

    override fun dismiss() {
        if (continuation.isActive) continuation.resume(ToastResult.DISMISSED)
    }
}

/** Remembers a [ToastHostState] across recompositions. */
@Composable
public fun rememberToastHostState(): ToastHostState = remember { ToastHostState() }

/**
 * Shows the toast held by [hostState] over the screen, sliding it in from [position], dismissing
 * it after its [ToastDuration], and keeping it inside the safe drawing area. Place it as the last
 * child of the screen so it floats above the content.
 *
 * An [ToastDuration.INDEFINITE] toast without an action is tap-to-dismiss, so it can always be
 * cleared; [dismissLabel] names that action for screen readers.
 */
@Composable
public fun ToastHost(
    hostState: ToastHostState,
    modifier: Modifier = Modifier,
    position: ToastPosition = ToastPosition.BOTTOM,
    dismissLabel: String = "Dismiss",
    toast: @Composable (ToastData) -> Unit = { data -> DefaultToast(data, dismissLabel) },
) {
    val current = hostState.currentToastData
    LaunchedEffect(current) {
        val timeout = current?.duration?.timeoutMillis
        if (current != null && timeout != null) {
            delay(timeout)
            current.dismiss()
        }
    }
    // Keep the last toast so the exit animation can still draw it after the state clears.
    var lastData by remember { mutableStateOf(current) }
    if (current != null) lastData = current

    val isTop = position == ToastPosition.TOP
    val slideOffset: (Int) -> Int = { height -> if (isTop) -height else height }
    val duration = MaterialTheme.kmpMotion.standardMillis
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = if (isTop) Alignment.TopCenter else Alignment.BottomCenter,
    ) {
        AnimatedVisibility(
            visible = current != null,
            enter = slideInVertically(tween(duration), slideOffset) + fadeIn(tween(duration)),
            exit = slideOutVertically(tween(duration), slideOffset) + fadeOut(tween(duration)),
        ) {
            lastData?.let { data ->
                Box(Modifier.windowInsetsPadding(WindowInsets.safeDrawing).padding(MaterialTheme.kmpSpacing.medium)) {
                    toast(data)
                }
            }
        }
    }
}

@Composable
private fun DefaultToast(data: ToastData, dismissLabel: String) {
    val tapToDismiss = data.duration == ToastDuration.INDEFINITE && data.actionLabel == null
    Toast(
        message = data.message,
        modifier = if (tapToDismiss) Modifier.clickable(onClickLabel = dismissLabel) { data.dismiss() } else Modifier,
        icon = data.icon,
        iconContentDescription = data.iconContentDescription,
        iconTint = data.iconTint,
        actionLabel = data.actionLabel,
        onAction = if (data.actionLabel != null) data::performAction else null,
    )
}
