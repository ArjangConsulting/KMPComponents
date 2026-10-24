package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class AccessibilityAuditTest {
    @Test
    fun `card and tab actions retain keyboard activation labels and separate nested targets`() = runComposeUiTest {
        var selected by mutableStateOf(false)
        var edits = 0
        var closes = 0
        setContent {
            KmpTheme {
                Column {
                    SelectableCard(selected, { selected = !selected }) {
                        Text("Option")
                        ThemeTextButton("Edit", { edits++ })
                    }
                    SelectableCard(false, { error("Disabled card activated") }, enabled = false) { Text("Unavailable") }
                    TabChip("Tab", true, {}, onClose = { closes++ })
                }
            }
        }
        onNodeWithText("Option").assertIsNotSelected().requestFocus().performKeyInput { pressKey(Key.Spacebar) }
        onNodeWithText("Option").assertIsSelected()
        onNodeWithText("Edit").requestFocus().performKeyInput { pressKey(Key.Enter) }
        assertEquals(1, edits)
        onNodeWithText("Option").assertIsSelected()
        onNodeWithContentDescription("Close Tab").requestFocus().performKeyInput { pressKey(Key.Enter) }
        assertEquals(1, closes)
        onNodeWithText("Unavailable").assertIsNotEnabled().performClick()
    }

    @Test
    fun `selection check remains visible when selected and unselected colors are identical`() = runComposeUiTest {
        var selected by mutableStateOf(false)
        setContent {
            KmpTheme {
                val colors = MaterialTheme.colorScheme
                MaterialTheme(
                    colorScheme = colors.copy(
                        secondaryContainer = colors.surfaceContainerLow,
                        outline = colors.onSurface,
                    ),
                ) {
                    SelectableCard(selected, { selected = !selected }, Modifier.width(300.dp)) { Text("Option") }
                }
            }
        }
        val beforeImage = onNodeWithText("Option").captureToImage()
        val before = IntArray(beforeImage.width * beforeImage.height)
        beforeImage.readPixels(before)
        runOnIdle { selected = true }
        val afterImage = onNodeWithText("Option").captureToImage()
        val after = IntArray(afterImage.width * afterImage.height)
        afterImage.readPixels(after)
        assertEquals(before.size, after.size)
        assertTrue(before.indices.count { before[it] != after[it] } > 10)
    }

    @Test
    fun `custom tab sidebar and card targets stay at least 48 dp at both font scales and directions`() {
        listOf(1f, 2f).forEach { scale ->
            listOf(LayoutDirection.Ltr, LayoutDirection.Rtl).forEach { direction -> verifyTargets(scale, direction) }
        }
    }

    private fun verifyTargets(scale: Float, direction: LayoutDirection) = runComposeUiTest {
        setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density.density, scale),
                LocalLayoutDirection provides direction,
            ) {
                KmpTheme {
                    Column(Modifier.width(360.dp)) {
                        TabChip("A", true, {}, onClose = {})
                        SidebarItem("Destination", false, {})
                        SelectableCard(false, {}) { Text("Option") }
                        ThemeSwitch(false, {}, "Switch")
                        ThemeCheckbox(false, {}, "Checkbox")
                    }
                }
            }
        }
        listOf("A", "Destination", "Option", "Switch", "Checkbox").forEach { label ->
            onNodeWithText(label).assertWidthIsAtLeast(48.dp).assertHeightIsAtLeast(48.dp)
        }
        onNodeWithContentDescription("Close A").assertWidthIsAtLeast(48.dp).assertHeightIsAtLeast(48.dp)
    }

    @Test
    fun `search field keeps its accessible name after typing`() = runComposeUiTest {
        var query by mutableStateOf("")
        setContent {
            KmpTheme { SearchField(query, { query = it }, labels = SearchFieldLabels("Find items", "Clear")) }
        }
        onNodeWithContentDescription("Find items").performTextInput("example")
        onNodeWithContentDescription("Find items").assertExists()
        onNodeWithContentDescription("Clear").performClick()
        assertEquals("", query)
    }
}
