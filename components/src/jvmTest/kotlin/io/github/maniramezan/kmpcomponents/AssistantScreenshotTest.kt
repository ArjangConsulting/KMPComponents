package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.RoborazziOptions
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

// Golden images of the assistant family: chat log, assistant cards, typing indicator, overlay.
@OptIn(ExperimentalTestApi::class)
class AssistantScreenshotTest {
    @Test
    fun `assistant in light mode`() = capture("assistant-light", ThemeMode.LIGHT)

    @Test
    fun `assistant in dark mode`() = capture("assistant-dark", ThemeMode.DARK)

    @Test
    fun `assistant at double font scale`() = capture("assistant-font-200", ThemeMode.LIGHT, fontScale = 2f)

    private fun capture(name: String, mode: ThemeMode, fontScale: Float = 1f) = runDesktopComposeUiTest(
        width = WINDOW_WIDTH,
        height = WINDOW_HEIGHT,
    ) {
        setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                KmpTheme(mode = mode) {
                    Surface(Modifier.width(GALLERY_WIDTH)) { AssistantGallery() }
                }
            }
        }
        onRoot().captureRoboImage("$SNAPSHOT_DIR/$name.png", roborazziOptions = OPTIONS)
    }

    private companion object {
        const val SNAPSHOT_DIR = "src/jvmTest/snapshots"
        const val WINDOW_WIDTH = 480
        const val WINDOW_HEIGHT = 2400
        val GALLERY_WIDTH = WINDOW_WIDTH.dp

        // Allow antialiasing noise between machines; real regressions change far more pixels.
        val OPTIONS = RoborazziOptions(compareOptions = RoborazziOptions.CompareOptions(changeThreshold = 0.01f))
    }
}

@Composable
private fun AssistantGallery() {
    val spacing = MaterialTheme.kmpSpacing
    Column(Modifier.padding(vertical = spacing.medium), verticalArrangement = Arrangement.spacedBy(spacing.small)) {
        ChatLog(
            messages = listOf(
                ChatMessage("1", ChatMessageSender.USER, ChatMessageState.Content("Why does login fail?")),
                ChatMessage(
                    "2",
                    ChatMessageSender.ASSISTANT,
                    ChatMessageState.Content("The Sign in button is disabled."),
                ),
                ChatMessage("3", ChatMessageSender.ASSISTANT, ChatMessageState.Error),
                ChatMessage("4", ChatMessageSender.ASSISTANT, ChatMessageState.Typing),
            ),
            idleHint = "",
            errorAction = ChatErrorAction("Could not reach the model.", "Retry") {},
            modifier = Modifier.height(CHAT_HEIGHT),
        )
        Column(
            Modifier.padding(horizontal = spacing.medium),
            verticalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            ChatComposer(value = "", onValueChange = {}, onSend = {})
            ChatComposer(value = "A drafted question that is long enough to wrap onto a second line", onValueChange = {
            }, onSend = {})
            ChatComposer(
                value = "",
                onValueChange = {},
                onSend = {},
                voiceInput = ChatComposerVoiceInput(ChatComposerVoiceState.Idle, onStart = {}, onStop = {}),
            )
            ChatComposer(
                value = "Dictated so far",
                onValueChange = {},
                onSend = {},
                voiceInput = ChatComposerVoiceInput(ChatComposerVoiceState.Listening(0.7f), onStart = {}, onStop = {}),
            )
            AssistantContextCard(
                title = "Login screen",
                highlight = "Sign in button",
                body = AssistantContextBody("Nothing happens when I tap it", isQuoted = true),
                footnote = "Captured 2 minutes ago",
            )
            AssistantQuickActionChips(
                actions = listOf("Summarize", "Explain", "Write test"),
                actionState = {
                    AssistantQuickActionState(isSelected = it == "Explain", isEnabled = it != "Write test")
                },
                label = { it },
                onAction = {},
            )
            AssistantStatusBanner(message = "The AI provider is offline.")
            AssistantLimitPromptCard(
                copy = AssistantLimitPromptCopy(
                    "You've used today's runs",
                    "Runs reset at midnight.",
                    "Upgrade",
                    "Not now",
                ),
                onPrimaryAction = {},
                onSecondaryAction = {},
            )
            TypingIndicator()
            LoadingOverlay(loading = true, contentDescription = "Exporting", message = "Exporting…") {
                ThemeCard(Modifier.height(OVERLAY_HEIGHT)) { Text("Content under the overlay") }
            }
        }
        AssistantDisclaimerFooter(text = "AI answers can be wrong. Check important results.")
    }
}

private val CHAT_HEIGHT = 360.dp
private val OVERLAY_HEIGHT = 200.dp
