package io.github.maniramezan.kmpcomponents.sample

import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.Taskbar
import javax.imageio.ImageIO

fun main() {
    val image = requireNotNull(Thread.currentThread().contextClassLoader.getResourceAsStream("app-icon.png")) {
        "Missing sample application icon"
    }.use { ImageIO.read(it) }
    val icon = BitmapPainter(image.toComposeImageBitmap())
    if (Taskbar.isTaskbarSupported()) {
        val taskbar = Taskbar.getTaskbar()
        if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) taskbar.iconImage = image
    }
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "KMPComponents catalog",
            icon = icon,
            state = rememberWindowState(width = 560.dp, height = 900.dp),
        ) {
            ComponentCatalog()
        }
    }
}
