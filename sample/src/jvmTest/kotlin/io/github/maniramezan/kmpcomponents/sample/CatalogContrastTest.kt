package io.github.maniramezan.kmpcomponents.sample

import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.maniramezan.kmpcomponents.KmpTheme
import io.github.maniramezan.kmpcomponents.ThemeMode
import io.github.maniramezan.kmpcomponents.ZoomableImage
import io.github.maniramezan.kommon.designsystem.ColorToken
import io.github.maniramezan.kommon.designsystem.KommonDesignTokens
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class CatalogContrastTest {
    @Test
    fun `generated preview rows have visible boundaries across themes and text scales`() {
        val themes = listOf(
            KommonDesignTokens.default,
            KommonDesignTokens.default.copy(
                lightColors = KommonDesignTokens.default.lightColors.copy(primary = ColorToken(0xFF_00_6C_4F)),
                darkColors = KommonDesignTokens.default.darkColors.copy(primary = ColorToken(0xFF_7F_D8_B5)),
            ),
        )
        themes.forEach { tokens ->
            listOf(ThemeMode.LIGHT, ThemeMode.DARK).forEach { mode ->
                listOf(1f, 2f).forEach { scale ->
                    runComposeUiTest {
                        setContent {
                            val density = LocalDensity.current
                            CompositionLocalProvider(LocalDensity provides Density(density.density, scale)) {
                                KmpTheme(mode = mode, tokens = tokens) {
                                    val colors = MaterialTheme.colorScheme
                                    val preview = sampleScreenshot(
                                        colors.surfaceVariant,
                                        colors.primary,
                                        colors.secondaryContainer,
                                        colors.onSecondaryContainer,
                                    )
                                    ZoomableImage(preview, "Preview", Modifier.size(300.dp, 180.dp))
                                }
                            }
                        }
                        waitForIdle()
                        val image = onNodeWithContentDescription("Preview").captureToImage()
                        val pixels = image.toPixelMap()
                        val border = pixels[image.width / 30, image.height * 3 / 10]
                        val fill = pixels[image.width / 2, image.height * 3 / 10]
                        val surroundings = pixels[0, image.height * 3 / 10]
                        assertTrue(contrast(border, fill) >= 3f, "Row border on fill in $mode at $scale")
                        assertTrue(
                            contrast(border, surroundings) >= 3f,
                            "Row border on image background in $mode at $scale",
                        )
                    }
                }
            }
        }
    }
}

private fun contrast(first: Color, second: Color): Float {
    val a = first.luminance()
    val b = second.luminance()
    return (maxOf(a, b) + 0.05f) / (minOf(a, b) + 0.05f)
}
