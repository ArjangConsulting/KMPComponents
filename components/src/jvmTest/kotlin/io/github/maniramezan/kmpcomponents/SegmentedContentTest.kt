package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.RoborazziOptions
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

// Interaction tests and goldens for SegmentedContent.
@OptIn(ExperimentalTestApi::class)
class SegmentedContentTest {
    private val items = listOf("Headers", "Body", "Cookies").map(::SegmentedItem)

    @Test
    fun `tapping a segment selects it and swaps the content`() = runComposeUiTest {
        var changes = 0
        setContent {
            KmpTheme {
                SegmentedContent(items = items, onSelectionChanged = {
                    changes++
                }) { _, item -> Text("${item.title} panel") }
            }
        }
        onNodeWithText("Headers panel").assertExists()
        onNodeWithText("Body").assertIsNotSelected().performClick()
        waitForIdle()
        onNodeWithText("Body").assertIsSelected()
        onNodeWithText("Body panel").assertExists()
        onNodeWithText("Headers panel").assertDoesNotExist()
        assertEquals(1, changes)
    }

    @Test
    fun `shrinking items during the content transition does not index removed items`() = runComposeUiTest {
        var currentItems by mutableStateOf(items)
        setContent {
            KmpTheme {
                SegmentedContent(items = currentItems, initialSelectedIndex = 2) { _, item ->
                    Text("${item.title} panel")
                }
            }
        }
        onNodeWithText("Cookies panel").assertExists()
        mainClock.autoAdvance = false
        runOnIdle { currentItems = items.take(1) }
        mainClock.advanceTimeByFrame()
        mainClock.autoAdvance = true
        waitForIdle()
        onNodeWithText("Headers panel").assertExists()
    }

    @Test
    fun `an overflowing single title keeps its full width at normal and double font scale`() {
        listOf(1f, 2f).forEach { fontScale ->
            runComposeUiTest {
                val title = "A translated segment title that is wider than the available window"
                setContent {
                    val density = LocalDensity.current
                    CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                        KmpTheme {
                            Box(Modifier.width(240.dp)) {
                                SegmentedContent(items = listOf(SegmentedItem(title))) { _, _ -> }
                            }
                        }
                    }
                }
                val layouts = mutableListOf<TextLayoutResult>()
                onNodeWithText(title).performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
                assertFalse(layouts.single().hasVisualOverflow)
            }
        }
    }

    @Test
    fun `segments in light mode`() = capture("segmented-light", ThemeMode.LIGHT)

    @Test
    fun `segments in dark mode`() = capture("segmented-dark", ThemeMode.DARK)

    @Test
    fun `segments at double font scale`() = capture("segmented-font-200", ThemeMode.LIGHT, fontScale = 2f)

    private fun capture(name: String, mode: ThemeMode, fontScale: Float = 1f) = runDesktopComposeUiTest(
        width = 480,
        height = 800,
    ) {
        setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                KmpTheme(mode = mode) { Surface(Modifier.width(480.dp)) { SegmentedGallery() } }
            }
        }
        onRoot().captureRoboImage("src/jvmTest/snapshots/$name.png", roborazziOptions = OPTIONS)
    }

    @Composable
    private fun SegmentedGallery() {
        val spacing = MaterialTheme.kmpSpacing
        Column(Modifier.padding(spacing.medium), verticalArrangement = Arrangement.spacedBy(spacing.medium)) {
            SegmentedContent(items = items, selectedIndex = 1, onSelectionChanged = {
            }) { _, item -> Text("${item.title} panel") }
            SegmentedContent(
                items = listOf("Overview", "Requests", "Responses", "Timeline", "Settings").map(::SegmentedItem),
                selectedIndex = 0,
                onSelectionChanged = {},
                indicator = SegmentSelectionIndicator.UNDERLINE,
                backgroundColor = MaterialTheme.colorScheme.surface,
            ) { _, _ -> }
            SegmentedContent(
                items = listOf("Day", "Week").map(::SegmentedItem),
                selectedIndex = 0,
                onSelectionChanged = {},
                fitMode = SegmentFitMode.INTRINSIC,
                widthMode = SegmentWidthMode.FIT,
                density = SegmentDensity.COMPACT,
            ) { _, _ -> }
        }
    }

    private companion object {
        // Allow antialiasing noise between machines; real regressions change far more pixels.
        val OPTIONS = RoborazziOptions(compareOptions = RoborazziOptions.CompareOptions(changeThreshold = 0.01f))
    }
}
