package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// Interaction tests for the app-essentials components (headers, rows, search, progress, notices,
// pickers, and toggle rows).
@OptIn(ExperimentalTestApi::class)
class EssentialsUiTest {
    private val politeLiveRegion = SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite)

    @Test
    fun `section header title is a heading and its action fires`() = runComposeUiTest {
        var clicks = 0
        setContent {
            KmpTheme {
                SectionHeader(title = "Endpoints", subtitle = "12 routes", actionLabel = "Add", onAction = { clicks++ })
            }
        }
        onNodeWithText("Endpoints").assert(isHeading())
        onNodeWithText("12 routes").assertIsDisplayed()
        onNodeWithText("Add").performClick()
        assertEquals(1, clicks)
    }

    @Test
    fun `content row is one click target and keeps trailing buttons separate`() = runComposeUiTest {
        var opened = 0
        var removed = 0
        setContent {
            KmpTheme {
                ContentRow(
                    title = "Project",
                    supportingText = "~/projects/demo",
                    onClick = { opened++ },
                    selected = true,
                    trailingContent = { TextButton(onClick = { removed++ }) { Text("Remove") } },
                )
            }
        }
        onNodeWithText("Project", useUnmergedTree = true).performClick()
        onNodeWithText("Remove").performClick()
        assertEquals(1, opened)
        assertEquals(1, removed)
        onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.Selected)).assertIsSelected()
        // The clickable row carries its own text, so screen readers announce more than "button".
        onNode(hasClickAction() and hasText("Project") and hasText("~/projects/demo")).assertIsSelected()
    }

    @Test
    fun `search field shows a clear button only while it has text`() = runComposeUiTest {
        var query by mutableStateOf("")
        var searched: String? = null
        setContent {
            KmpTheme { SearchField(value = query, onValueChange = { query = it }, onSearch = { searched = it }) }
        }
        onNodeWithContentDescription("Clear search").assertDoesNotExist()
        onNode(hasSetTextAction()).performTextInput("users")
        waitForIdle()
        assertEquals("users", query)
        onNodeWithContentDescription("Clear search").performClick()
        waitForIdle()
        assertEquals("", query)
        onNodeWithContentDescription("Clear search").assertDoesNotExist()
        assertEquals(null, searched)
    }

    @Test
    fun `progress indicators announce their label`() = runComposeUiTest {
        setContent {
            KmpTheme {
                Column {
                    ProgressIndicator(label = "Loading endpoints")
                    InlineProgressIndicator(label = "Booting device")
                }
            }
        }
        onNodeWithText("Loading endpoints").assert(politeLiveRegion)
        onNodeWithText("Booting device").assert(politeLiveRegion)
    }

    @Test
    fun `notice card runs its action and dismiss callbacks`() = runComposeUiTest {
        var retried = 0
        var dismissed = 0
        setContent {
            KmpTheme {
                NoticeCard(
                    message = "Device disconnected",
                    tone = StatusTone.ERROR,
                    actionLabel = "Retry",
                    onAction = { retried++ },
                    onDismiss = { dismissed++ },
                )
            }
        }
        onNodeWithText("Device disconnected").assert(politeLiveRegion)
        onNodeWithText("Retry").performClick()
        onNodeWithContentDescription("Dismiss").performClick()
        assertEquals(1, retried)
        assertEquals(1, dismissed)
    }

    @Test
    fun `info notice is not announced`() = runComposeUiTest {
        setContent { KmpTheme { NoticeCard(message = "Tip") } }
        onNodeWithText("Tip").assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.LiveRegion))
    }

    @Test
    fun `menu picker selects an item from its menu`() = runComposeUiTest {
        var language by mutableStateOf("Kotlin")
        setContent {
            KmpTheme {
                MenuPicker(
                    items = listOf("Kotlin", "Swift", "TypeScript"),
                    selected = language,
                    onSelect = { language = it },
                    label = "Language",
                    itemLabel = { it },
                )
            }
        }
        onNodeWithText("Kotlin").performClick()
        onNodeWithText("Swift").performClick()
        waitForIdle()
        assertEquals("Swift", language)
        onNodeWithText("Swift").assertIsDisplayed()
        onNodeWithText("Kotlin").assertDoesNotExist()
    }

    @Test
    fun `tapping a toggle row label toggles it`() = runComposeUiTest {
        var enabled by mutableStateOf(false)
        var include by mutableStateOf(true)
        setContent {
            KmpTheme {
                Column {
                    ThemeSwitch(checked = enabled, onCheckedChange = { enabled = it }, label = "Record traffic")
                    ThemeCheckbox(checked = include, onCheckedChange = { include = it }, label = "Include headers")
                }
            }
        }
        onNodeWithText("Record traffic").assertIsOff().performClick()
        onNodeWithText("Include headers").assertIsOn().performClick()
        waitForIdle()
        assertTrue(enabled)
        assertFalse(include)
        onNodeWithText("Record traffic").assertIsOn()
    }

    @Test
    fun `tri-state checkbox reports its state and click`() = runComposeUiTest {
        var clicks = 0
        setContent {
            KmpTheme {
                ThemeTriStateCheckbox(state = ToggleableState.Indeterminate, onClick = {
                    clicks++
                }, label = "Select all")
            }
        }
        onNodeWithText("Select all")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.ToggleableState, ToggleableState.Indeterminate))
            .performClick()
        assertEquals(1, clicks)
    }

    @Test
    fun `key value row reads as one item`() = runComposeUiTest {
        setContent { KmpTheme { KeyValueRow(key = "Model", value = "llama3") } }
        onNodeWithText("Model").assertTextEquals("Model", "llama3")
    }
}
