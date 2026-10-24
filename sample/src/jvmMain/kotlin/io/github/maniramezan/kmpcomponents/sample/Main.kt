package io.github.maniramezan.kmpcomponents.sample

import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.Taskbar
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

fun main() {
    System.setProperty("apple.awt.application.name", "KMPComponents")
    val image = requireNotNull(Thread.currentThread().contextClassLoader.getResourceAsStream("app-icon.png")) {
        "Missing sample application icon"
    }.use { ImageIO.read(it) }
    val icon = BitmapPainter(image.toComposeImageBitmap())
    setApplicationIcon(image)
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "KMPComponents catalog",
            icon = icon,
            state = rememberWindowState(width = 560.dp, height = 900.dp),
        ) {
            DisposableEffect(window) {
                val listener = object : WindowAdapter() {
                    override fun windowOpened(event: WindowEvent) {
                        setApplicationIcon(image)
                    }
                }
                window.addWindowListener(listener)
                if (window.isShowing) setApplicationIcon(image)
                onDispose { window.removeWindowListener(listener) }
            }
            ComponentCatalog()
        }
    }
}

private fun setApplicationIcon(image: BufferedImage) {
    if (Taskbar.isTaskbarSupported()) {
        val taskbar = Taskbar.getTaskbar()
        if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) taskbar.iconImage = image
    }
}
