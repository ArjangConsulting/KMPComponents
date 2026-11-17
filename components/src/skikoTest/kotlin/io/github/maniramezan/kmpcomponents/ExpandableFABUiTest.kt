package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class ExpandableFABUiTest {
    private var expanded by mutableStateOf(false)
    private val events = mutableListOf<String>()

    private fun stateIs(value: String) = SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, value)

    private fun ComposeUiTest.setMenu(
        actionIds: List<String> = listOf("Share", "Edit"),
        direction: FABExpansionDirection = FABExpansionDirection.UP,
        collapseOnAction: Boolean = true,
        showActionLabels: Boolean = true,
        triggerLabel: String? = null,
        disabledIds: Set<String> = emptySet(),
        layoutDirection: LayoutDirection = LayoutDirection.Ltr,
        boxSize: Int? = null,
        scrollingParent: Boolean = false,
    ) {
        setContent {
            CompositionLocalProvider(
                LocalReduceMotionEnabled provides true,
                LocalLayoutDirection provides layoutDirection,
            ) {
                KmpTheme {
                    val plus = rememberVectorPainter(TestPlusIcon)
                    val actions = actionIds.map { id ->
                        FABAction(
                            id = id,
                            label = id,
                            icon = plus,
                            onClick = { events += id },
                            enabled =
                            id !in disabledIds,
                        )
                    }
                    val parent = when {
                        boxSize != null -> Modifier.size(boxSize.dp)
                        scrollingParent -> Modifier.verticalScroll(rememberScrollState())
                        else -> Modifier
                    }
                    Box(parent) {
                        ExpandableFAB(
                            expanded = expanded,
                            onExpandedChange = {
                                events += "expanded=$it"
                                expanded = it
                            },
                            actions = actions,
                            icon = plus,
                            contentDescription = "Actions",
                            expandedStateDescription = "Expanded",
                            collapsedStateDescription = "Collapsed",
                            direction = direction,
                            collapseOnAction = collapseOnAction,
                            showActionLabels = showActionLabels,
                            triggerLabel = triggerLabel,
                        )
                    }
                }
            }
        }
    }

    @Test
    fun `toggling reveals labeled actions and announces the state`() = runComposeUiTest {
        setMenu()
        onNodeWithContentDescription("Actions").assert(stateIs("Collapsed"))
        onNodeWithText("Share").assertDoesNotExist()
        onNodeWithContentDescription("Actions").performClick()
        onNodeWithContentDescription("Actions").assert(stateIs("Expanded"))
        onNodeWithText("Share").assertExists()
        onNodeWithText("Edit").assertExists()
        onNodeWithContentDescription("Actions").performClick()
        onNodeWithText("Share").assertDoesNotExist()
    }

    @Test
    fun `selecting an action collapses before invoking it`() = runComposeUiTest {
        expanded = true
        setMenu()
        onNodeWithText("Share").performClick()
        assertEquals(listOf("expanded=false", "Share"), events)
        onNodeWithText("Edit").assertDoesNotExist()
    }

    @Test
    fun `a persistent menu stays open and disabled actions ignore clicks`() = runComposeUiTest {
        expanded = true
        setMenu(collapseOnAction = false, disabledIds = setOf("Edit"))
        onNodeWithText("Share").performClick()
        onNodeWithText("Edit").performClick()
        assertEquals(listOf("Share"), events)
        onNodeWithText("Share").assertExists()
    }

    @Test
    fun `icon-only actions keep their names roles and touch targets`() = runComposeUiTest {
        expanded = true
        setMenu(showActionLabels = false)
        onNodeWithText("Share").assertDoesNotExist()
        onNodeWithContentDescription("Share")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            .assertWidthIsAtLeast(48.dp)
            .assertHeightIsAtLeast(48.dp)
        onNodeWithContentDescription("Actions").assertWidthIsAtLeast(48.dp).assertHeightIsAtLeast(48.dp)
    }

    @Test
    fun `an extended trigger is named by its label and a blank label stays icon-only`() = runComposeUiTest {
        var label by mutableStateOf<String?>("Create")
        setContent {
            CompositionLocalProvider(LocalReduceMotionEnabled provides true) {
                KmpTheme {
                    val plus = rememberVectorPainter(TestPlusIcon)
                    ExpandableFAB(
                        expanded = false,
                        onExpandedChange = {},
                        actions = listOf(FABAction("a", "Share", plus, {})),
                        icon = plus,
                        contentDescription = "Actions",
                        expandedStateDescription = "Expanded",
                        collapsedStateDescription = "Collapsed",
                        triggerLabel = label,
                    )
                }
            }
        }
        onNodeWithContentDescription("Create").assert(stateIs("Collapsed"))
        label = " "
        onNodeWithContentDescription("Actions").assert(stateIs("Collapsed"))
    }

    @Test
    fun `without actions the trigger toggles but reports no state`() = runComposeUiTest {
        setMenu(actionIds = emptyList())
        onNodeWithContentDescription("Actions")
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.StateDescription))
            .performClick()
        assertEquals(listOf("expanded=true"), events)
    }

    @Test
    fun `each direction keeps the trigger anchored and places the actions on its side`() = runComposeUiTest {
        var direction by mutableStateOf(FABExpansionDirection.UP)
        var layoutDirection by mutableStateOf(LayoutDirection.Ltr)
        expanded = true
        setContent {
            CompositionLocalProvider(
                LocalReduceMotionEnabled provides true,
                LocalLayoutDirection provides layoutDirection,
            ) {
                KmpTheme {
                    val plus = rememberVectorPainter(TestPlusIcon)
                    ExpandableFAB(
                        expanded = true,
                        onExpandedChange = {},
                        actions = listOf(FABAction("a", "Share", plus, {})),
                        icon = plus,
                        contentDescription = "Actions",
                        expandedStateDescription = "Expanded",
                        collapsedStateDescription = "Collapsed",
                        direction = direction,
                    )
                }
            }
        }
        fun check(expected: String) {
            val trigger = onNodeWithContentDescription("Actions").getUnclippedBoundsInRoot()
            val action = onNodeWithText("Share").getUnclippedBoundsInRoot()
            val ok = when (expected) {
                "above" -> action.bottom <= trigger.top
                "below" -> action.top >= trigger.bottom
                "left" -> action.right <= trigger.left
                else -> action.left >= trigger.right
            }
            assertTrue(ok, "$direction $layoutDirection: action $action is not $expected trigger $trigger")
        }
        check("above")
        direction = FABExpansionDirection.DOWN
        waitForIdle()
        check("below")
        direction = FABExpansionDirection.START
        waitForIdle()
        check("left")
        direction = FABExpansionDirection.END
        waitForIdle()
        check("right")
        layoutDirection = LayoutDirection.Rtl
        waitForIdle()
        check("left")
        direction = FABExpansionDirection.START
        waitForIdle()
        check("right")
    }

    @Test
    fun `overflowing actions scroll without hiding the trigger`() = runComposeUiTest {
        expanded = true
        setMenu(actionIds = (1..12).map { "Action $it" }, boxSize = 200)
        onNodeWithContentDescription("Actions").assertExists()
        val trigger = onNodeWithContentDescription("Actions").getUnclippedBoundsInRoot()
        assertTrue(trigger.bottom <= 200.dp, "Trigger was pushed out: $trigger")
        // Up places actions before the trigger, so the nearest (last) action is visible first.
        val last = onNodeWithText("Action 12").getUnclippedBoundsInRoot()
        assertTrue(last.bottom <= trigger.top, "The nearest action is not just above the trigger: $last")
    }

    @Test
    fun `the menu works in a vertically scrolling parent`() = runComposeUiTest {
        expanded = true
        setMenu(scrollingParent = true)
        onNodeWithText("Share").assertExists()
        onNodeWithContentDescription("Actions").assertExists()
    }

    @Test
    fun `exiting actions leave the accessibility tree at once`() = runComposeUiTest {
        mainClock.autoAdvance = false
        expanded = true
        setContent {
            CompositionLocalProvider(LocalReduceMotionEnabled provides false) {
                KmpTheme {
                    val plus = rememberVectorPainter(TestPlusIcon)
                    ExpandableFAB(
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        actions = listOf(FABAction("a", "Share", plus, { events += "Share" })),
                        icon = plus,
                        contentDescription = "Actions",
                        expandedStateDescription = "Expanded",
                        collapsedStateDescription = "Collapsed",
                    )
                }
            }
        }
        mainClock.advanceTimeBy(SETTLE_MILLIS)
        onNodeWithText("Share").assertExists()
        expanded = false
        mainClock.advanceTimeByFrame()
        onNodeWithText("Share").assertDoesNotExist()
        mainClock.advanceTimeBy(SETTLE_MILLIS)
        assertEquals(emptyList(), events)
    }

    private companion object {
        const val SETTLE_MILLIS = 2_000L
    }
}
