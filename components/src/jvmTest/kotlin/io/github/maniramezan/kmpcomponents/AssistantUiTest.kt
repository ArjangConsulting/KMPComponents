package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

// Interaction tests for chat logs, assistant cards, typing indicators, and loading overlays.
@OptIn(ExperimentalTestApi::class)
class AssistantUiTest {
    private val politeLiveRegion = SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite)

    @Test
    fun `chat log shows the idle hint, messages, typing, and a retryable error`() = runComposeUiTest {
        var messages by mutableStateOf(emptyList<ChatMessage>())
        var retries = 0
        setContent {
            KmpTheme {
                ChatLog(
                    messages = messages,
                    idleHint = "Ask anything",
                    errorAction = ChatErrorAction("Something went wrong", "Retry") { retries++ },
                    modifier = Modifier.height(400.dp),
                )
            }
        }
        onNodeWithText("Ask anything").assertExists()
        messages = listOf(
            ChatMessage("1", ChatMessageSender.USER, ChatMessageState.Content("Open the app")),
            ChatMessage("2", ChatMessageSender.ASSISTANT, ChatMessageState.Typing),
        )
        waitForIdle()
        onNodeWithText("Open the app").assertExists()
        onNodeWithContentDescription("Responding").assert(politeLiveRegion)
        messages = messages.dropLast(1) + ChatMessage("2", ChatMessageSender.ASSISTANT, ChatMessageState.Error)
        waitForIdle()
        onNodeWithText("Something went wrong").assert(politeLiveRegion)
        onNodeWithText("Retry").performClick()
        assertEquals(1, retries)
    }

    @Test
    fun `quick action chips report taps and their state`() = runComposeUiTest {
        var tapped: String? = null
        setContent {
            KmpTheme {
                AssistantQuickActionChips(
                    actions = listOf("Summarize", "Explain"),
                    actionState = {
                        AssistantQuickActionState(
                            isSelected = it == "Summarize",
                            isEnabled =
                            it != "Explain",
                        )
                    },
                    label = { it },
                    onAction = { tapped = it },
                )
            }
        }
        onNodeWithText("Summarize").assertIsSelected().performClick()
        onNodeWithText("Explain").assertIsNotEnabled()
        assertEquals("Summarize", tapped)
    }

    @Test
    fun `assistant cards render their copy and actions`() = runComposeUiTest {
        var upgraded = 0
        setContent {
            KmpTheme {
                Column {
                    AssistantContextCard(
                        title = "Login screen",
                        highlight = "Sign in button",
                        body = AssistantContextBody("Tap fails", isQuoted = true),
                    )
                    AssistantStatusBanner(message = "Provider offline")
                    AssistantLimitPromptCard(
                        copy = AssistantLimitPromptCopy("Limit reached", "Upgrade for more runs", "Upgrade", "Not now"),
                        onPrimaryAction = { upgraded++ },
                        onSecondaryAction = {},
                    )
                    AssistantDisclaimerFooter(text = "AI can make mistakes.")
                    AssistantDisclaimerFooter(text = " ")
                }
            }
        }
        onNodeWithText("Login screen").assert(isHeading())
        onNodeWithText("“Tap fails”").assertExists()
        onNodeWithText("Provider offline").assert(politeLiveRegion)
        onNodeWithText("Upgrade").performClick()
        onNodeWithText("AI can make mistakes.").assertExists()
        assertEquals(1, upgraded)
    }

    @Test
    fun `loading overlay blocks taps and announces the operation`() = runComposeUiTest {
        var loading by mutableStateOf(true)
        var clicks = 0
        setContent {
            KmpTheme {
                LoadingOverlay(loading = loading, contentDescription = "Signing in") {
                    Button(onClick = { clicks++ }) { Text("Sign in") }
                }
            }
        }
        waitForIdle()
        onNodeWithContentDescription("Signing in").assert(politeLiveRegion).performClick()
        onNodeWithText("Sign in").assertDoesNotExist()
        assertEquals(0, clicks)
        loading = false
        waitForIdle()
        onNodeWithText("Sign in").performClick()
        assertEquals(1, clicks)
    }

    @Test
    fun `loading overlay blocks keyboard activation and restores focus afterward`() = runComposeUiTest {
        var loading by mutableStateOf(false)
        var clicks = 0
        setContent {
            KmpTheme {
                LoadingOverlay(loading = loading, contentDescription = "Signing in") {
                    Button(onClick = { clicks++ }) { Text("Sign in") }
                }
            }
        }
        onNodeWithText("Sign in").requestFocus()
        runOnIdle { loading = true }
        onRoot().performKeyInput {
            pressKey(Key.Enter)
            pressKey(Key.Tab)
            pressKey(Key.Enter)
        }
        runOnIdle { assertEquals(0, clicks) }
        onNodeWithContentDescription("Signing in").assertIsFocused()
        runOnIdle { loading = false }
        onNodeWithText("Sign in").assertIsFocused()
    }

    @Test
    fun `appending a message preserves the reader position inside a tall reply`() = runComposeUiTest {
        var messages by mutableStateOf(
            listOf(
                ChatMessage("1", ChatMessageSender.USER, ChatMessageState.Content("First")),
                ChatMessage("2", ChatMessageSender.ASSISTANT, ChatMessageState.Content("Latest")),
            ),
        )
        setContent {
            KmpTheme {
                ChatLog(
                    messages = messages,
                    idleHint = "",
                    errorAction = ChatErrorAction("Failed", "Retry") {},
                    modifier = Modifier.height(300.dp).testTag("chat"),
                    messageContent = { _, text -> Text(text, modifier = Modifier.height(900.dp)) },
                )
            }
        }
        onNodeWithTag("chat").performTouchInput { swipeDown() }
        waitForIdle()
        val before = onNodeWithText("Latest").fetchSemanticsNode().boundsInRoot
        runOnIdle {
            messages = messages + ChatMessage("3", ChatMessageSender.ASSISTANT, ChatMessageState.Content("New reply"))
        }
        waitForIdle()
        onNodeWithText("Latest").assertIsDisplayed()
        assertEquals(before, onNodeWithText("Latest").fetchSemanticsNode().boundsInRoot)
    }

    @Test
    fun `typing indicator is announced only with a description`() = runComposeUiTest {
        setContent {
            KmpTheme {
                Column {
                    TypingIndicator(contentDescription = "Writing a reply")
                    TypingIndicator()
                }
            }
        }
        onNodeWithContentDescription("Writing a reply").assert(politeLiveRegion)
    }
}
