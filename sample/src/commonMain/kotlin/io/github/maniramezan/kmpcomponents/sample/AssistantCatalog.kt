package io.github.maniramezan.kmpcomponents.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.maniramezan.kmpcomponents.AssistantContextBody
import io.github.maniramezan.kmpcomponents.AssistantContextCard
import io.github.maniramezan.kmpcomponents.AssistantDisclaimerFooter
import io.github.maniramezan.kmpcomponents.AssistantLimitPromptCard
import io.github.maniramezan.kmpcomponents.AssistantLimitPromptCopy
import io.github.maniramezan.kmpcomponents.AssistantQuickActionChips
import io.github.maniramezan.kmpcomponents.AssistantQuickActionState
import io.github.maniramezan.kmpcomponents.AssistantStatusBanner
import io.github.maniramezan.kmpcomponents.ChatErrorAction
import io.github.maniramezan.kmpcomponents.ChatLog
import io.github.maniramezan.kmpcomponents.ChatMessage
import io.github.maniramezan.kmpcomponents.ChatMessageSender
import io.github.maniramezan.kmpcomponents.ChatMessageState
import io.github.maniramezan.kmpcomponents.LoadingOverlay
import io.github.maniramezan.kmpcomponents.PrimaryButton
import io.github.maniramezan.kmpcomponents.SearchField
import io.github.maniramezan.kmpcomponents.SearchFieldLabels
import io.github.maniramezan.kmpcomponents.SectionCard
import io.github.maniramezan.kmpcomponents.ThemeCard
import io.github.maniramezan.kmpcomponents.ThemeSwitch
import io.github.maniramezan.kmpcomponents.TypingIndicator
import io.github.maniramezan.kmpcomponents.kmpSpacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Chat log, assistant cards, typing indicator, and loading overlay. */
@Composable
fun AssistantSection() {
    ChatDemo()
    AssistantCardsDemo()
    OverlayDemo()
}

private enum class QuickAction(val label: String) { SUMMARIZE("Summarize"), EXPLAIN("Explain"), REPHRASE("Rephrase") }

@Composable
private fun ChatDemo() {
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var draft by remember { mutableStateOf("") }
    var failNext by remember { mutableStateOf(false) }
    var nextId by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    fun reply(replyId: String, question: String) {
        scope.launch {
            delay(REPLY_MILLIS)
            val index = messages.indexOfFirst { it.id == replyId }
            if (index < 0) return@launch
            val state = if (failNext) ChatMessageState.Error else ChatMessageState.Content("You asked: “$question”")
            messages[index] = messages[index].copy(state = state)
        }
    }
    fun send(text: String) {
        if (text.isBlank()) return
        val replyId = "r${nextId++}"
        messages += ChatMessage("q${nextId++}", ChatMessageSender.USER, ChatMessageState.Content(text))
        messages += ChatMessage(replyId, ChatMessageSender.ASSISTANT, ChatMessageState.Typing)
        draft = ""
        reply(replyId, text)
    }
    SectionCard(
        title = "Chat log",
        description = "Local echo simulation: send text to preview pending, success, and error messages.",
    ) {
        ChatLog(
            messages = messages.toList(),
            idleHint = "Type an example message below.",
            errorAction = ChatErrorAction("The example reply failed.", "Retry") {
                val failed = messages.lastOrNull { it.state == ChatMessageState.Error } ?: return@ChatErrorAction
                failNext = false
                val index = messages.indexOf(failed)
                messages[index] = failed.copy(state = ChatMessageState.Typing)
                reply(failed.id, "retry")
            },
            modifier = Modifier.height(CHAT_HEIGHT),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SearchField(
                value = draft,
                onValueChange = { draft = it },
                modifier = Modifier.weight(1f),
                onSearch = ::send,
                labels = SearchFieldLabels(placeholder = "Message", clear = "Clear message"),
            )
            PrimaryButton(text = "Send", onClick = { send(draft) })
        }
        ThemeSwitch(checked = failNext, onCheckedChange = { failNext = it }, label = "Fail the next reply")
        AssistantDisclaimerFooter(text = "Sample disclaimer text. Replies in this catalog are simulated locally.")
    }
}

private const val REPLY_MILLIS = 1_500L
private val CHAT_HEIGHT = 320.dp

@Composable
private fun AssistantCardsDemo() {
    var selected by remember { mutableStateOf(QuickAction.EXPLAIN) }
    var actionsEnabled by remember { mutableStateOf(true) }
    var lastAction by remember { mutableStateOf("No card action selected") }
    SectionCard(title = "Assistant cards", description = lastAction) {
        AssistantContextCard(
            title = "Example context",
            highlight = "Highlighted text",
            body = AssistantContextBody("An example quotation", isQuoted = true),
            footnote = "Example footnote",
        )
        AssistantQuickActionChips(
            actions = QuickAction.entries,
            actionState = { AssistantQuickActionState(isSelected = it == selected, isEnabled = actionsEnabled) },
            label = { it.label },
            onAction = { selected = it },
        )
        ThemeSwitch(checked = actionsEnabled, onCheckedChange = {
            actionsEnabled = it
        }, label = "Quick actions enabled")
        if (!actionsEnabled) AssistantStatusBanner(message = "Example status: actions are disabled.")
        AssistantLimitPromptCard(
            copy = AssistantLimitPromptCopy(
                "Example limit reached",
                "This card previews two available actions.",
                "Primary action",
                "Secondary action",
            ),
            onPrimaryAction = { lastAction = "Primary action selected" },
            onSecondaryAction = { lastAction = "Secondary action selected" },
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("TypingIndicator ", style = MaterialTheme.typography.bodyMedium)
            TypingIndicator(contentDescription = "Writing a reply")
        }
    }
}

@Composable
private fun OverlayDemo() {
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    SectionCard(title = "Loading overlay") {
        LoadingOverlay(
            loading = loading,
            contentDescription = "Exporting",
            modifier = Modifier.fillMaxWidth(),
            message = "Exporting…",
        ) {
            ThemeCard(Modifier.height(OVERLAY_HEIGHT)) {
                Text("Taps here are blocked while exporting.")
                PrimaryButton(
                    text = "Export",
                    onClick = {
                        loading = true
                        scope.launch {
                            delay(EXPORT_MILLIS)
                            loading = false
                        }
                    },
                )
            }
        }
    }
}

private const val EXPORT_MILLIS = 2_000L
private val OVERLAY_HEIGHT = 200.dp
