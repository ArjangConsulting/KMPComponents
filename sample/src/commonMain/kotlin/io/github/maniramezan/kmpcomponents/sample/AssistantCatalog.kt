package io.github.maniramezan.kmpcomponents.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import io.github.maniramezan.kmpcomponents.ChatComposer
import io.github.maniramezan.kmpcomponents.ChatComposerVoiceInput
import io.github.maniramezan.kmpcomponents.ChatComposerVoiceState
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
import io.github.maniramezan.kmpcomponents.rememberTypewriterReveal
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Chat log, assistant cards, typing indicator, and loading overlay. */
@Composable
fun AssistantSection() {
    ChatDemo()
    AssistantCardsDemo()
    StreamingDemo()
    OverlayDemo()
}

private enum class QuickAction(val label: String) { SUMMARIZE("Summarize"), EXPLAIN("Explain"), REPHRASE("Rephrase") }

@Composable
private fun ChatDemo() {
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var draft by remember { mutableStateOf("") }
    var failNext by remember { mutableStateOf(false) }
    var nextId by remember { mutableIntStateOf(0) }
    var voiceEnabled by remember { mutableStateOf(false) }
    var voiceState: ChatComposerVoiceState by remember { mutableStateOf(ChatComposerVoiceState.Idle) }
    var voiceJob by remember { mutableStateOf<Job?>(null) }
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
        ChatComposer(
            value = draft,
            onValueChange = { draft = it },
            onSend = { send(draft) },
            modifier = Modifier.fillMaxWidth(),
            voiceInput = if (voiceEnabled) {
                ChatComposerVoiceInput(
                    state = voiceState,
                    onStart = {
                        voiceJob = scope.launch {
                            simulateDictation(
                                onState = { voiceState = it },
                                onTranscript = { draft = it },
                            )
                        }
                    },
                    onStop = {
                        voiceJob?.cancel()
                        voiceState = ChatComposerVoiceState.Idle
                    },
                )
            } else {
                null
            },
        )
        ThemeSwitch(checked = failNext, onCheckedChange = { failNext = it }, label = "Fail the next reply")
        ThemeSwitch(
            checked = voiceEnabled,
            onCheckedChange = { voiceEnabled = it },
            label = "Simulated voice input",
            supportingText = "The microphone types a sample transcript; no audio is recorded.",
        )
        AssistantDisclaimerFooter(text = "Sample disclaimer text. Replies in this catalog are simulated locally.")
    }
}

private const val REPLY_MILLIS = 1_500L

/** Pretends to dictate: a few listening frames with changing levels, then processing, then a transcript. */
private suspend fun simulateDictation(onState: (ChatComposerVoiceState) -> Unit, onTranscript: (String) -> Unit) {
    val words = SIMULATED_TRANSCRIPT.split(" ")
    words.forEachIndexed { index, _ ->
        onState(ChatComposerVoiceState.Listening(level = SIMULATED_LEVELS[index % SIMULATED_LEVELS.size]))
        onTranscript(words.take(index + 1).joinToString(" "))
        delay(SIMULATED_WORD_MILLIS)
    }
    onState(ChatComposerVoiceState.Processing)
    delay(SIMULATED_WORD_MILLIS * 2)
    onTranscript(SIMULATED_TRANSCRIPT)
    onState(ChatComposerVoiceState.Idle)
}

private const val SIMULATED_TRANSCRIPT = "This sample transcript was typed by a simulation"
private val SIMULATED_LEVELS = listOf(0.3f, 0.8f, 0.5f, 1f, 0.2f, 0.6f)
private const val SIMULATED_WORD_MILLIS = 350L
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

@Composable
private fun StreamingDemo() {
    var target by remember { mutableStateOf("") }
    var streaming by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val revealed by rememberTypewriterReveal(target, charsPerSecond = STREAM_CHARS_PER_SECOND)
    SectionCard(
        title = "Streaming reply",
        description = "Simulated: the example text arrives in chunks and is revealed character by character. " +
            "With reduced motion it appears at once.",
    ) {
        Text(
            text = revealed.ifEmpty { "Press Stream example reply to start." },
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth().heightIn(min = STREAM_MIN_HEIGHT),
        )
        PrimaryButton(
            text = "Stream example reply",
            enabled = !streaming,
            onClick = {
                streaming = true
                target = ""
                scope.launch {
                    STREAM_CHUNKS.forEach { chunk ->
                        target += chunk
                        delay(STREAM_CHUNK_MILLIS)
                    }
                    streaming = false
                }
            },
        )
    }
}

private val STREAM_CHUNKS = listOf(
    "This example reply ",
    "arrives in several chunks, ",
    "the way a streamed response would, ",
    "and keeps revealing from where it left off. 🎉",
)
private const val STREAM_CHARS_PER_SECOND = 40
private const val STREAM_CHUNK_MILLIS = 600L
private val STREAM_MIN_HEIGHT = 72.dp
