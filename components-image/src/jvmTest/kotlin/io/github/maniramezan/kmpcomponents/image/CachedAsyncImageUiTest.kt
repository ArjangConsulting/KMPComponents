package io.github.maniramezan.kmpcomponents.image

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import com.sun.net.httpserver.HttpServer
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.net.InetSocketAddress
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals

// Desktop-only because runComposeUiTest needs a Skiko runtime.
@OptIn(ExperimentalTestApi::class)
class CachedAsyncImageUiTest {
    @Test
    fun `missing data shows the fallback and keeps the description`() = runComposeUiTest {
        setContent {
            CachedAsyncImage(
                data = null,
                contentDescription = "Avatar",
                modifier = Modifier.size(48.dp),
                fallback = ColorPainter(Color.Gray),
            )
        }
        onNodeWithContentDescription("Avatar").assertIsDisplayed()
        assertEquals(Color.Gray, onNodeWithContentDescription("Avatar").captureToImage().toPixelMap()[24, 24])
    }

    @Test
    fun `network image loads and a stable memory key reuses it for another URL`() {
        ImageServer().use { server ->
            runComposeUiTest {
                var showSecond by mutableStateOf(false)
                val cacheKey = UUID.randomUUID().toString()
                setContent {
                    Column {
                        CachedAsyncImage(
                            data = "${server.url}/first.png",
                            contentDescription = "First",
                            modifier = Modifier.size(48.dp),
                            memoryCacheKey = cacheKey,
                            diskCachePolicy = ImageCachePolicy.DISABLED,
                            crossfade = false,
                        )
                        if (showSecond) {
                            CachedAsyncImage(
                                data = "${server.url}/second.png",
                                contentDescription = "Second",
                                modifier = Modifier.size(48.dp),
                                memoryCacheKey = cacheKey,
                                diskCachePolicy = ImageCachePolicy.DISABLED,
                                crossfade = false,
                            )
                        }
                    }
                }
                waitUntil(timeoutMillis = 10_000L) {
                    onNodeWithContentDescription("First").captureToImage().toPixelMap()[24, 24] == Color.Red
                }
                runOnIdle { showSecond = true }
                waitUntil(timeoutMillis = 10_000L) {
                    onNodeWithContentDescription("Second").captureToImage().toPixelMap()[24, 24] == Color.Red
                }
                assertEquals(1, server.requests.get())
            }
        }
    }

    @Test
    fun `disabled network reads still allow a disk cache hit`() {
        ImageServer().use { server ->
            runComposeUiTest {
                var showSecond by mutableStateOf(false)
                val cacheKey = UUID.randomUUID().toString()
                setContent {
                    Column {
                        CachedAsyncImage(
                            data = "${server.url}/first.png",
                            contentDescription = "First",
                            modifier = Modifier.size(48.dp),
                            memoryCachePolicy = ImageCachePolicy.DISABLED,
                            diskCacheKey = cacheKey,
                            crossfade = false,
                        )
                        if (showSecond) {
                            CachedAsyncImage(
                                data = "${server.url}/second.png",
                                contentDescription = "Second",
                                modifier = Modifier.size(48.dp),
                                memoryCachePolicy = ImageCachePolicy.DISABLED,
                                diskCachePolicy = ImageCachePolicy.READ_ONLY,
                                networkCachePolicy = ImageCachePolicy.DISABLED,
                                diskCacheKey = cacheKey,
                                crossfade = false,
                            )
                        }
                    }
                }
                waitUntil(timeoutMillis = 10_000L) {
                    onNodeWithContentDescription("First").captureToImage().toPixelMap()[24, 24] == Color.Red
                }
                runOnIdle { showSecond = true }
                waitUntil(timeoutMillis = 10_000L) {
                    onNodeWithContentDescription("Second").captureToImage().toPixelMap()[24, 24] == Color.Red
                }
                assertEquals(1, server.requests.get())
            }
        }
    }

    @Test
    fun `disabled network reads show the error without contacting the server`() {
        ImageServer().use { server ->
            runComposeUiTest {
                setContent {
                    CachedAsyncImage(
                        data = "${server.url}/offline.png",
                        contentDescription = "Offline",
                        modifier = Modifier.size(48.dp),
                        memoryCachePolicy = ImageCachePolicy.DISABLED,
                        diskCachePolicy = ImageCachePolicy.DISABLED,
                        networkCachePolicy = ImageCachePolicy.DISABLED,
                        crossfade = false,
                        placeholder = ColorPainter(Color.Blue),
                        error = ColorPainter(Color.Green),
                    )
                }
                waitUntil(timeoutMillis = 10_000L) {
                    val color = onNodeWithContentDescription("Offline").captureToImage().toPixelMap()[24, 24]
                    color == Color.Red || color == Color.Green
                }
                assertEquals(Color.Green, onNodeWithContentDescription("Offline").captureToImage().toPixelMap()[24, 24])
                assertEquals(0, server.requests.get())
            }
        }
    }
}

private class ImageServer : AutoCloseable {
    val requests = AtomicInteger()
    private val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
    val url: String get() = "http://127.0.0.1:${server.address.port}"

    init {
        val pixels = BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB)
        repeat(2) { x -> repeat(2) { y -> pixels.setRGB(x, y, java.awt.Color.RED.rgb) } }
        val bytes = ByteArrayOutputStream().apply { ImageIO.write(pixels, "png", this) }.toByteArray()
        server.createContext("/") { exchange ->
            requests.incrementAndGet()
            exchange.responseHeaders.add("Content-Type", "image/png")
            exchange.sendResponseHeaders(200, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
            exchange.close()
        }
        server.start()
    }

    override fun close() {
        server.stop(0)
    }
}
