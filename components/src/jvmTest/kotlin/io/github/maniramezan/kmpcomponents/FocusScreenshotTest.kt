package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.RoborazziOptions
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class FocusScreenshotTest {
    @Test
    fun `keyboard focus in light mode`() = capture("focus-light", ThemeMode.LIGHT)

    @Test
    fun `keyboard focus in dark mode`() = capture("focus-dark", ThemeMode.DARK)

    @Test
    fun `keyboard focus at double font scale`() = capture("focus-font-200", ThemeMode.LIGHT, 2f)

    private fun capture(name: String, mode: ThemeMode, fontScale: Float = 1f) = runDesktopComposeUiTest(
        width = 480,
        height = 800,
    ) {
        setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                KmpTheme(mode = mode) {
                    Surface(Modifier.width(480.dp)) {
                        val spacing = MaterialTheme.kmpSpacing
                        Column(
                            Modifier.padding(spacing.medium),
                            verticalArrangement = Arrangement.spacedBy(spacing.small),
                        ) {
                            SectionHeader("Keyboard focus")
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(spacing.small),
                                verticalArrangement = Arrangement.spacedBy(spacing.small),
                            ) {
                                PrimaryButton("Save", {})
                                SecondaryButton("Cancel", {})
                            }
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(spacing.small),
                                verticalArrangement = Arrangement.spacedBy(spacing.small),
                            ) {
                                PillChip("Selected", true, onClick = {})
                                PillChip("Filter", false, onClick = {})
                                ActionPill({}) { Text("Focused action") }
                            }
                            SegmentedControl(listOf("Day", "Week"), 0, {})
                        }
                    }
                }
            }
        }
        onNodeWithText("Focused action").requestFocus()
        onRoot().captureRoboImage(
            "src/jvmTest/snapshots/$name.png",
            roborazziOptions = RoborazziOptions(
                compareOptions = RoborazziOptions.CompareOptions(changeThreshold = 0.01f),
            ),
        )
    }
}
