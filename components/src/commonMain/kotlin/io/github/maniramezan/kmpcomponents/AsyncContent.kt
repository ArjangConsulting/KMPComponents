package io.github.maniramezan.kmpcomponents

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics

/**
 * The state rendered by [AsyncContent]: loading, empty, failed, or showing [Content] of type [T].
 *
 * The state is hoisted: derive it from your own data holder and pass it to [AsyncContent].
 */
public sealed interface AsyncContentState<out T> {
    /** Data is being fetched; [AsyncContent] shows its loading placeholder. */
    public data object Loading : AsyncContentState<Nothing>

    /** The request succeeded but there is nothing to show; [AsyncContent] shows its empty placeholder. */
    public data object Empty : AsyncContentState<Nothing>

    /**
     * The request failed; [AsyncContent] shows its error placeholder.
     *
     * @param message optional, caller-supplied (localized) failure description that takes precedence
     *   over [AsyncContent]'s `errorMessage`. A blank value is ignored.
     */
    public data class Error(public val message: String? = null) : AsyncContentState<Nothing>

    /** Data is available; [AsyncContent] renders its `content` slot with [data]. */
    public data class Content<out T>(public val data: T) : AsyncContentState<T>
}

/**
 * Switches between loading, empty, error, and content presentations of [state], with the stock
 * [LoadingState], [EmptyState], and [ErrorState] placeholders.
 *
 * All text is caller-supplied. Every placeholder is announced as a polite live region:
 * [LoadingState] and [ErrorState] provide their own, and the empty placeholder is wrapped in one.
 *
 * Presentations cross-fade over `MaterialTheme.kmpMotion.fastMillis`. The cross-fade runs only when
 * the kind of state changes (not when [AsyncContentState.Content.data] changes) and is skipped when
 * the system reduces motion. The `content` slot leaves composition while another state is shown, so
 * hoist scroll or input state that must survive a reload.
 *
 * Use the slot-based overload to replace any placeholder.
 *
 * @param state which presentation to show.
 * @param loadingLabel visible and spoken label of the loading placeholder.
 * @param emptyTitle title of the empty placeholder.
 * @param errorTitle title of the error placeholder.
 * @param errorMessage message of the error placeholder, unless [AsyncContentState.Error.message] is set.
 * @param emptyMessage optional supporting text of the empty placeholder.
 * @param emptyAction optional action (for example a button) of the empty placeholder.
 * @param errorAction optional action (for example a Retry button) of the error placeholder.
 * @param contentAlignment where placeholders and [content] sit when the container is larger than they are.
 * @param content content for [AsyncContentState.Content]; receives the state's data.
 */
@Composable
public fun <T> AsyncContent(
    state: AsyncContentState<T>,
    loadingLabel: String,
    emptyTitle: String,
    errorTitle: String,
    errorMessage: String,
    modifier: Modifier = Modifier,
    emptyMessage: String? = null,
    emptyAction: (@Composable () -> Unit)? = null,
    errorAction: (@Composable () -> Unit)? = null,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable (data: T) -> Unit,
) {
    AsyncContent(
        state = state,
        modifier = modifier,
        contentAlignment = contentAlignment,
        loading = { LoadingState(label = loadingLabel) },
        // Full width, like LoadingState and ErrorState, so all three placeholders center the same way.
        empty = {
            EmptyState(
                title = emptyTitle,
                modifier = Modifier.fillMaxWidth(),
                message = emptyMessage,
                action = emptyAction,
            )
        },
        error = { error ->
            ErrorState(title = errorTitle, message = asyncErrorMessage(error, errorMessage), action = errorAction)
        },
        content = content,
    )
}

/**
 * Slot-based [AsyncContent]: switches between caller-supplied [loading], [empty], [error], and
 * [content] presentations of [state].
 *
 * The [empty] slot is wrapped in a polite live region so its appearance is announced. [loading] and
 * [error] are not wrapped: use [LoadingState] and [ErrorState] (which announce themselves), or add
 * `Modifier.semantics { liveRegion = LiveRegionMode.Polite }` to a custom slot. Cross-fade and
 * recomposition behavior match the string-based overload.
 *
 * @param state which presentation to show.
 * @param contentAlignment where the active slot sits when the container is larger than it is.
 * @param loading shown for [AsyncContentState.Loading].
 * @param empty shown for [AsyncContentState.Empty].
 * @param error shown for [AsyncContentState.Error]; receives the state so it can read its message.
 * @param content shown for [AsyncContentState.Content]; receives the state's data.
 */
@Composable
public fun <T> AsyncContent(
    state: AsyncContentState<T>,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    loading: @Composable () -> Unit,
    empty: @Composable () -> Unit,
    error: @Composable (error: AsyncContentState.Error) -> Unit,
    content: @Composable (data: T) -> Unit,
) {
    val fadeMillis = MaterialTheme.kmpMotion.fastMillis
    val render: @Composable (AsyncContentState<T>) -> Unit = { target ->
        when (target) {
            AsyncContentState.Loading -> loading()
            AsyncContentState.Empty -> Box(Modifier.semantics { liveRegion = LiveRegionMode.Polite }) { empty() }
            is AsyncContentState.Error -> error(target)
            is AsyncContentState.Content -> content(target.data)
        }
    }
    if (decorativeAnimationsEnabled()) {
        AnimatedContent(
            targetState = state,
            modifier = modifier,
            transitionSpec = {
                ContentTransform(
                    targetContentEnter = fadeIn(animationSpec = tween(fadeMillis)),
                    initialContentExit = fadeOut(animationSpec = tween(fadeMillis)),
                    sizeTransform = null,
                )
            },
            contentAlignment = contentAlignment,
            contentKey = ::asyncContentKind,
            label = "AsyncContent",
        ) { target -> render(target) }
    } else {
        Box(modifier = modifier, contentAlignment = contentAlignment) { render(state) }
    }
}

/**
 * Identifies the kind of [state], so [AsyncContent] cross-fades between kinds but not when a
 * [AsyncContentState.Content]'s data or an [AsyncContentState.Error]'s message changes.
 */
internal fun asyncContentKind(state: AsyncContentState<*>): Int = when (state) {
    AsyncContentState.Loading -> 0
    AsyncContentState.Empty -> 1
    is AsyncContentState.Error -> 2
    is AsyncContentState.Content -> 3
}

/** [error]'s own message when it is not blank, otherwise [fallback]. */
internal fun asyncErrorMessage(error: AsyncContentState.Error, fallback: String): String =
    error.message?.takeIf { it.isNotBlank() } ?: fallback
