package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.RoborazziOptions
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

// Golden images of AsyncContent's four presentations.
@OptIn(ExperimentalTestApi::class)
class AsyncContentScreenshotTest {
    @Test
    fun `async content in light mode`() = capture("async-content-light", ThemeMode.LIGHT)

    @Test
    fun `async content in dark mode`() = capture("async-content-dark", ThemeMode.DARK)

    @Test
    fun `async content at double font scale`() = capture("async-content-font-200", ThemeMode.LIGHT, fontScale = 2f)

    private fun capture(name: String, mode: ThemeMode, fontScale: Float = 1f) = runDesktopComposeUiTest(
        width = WINDOW_WIDTH,
        height = WINDOW_HEIGHT,
    ) {
        setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density.density, fontScale),
                LocalReduceMotionEnabled provides true,
            ) {
                KmpTheme(mode = mode) {
                    Surface(Modifier.width(GALLERY_WIDTH)) { AsyncContentGallery() }
                }
            }
        }
        onRoot().captureRoboImage("$SNAPSHOT_DIR/$name.png", roborazziOptions = OPTIONS)
    }

    private companion object {
        const val SNAPSHOT_DIR = "src/jvmTest/snapshots"
        const val WINDOW_WIDTH = 480
        const val WINDOW_HEIGHT = 1400
        val GALLERY_WIDTH = WINDOW_WIDTH.dp

        // Allow antialiasing noise between machines; real regressions change far more pixels.
        val OPTIONS = RoborazziOptions(compareOptions = RoborazziOptions.CompareOptions(changeThreshold = 0.01f))
    }
}

@Composable
private fun AsyncContentGallery() {
    val states = listOf(
        AsyncContentState.Loading,
        AsyncContentState.Empty,
        AsyncContentState.Error(),
        AsyncContentState.Content("Quarterly report"),
    )
    Column(
        Modifier.padding(MaterialTheme.kmpSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small),
    ) {
        states.forEach { state ->
            SectionCard(title = state::class.simpleName.orEmpty()) {
                AsyncContent(
                    state = state,
                    loadingLabel = "Loading reports",
                    emptyTitle = "No reports yet",
                    errorTitle = "Could not load reports",
                    errorMessage = "The server did not respond.",
                    emptyMessage = "Reports you create appear here.",
                    emptyAction = { ThemeTextButton(text = "Create report", onClick = {}) },
                    errorAction = { ThemeTextButton(text = "Retry", onClick = {}) },
                ) { Text(it) }
            }
        }
    }
}
