package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
    fun `expanding the last parent reveals all children in a bounded viewport`() {
        listOf(1f, 2f).forEach { fontScale ->
            listOf(LayoutDirection.Ltr, LayoutDirection.Rtl).forEach { direction ->
                verifyExpandedGroupVisibility(fontScale, direction)
            }
        }
    }

    private fun verifyExpandedGroupVisibility(fontScale: Float, direction: LayoutDirection) = runComposeUiTest {
        val children = listOf(SelectionListNode("br", "Brasil"), SelectionListNode("pt", "Portugal"))
        val choices = List(3) { SelectionListNode("item-$it", "Item $it") } +
            SelectionListNode("portuguese", "Português", children = children)
        var selected: String? = null
        setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density.density, fontScale),
                LocalLayoutDirection provides direction,
            ) {
                KmpTheme {
                    SelectionListContent(
                        title = "Languages",
                        nodes = choices,
                        selectedIds = emptySet(),
                        onSelect = { selected = it },
                        modifier = Modifier.height(400.dp),
                        isSearchable = true,
                    )
                }
            }
        }
        onNodeWithText("Português").performScrollTo().performClick()
        waitForIdle()
        val viewport = onNode(hasScrollToIndexAction()).getUnclippedBoundsInRoot()
        listOf("Português", "Brasil", "Portugal").forEach { label ->
            val bounds = onNodeWithText(label).assertIsDisplayed().getUnclippedBoundsInRoot()
            assertTrue(bounds.top >= viewport.top, "$label starts above viewport at $fontScale $direction")
            assertTrue(bounds.bottom <= viewport.bottom, "$label ends below viewport at $fontScale $direction")
        }
        onNodeWithText("Portugal").performClick()
        assertEquals("pt", selected)
        onNodeWithText("Português").performClick()
        onNodeWithText("Portugal").assertDoesNotExist()
    }

    @Test
    fun `expansion reveals children inside an enclosing scroll container`() = runComposeUiTest {
        val choices = List(3) { SelectionListNode("item-$it", "Item $it") } + SelectionListNode(
            "group",
            "More choices",
            children = listOf(SelectionListNode("first", "First child"), SelectionListNode("last", "Last child")),
        )
        setContent {
            KmpTheme {
                Column(Modifier.height(300.dp).testTag("outer").verticalScroll(rememberScrollState())) {
                    Spacer(Modifier.height(120.dp))
                    SelectionListContent("", choices, emptySet(), {}, Modifier.height(240.dp))
                }
            }
        }
        onNodeWithText("More choices").performScrollTo().performClick()
        val outer = onNodeWithTag("outer").getUnclippedBoundsInRoot()
        val inner = onNode(hasScrollToIndexAction()).getUnclippedBoundsInRoot()
        listOf("More choices", "First child", "Last child").forEach { label ->
            val bounds = onNodeWithText(label).assertIsDisplayed().getUnclippedBoundsInRoot()
            assertTrue(bounds.top >= maxOf(outer.top, inner.top), "$label starts above a viewport")
            assertTrue(bounds.bottom <= minOf(outer.bottom, inner.bottom), "$label ends below a viewport")
        }
    }

    @Test
    fun `oversized expanded groups keep the parent accessible and children scrollable`() = runComposeUiTest {
        val choices = List(3) { SelectionListNode("item-$it", "Item $it") } + SelectionListNode(
            "group",
            "More choices",
            children = List(12) { SelectionListNode("child-$it", "Choice $it") },
        )
        var selected: String? = null
        setContent {
            KmpTheme {
                SelectionListContent("", choices, emptySet(), { selected = it }, Modifier.height(240.dp))
            }
        }
        onNodeWithText("More choices").performScrollTo().performClick()
        onNodeWithText("More choices").assertIsDisplayed()
        onNodeWithText("Choice 0").assertIsDisplayed()
        onNodeWithText("Choice 11").performScrollTo().performClick()
        assertEquals("child-11", selected)
    }

    @Test
    fun `initial expanded selection keeps the list at its starting position`() = runComposeUiTest {
        val choices = List(3) { SelectionListNode("item-$it", "Item $it") } + SelectionListNode(
            "group",
            "More choices",
            children = List(12) { SelectionListNode("child-$it", "Choice $it") },
        )
        setContent {
            KmpTheme {
                SelectionListContent("", choices, setOf("child-11"), {}, Modifier.height(240.dp))
            }
        }
        onNodeWithText("Item 0").assertIsDisplayed()
    }

    @Test
    fun `selected parents expand when nodes arrive after initial composition`() = runComposeUiTest {
        var loadedNodes by mutableStateOf(emptyList<SelectionListNode<String>>())
        setContent {
            KmpTheme {
                SelectionListContent(
                    title = "Language",
                    nodes = loadedNodes,
                    selectedIds = setOf("en-GB"),
                    onSelect = {},
                )
            }
        }
        runOnIdle { loadedNodes = nodes }
        onNodeWithText("English").assert(stateIs("Expanded"))
        onNodeWithText("United Kingdom").assertIsSelected()
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
