package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.maniramezan.kommon.designsystem.ColorSchemeTokens
import io.github.maniramezan.kommon.designsystem.ColorToken
import io.github.maniramezan.kommon.designsystem.FontWeightToken
import io.github.maniramezan.kommon.designsystem.KommonDesignTokens
import io.github.maniramezan.kommon.designsystem.SpacingTokens
import io.github.maniramezan.kommon.designsystem.ThemeTokens
import io.github.maniramezan.kommon.designsystem.TypeStyleToken

public enum class ThemeMode { SYSTEM, LIGHT, DARK }

public fun ThemeMode.isDark(systemInDarkTheme: Boolean): Boolean = when (this) {
    ThemeMode.SYSTEM -> systemInDarkTheme
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}

@Immutable
public data class ComposeSpacing(
    public val none: Dp,
    public val extraSmall: Dp,
    public val small: Dp,
    public val medium: Dp,
    public val large: Dp,
    public val extraLarge: Dp,
)

public val LocalKmpSpacing: androidx.compose.runtime.ProvidableCompositionLocal<ComposeSpacing> =
    staticCompositionLocalOf { KommonDesignTokens.default.spacing.toComposeSpacing() }

public val MaterialTheme.kmpSpacing: ComposeSpacing
    @Composable get() = LocalKmpSpacing.current

@Composable
public fun KmpTheme(
    mode: ThemeMode = ThemeMode.SYSTEM,
    tokens: ThemeTokens = KommonDesignTokens.default,
    content: @Composable () -> Unit,
) {
    val dark = mode.isDark(isSystemInDarkTheme())
    androidx.compose.runtime.CompositionLocalProvider(
        LocalKmpSpacing provides tokens.spacing.toComposeSpacing(),
    ) {
        MaterialTheme(
            colorScheme = (if (dark) tokens.darkColors else tokens.lightColors).toMaterialColors(dark),
            typography = tokens.toMaterialTypography(),
            content = content,
        )
    }
}

private fun SpacingTokens.toComposeSpacing(): ComposeSpacing = ComposeSpacing(
    none.dp,
    extraSmall.dp,
    small.dp,
    medium.dp,
    large.dp,
    extraLarge.dp,
)

private fun ColorSchemeTokens.toMaterialColors(dark: Boolean): ColorScheme {
    val colors = if (dark) darkColorScheme() else lightColorScheme()
    return colors.copy(
        primary = primary.toColor(),
        onPrimary = onPrimary.toColor(),
        surface = surface.toColor(),
        onSurface = onSurface.toColor(),
        surfaceVariant = surfaceVariant.toColor(),
        outline = outline.toColor(),
        error = error.toColor(),
    )
}

private fun ThemeTokens.toMaterialTypography(): Typography = Typography(
    displaySmall = typography.display.toTextStyle(),
    titleLarge = typography.title.toTextStyle(),
    bodyMedium = typography.body.toTextStyle(),
    labelMedium = typography.label.toTextStyle(),
    bodySmall = typography.code.toTextStyle(fontFamily = FontFamily.Monospace),
)

private fun TypeStyleToken.toTextStyle(fontFamily: FontFamily? = null): TextStyle = TextStyle(
    fontSize = fontSize.sp,
    lineHeight = lineHeight.sp,
    fontWeight = when (weight) {
        FontWeightToken.NORMAL -> FontWeight.Normal
        FontWeightToken.MEDIUM -> FontWeight.Medium
        FontWeightToken.SEMI_BOLD -> FontWeight.SemiBold
        FontWeightToken.BOLD -> FontWeight.Bold
    },
    fontFamily = fontFamily,
)

private fun ColorToken.toColor(): Color = Color(argb.toULong())
