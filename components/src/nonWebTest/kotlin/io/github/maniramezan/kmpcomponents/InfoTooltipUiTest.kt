package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class InfoTooltipUiTest {
    @Test
    fun `tooltip stays visible while hovered without a timeout and closes on exit`() = runComposeUiTest {
        setContent {
            KmpTheme {
                Box(Modifier.fillMaxSize()) { InfoTooltip("Hover explanation", displayDurationMillis = null) }
            }
        }
        onNodeWithContentDescription("Hover explanation").performMouseInput { enter(center) }
        onNodeWithText("Hover explanation").assertIsDisplayed()
        withContext(Dispatchers.Default) { delay(2_000L) }
        onNodeWithText("Hover explanation").assertIsDisplayed()
        onNodeWithContentDescription("Hover explanation").performMouseInput { exit() }
        onNodeWithText("Hover explanation").assertDoesNotExist()
        onNodeWithContentDescription("Hover explanation").performMouseInput { enter(center) }
        onNodeWithText("Hover explanation").assertIsDisplayed()
    }

    @Test
    fun `tooltip honors a custom hover duration`() = runComposeUiTest {
        setContent { KmpTheme { InfoTooltip("Timed explanation", displayDurationMillis = 2_500L) } }
        onNodeWithContentDescription("Timed explanation").performMouseInput { enter(center) }
        onNodeWithText("Timed explanation").assertIsDisplayed()
        withContext(Dispatchers.Default) { delay(1_800L) }
        onNodeWithText("Timed explanation").assertIsDisplayed()
        waitUntil(timeoutMillis = 3_000L) {
            onAllNodesWithText("Timed explanation").fetchSemanticsNodes().isEmpty()
        }
    }

    @Test
    fun `tooltip keeps its default hover timeout`() = runComposeUiTest {
        setContent { KmpTheme { InfoTooltip("Default explanation") } }
        onNodeWithContentDescription("Default explanation").performMouseInput { enter(center) }
        onNodeWithText("Default explanation").assertIsDisplayed()
        waitUntil(timeoutMillis = 3_000L) {
            onAllNodesWithText("Default explanation").fetchSemanticsNodes().isEmpty()
        }
    }

    @Test
    fun `keyboard explanation stays visible until focus leaves`() = runComposeUiTest {
        setContent {
            KmpTheme {
                Column {
                    InfoTooltip("Focus explanation", displayDurationMillis = 500L)
                    TextButton(onClick = {}) { Text("Next") }
                }
            }
        }
        onNodeWithContentDescription("Focus explanation").requestFocus()
        withContext(Dispatchers.Default) { delay(2_000L) }
        onNodeWithText("Focus explanation").assertIsDisplayed()
        onNodeWithText("Next").requestFocus()
        onNodeWithText("Focus explanation").assertDoesNotExist()
    }
}
