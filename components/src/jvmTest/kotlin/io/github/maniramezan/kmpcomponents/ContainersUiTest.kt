package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AccessibilityManager
import androidx.compose.ui.platform.LocalAccessibilityManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

// Interaction tests for containers, dialogs, toasts, state placeholders, and layout helpers.
@OptIn(ExperimentalTestApi::class)
class ContainersUiTest {
    private val politeLiveRegion = SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite)

    private fun stateIs(value: String) = SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, value)

    @Test
    fun `clickable card runs its action`() = runComposeUiTest {
        var clicks = 0
        setContent { KmpTheme { ThemeCard(onClick = { clicks++ }) { Text("Project") } } }
        onNodeWithText("Project").performClick()
        assertEquals(1, clicks)
    }

    @Test
    fun `dialog without dismiss text shows only the confirm button`() = runComposeUiTest {
        var confirmed = 0
        setContent {
            KmpTheme {
                ThemeDialog(title = "Saved", text = "All changes saved.", confirmText = "OK", onConfirm = {
                    confirmed++
                }, onDismissRequest = {})
            }
        }
        onNodeWithText("Saved").assert(isHeading())
        onNodeWithText("Cancel").assertDoesNotExist()
        onNodeWithText("OK").performClick()
        assertEquals(1, confirmed)
    }

    @Test
    fun `snackbar announces its message and runs its action`() = runComposeUiTest {
        var undone = 0
        setContent {
            KmpTheme { ThemeSnackbar(message = "Endpoint deleted", actionLabel = "Undo", onAction = { undone++ }) }
        }
        onNodeWithText("Endpoint deleted").assert(politeLiveRegion)
        onNodeWithText("Undo").performClick()
        assertEquals(1, undone)
    }

    @Test
    fun `toast host reports the action and times out otherwise`() = runComposeUiTest {
        val host = ToastHostState()
        var result: ToastResult? = null
        lateinit var show: (String?) -> Unit
        setContent {
            KmpTheme {
                val scope = rememberCoroutineScope()
                show = { action -> scope.launch { result = host.showToast("Copied", actionLabel = action) } }
                Box { ToastHost(hostState = host) }
            }
        }
        show("Undo")
        waitForIdle()
        onNodeWithText("Copied").assert(politeLiveRegion)
        onNodeWithText("Undo").performClick()
        waitForIdle()
        assertEquals(ToastResult.ACTION_PERFORMED, result)

        result = null
        mainClock.autoAdvance = false
        show(null)
        mainClock.advanceTimeByFrame()
        assertNull(result)
        mainClock.advanceTimeBy(ToastDuration.SHORT.timeoutMillis ?: 0L)
        mainClock.autoAdvance = true
        waitForIdle()
        assertEquals(ToastResult.DISMISSED, result)
    }

    @Test
    fun `toast host honors the accessibility timeout and action flag`() = runComposeUiTest {
        val host = ToastHostState()
        var hasControls = false
        val accessibilityManager = object : AccessibilityManager {
            override fun calculateRecommendedTimeoutMillis(
                originalTimeoutMillis: Long,
                containsIcons: Boolean,
                containsText: Boolean,
                containsControls: Boolean,
            ): Long {
                hasControls = containsControls
                return 30_000L
            }
        }
        lateinit var show: () -> Unit
        setContent {
            KmpTheme {
                val scope = rememberCoroutineScope()
                show = { scope.launch { host.showToast("Deleted", actionLabel = "Undo") } }
                CompositionLocalProvider(LocalAccessibilityManager provides accessibilityManager) {
                    ToastHost(hostState = host)
                }
            }
        }
        mainClock.autoAdvance = false
        runOnIdle { show() }
        mainClock.advanceTimeByFrame()
        mainClock.advanceTimeBy(11_000L)
        runOnIdle {
            assertEquals(true, hasControls)
            assertNotNull(host.currentToastData)
        }
        mainClock.advanceTimeBy(20_000L)
        runOnIdle { assertNull(host.currentToastData) }
        mainClock.autoAdvance = true
    }

    @Test
    fun `loading and error states are announced`() = runComposeUiTest {
        var retried = 0
        setContent {
            KmpTheme {
                Column {
                    LoadingState(label = "Loading reports")
                    ErrorState(
                        title = "Could not load reports",
                        message = "The server did not respond.",
                        action = { ThemeTextButton(text = "Retry", onClick = { retried++ }) },
                    )
                }
            }
        }
        onNodeWithText("Loading reports").assert(politeLiveRegion)
        onNodeWithText("Could not load reports").assert(politeLiveRegion)
        onNodeWithText("Could not load reports", useUnmergedTree = true).assert(isHeading())
        onNodeWithText("Retry").performClick()
        assertEquals(1, retried)
    }

    @Test
    fun `skeletons are hidden from screen readers`() = runComposeUiTest {
        setContent { KmpTheme { Box(Modifier.testTag("skeleton")) { Skeleton() } } }
        onNodeWithTag("skeleton").assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Text))
    }

    @Test
    fun `disclosure card toggles and announces its state`() = runComposeUiTest {
        var expanded by mutableStateOf(false)
        setContent {
            KmpTheme {
                DisclosureCard(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    summary = { Text("Advanced") },
                    detail = { Text("Proxy settings") },
                )
            }
        }
        onNodeWithText("Proxy settings").assertDoesNotExist()
        onNodeWithText("Advanced").assert(stateIs("Collapsed")).performClick()
        waitForIdle()
        onNodeWithText("Advanced").assert(stateIs("Expanded"))
        onNodeWithText("Proxy settings").assertExists()
    }

    @Test
    fun `adaptive container caps its content width`() = runComposeUiTest {
        setContent {
            KmpTheme {
                Box(Modifier.width(1_000.dp)) {
                    AdaptiveContentContainer(maxWidth = 300.dp) { Box(Modifier.testTag("content").width(2_000.dp)) }
                }
            }
        }
        onNodeWithTag("content").assertWidthIsEqualTo(300.dp)
    }
}
