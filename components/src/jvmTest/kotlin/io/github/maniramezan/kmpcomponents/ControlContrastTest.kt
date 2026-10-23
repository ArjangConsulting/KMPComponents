package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.maniramezan.kommon.designsystem.ColorToken
import io.github.maniramezan.kommon.designsystem.KommonDesignTokens
import io.github.maniramezan.kommon.designsystem.ThemeTokens
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class ControlContrastTest {
    @Test
    fun `rendered enabled boundaries contrast on page and card surfaces before and after focus`() {
        val themes = listOf(
            KommonDesignTokens.default,
            KommonDesignTokens.default.copy(
                lightColors = KommonDesignTokens.default.lightColors.copy(primary = ColorToken(0xFF_00_6C_4F)),
                darkColors = KommonDesignTokens.default.darkColors.copy(primary = ColorToken(0xFF_7F_D8_B5)),
            ),
        )
        themes.forEach { tokens ->
            listOf(ThemeMode.LIGHT, ThemeMode.DARK).forEach { mode ->
                listOf(1f, 2f).forEach { fontScale ->
                    listOf(false, true).forEach { cardSurface ->
                        Boundary.entries.forEach { boundary ->
                            verifyContrast(boundary, tokens, mode, fontScale, cardSurface)
                        }
                    }
                }
            }
        }
    }

    private fun verifyContrast(
        boundary: Boundary,
        tokens: ThemeTokens,
        mode: ThemeMode,
        fontScale: Float,
        cardSurface: Boolean,
    ) = runComposeUiTest {
        var background = Color.Unspecified
        setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                KmpTheme(mode = mode, tokens = tokens) {
                    background = if (cardSurface) {
                        MaterialTheme.colorScheme.surfaceVariant
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                    Surface(color = background) {
                        when (boundary) {
                            Boundary.BUTTON -> SecondaryButton("Boundary", {})

                            Boundary.HORIZONTAL_SPLIT -> HorizontalSplitPane(
                                0.5f,
                                {},
                                Modifier.size(300.dp),
                                leading = {},
                                trailing = {},
                            )

                            Boundary.VERTICAL_SPLIT -> VerticalSplitPane(
                                0.5f,
                                {},
                                Modifier.size(300.dp),
                                top = {},
                                bottom = {},
                            )
                        }
                    }
                }
            }
        }
        val node = if (boundary == Boundary.BUTTON) {
            onNodeWithText("Boundary")
        } else {
            onNodeWithContentDescription("Resize panes")
        }
        val normal = boundaryContrast(node.captureToImage(), background, boundary)
        assertTrue(normal >= 3f, "$boundary $mode $fontScale card=$cardSurface normal contrast=$normal")
        node.requestFocus()
        val focused = boundaryContrast(node.captureToImage(), background, boundary)
        assertTrue(focused >= 3f, "$boundary $mode $fontScale card=$cardSurface focused contrast=$focused")
    }
}

private enum class Boundary { BUTTON, HORIZONTAL_SPLIT, VERTICAL_SPLIT }

private fun boundaryContrast(image: ImageBitmap, background: Color, boundary: Boundary): Float {
    val pixels = image.toPixelMap()
    val colors = when (boundary) {
        Boundary.BUTTON -> (0 until minOf(3, image.height)).map { pixels[image.width / 2, it] }
        Boundary.HORIZONTAL_SPLIT -> (0 until image.width).map { pixels[it, image.height / 2] }
        Boundary.VERTICAL_SPLIT -> (0 until image.height).map { pixels[image.width / 2, it] }
    }
    return colors.maxOf { color ->
        val first = color.luminance()
        val second = background.luminance()
        (maxOf(first, second) + 0.05f) / (minOf(first, second) + 0.05f)
    }
}
