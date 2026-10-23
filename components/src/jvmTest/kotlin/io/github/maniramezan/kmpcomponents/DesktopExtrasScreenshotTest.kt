package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.RoborazziOptions
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

// Golden images of the desktop extras: status bar, copy button, zoomable image, vertical split pane.
@OptIn(ExperimentalTestApi::class)
class DesktopExtrasScreenshotTest {
    @Test
    fun `desktop extras in light mode`() = capture("desktop-light", ThemeMode.LIGHT)

    @Test
    fun `desktop extras in dark mode`() = capture("desktop-dark", ThemeMode.DARK)

    @Test
    fun `desktop extras at double font scale`() = capture("desktop-font-200", ThemeMode.LIGHT, fontScale = 2f)

    private fun capture(name: String, mode: ThemeMode, fontScale: Float = 1f) = runDesktopComposeUiTest(
        width = 480,
        height = 1000,
    ) {
        setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                KmpTheme(mode = mode) { Surface(Modifier.width(480.dp)) { DesktopGallery() } }
            }
        }
        onRoot().captureRoboImage("src/jvmTest/snapshots/$name.png", roborazziOptions = OPTIONS)
    }

    private companion object {
        // Allow antialiasing noise between machines; real regressions change far more pixels.
        val OPTIONS = RoborazziOptions(compareOptions = RoborazziOptions.CompareOptions(changeThreshold = 0.01f))
    }
}

@Composable
private fun DesktopGallery() {
    val spacing = MaterialTheme.kmpSpacing
    Column(verticalArrangement = Arrangement.spacedBy(spacing.small)) {
        Row(
            Modifier.padding(horizontal = spacing.medium),
            horizontalArrangement = Arrangement.spacedBy(spacing.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            KeyValueRow(key = "Base URL", value = "https://api.example.com", modifier = Modifier.weight(1f))
            CopyButton(text = "https://api.example.com")
        }
        ZoomableImage(
            image = checkerboard(),
            contentDescription = "Screenshot",
            modifier = Modifier.fillMaxWidth().height(160.dp).padding(horizontal = spacing.medium),
            marker = Offset(60f, 30f),
        )
        VerticalSplitPane(
            split = 0.4f,
            onSplitChange = {},
            modifier = Modifier.fillMaxWidth().height(200.dp),
            top = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Editor") } },
            bottom = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Console") } },
        )
        StatusBar(
            start = { StatusBarItem("AI: Ollama") },
            end = {
                StatusBarItem("v1.2.0")
                StatusBarSeparator()
                StatusBarItem("12 endpoints")
                StatusBarSeparator()
                StatusBarItem("Unsaved changes", color = MaterialTheme.colorScheme.error)
            },
        )
    }
}

/** A small two-tone image so the zoomed screenshot has visible content. */
private fun checkerboard(): ImageBitmap {
    val image = ImageBitmap(120, 60)
    val canvas = Canvas(image)
    val light = Paint().apply { color = Color(0xFFDDE3EA) }
    val dark = Paint().apply { color = Color(0xFF8A99AA) }
    for (x in 0 until 120 step 20) {
        for (y in 0 until 60 step 20) {
            canvas.drawRect(x.toFloat(), y.toFloat(), x + 20f, y + 20f, if ((x + y) / 20 % 2 == 0) light else dark)
        }
    }
    return image
}
