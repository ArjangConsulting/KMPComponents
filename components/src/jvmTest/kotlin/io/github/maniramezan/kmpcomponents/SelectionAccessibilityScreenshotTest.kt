package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.github.takahirom.roborazzi.RoborazziOptions
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SelectionAccessibilityScreenshotTest {
    @Test
    fun `selection cues in light mode`() = capture("light", ThemeMode.LIGHT)

    @Test
    fun `selection cues in dark mode`() = capture("dark", ThemeMode.DARK)

    @Test
    fun `selection cues at double text size`() = capture("font-200", ThemeMode.LIGHT, 2f)

    @Test
    fun `selection cues in RTL dark mode at double text size`() =
        capture("rtl-dark-font-200", ThemeMode.DARK, 2f, LayoutDirection.Rtl)

    private fun capture(
        name: String,
        mode: ThemeMode,
        scale: Float = 1f,
        direction: LayoutDirection = LayoutDirection.Ltr,
    ) = runDesktopComposeUiTest(width = 560, height = 1500) {
        setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density.density, scale),
                LocalLayoutDirection provides direction,
            ) {
                KmpTheme(mode = mode) {
                    Surface {
                        Column(
                            Modifier.padding(MaterialTheme.kmpSpacing.medium),
                            verticalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small),
                        ) {
                            SectionHeader("Selection and focus")
                            ThemeCard { Text("Static card") }
                            SelectableCard(true, {}, Modifier.fillMaxWidth()) {
                                Text("First option")
                                Text("Selected with a check mark")
                            }
                            SelectableCard(false, {}, Modifier.fillMaxWidth()) {
                                Text("Second option")
                                Text("Available for selection")
                            }
                            SelectableCard(true, {}, Modifier.fillMaxWidth(), enabled = false) { Text("Unavailable") }
                            ContentRow("Selected row", selected = true, onClick = {})
                            SidebarItem("Selected destination", true, {})
                            SidebarItem("Other destination", false, {})
                            TabChipRow {
                                TabChip("Selected tab", true, {}, onClose = {})
                                TabChip("Other tab", false, {})
                            }
                            ThemeSwitch(true, {}, "Switch")
                            ThemeCheckbox(true, {}, "Checkbox")
                            StatusMessage("Operation succeeded", tone = StatusTone.SUCCESS, title = "Success")
                            TonalBadge("Success", StatusTone.SUCCESS)
                        }
                    }
                }
            }
        }
        onNodeWithText("First option").requestFocus()
        onRoot().captureRoboImage(
            "src/jvmTest/snapshots/accessibility-selection-$name.png",
            roborazziOptions = RoborazziOptions(
                compareOptions = RoborazziOptions.CompareOptions(changeThreshold = 0.01f),
            ),
        )
    }
}
