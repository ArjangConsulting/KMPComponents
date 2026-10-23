package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign

/** Who sent a chat message, which decides its alignment and colors. */
public enum class ChatMessageSender { USER, ASSISTANT }

/** What a chat message bubble shows. */
public sealed interface ChatMessageState {
    /** The message [text], drawn by the log's `messageContent` slot. */
    public data class Content(public val text: String) : ChatMessageState

    /** A reply is pending; shows a [TypingIndicator]. */
    public data object Typing : ChatMessageState

    /** The message failed; shows the log's [ChatErrorAction]. */
    public data object Error : ChatMessageState
}

/** One item of a [ChatLog]. [id] must be unique and stable; it is the list key. */
@Immutable
public data class ChatMessage(
    public val id: String,
    public val sender: ChatMessageSender,
    public val state: ChatMessageState,
)

/** The text and retry action shown in failed [ChatLog] bubbles. */
@Immutable
public data class ChatErrorAction(
    public val message: String,
    public val retryLabel: String,
    public val onRetry: () -> Unit,
)

/**
 * A chat conversation: user bubbles on the end edge, assistant bubbles on the start edge, a typing
 * bubble for pending replies, and a retryable error bubble. With no messages it shows [idleHint].
 *
 * The log follows new messages and growing replies only while the reader is at the latest message,
 * so scrolling back through history is never interrupted. Use [messageContent] to render markdown,
 * links, or code; the default draws plain text. [typingDescription] is announced while a reply is
 * pending.
 */
@Composable
public fun ChatLog(
    messages: List<ChatMessage>,
    idleHint: String,
    errorAction: ChatErrorAction,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(vertical = MaterialTheme.kmpSpacing.small),
    typingDescription: String = "Responding",
    messageContent: @Composable (ChatMessageSender, String) -> Unit = { sender, text -> ChatMessageText(sender, text) },
) {
    if (messages.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (idleHint.isNotBlank()) {
                Text(
                    text = idleHint,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
        return
    }
    val listState = rememberLazyListState()
    val followUpdate = remember(messages) { !listState.canScrollForward }
    LaunchedEffect(messages.size, messages.lastOrNull()?.state) {
        if (followUpdate) {
            val lastIndex = messages.lastIndex
            listState.scrollToItem(lastIndex)
            // Scrolling by the item's height reaches the end of even a very tall reply; the list clamps it.
            listState.layoutInfo.visibleItemsInfo.lastOrNull { it.index == lastIndex }?.let { item ->
                listState.animateScrollToItem(lastIndex, scrollOffset = item.size)
            }
        }
    }
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small),
    ) {
        items(messages, key = { it.id }) { message ->
            ChatBubble(message, errorAction, typingDescription, messageContent)
        }
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage,
    errorAction: ChatErrorAction,
    typingDescription: String,
    content: @Composable (ChatMessageSender, String) -> Unit,
) {
    val isUser = message.sender == ChatMessageSender.USER
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = MaterialTheme.kmpSpacing.medium),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(BUBBLE_MAX_WIDTH_FRACTION),
            contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart,
        ) {
            val colors = MaterialTheme.colorScheme
            val bubble = Modifier
                .clip(MaterialTheme.shapes.large)
                .background(if (isUser) colors.primaryContainer else colors.surfaceVariant)
                .padding(horizontal = MaterialTheme.kmpSpacing.medium, vertical = MaterialTheme.kmpSpacing.small)
            when (val state = message.state) {
                is ChatMessageState.Content -> Box(bubble) { content(message.sender, state.text) }

                ChatMessageState.Typing -> Box(
                    bubble.semantics {
                        if (typingDescription.isNotBlank()) contentDescription = typingDescription
                        liveRegion = LiveRegionMode.Polite
                    },
                ) {
                    // The bubble carries the announcement, so the dots stay silent.
                    TypingIndicator()
                }

                ChatMessageState.Error -> Column(
                    bubble,
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.extraSmall),
                ) {
                    Text(
                        text = errorAction.message,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                    ThemeTextButton(text = errorAction.retryLabel, onClick = errorAction.onRetry)
                }
            }
        }
    }
}

/** The default message text, colored for the bubble it sits in. */
@Composable
private fun ChatMessageText(sender: ChatMessageSender, text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = if (sender == ChatMessageSender.USER) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
    )
}

private const val BUBBLE_MAX_WIDTH_FRACTION = 0.85f
