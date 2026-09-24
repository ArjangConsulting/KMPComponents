package io.github.maniramezan.kmpcomponents

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.maniramezan.kommon.designsystem.KommonDesignTokens
import io.github.maniramezan.kommon.designsystem.MotionTokens
import io.github.maniramezan.kommon.designsystem.ShapeTokens
import io.github.maniramezan.kommon.designsystem.SpacingTokens
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class TokenAdaptersTest {
    private val tokens = KommonDesignTokens.default

    @Test
    fun `kommon color roles map directly`() {
        listOf(false to tokens.lightColors, true to tokens.darkColors).forEach { (dark, colors) ->
            val scheme = colors.toMaterialColorScheme(dark)
            assertEquals(colors.primary.toComposeColor(), scheme.primary)
            assertEquals(colors.onPrimary.toComposeColor(), scheme.onPrimary)
            assertEquals(colors.surface.toComposeColor(), scheme.surface)
            assertEquals(colors.onSurface.toComposeColor(), scheme.onSurface)
            assertEquals(colors.surfaceVariant.toComposeColor(), scheme.surfaceVariant)
            assertEquals(colors.outline.toComposeColor(), scheme.outline)
            assertEquals(colors.error.toComposeColor(), scheme.error)
        }
    }

    @Test
    fun `material-only roles are derived from kommon instead of the baseline palette`() {
        val scheme = tokens.lightColors.toMaterialColorScheme(dark = false)
        val baseline = lightColorScheme()
        assertEquals(scheme.surface, scheme.background)
        assertEquals(scheme.onSurface, scheme.onBackground)
        assertEquals(scheme.primary, scheme.secondary)
        assertEquals(scheme.primary, scheme.surfaceTint)
        assertNotEquals(baseline.primaryContainer, scheme.primaryContainer)
        assertNotEquals(baseline.surfaceContainerHighest, scheme.surfaceContainerHighest)
    }

    @Test
    fun `extended colors expose success and warning`() {
        val colors = tokens.lightColors.toKmpColors()
        assertEquals(tokens.lightColors.success.toComposeColor(), colors.success)
        assertEquals(tokens.lightColors.warning.toComposeColor(), colors.warning)
    }

    @Test
    fun `typography fills material slots without making supporting text monospace`() {
        val typography = tokens.typography.toMaterialTypography()
        assertEquals(tokens.typography.body.fontSize.sp, typography.bodyLarge.fontSize)
        assertEquals(tokens.typography.title.fontSize.sp, typography.titleLarge.fontSize)
        assertEquals(tokens.typography.display.fontSize.sp, typography.displaySmall.fontSize)
        assertEquals(tokens.typography.label.fontSize.sp, typography.bodySmall.fontSize)
        assertEquals(FontWeight.Normal, typography.bodySmall.fontWeight)
        assertNotEquals(FontFamily.Monospace, typography.bodySmall.fontFamily)
    }

    @Test
    fun `code style is monospace`() {
        val code = tokens.typography.toKmpTypography().code
        assertEquals(FontFamily.Monospace, code.fontFamily)
        assertEquals(tokens.typography.code.fontSize.sp, code.fontSize)
    }

    @Test
    fun `spacing shapes and motion convert units`() {
        val spacing = SpacingTokens(extraSmall = 2f, medium = 20f).toComposeSpacing()
        assertEquals(2.dp, spacing.extraSmall)
        assertEquals(20.dp, spacing.medium)

        val shapes = ShapeTokens(smallRadius = 3f, mediumRadius = 6f, largeRadius = 9f).toMaterialShapes()
        assertEquals(androidx.compose.foundation.shape.RoundedCornerShape(6.dp), shapes.medium)

        assertEquals(KmpMotion(1, 2, 3), MotionTokens(1, 2, 3).toKmpMotion())
    }
}
