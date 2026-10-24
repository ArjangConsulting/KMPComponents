package io.github.maniramezan.kmpcomponents.image

import androidx.compose.foundation.layout.size
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.io.encoding.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

// Runs on every Skiko target (desktop, iOS simulator, and web) to show that Coil decodes and draws
// without a network engine. Network loading is covered on desktop by CachedAsyncImageUiTest.
@OptIn(ExperimentalTestApi::class)
class CachedAsyncImageRenderTest {
    @Test
    fun embeddedBytesDecodeAndDraw() = runComposeUiTest {
        setContent {
            CachedAsyncImage(
                data = Base64.decode(RED_PNG_BASE64),
                contentDescription = "Red square",
                modifier = Modifier.size(48.dp),
                crossfade = false,
            )
        }
        // Coil decodes off the test clock; on the web that work only runs when the event loop gets a
        // turn, so yield real time between checks instead of busy-waiting.
        val node = onNodeWithContentDescription("Red square")
        repeat(200) {
            awaitIdle()
            if (node.captureToImage().toPixelMap()[24, 24] == Color.Red) return@runComposeUiTest
            withContext(Dispatchers.Default) { delay(50) }
        }
        fail("The embedded image was not drawn within 10 seconds")
    }

    @Test
    fun missingDataShowsTheFallbackAndKeepsTheDescription() = runComposeUiTest {
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

    private companion object {
        // A 4x4 opaque red PNG.
        const val RED_PNG_BASE64 =
            "iVBORw0KGgoAAAANSUhEUgAAAAQAAAAECAIAAAAmkwkpAAAAEElEQVR4nGP4z8AARwzEcQCukw/x0F8jngAAAABJRU5ErkJggg=="
    }
}
