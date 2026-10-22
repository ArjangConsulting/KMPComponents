package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

// Interaction tests for lists, paging, showcase rows, flip cards, and navigation bars.
@OptIn(ExperimentalTestApi::class)
class NavigationUiTest {
    private fun stateIs(value: String) = SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, value)

    private val destinations = listOf("Home", "Search", "Settings")

    @Test
    fun `list item reads as one node and load more footer fires and is named`() = runComposeUiTest {
        var loads = 0
        setContent {
            KmpTheme {
                LazyColumn {
                    item { ThemeListItem(headline = "Device", supportingText = "iPhone 16") }
                    item { LoadMoreFooter(isLoadingMore = true, onLoadMore = { loads++ }) }
                }
            }
        }
        onNodeWithText("Device").assertTextEquals("Device", "iPhone 16")
        onNodeWithContentDescription("Loading more").assertExists()
        assertEquals(1, loads)
    }

    @Test
    fun `paginated content reports swipes and announces the position`() = runComposeUiTest {
        var page by mutableIntStateOf(0)
        setContent {
            KmpTheme {
                PaginatedContent(
                    pages = listOf(PaginationPage("Intro"), PaginationPage("Setup")),
                    showScrollHint = false,
                    onPageChanged = { page = it },
                    pagePositionDescription = { index, count -> "Page ${index + 1} of $count" },
                ) { _, item -> Box(Modifier.size(300.dp)) { Text("Body of ${item.title}") } }
            }
        }
        onNodeWithContentDescription("Page 1 of 2").assertExists()
        onNodeWithText("Body of Intro").performTouchInput { swipeLeft() }
        waitForIdle()
        assertEquals(1, page)
        onNodeWithContentDescription("Page 2 of 2").assertExists()
    }

    @Test
    fun `showcase feed renders headers and items`() = runComposeUiTest {
        var seeAll = 0
        setContent {
            KmpTheme {
                ShowcaseFeed {
                    section(title = "Recent", items = listOf("A", "B"), actionLabel = "See all", onAction = {
                        seeAll++
                    }) {
                        Text("Item $it")
                    }
                    customSection { Text("Banner") }
                }
            }
        }
        onNodeWithText("Item A").assertExists()
        onNodeWithText("Banner").assertExists()
        onNodeWithText("See all").performClick()
        assertEquals(1, seeAll)
    }

    @Test
    fun `flip card toggles between faces and announces the face`() = runComposeUiTest {
        setContent {
            KmpTheme {
                FlipCard(
                    front = { Text("Question") },
                    back = { Text("Answer") },
                    modifier = Modifier.size(200.dp),
                    frontLabel = "Front",
                    backLabel = "Back",
                )
            }
        }
        onNodeWithText("Answer").assertDoesNotExist()
        onNode(stateIs("Front")).performClick()
        waitForIdle()
        onNode(stateIs("Back")).assertExists()
        onNodeWithText("Answer").assertExists()
        onNodeWithText("Question").assertDoesNotExist()
    }

    @Test
    fun `tab bar and tab row select the tapped destination`() = runComposeUiTest {
        var selected by mutableStateOf("Home")
        var tab by mutableIntStateOf(0)
        setContent {
            KmpTheme {
                val icon = rememberVectorPainter(TestPlusIcon)
                Column {
                    ThemeTopAppBar(title = "Inbox")
                    ThemeTabRow(tabs = listOf("All", "Unread"), selectedIndex = tab, onItemSelected = { tab = it })
                    TabBar(
                        items = destinations.map { name ->
                            TabBarItemData(name, icon = { Icon(icon, null) }, label = { Text(name) })
                        },
                        selection = selected,
                        onSelectionChange = { selected = it },
                    )
                }
            }
        }
        onNodeWithText("Inbox").assertExists()
        onNodeWithText("Settings").performClick()
        onNodeWithText("Unread").performClick()
        waitForIdle()
        assertEquals("Settings", selected)
        assertEquals(1, tab)
        onNodeWithText("Settings").assertIsSelected()
    }

    @Test
    fun `adaptive scaffold uses a rail in wide windows and a bar in compact ones`() {
        fun render(width: Int, expectRail: Boolean) = runDesktopComposeUiTest(width = width, height = 600) {
            setContent {
                KmpTheme {
                    val icon = rememberVectorPainter(TestPlusIcon)
                    AdaptiveNavScaffold(
                        items = destinations.map { name ->
                            TabBarItemData(name, icon = { Icon(icon, null) }, contentDescription = name)
                        },
                        selection = "Home",
                        onSelectionChange = {},
                    ) { Text("Content") }
                }
            }
            // The rail and the bar both name destinations; the rail is a vertical column on the start edge.
            val home = onNodeWithContentDescription("Home").fetchSemanticsNode().boundsInRoot
            val search = onNodeWithContentDescription("Search").fetchSemanticsNode().boundsInRoot
            assertEquals(expectRail, search.top > home.top)
        }
        render(width = 1_000, expectRail = true)
        render(width = 400, expectRail = false)
    }
}
