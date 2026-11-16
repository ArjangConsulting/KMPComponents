package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
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

// Golden images of ThumbsFeedbackRow in each rating, and disabled.
@OptIn(ExperimentalTestApi::class)
class ThumbsFeedbackRowScreenshotTest {
    @Test
    fun `thumbs feedback row in light mode`() = capture("thumbs-feedback-light", ThemeMode.LIGHT)

    @Test
    fun `thumbs feedback row in dark mode`() = capture("thumbs-feedback-dark", ThemeMode.DARK)

    @Test
    fun `thumbs feedback row at double font scale`() = capture(
        "thumbs-feedback-font-200",
        ThemeMode.LIGHT,
        fontScale = 2f,
    )

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
                    Surface(Modifier.width(GALLERY_WIDTH)) { ThumbsFeedbackGallery() }
                }
            }
        }
        onRoot().captureRoboImage("$SNAPSHOT_DIR/$name.png", roborazziOptions = OPTIONS)
    }

    private companion object {
        const val SNAPSHOT_DIR = "src/jvmTest/snapshots"
        const val WINDOW_WIDTH = 480
        const val WINDOW_HEIGHT = 400
        val GALLERY_WIDTH = WINDOW_WIDTH.dp

        // Allow antialiasing noise between machines; real regressions change far more pixels.
        val OPTIONS = RoborazziOptions(compareOptions = RoborazziOptions.CompareOptions(changeThreshold = 0.01f))
    }
}

@Composable
private fun ThumbsFeedbackGallery() {
    Column(
        Modifier.padding(MaterialTheme.kmpSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small),
    ) {
        ThumbsRating.entries.forEach { rating ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(rating.name, Modifier.width(LABEL_WIDTH))
                ThumbsFeedbackRow(rating, {}, "Good response", "Bad response")
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Disabled", Modifier.width(LABEL_WIDTH))
            ThumbsFeedbackRow(ThumbsRating.POSITIVE, {}, "Good response", "Bad response", enabled = false)
        }
    }
}

private val LABEL_WIDTH = 200.dp
