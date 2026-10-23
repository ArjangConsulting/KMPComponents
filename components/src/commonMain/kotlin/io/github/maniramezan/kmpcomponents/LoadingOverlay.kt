package io.github.maniramezan.kmpcomponents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign

/**
 * Hosts [content] and, while [loading], covers it with a scrim that blocks interaction, for work
 * the user started and must wait for (signing in, submitting, exporting).
 *
 * While loading, every pointer event over [content] is swallowed, [content] leaves the
 * accessibility tree, and [contentDescription] ("Signing in") is announced politely instead. With
 * [showIndicator], a card in the middle shows a spinner and the optional [message]; the message is
 * visual only, so include it in [contentDescription] if screen reader users need it. Pass
 * `showIndicator = false` when the triggering control already shows a spinner. For loads the user
 * can keep working through, prefer [LoadingState], [ProgressIndicator], or a skeleton.
 *
 * Keyboard focus moves to the scrim while loading and returns to the previously focused child
 * afterward. Underlying controls cannot receive keyboard input or focus while loading.
 *
 * The container sizes to [content]; pass `Modifier.fillMaxSize()` to cover a screen.
 */
@Composable
public fun LoadingOverlay(
    loading: Boolean,
    contentDescription: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    showIndicator: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val contentFocusRequester = remember { FocusRequester() }
    val overlayFocusRequester = remember { FocusRequester() }
    LaunchedEffect(loading) {
        if (loading) {
            contentFocusRequester.saveFocusedChild()
            overlayFocusRequester.requestFocus()
        } else {
            contentFocusRequester.restoreFocusedChild()
        }
    }
    Box(modifier = modifier, propagateMinConstraints = true) {
        Box(
            modifier = Modifier
                .focusRequester(contentFocusRequester)
                .focusProperties { onEnter = { if (loading) cancelFocusChange() } }
                .focusGroup()
                .then(if (loading) Modifier.clearAndSetSemantics {} else Modifier),
            content = content,
        )
        AnimatedVisibility(
            visible = loading,
            modifier = Modifier.matchParentSize(),
            enter = fadeIn(tween(MaterialTheme.kmpMotion.fastMillis)),
            exit = fadeOut(tween(MaterialTheme.kmpMotion.fastMillis)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .focusRequester(overlayFocusRequester)
                    .onPreviewKeyEvent { loading }
                    .focusable(enabled = loading)
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = SCRIM_ALPHA))
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) awaitPointerEvent().changes.forEach { it.consume() }
                        }
                    }
                    .semantics {
                        if (contentDescription.isNotBlank()) this.contentDescription = contentDescription
                        liveRegion = LiveRegionMode.Polite
                    },
                contentAlignment = Alignment.Center,
            ) {
                if (showIndicator) LoadingOverlayCard(message?.takeIf { it.isNotBlank() })
            }
        }
    }
}

@Composable
private fun LoadingOverlayCard(message: String?) {
    Surface(
        modifier = Modifier.padding(MaterialTheme.kmpSpacing.extraLarge),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            // The scrim announces the operation; the card must not announce it a second time.
            modifier = Modifier.padding(MaterialTheme.kmpSpacing.large).clearAndSetSemantics {},
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.medium),
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            if (message != null) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

// Dims the content enough to read as inert while keeping it legible.
private const val SCRIM_ALPHA = 0.32f
