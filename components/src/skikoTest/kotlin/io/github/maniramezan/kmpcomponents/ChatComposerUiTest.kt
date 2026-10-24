package io.github.maniramezan.kmpcomponents

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.test.withKeyDown
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ChatComposerUiTest {
    @Test
    fun `sends a draft from the button the keyboard action and Enter`() = runComposeUiTest {
        var draft by mutableStateOf("")
        val sent = mutableListOf<String>()
        setContent {
            KmpTheme {
                ChatComposer(value = draft, onValueChange = { draft = it }, onSend = {
                    sent += draft
                    draft = ""
                })
            }
        }
        onNodeWithContentDescription("Send").assertIsNotEnabled()
        val field = onNode(hasSetTextAction())
        field.performTextInput("First")
        onNodeWithContentDescription("Send").assertIsEnabled().performClick()
        field.performTextInput("Second")
        field.performImeAction()
        field.performTextInput("Third")
        field.performKeyInput { pressKey(Key.Enter) }
        assertEquals(listOf("First", "Second", "Third"), sent)
    }

    @Test
    fun `shift enter keeps a line break and blank or disabled drafts do not send`() = runComposeUiTest {
        var draft by mutableStateOf("")
        var sendEnabled by mutableStateOf(true)
        var sends = 0
        setContent {
            KmpTheme {
                ChatComposer(value = draft, onValueChange = {
                    draft = it
                }, onSend = { sends++ }, sendEnabled = sendEnabled)
            }
        }
        val field = onNode(hasSetTextAction())
        field.performTextInput("   ")
        field.performKeyInput { pressKey(Key.Enter) }
        assertEquals(0, sends)
        field.performTextInput("Line")
        field.performKeyInput { withKeyDown(Key.ShiftLeft) { pressKey(Key.Enter) } }
        assertEquals(0, sends)
        assertEquals("   Line\n", draft)
        sendEnabled = false
        field.performKeyInput { pressKey(Key.Enter) }
        onNodeWithContentDescription("Send").assertIsNotEnabled()
        assertEquals(0, sends)
    }

    @Test
    fun `the field keeps its accessible name after typing`() = runComposeUiTest {
        var draft by mutableStateOf("")
        setContent {
            KmpTheme {
                ChatComposer(
                    value = draft,
                    onValueChange = { draft = it },
                    onSend = {},
                    labels = ChatComposerLabels(placeholder = "Ask anything"),
                )
            }
        }
        // While empty, the placeholder alone names the field, so the name is not read twice.
        onNode(hasSetTextAction()).assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.ContentDescription))
        onNodeWithText("Ask anything").assertExists()
        onNode(hasSetTextAction()).performTextInput("Hello")
        onNodeWithContentDescription("Ask anything").assertExists()
    }

    @Test
    fun `voice input moves from microphone to stop to processing`() = runComposeUiTest {
        var state: ChatComposerVoiceState by mutableStateOf(ChatComposerVoiceState.Idle)
        var draft by mutableStateOf("")
        var starts = 0
        var stops = 0
        setContent {
            KmpTheme {
                ChatComposer(
                    value = draft,
                    onValueChange = { draft = it },
                    onSend = {},
                    voiceInput = ChatComposerVoiceInput(state = state, onStart = { starts++ }, onStop = { stops++ }),
                )
            }
        }
        onNodeWithContentDescription("Start voice input").performClick()
        assertEquals(1, starts)
        state = ChatComposerVoiceState.Listening(level = 0.6f)
        draft = "Dictated"
        onNodeWithContentDescription("Stop voice input").assertIsEnabled().performClick()
        assertEquals(1, stops)
        onNodeWithContentDescription("Message").assert(SemanticsMatcher.keyNotDefined(SemanticsActions.SetText))
        state = ChatComposerVoiceState.Processing
        onNodeWithContentDescription("Processing speech").assertIsNotEnabled()
        state = ChatComposerVoiceState.Idle
        onNodeWithContentDescription("Send").assertIsEnabled()
        state = ChatComposerVoiceState.Unavailable
        draft = ""
        onNodeWithContentDescription("Start voice input").assertIsNotEnabled()
    }
}
