package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.RoborazziOptions
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

// Golden images of ExpandableFAB opened in each direction, with labeled, icon-only, and extended variants.
@OptIn(ExperimentalTestApi::class)
class ExpandableFABScreenshotTest {
    @Test
    fun `expandable fab in light mode`() = capture("expandable-fab-light", ThemeMode.LIGHT)

    @Test
    fun `expandable fab in dark mode`() = capture("expandable-fab-dark", ThemeMode.DARK)

    @Test
    fun `expandable fab at double font scale`() = capture("expandable-fab-font-200", ThemeMode.LIGHT, fontScale = 2f)

    @Test
    fun `expandable fab right to left`() = capture("expandable-fab-rtl", ThemeMode.LIGHT, LayoutDirection.Rtl)

    private fun capture(
        name: String,
        mode: ThemeMode,
        layoutDirection: LayoutDirection = LayoutDirection.Ltr,
        fontScale: Float = 1f,
    ) = runDesktopComposeUiTest(width = WINDOW_WIDTH, height = WINDOW_HEIGHT) {
        setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density.density, fontScale),
                LocalLayoutDirection provides layoutDirection,
                LocalReduceMotionEnabled provides true,
            ) {
                KmpTheme(mode = mode) {
                    Surface(Modifier.width(GALLERY_WIDTH)) { ExpandableFABGallery() }
                }
            }
        }
        onRoot().captureRoboImage("$SNAPSHOT_DIR/$name.png", roborazziOptions = OPTIONS)
    }

    private companion object {
        const val SNAPSHOT_DIR = "src/jvmTest/snapshots"
        const val WINDOW_WIDTH = 480
        const val WINDOW_HEIGHT = 1100
        val GALLERY_WIDTH = WINDOW_WIDTH.dp

        // Allow antialiasing noise between machines; real regressions change far more pixels.
        val OPTIONS = RoborazziOptions(compareOptions = RoborazziOptions.CompareOptions(changeThreshold = 0.01f))
    }
}

@Composable
private fun ExpandableFABGallery() {
    val plus = rememberVectorPainter(TestPlusIcon)
    val actions = listOf("Share", "Edit", "Archive").map { FABAction(id = it, label = it, icon = plus, onClick = {}) }
    val disabled = actions.mapIndexed { index, action ->
        FABAction(action.id, action.label, action.icon, action.onClick, enabled = index != 1)
    }

    @Composable
    fun Example(
        direction: FABExpansionDirection,
        alignment: Alignment,
        height: Int,
        showLabels: Boolean,
        trigger: String?,
    ) {
        Box(Modifier.fillMaxWidth().height(height.dp), contentAlignment = alignment) {
            ExpandableFAB(
                expanded = true,
                onExpandedChange = {},
                actions = if (showLabels) actions else disabled,
                icon = plus,
                contentDescription = "Actions",
                expandedStateDescription = "Expanded",
                collapsedStateDescription = "Collapsed",
                direction = direction,
                showActionLabels = showLabels,
                triggerLabel = trigger,
            )
        }
    }
    Column(
        Modifier.padding(MaterialTheme.kmpSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.large),
    ) {
        Example(FABExpansionDirection.UP, Alignment.BottomEnd, height = 360, showLabels = true, trigger = null)
        Example(FABExpansionDirection.DOWN, Alignment.TopEnd, height = 260, showLabels = false, trigger = "Create")
        Example(FABExpansionDirection.START, Alignment.CenterEnd, height = 80, showLabels = false, trigger = null)
        Example(FABExpansionDirection.END, Alignment.CenterStart, height = 80, showLabels = false, trigger = null)
    }
}
