package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.NativeClipboard
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// Interaction tests for desktop-oriented helpers: Tab traversal, copy, status bar, zoom, and the
// vertical split pane.
@OptIn(ExperimentalTestApi::class)
class DesktopExtrasUiTest {
    private fun stateIs(value: String) = SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, value)

    @Test
    fun `tab moves focus out of a multi-line field`() = runComposeUiTest {
        setContent {
            KmpTheme {
                Column {
                    OutlinedTextField(value = "", onValueChange = {
                    }, modifier = Modifier.testTag("notes").focusNextOnTab())
                    OutlinedTextField(value = "", onValueChange = {
                    }, modifier = Modifier.testTag("next"), singleLine = true)
                }
            }
        }
        onNodeWithTag("notes").requestFocus()
        onNodeWithTag("notes").performKeyInput { pressKey(Key.Tab) }
        waitForIdle()
        onNodeWithTag("next").assertIsFocused()
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Test
    fun `copy button puts the text on the clipboard and confirms`() = runComposeUiTest {
        val clipboard = RecordingClipboard()
        var copies = 0
        setContent {
            CompositionLocalProvider(LocalClipboard provides clipboard) {
                KmpTheme { CopyButton(text = "curl https://example.com", onCopied = { copies++ }) }
            }
        }
        onNodeWithContentDescription("Copy").performClick()
        waitForIdle()
        val copied = (clipboard.entry?.nativeClipEntry as Transferable).getTransferData(DataFlavor.stringFlavor)
        assertEquals("curl https://example.com", copied)
        assertEquals(1, copies)
        onNodeWithContentDescription("Copy").assert(stateIs("Copied"))
    }

    @Test
    fun `status bar shows its items`() = runComposeUiTest {
        setContent {
            KmpTheme {
                StatusBar(
                    start = { StatusBarItem("AI: Ollama") },
                    end = {
                        StatusBarItem("12 endpoints")
                        StatusBarSeparator()
                        StatusBarItem("Saved")
                    },
                )
            }
        }
        onNodeWithText("AI: Ollama").assertExists()
        onNodeWithText("Saved").assertExists()
    }

    @Test
    fun `zoomable image zooms with the keyboard and announces the level`() = runComposeUiTest {
        setContent {
            KmpTheme {
                Box(Modifier.size(200.dp)) {
                    ZoomableImage(
                        ImageBitmap(100, 50),
                        contentDescription = "Screenshot",
                        modifier = Modifier.testTag("image"),
                    )
                }
            }
        }
        onNodeWithTag("image").assert(stateIs("Zoom 100%")).requestFocus()
        onNodeWithTag("image").performKeyInput { pressKey(Key.Equals) }
        waitForIdle()
        onNodeWithTag("image").assert(stateIs("Zoom 110%"))
        onNodeWithTag("image").performKeyInput { pressKey(Key.Zero) }
        waitForIdle()
        onNodeWithTag("image").assert(stateIs("Zoom 100%"))
    }

    @Test
    fun `vertical split pane moves with the arrow keys`() = runComposeUiTest {
        var split by mutableFloatStateOf(0.5f)
        setContent {
            KmpTheme {
                VerticalSplitPane(
                    split = split,
                    onSplitChange = { split = it },
                    modifier = Modifier.size(300.dp),
                    top = { Text("Editor") },
                    bottom = { Text("Console") },
                )
            }
        }
        onNodeWithContentDescription("Resize panes").assert(stateIs("50% top pane")).requestFocus()
        onNodeWithContentDescription("Resize panes").performKeyInput { pressKey(Key.DirectionDown) }
        waitForIdle()
        assertEquals(0.55f, split, absoluteTolerance = 0.001f)
        assertTrue(split > 0.5f)
    }
}

/** Keeps the last clip entry in memory so tests never touch the real system clipboard. */
private class RecordingClipboard : Clipboard {
    var entry: ClipEntry? by mutableStateOf(null)

    override suspend fun getClipEntry(): ClipEntry? = entry

    override suspend fun setClipEntry(clipEntry: ClipEntry?) {
        entry = clipEntry
    }

    // Clipboard still requires this deprecated member; the fake has no native clipboard to expose.
    @Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")
    override val nativeClipboard: NativeClipboard
        get() = throw UnsupportedOperationException("No native clipboard in tests")
}
