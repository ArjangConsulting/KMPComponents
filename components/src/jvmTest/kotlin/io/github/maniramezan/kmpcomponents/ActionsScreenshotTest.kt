package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.RoborazziOptions
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

// Golden images of the actions family: buttons, segmented controls, and pills.
@OptIn(ExperimentalTestApi::class)
class ActionsScreenshotTest {
    @Test
    fun `actions in light mode`() = capture("actions-light", ThemeMode.LIGHT)

    @Test
    fun `actions in dark mode`() = capture("actions-dark", ThemeMode.DARK)

    @Test
    fun `actions at double font scale`() = capture("actions-font-200", ThemeMode.LIGHT, fontScale = 2f)

    private fun capture(name: String, mode: ThemeMode, fontScale: Float = 1f) = runDesktopComposeUiTest(
        width = WINDOW_WIDTH,
        height = WINDOW_HEIGHT,
    ) {
        setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                KmpTheme(mode = mode) {
                    Surface(Modifier.width(GALLERY_WIDTH)) { ActionsGallery() }
                }
            }
        }
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
private fun ActionsGallery() {
    val spacing = MaterialTheme.kmpSpacing
    val plus = rememberVectorPainter(TestPlusIcon)
    Column(Modifier.padding(spacing.medium), verticalArrangement = Arrangement.spacedBy(spacing.small)) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(spacing.small),
            verticalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            PrimaryButton(text = "Save", onClick = {})
            SecondaryButton(text = "Test", onClick = {})
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(spacing.small),
            verticalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            ThemeTextButton(text = "Cancel", onClick = {})
            ThemeTextButton(text = "Delete all", onClick = {}, destructive = true)
            PrimaryButton(text = "Disabled", onClick = {}, enabled = false)
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(spacing.small),
            verticalArrangement = Arrangement.spacedBy(spacing.small),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            ThemeIconButton(icon = plus, contentDescription = "Add", onClick = {})
            LoadingIconButton(loading = false, icon = plus, contentDescription = "Refresh", onClick = {})
            FAB(icon = plus, contentDescription = "New", onClick = {})
            ThemeExtendedFloatingActionButton(text = "Record", icon = plus, onClick = {})
        }
        SegmentedControl(options = listOf("Day", "Week", "Month"), selectedIndex = 1, onOptionSelected = {})
        SegmentedControl(
            options = listOf("Compact", "Fit"),
            selectedIndex = 0,
            onOptionSelected = {},
            density = SegmentDensity.COMPACT,
            widthMode = SegmentWidthMode.FIT,
        )
        ThemeSingleChoiceSegmentedButtonRow(
            options = listOf("JSON", "YAML"),
            selectedIndex = 0,
            onOptionSelected = {},
            modifier = Modifier.fillMaxWidth(),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(spacing.small),
            verticalArrangement = Arrangement.spacedBy(spacing.small),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            PillChip(label = "All", isSelected = true, onClick = {})
            PillChip(label = "Errors", isSelected = false, onClick = {})
            ActionPill(onClick = {}) {
                Icon(plus, contentDescription = null, modifier = Modifier.width(spacing.medium))
                Text("Add filter")
            }
        }
    }
}
