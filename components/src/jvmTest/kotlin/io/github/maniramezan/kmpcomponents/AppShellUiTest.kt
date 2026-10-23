package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// Interaction tests for the navigation, layout, and feedback components shared by desktop apps.
@OptIn(ExperimentalTestApi::class)
class AppShellUiTest {
    @Test
    fun `theme mode dropdown opens and reports the chosen mode`() = runComposeUiTest {
        var mode by mutableStateOf(ThemeMode.SYSTEM)
        setContent {
            KmpTheme { ThemeModeDropdown(selected = mode, onSelect = { mode = it }, label = "Color scheme") }
        }
        onNodeWithText("System").performClick()
        onNodeWithText("Dark").performClick()
        waitForIdle()
        assertEquals(ThemeMode.DARK, mode)
    }

    @Test
    fun `disabling an open theme dropdown closes it and prevents selection until reopened`() = runComposeUiTest {
        var enabled by mutableStateOf(true)
        var mode by mutableStateOf(ThemeMode.SYSTEM)
        var selections = 0
        setContent {
            KmpTheme {
                ThemeModeDropdown(
                    selected = mode,
                    onSelect = {
                        mode = it
                        selections++
                    },
                    label = "Color scheme",
                    enabled = enabled,
                )
            }
        }
        onNodeWithText("System").performClick()
        onNodeWithText("Dark").assertIsDisplayed()
        enabled = false
        onNodeWithText("Dark").assertDoesNotExist()
        onNodeWithText("System").assertIsNotEnabled()
        onNodeWithText("System").performClick()
        onNodeWithText("Dark").assertDoesNotExist()
        assertEquals(0, selections)
        enabled = true
        onNodeWithText("Dark").assertDoesNotExist()
        onNodeWithText("System").performClick()
        onNodeWithText("Dark").performClick()
        waitForIdle()
        assertEquals(ThemeMode.DARK, mode)
        assertEquals(1, selections)
    }

    @Test
    fun `check status row shows each state and announces results`() = runComposeUiTest {
        var status by mutableStateOf<CheckStatus>(CheckStatus.Idle)
        setContent { KmpTheme { CheckStatusRow(status) } }
        onNodeWithText("Not tested").assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.LiveRegion))
        status = CheckStatus.Checking
        onNodeWithText("Checking…").assertIsDisplayed()
        status = CheckStatus.Failure("Unauthorized")
        onNodeWithText("Unauthorized")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
    }

    @Test
    fun `badges show capped counts and custom descriptions`() = runComposeUiTest {
        setContent {
            KmpTheme {
                Column {
                    TonalBadge("GET")
                    TonalBadge("404", tone = StatusTone.WARNING)
                    CountBadge(150)
                    CountBadge(3, contentDescription = "3 variants")
                }
            }
        }
        onNodeWithText("GET").assertIsDisplayed()
        onNodeWithText("404").assertIsDisplayed()
        onNodeWithText("99+").assertIsDisplayed()
        onNodeWithContentDescription("3 variants").assertIsDisplayed()
    }

    @Test
    fun `empty state title is a heading and its action is clickable`() = runComposeUiTest {
        var clicked = false
        setContent {
            KmpTheme {
                EmptyState(
                    title = "No project loaded",
                    message = "Open a project to start",
                    action = { TextButton(onClick = { clicked = true }) { Text("Open") } },
                )
            }
        }
        onNodeWithText("No project loaded").assert(isHeading())
        onNodeWithText("Open").performClick()
        assertTrue(clicked)
    }

    @Test
    fun `confirm dialog routes confirm and dismiss`() = runComposeUiTest {
        var result by mutableStateOf<Boolean?>(null)
        setContent {
            KmpTheme {
                if (result == null) {
                    ConfirmDialog(
                        title = "Delete all?",
                        message = "This cannot be undone.",
                        onConfirm = { result = true },
                        onDismiss = { result = false },
                        confirmLabel = "Delete all",
                        destructive = true,
                    )
                }
            }
        }
        onNodeWithText("This cannot be undone.").assertIsDisplayed()
        onNodeWithText("Cancel").performClick()
        waitForIdle()
        assertEquals(false, result)
        result = null
        onNodeWithText("Delete all").performClick()
        waitForIdle()
        assertEquals(true, result)
    }

    @Test
    fun `info tooltip exposes its text to screen readers`() = runComposeUiTest {
        setContent { KmpTheme { InfoTooltip("Used when no key is set") } }
        onNodeWithContentDescription("Used when no key is set").assertIsDisplayed()
    }

    @Test
    fun `selectable card reports selection and clicks`() = runComposeUiTest {
        var selected by mutableStateOf(false)
        setContent {
            KmpTheme { SelectableCard(selected = selected, onClick = { selected = true }) { Text("Ollama") } }
        }
        onNodeWithText("Ollama").assertIsNotSelected()
        onNodeWithText("Ollama").performClick()
        onNodeWithText("Ollama").assertIsSelected()
    }

    @Test
    fun `sidebar items select one destination`() = runComposeUiTest {
        var section by mutableStateOf("General")
        setContent {
            KmpTheme {
                SidebarNavigation(header = { Text("Settings") }) {
                    listOf("General", "AI").forEach { name ->
                        SidebarItem(label = name, selected = section == name, onClick = { section = name })
                    }
                }
            }
        }
        onNodeWithText("General").assertIsSelected()
        onNodeWithText("AI").performClick()
        onNodeWithText("AI").assertIsSelected()
        onNodeWithText("General").assertIsNotSelected()
    }

    @Test
    fun `tab chips select and close separately`() = runComposeUiTest {
        var selected by mutableStateOf("Success")
        var closed: String? = null
        setContent {
            KmpTheme {
                TabChipRow {
                    listOf("Success", "Not found").forEach { name ->
                        TabChip(
                            label = name,
                            selected = selected == name,
                            onClick = { selected = name },
                            onClose = { closed = name },
                        )
                    }
                }
            }
        }
        onNodeWithText("Not found").performClick()
        onNodeWithText("Not found").assertIsSelected()
        onNodeWithContentDescription("Close Success").performClick()
        assertEquals("Success", closed)
        assertEquals("Not found", selected)
    }

    @Test
    fun `split pane divider moves with arrow keys and accessibility actions`() = runComposeUiTest {
        var split by mutableStateOf(0.5f)
        setContent {
            KmpTheme {
                HorizontalSplitPane(
                    split = split,
                    onSplitChange = { split = it },
                    modifier = Modifier.size(width = 800.dp, height = 400.dp),
                    leading = { Text("Console", Modifier.fillMaxSize()) },
                    trailing = { Text("Device", Modifier.fillMaxSize()) },
                )
            }
        }
        val divider = onNodeWithContentDescription("Resize panes")
        divider.assert(hasStateDescription("50% leading pane"))
        divider.requestFocus()
        divider.performKeyInput { pressKey(Key.DirectionRight) }
        waitForIdle()
        assertEquals(0.55f, split, 0.0001f)
        divider.performSemanticsAction(SemanticsActions.SetProgress) { it(0.95f) }
        waitForIdle()
        assertEquals(SplitPaneDefaults.MAX_SPLIT, split)
        onNode(hasText("Console")).assertIsDisplayed()
    }

    @Test
    fun `split pane divider follows arrow directions in right-to-left layouts`() = runComposeUiTest {
        var split by mutableStateOf(0.5f)
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                KmpTheme {
                    HorizontalSplitPane(
                        split = split,
                        onSplitChange = { split = it },
                        modifier = Modifier.size(width = 800.dp, height = 400.dp),
                        leading = { Text("Leading") },
                        trailing = { Text("Trailing") },
                    )
                }
            }
        }
        val divider = onNodeWithContentDescription("Resize panes")
        val originalLeft = divider.fetchSemanticsNode().boundsInRoot.left
        divider.requestFocus()
        divider.performKeyInput { pressKey(Key.DirectionRight) }
        assertTrue(divider.fetchSemanticsNode().boundsInRoot.left > originalLeft)
        assertEquals(0.45f, split, 0.0001f)
        divider.performKeyInput { pressKey(Key.DirectionLeft) }
        assertEquals(originalLeft, divider.fetchSemanticsNode().boundsInRoot.left, 1f)
        assertEquals(0.5f, split, 0.0001f)
    }

    @Test
    fun `split pane dragging accumulates movement between frames`() = assertSplitPaneDragging(LayoutDirection.Ltr)

    @Test
    fun `split pane dragging follows the pointer in right-to-left layouts`() = assertSplitPaneDragging(
        LayoutDirection.Rtl,
    )

    private fun assertSplitPaneDragging(layoutDirection: LayoutDirection) = runComposeUiTest {
        var split by mutableStateOf(0.5f)
        var touchSlop = 0f
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                touchSlop = LocalViewConfiguration.current.touchSlop
                KmpTheme {
                    HorizontalSplitPane(
                        split = split,
                        onSplitChange = { split = it },
                        modifier = Modifier.size(width = 800.dp, height = 400.dp).testTag("split pane"),
                        leading = { Text("Leading") },
                        trailing = { Text("Trailing") },
                    )
                }
            }
        }
        val divider = onNodeWithContentDescription("Resize panes")
        val paneBounds = onNodeWithTag("split pane").fetchSemanticsNode().boundsInRoot
        val dividerBounds = divider.fetchSemanticsNode().boundsInRoot
        val paneWidth = paneBounds.width - dividerBounds.width
        val dragDistance = paneWidth / 8f
        val direction = if (layoutDirection == LayoutDirection.Rtl) -1 else 1
        val expectedChange = (dragDistance - touchSlop) / paneWidth
        repeat(2) { gesture ->
            val initialSplit = if (gesture == 0) 0.5f else 0.35f
            split = initialSplit
            val initialBounds = divider.fetchSemanticsNode().boundsInRoot
            mainClock.autoAdvance = false
            try {
                onRoot().performTouchInput {
                    down(initialBounds.center)
                    repeat(10) { moveBy(Offset(direction * dragDistance / 10f, 0f), delayMillis = 1) }
                    up()
                }
            } finally {
                mainClock.autoAdvance = true
            }
            waitForIdle()
            assertEquals(initialSplit + expectedChange, split, 0.0001f)
            val movement = divider.fetchSemanticsNode().boundsInRoot.left - initialBounds.left
            assertEquals(direction * (dragDistance - touchSlop), movement, 1f)
        }
    }

    private fun hasStateDescription(value: String): SemanticsMatcher =
        SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, value)
}
