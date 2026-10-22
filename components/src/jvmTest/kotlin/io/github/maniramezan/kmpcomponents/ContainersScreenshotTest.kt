package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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

// Golden images of the containers family: cards, surfaces, feedback, state placeholders, and layout.
@OptIn(ExperimentalTestApi::class)
class ContainersScreenshotTest {
    @Test
    fun `containers in light mode`() = capture("containers-light", ThemeMode.LIGHT)

    @Test
    fun `containers in dark mode`() = capture("containers-dark", ThemeMode.DARK)

    @Test
    fun `containers at double font scale`() = capture("containers-font-200", ThemeMode.LIGHT, fontScale = 2f)

    private fun capture(name: String, mode: ThemeMode, fontScale: Float = 1f) = runDesktopComposeUiTest(
        width = WINDOW_WIDTH,
        height = WINDOW_HEIGHT,
    ) {
        setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density.density, fontScale),
                LocalSkeletonShimmerEnabled provides false,
            ) {
                KmpTheme(mode = mode) {
                    Surface(Modifier.width(GALLERY_WIDTH)) { ContainersGallery() }
                }
            }
        }
        onRoot().captureRoboImage("$SNAPSHOT_DIR/$name.png", roborazziOptions = OPTIONS)
    }

    private companion object {
        const val SNAPSHOT_DIR = "src/jvmTest/snapshots"
        const val WINDOW_WIDTH = 480
        const val WINDOW_HEIGHT = 2400
        val GALLERY_WIDTH = WINDOW_WIDTH.dp

        // Allow antialiasing noise between machines; real regressions change far more pixels.
        val OPTIONS = RoborazziOptions(compareOptions = RoborazziOptions.CompareOptions(changeThreshold = 0.01f))
    }
}

@Composable
private fun ContainersGallery() {
    val spacing = MaterialTheme.kmpSpacing
    Column(Modifier.padding(spacing.medium), verticalArrangement = Arrangement.spacedBy(spacing.small)) {
        ThemeCard(onClick = {}) {
            Text("ThemeCard", style = MaterialTheme.typography.titleMedium)
            Text("Clickable, with stacked content", style = MaterialTheme.typography.bodySmall)
        }
        ThemeSurface { Text("ThemeSurface") }
        ThemeHorizontalDivider()
        DisclosureCard(
            expanded = true,
            onExpandedChange = {},
            summary = { Text("Advanced") },
            detail = { Text("Proxy, timeouts, and retries") },
        )
        DisclosureCard(expanded = false, onExpandedChange = {}, summary = { Text("Collapsed") }, detail = {})
        Box(Modifier.fillMaxWidth().height(OVERLAY_DEMO_HEIGHT).background(MaterialTheme.colorScheme.tertiary)) {
            OverlayCard(Modifier.align(Alignment.BottomStart).padding(spacing.small)) { Text("OverlayCard") }
        }
        Toast(
            message = "Copied to clipboard",
            icon = rememberVectorPainter(
                TestPlusIcon,
            ),
            actionLabel = "Undo",
            onAction = {
            },
        )
        ThemeSnackbar(message = "Endpoint deleted", actionLabel = "Undo", onAction = {})
        FlowLayout {
            listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD").forEach { TonalBadge(it) }
        }
        Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(spacing.small)) {
            SkeletonBlock(
                height = SKELETON_ROW_HEIGHT,
                width = SKELETON_ROW_HEIGHT,
                shape = MaterialTheme.shapes.medium,
            )
            ThemeVerticalDivider()
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing.small)) {
                Skeleton()
                Skeleton(Modifier.width(SKELETON_SHORT_WIDTH))
            }
        }
        LoadingState(label = "Loading reports")
        ErrorState(
            title = "Could not load reports",
            message = "The server did not respond.",
            action = { ThemeTextButton(text = "Retry", onClick = {}) },
        )
    }
}

private val OVERLAY_DEMO_HEIGHT = 96.dp
private val SKELETON_ROW_HEIGHT = 56.dp
private val SKELETON_SHORT_WIDTH = 160.dp
