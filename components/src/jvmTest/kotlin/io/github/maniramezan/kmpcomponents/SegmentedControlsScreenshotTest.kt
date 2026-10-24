package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.github.takahirom.roborazzi.RoborazziOptions
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SegmentedControlsScreenshotTest {
    @Test
    fun `segmented controls in both directions across themes and text sizes`() {
        listOf(ThemeMode.LIGHT, ThemeMode.DARK).forEach { mode ->
            listOf(1f, 2f).forEach { fontScale ->
                listOf(LayoutDirection.Ltr, LayoutDirection.Rtl).forEach { direction ->
                    capture(mode, fontScale, direction)
                }
            }
        }
    }

    private fun capture(mode: ThemeMode, fontScale: Float, direction: LayoutDirection) = runDesktopComposeUiTest(
        width = 800,
        height = 300,
    ) {
        var currentDirection by mutableStateOf(
            if (direction == LayoutDirection.Ltr) LayoutDirection.Rtl else LayoutDirection.Ltr,
        )
        setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density.density, fontScale),
                LocalLayoutDirection provides currentDirection,
            ) {
                KmpTheme(mode = mode) {
                    Surface {
                        Column(
                            Modifier.padding(MaterialTheme.kmpSpacing.medium),
                            verticalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small),
                        ) {
                            ThemeModeSelector(ThemeMode.SYSTEM, {})
                            ThemeSingleChoiceSegmentedButtonRow(listOf("First", "Middle", "Last"), 0, {})
                            ThemeSingleChoiceSegmentedButtonRow(listOf("Only"), 0, {})
                        }
                    }
                }
            }
        }
        waitForIdle()
        currentDirection = direction
        waitForIdle()
        onRoot().captureRoboImage(
            "src/jvmTest/snapshots/segments-${mode.name.lowercase()}-${fontScale.toInt()}-${direction.name}.png",
            roborazziOptions = RoborazziOptions(
                compareOptions = RoborazziOptions.CompareOptions(changeThreshold = 0.01f),
            ),
        )
    }
}
