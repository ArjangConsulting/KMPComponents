package io.github.maniramezan.kmpcomponents

import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class AsyncContentUiTest {
    private var state by mutableStateOf<AsyncContentState<String>>(AsyncContentState.Loading)
    private var retries = 0
    private var creates = 0

    private fun livePolite(text: String): SemanticsMatcher =
        SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite) and
            (hasText(text) or hasAnyDescendant(hasText(text)))

    private fun ComposeUiTest.setAsyncContent(reduceMotion: Boolean = true) {
        setContent {
            CompositionLocalProvider(LocalReduceMotionEnabled provides reduceMotion) {
                KmpTheme {
                    AsyncContent(
                        state = state,
                        loadingLabel = "Loading reports",
                        emptyTitle = "No reports",
                        errorTitle = "Could not load",
                        errorMessage = "Try again later",
                        emptyMessage = "Create one to start",
                        emptyAction = { ThemeTextButton(text = "Create", onClick = { creates++ }) },
                        errorAction = { ThemeTextButton(text = "Retry", onClick = { retries++ }) },
                    ) { data -> Text(data) }
                }
            }
        }
    }

    @Test
    fun `each state shows its placeholder and actions reach the caller`() = runComposeUiTest {
        setAsyncContent()
        onNodeWithText("Loading reports").assertExists()

        state = AsyncContentState.Empty
        onNodeWithText("No reports").assertExists()
        onNodeWithText("Create one to start").assertExists()
        onNodeWithText("Create").performClick()
        assertEquals(1, creates)
        onNodeWithText("Loading reports").assertDoesNotExist()

        state = AsyncContentState.Error()
        onNodeWithText("Try again later", substring = true).assertExists()
        onNodeWithText("Retry").performClick()
        assertEquals(1, retries)

        state = AsyncContentState.Content("Quarterly report")
        onNodeWithText("Quarterly report").assertExists()
        onNodeWithText("Could not load", substring = true).assertDoesNotExist()
    }

    @Test
    fun `an error's own message replaces the default unless blank`() = runComposeUiTest {
        state = AsyncContentState.Error("Offline")
        setAsyncContent()
        onNodeWithText("Offline", substring = true).assertExists()
        onNodeWithText("Try again later", substring = true).assertDoesNotExist()
        state = AsyncContentState.Error(" ")
        onNodeWithText("Try again later", substring = true).assertExists()
    }

    @Test
    fun `every placeholder is announced politely`() = runComposeUiTest {
        setAsyncContent()
        onNode(livePolite("Loading reports"), useUnmergedTree = true).assertExists()
        state = AsyncContentState.Empty
        waitForIdle()
        onNode(livePolite("No reports"), useUnmergedTree = true).assertExists()
        state = AsyncContentState.Error()
        waitForIdle()
        onNode(livePolite("Could not load"), useUnmergedTree = true).assertExists()
    }

    @Test
    fun `the cross-fade runs between kinds but not when content data changes`() = runComposeUiTest {
        state = AsyncContentState.Content("First")
        setAsyncContent(reduceMotion = false)
        mainClock.autoAdvance = false
        state = AsyncContentState.Content("Second")
        mainClock.advanceTimeByFrame()
        onNodeWithText("First").assertDoesNotExist()
        onNodeWithText("Second").assertExists()

        state = AsyncContentState.Empty
        mainClock.advanceTimeByFrame()
        mainClock.advanceTimeByFrame()
        onNodeWithText("Second").assertExists()
        onNodeWithText("No reports").assertExists()
        mainClock.advanceTimeBy(FADE_ALLOWANCE_MILLIS)
        onNodeWithText("Second").assertDoesNotExist()
    }

    @Test
    fun `the slot overload passes the error state to its slot`() = runComposeUiTest {
        setContent {
            KmpTheme {
                AsyncContent(
                    state = AsyncContentState.Error("Timed out") as AsyncContentState<String>,
                    loading = { Text("Custom loading") },
                    empty = { Text("Custom empty") },
                    error = { Text("Custom error: ${it.message}") },
                ) { Text(it) }
            }
        }
        onNodeWithText("Custom error: Timed out").assertExists()
    }

    private companion object {
        const val FADE_ALLOWANCE_MILLIS = 1_000L
    }
}
