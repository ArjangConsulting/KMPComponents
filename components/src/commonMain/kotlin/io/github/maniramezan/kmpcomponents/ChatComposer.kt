package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization

/** Where a [ChatComposer]'s voice input is. The caller runs speech recognition and moves between states. */
@Immutable
public sealed interface ChatComposerVoiceState {
    /** Voice input is ready. The composer shows a microphone button while the draft is blank. */
    public data object Idle : ChatComposerVoiceState

    /**
     * The microphone is live. [level] is the input loudness from 0 (silence) to 1 (loud), drawn as a
     * ring around the stop button; values outside that range are clamped.
     */
    public data class Listening(public val level: Float) : ChatComposerVoiceState

    /** Recording stopped and the transcript is being produced. The button shows progress. */
    public data object Processing : ChatComposerVoiceState

    /** Voice input cannot be used, for example because permission was denied. The microphone is disabled. */
    public data object Unavailable : ChatComposerVoiceState
}

/**
 * Voice input for a [ChatComposer]. Start recognition in [onStart], write the live transcript into
 * the composer's value so it can be reviewed before sending, update [state] as recognition
 * progresses, and stop in [onStop].
 */
@Immutable
public data class ChatComposerVoiceInput(
    public val state: ChatComposerVoiceState,
    public val onStart: () -> Unit,
    public val onStop: () -> Unit,
)

/** Localizable text for [ChatComposer]. */
@Immutable
public data class ChatComposerLabels(
    /** Placeholder while the draft is empty; also the field's accessible name. */
    public val placeholder: String = "Message",
    /** Accessible name and tooltip of the send button. */
    public val send: String = "Send",
    /** Accessible name and tooltip of the microphone button. */
    public val startVoice: String = "Start voice input",
    /** Accessible name and tooltip of the stop button while listening. */
    public val stopVoice: String = "Stop voice input",
    /** Accessible name of the progress button while the transcript is produced. */
    public val processingVoice: String = "Processing speech",
)

/**
 * A chat message field with a send button and optional voice input.
 *
 * The field grows with its text up to [maxLines] and then scrolls. The trailing button sends a
 * non-blank draft. With [voiceInput], an empty draft shows a microphone instead, which becomes a stop
 * button with a level ring while listening and a spinner while processing; editing pauses during
 * both so the transcript is not overwritten.
 *
 * Send runs [onSend] from the button, the keyboard's Send action, or Enter on a hardware keyboard
 * (Shift+Enter inserts a line break). It never runs for a blank draft or while [sendEnabled] is
 * `false`, for example while a reply is streaming. The composer never clears [value]; clear it in
 * [onSend]. Stopping an active recording is always allowed.
 */
@Composable
public fun ChatComposer(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
    sendEnabled: Boolean = true,
    maxLines: Int = DEFAULT_MAX_LINES,
    voiceInput: ChatComposerVoiceInput? = null,
    labels: ChatComposerLabels = ChatComposerLabels(),
) {
    val action = resolveChatComposerAction(value, voiceInput?.state, sendEnabled)
    val submit = {
        if (sendEnabled && value.isNotBlank()) onSend()
    }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small),
        verticalAlignment = Alignment.Bottom,
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .onPreviewKeyEvent { event ->
                    val sendKey = event.key == Key.Enter || event.key == Key.NumPadEnter
                    if (sendKey && !event.isShiftPressed && action.allowsEditing) {
                        if (event.type == KeyEventType.KeyDown) submit()
                        true
                    } else {
                        false
                    }
                }
                .semantics { if (labels.placeholder.isNotBlank()) contentDescription = labels.placeholder },
            readOnly = !action.allowsEditing,
            placeholder = labels.placeholder.takeIf { it.isNotBlank() }?.let { text -> { Text(text) } },
            maxLines = maxLines.coerceAtLeast(1),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Send,
            ),
            keyboardActions = KeyboardActions(onSend = { submit() }),
        )
        ChatComposerActionButton(
            action = action,
            labels = labels,
            onSend = submit,
            onStartVoice = { voiceInput?.onStart?.invoke() },
            onStopVoice = { voiceInput?.onStop?.invoke() },
        )
    }
}

@Composable
private fun ChatComposerActionButton(
    action: ChatComposerAction,
    labels: ChatComposerLabels,
    onSend: () -> Unit,
    onStartVoice: () -> Unit,
    onStopVoice: () -> Unit,
) {
    val style = MaterialTheme.typography.titleLarge
    val iconSize = glyphSize(style)
    val label = when (action) {
        is ChatComposerAction.Send -> labels.send
        is ChatComposerAction.StartVoice -> labels.startVoice
        is ChatComposerAction.StopVoice -> labels.stopVoice
        ChatComposerAction.Processing -> labels.processingVoice
    }
    TooltipIconButton(
        label = label,
        onClick = {
            when (action) {
                is ChatComposerAction.Send -> onSend()
                is ChatComposerAction.StartVoice -> onStartVoice()
                is ChatComposerAction.StopVoice -> onStopVoice()
                ChatComposerAction.Processing -> Unit
            }
        },
        iconSize = iconSize,
        enabled = action.isTappable,
    ) {
        // A disabled IconButton already supplies Material's disabled content color.
        val tint = if (action.isTappable) MaterialTheme.colorScheme.primary else LocalContentColor.current
        when (action) {
            is ChatComposerAction.Send -> GlyphIcon(glyph = Glyph.SEND, size = iconSize, color = tint)

            is ChatComposerAction.StartVoice -> GlyphIcon(glyph = Glyph.MIC, size = iconSize, color = tint)

            is ChatComposerAction.StopVoice -> Box(contentAlignment = Alignment.Center) {
                val scale = if (rememberReduceMotionEnabled()) 1f else chatComposerRingScale(action.level)
                Box(
                    Modifier
                        .size(iconSize)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                )
                GlyphIcon(glyph = Glyph.STOP, size = iconSize, color = tint)
            }

            ChatComposerAction.Processing -> InlineSpinner(style = style, color = MaterialTheme.colorScheme.primary)
        }
    }
}

/** What a [ChatComposer]'s trailing button does, derived from the draft and voice state. */
internal sealed interface ChatComposerAction {
    data class Send(val enabled: Boolean) : ChatComposerAction

    data class StartVoice(val enabled: Boolean) : ChatComposerAction

    data class StopVoice(val level: Float) : ChatComposerAction

    data object Processing : ChatComposerAction

    /** Whether the button responds to presses. Stopping a recording is always allowed. */
    val isTappable: Boolean
        get() = when (this) {
            is Send -> enabled
            is StartVoice -> enabled
            is StopVoice -> true
            Processing -> false
        }

    /** Whether the field accepts edits; typing pauses while the transcript is live. */
    val allowsEditing: Boolean
        get() = this is Send || this is StartVoice
}

/**
 * Chooses the trailing action. Listening and processing win so the user can always stop recording.
 * Otherwise a non-blank draft sends, and a blank draft shows the microphone when voice input is
 * configured (a disabled send button when it is not).
 */
internal fun resolveChatComposerAction(
    text: String,
    voiceState: ChatComposerVoiceState?,
    sendEnabled: Boolean,
): ChatComposerAction = when {
    voiceState is ChatComposerVoiceState.Listening -> ChatComposerAction.StopVoice(voiceState.level.coerceIn(0f, 1f))
    voiceState == ChatComposerVoiceState.Processing -> ChatComposerAction.Processing
    text.isNotBlank() -> ChatComposerAction.Send(sendEnabled)
    voiceState == ChatComposerVoiceState.Idle -> ChatComposerAction.StartVoice(sendEnabled)
    voiceState == ChatComposerVoiceState.Unavailable -> ChatComposerAction.StartVoice(enabled = false)
    else -> ChatComposerAction.Send(enabled = false)
}

/** The listening ring's scale for an input level from 0 to 1. */
internal fun chatComposerRingScale(level: Float): Float =
    RING_REST_SCALE + level.coerceIn(0f, 1f) * RING_MAX_GROWTH

internal const val RING_REST_SCALE = 0.8f
internal const val RING_MAX_GROWTH = 0.45f
private const val DEFAULT_MAX_LINES = 5
