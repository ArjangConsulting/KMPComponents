package io.github.maniramezan.kmpcomponents.image

import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlin.test.Test

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
    }
}
