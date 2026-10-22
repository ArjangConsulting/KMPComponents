package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals

// Interaction tests for radio groups, sliders, and selection lists.
@OptIn(ExperimentalTestApi::class)
class SelectionUiTest {
    private val nodes = listOf(
        SelectionListNode(
            id = "en",
            title = "English",
            children = listOf(
                SelectionListNode("en-US", "United States"),
                SelectionListNode("en-GB", "United Kingdom"),
            ),
        ),
        SelectionListNode("de", "Deutsch"),
    )

    private fun stateIs(value: String) = SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, value)

    @Test
    fun `radio group selects the tapped row`() = runComposeUiTest {
        var selected by mutableIntStateOf(0)
        setContent {
            KmpTheme {
                RadioGroup(options = listOf("HTTP", "HTTPS"), selectedIndex = selected, onOptionSelected = {
                    selected =
                        it
                })
            }
        }
        onNodeWithText("HTTPS").assertIsNotSelected().performClick()
        waitForIdle()
        assertEquals(1, selected)
        onNodeWithText("HTTPS").assertIsSelected()
    }

    @Test
    fun `slider announces its label and value description`() = runComposeUiTest {
        setContent {
            KmpTheme {
                ThemeSlider(value = 0.5f, onValueChange = {}, label = "Timeout", valueDescription = "30 seconds")
            }
        }
        onNodeWithContentDescription("Timeout").assert(stateIs("30 seconds"))
    }

    @Test
    fun `selection list expands parents and reports the tapped child`() = runComposeUiTest {
        var selected by mutableStateOf(setOf("de"))
        setContent {
            KmpTheme {
                Column {
                    SelectionListContent(
                        title = "Language",
                        nodes = nodes,
                        selectedIds = selected,
                        onSelect = { id -> selected = if (id in selected) selected - id else selected + id },
                    )
                }
            }
        }
        onNodeWithText("Language").assert(isHeading())
        onNodeWithText("Deutsch").assertIsSelected()
        onNodeWithText("United Kingdom").assertDoesNotExist()
        onNodeWithText("English").assert(stateIs("Collapsed")).performClick()
        waitForIdle()
        onNodeWithText("English").assert(stateIs("Expanded"))
        onNodeWithText("United Kingdom").performClick()
        waitForIdle()
        assertEquals(setOf("de", "en-GB"), selected)
        // Collapsing the parent lists its selected children as the subtitle.
        onNodeWithText("English").performClick()
        waitForIdle()
        onNodeWithText("United Kingdom").assertExists()
    }

    @Test
    fun `searching filters across both levels and shows no results`() = runComposeUiTest {
        setContent {
            KmpTheme {
                SelectionListContent(
                    title = "",
                    nodes = nodes,
                    selectedIds = emptySet(),
                    onSelect = {},
                    isSearchable = true,
                )
            }
        }
        onNode(hasSetTextAction()).performTextInput("states")
        waitForIdle()
        onNodeWithText("United States").assertExists()
        onNodeWithText("United Kingdom").assertDoesNotExist()
        onNodeWithText("Deutsch").assertDoesNotExist()
        onNodeWithContentDescription("Clear search").performClick()
        onNode(hasSetTextAction()).performTextInput("zzz")
        waitForIdle()
        onNodeWithText("No results").assertExists()
    }
}
