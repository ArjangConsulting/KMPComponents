package io.github.maniramezan.kmpcomponents.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.unit.dp
import io.github.maniramezan.kmpcomponents.CopyButton
import io.github.maniramezan.kmpcomponents.KeyValueRow
import io.github.maniramezan.kmpcomponents.SectionCard
import io.github.maniramezan.kmpcomponents.StatusBar
import io.github.maniramezan.kmpcomponents.StatusBarItem
import io.github.maniramezan.kmpcomponents.StatusBarSeparator
import io.github.maniramezan.kmpcomponents.ThemeSwitch
import io.github.maniramezan.kmpcomponents.VerticalSplitPane
import io.github.maniramezan.kmpcomponents.ZoomableImage
import io.github.maniramezan.kmpcomponents.focusNextOnTab

/** Tab traversal, copy, zoomable images, the vertical split pane, and the status bar. */
@Composable
fun DesktopSection() {
    var notes by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var split by remember { mutableFloatStateOf(DEFAULT_SPLIT) }
    var dirty by remember { mutableStateOf(true) }
    val image = remember { sampleScreenshot() }
    SectionCard(
        title = "Desktop helpers",
        description = "Tab out of the multi-line notes field; zoom the image with the wheel or +/-.",
    ) {
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            modifier = Modifier.fillMaxWidth().focusNextOnTab(),
            label = { Text("Notes (Tab moves on)") },
            minLines = 2,
        )
        OutlinedTextField(value = title, onValueChange = {
            title = it
        }, modifier = Modifier.fillMaxWidth(), label = { Text("Title") })
        Row(verticalAlignment = Alignment.CenterVertically) {
            KeyValueRow(key = "Base URL", value = BASE_URL, modifier = Modifier.weight(1f))
            CopyButton(text = BASE_URL)
        }
        ZoomableImage(
            image = image,
            contentDescription = "Device screenshot",
            modifier = Modifier.fillMaxWidth().height(IMAGE_HEIGHT),
            marker = Offset(MARKER_X, MARKER_Y),
        )
        VerticalSplitPane(
            split = split,
            onSplitChange = { split = it },
            modifier = Modifier.fillMaxWidth().height(SPLIT_HEIGHT),
            top = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Editor") } },
            bottom = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Console") } },
        )
        ThemeSwitch(checked = dirty, onCheckedChange = { dirty = it }, label = "Unsaved changes")
        StatusBar(
            start = { StatusBarItem("AI: Ollama") },
            end = {
                StatusBarItem("v1.2.0")
                StatusBarSeparator()
                StatusBarItem("12 endpoints")
                StatusBarSeparator()
                StatusBarItem(
                    text = if (dirty) "Unsaved changes" else "Saved",
                    color = if (dirty) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
        )
    }
}

private const val BASE_URL = "https://api.example.com"
private const val DEFAULT_SPLIT = 0.6f
private const val MARKER_X = 150f
private const val MARKER_Y = 90f
private val IMAGE_HEIGHT = 200.dp
private val SPLIT_HEIGHT = 220.dp

/** A drawn stand-in for a screenshot, so the sample needs no image resources. */
private fun sampleScreenshot(): ImageBitmap {
    val image = ImageBitmap(SCREEN_WIDTH, SCREEN_HEIGHT)
    val canvas = Canvas(image)
    val background = Paint().apply { color = Color(0xFFE8ECF2) }
    val bar = Paint().apply { color = Color(0xFF3F51B5) }
    val row = Paint().apply { color = Color(0xFFB8C2D0) }
    canvas.drawRect(0f, 0f, SCREEN_WIDTH.toFloat(), SCREEN_HEIGHT.toFloat(), background)
    canvas.drawRect(0f, 0f, SCREEN_WIDTH.toFloat(), BAR_HEIGHT, bar)
    repeat(ROWS) { index ->
        val top = BAR_HEIGHT + ROW_GAP + index * (ROW_HEIGHT + ROW_GAP)
        canvas.drawRect(ROW_GAP, top, SCREEN_WIDTH - ROW_GAP, top + ROW_HEIGHT, row)
    }
    return image
}

private const val SCREEN_WIDTH = 300
private const val SCREEN_HEIGHT = 180
private const val BAR_HEIGHT = 30f
private const val ROWS = 4
private const val ROW_HEIGHT = 26f
private const val ROW_GAP = 10f
