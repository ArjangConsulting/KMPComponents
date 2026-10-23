package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals

// Interaction tests for buttons, segmented controls, and pills.
@OptIn(ExperimentalTestApi::class)
class ActionsUiTest {
    @Test
    fun `text buttons run their action`() = runComposeUiTest {
        var clicks = 0
        setContent {
            KmpTheme {
                Column {
                    PrimaryButton(text = "Save", onClick = { clicks++ })
                    SecondaryButton(text = "Test", onClick = { clicks++ })
                    ThemeTextButton(text = "Delete all", onClick = { clicks++ }, destructive = true)
                }
            }
        }
        onNodeWithText("Save").performClick()
        onNodeWithText("Test").performClick()
        onNodeWithText("Delete all").performClick()
        assertEquals(3, clicks)
    }

    @Test
    fun `icon buttons are named by their content description`() = runComposeUiTest {
        var clicks = 0
        setContent {
            KmpTheme {
                val plus = rememberVectorPainter(TestPlusIcon)
                Column {
                    ThemeIconButton(icon = plus, contentDescription = "Add endpoint", onClick = { clicks++ })
                    FAB(icon = plus, contentDescription = "New project", onClick = { clicks++ })
                    ThemeExtendedFloatingActionButton(text = "Record", icon = plus, onClick = { clicks++ })
                }
            }
        }
        onNodeWithContentDescription("Add endpoint").performClick()
        onNodeWithContentDescription("New project").performClick()
        // Material's extended FAB clears its label's semantics; the wrapper restores the name.
        onNodeWithContentDescription("Record").performClick()
        assertEquals(3, clicks)
    }

    @Test
    fun `loading icon button ignores clicks and reports its state while loading`() = runComposeUiTest {
        var loading by mutableStateOf(false)
        var clicks = 0
        setContent {
            KmpTheme {
                LoadingIconButton(
                    loading = loading,
                    icon = rememberVectorPainter(TestPlusIcon),
                    contentDescription = "Refresh",
                    onClick = {
                        clicks++
                        loading = true
                    },
                )
            }
        }
        onNodeWithContentDescription("Refresh").performClick()
        waitForIdle()
        onNodeWithContentDescription("Refresh")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Loading"))
            .assertIsNotEnabled()
            .performClick()
        assertEquals(1, clicks)
    }

    @Test
    fun `segmented controls select the tapped option`() = runComposeUiTest {
        var custom by mutableIntStateOf(0)
        var material by mutableIntStateOf(0)
        setContent {
            KmpTheme {
                Column {
                    SegmentedControl(
                        options = listOf("Day", "Week"),
                        selectedIndex = custom,
                        onOptionSelected = { custom = it },
                    )
                    ThemeSingleChoiceSegmentedButtonRow(
                        options = listOf("JSON", "YAML"),
                        selectedIndex = material,
                        onOptionSelected = { material = it },
                    )
                }
            }
        }
        onNodeWithText("Week").assertIsNotSelected().performClick()
        onNodeWithText("YAML").performClick()
        waitForIdle()
        assertEquals(1, custom)
        assertEquals(1, material)
        onNodeWithText("Week").assertIsSelected()
        onNodeWithText("YAML").assertIsSelected()
    }

    @Test
    fun `pill chip toggles when clickable and action pill runs its action`() = runComposeUiTest {
        var selected by mutableStateOf(false)
        var opened = 0
        setContent {
            KmpTheme {
                Column {
                    PillChip(label = "Errors", isSelected = selected, onClick = { selected = !selected })
                    PillChip(label = "Static", isSelected = true)
                    ActionPill(onClick = { opened++ }) { Text("Docs") }
                }
            }
        }
        onNodeWithText("Errors").assertIsNotSelected().performClick()
        waitForIdle()
        onNodeWithText("Errors").assertIsSelected()
        onNodeWithText("Static").assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Selected))
        onNodeWithText("Docs").performClick()
        assertEquals(1, opened)
    }
}
