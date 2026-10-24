package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.RoborazziOptions
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

// Golden images of the selection family: radio groups, sliders, and selection lists.
@OptIn(ExperimentalTestApi::class)
class SelectionScreenshotTest {
    @Test
    fun `selection in light mode`() = capture("selection-light", ThemeMode.LIGHT)

    @Test
    fun `selection in dark mode`() = capture("selection-dark", ThemeMode.DARK)

    @Test
    fun `selection at double font scale`() = capture("selection-font-200", ThemeMode.LIGHT, fontScale = 2f)

    @Test
    fun `expanded last group scrolls into view in light mode`() = captureExpandedGroup("light", ThemeMode.LIGHT)

    @Test
    fun `expanded last group scrolls into view in dark mode`() = captureExpandedGroup("dark", ThemeMode.DARK)

    @Test
    fun `expanded last group scrolls into view at double font scale`() =
        captureExpandedGroup("font-200", ThemeMode.LIGHT, 2f)

    private fun captureExpandedGroup(name: String, mode: ThemeMode, fontScale: Float = 1f) = runDesktopComposeUiTest(
        width = 480,
        height = 480,
    ) {
        val nodes = List(3) { SelectionListNode("item-$it", "Item $it") } + SelectionListNode(
            "pt",
            "Português",
            children = listOf(SelectionListNode("pt-BR", "Brasil"), SelectionListNode("pt-PT", "Portugal")),
        )
        setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                KmpTheme(mode = mode) {
                    Surface {
                        Column(Modifier.padding(MaterialTheme.kmpSpacing.medium)) {
                            SelectionListContent(
                                title = "Also translate to",
                                nodes = nodes,
                                selectedIds = emptySet(),
                                onSelect = {},
                                modifier = Modifier.height(400.dp),
                                isSearchable = true,
                            )
                        }
                    }
                }
            }
        }
        onNodeWithText("Português").performScrollTo().performClick()
        onRoot().captureRoboImage("$SNAPSHOT_DIR/selection-expanded-$name.png", roborazziOptions = OPTIONS)
    }

    private fun capture(name: String, mode: ThemeMode, fontScale: Float = 1f) = runDesktopComposeUiTest(
        width = WINDOW_WIDTH,
        height = WINDOW_HEIGHT,
    ) {
        setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                KmpTheme(mode = mode) {
                    Surface(Modifier.width(GALLERY_WIDTH)) { SelectionGallery() }
                }
            }
        }
        waitForIdle()
        onRoot().captureRoboImage("$SNAPSHOT_DIR/$name.png", roborazziOptions = OPTIONS)
    }

    private companion object {
        const val SNAPSHOT_DIR = "src/jvmTest/snapshots"
        const val WINDOW_WIDTH = 480
        const val WINDOW_HEIGHT = 1600
        val GALLERY_WIDTH = WINDOW_WIDTH.dp

        // Allow antialiasing noise between machines; real regressions change far more pixels.
        val OPTIONS = RoborazziOptions(compareOptions = RoborazziOptions.CompareOptions(changeThreshold = 0.01f))
    }
}

@Composable
private fun SelectionGallery() {
    val spacing = MaterialTheme.kmpSpacing
    Column(Modifier.padding(spacing.medium), verticalArrangement = Arrangement.spacedBy(spacing.small)) {
        RadioGroup(options = listOf("HTTP", "HTTPS"), selectedIndex = 1, onOptionSelected = {})
        ThemeSlider(value = 0.3f, onValueChange = {}, label = "Timeout")
        ThemeSlider(value = 2f, onValueChange = {}, valueRange = 0f..4f, steps = 3, label = "Retries")
        SelectionListContent(
            title = "Language",
            nodes = listOf(
                SelectionListNode(
                    id = "en",
                    title = "English",
                    children = listOf(
                        SelectionListNode("en-US", "United States", subtitle = "en-US"),
                        SelectionListNode("en-GB", "United Kingdom", subtitle = "en-GB"),
                    ),
                ),
                SelectionListNode("es", "Español", subtitle = "es", leadingGlyph = "🇪🇸"),
                SelectionListNode(
                    id = "pt",
                    title = "Português",
                    children = listOf(SelectionListNode("pt-BR", "Brasil"), SelectionListNode("pt-PT", "Portugal")),
                ),
            ),
            selectedIds = setOf("en-GB", "pt-BR"),
            onSelect = {},
            isSearchable = true,
        )
    }
}
