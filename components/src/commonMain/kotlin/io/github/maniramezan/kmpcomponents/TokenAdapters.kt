package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.maniramezan.kommon.designsystem.ColorSchemeTokens
import io.github.maniramezan.kommon.designsystem.ColorToken
import io.github.maniramezan.kommon.designsystem.FontWeightToken
import io.github.maniramezan.kommon.designsystem.MotionTokens
import io.github.maniramezan.kommon.designsystem.ShapeTokens
import io.github.maniramezan.kommon.designsystem.SpacingTokens
import io.github.maniramezan.kommon.designsystem.TypeStyleToken
import io.github.maniramezan.kommon.designsystem.TypographyTokens

// Pure token -> Compose conversions. Kept free of @Composable so they can be unit tested directly.

internal fun ColorToken.toComposeColor(): Color = Color(argb.toInt())

// Blend fractions used to derive Material roles that kommon has no token for.
private const val CONTAINER_TINT = 0.16f
private const val ON_SURFACE_VARIANT_BLEND = 0.25f
private const val OUTLINE_VARIANT_BLEND = 0.5f
private const val INVERSE_PRIMARY_BLEND = 0.6f
private const val SURFACE_CONTAINER_LOW = 0.03f
private const val SURFACE_CONTAINER = 0.05f
private const val SURFACE_CONTAINER_HIGH = 0.08f
private const val SURFACE_CONTAINER_HIGHEST = 0.11f
private const val SURFACE_DIM = 0.13f
private const val SURFACE_BRIGHT = 0.16f

/**
 * Maps kommon's semantic roles onto a full Material 3 [ColorScheme].
 *
 * kommon defines nine roles; Material defines ~35. Every role Material components read is set here
 * from kommon values (directly or by blending two of them), so a product that re-brands the tokens
 * never sees Material's baseline purple in secondary/tertiary/container slots.
 */
internal fun ColorSchemeTokens.toMaterialColorScheme(dark: Boolean): ColorScheme {
    val primary = primary.toComposeColor()
    val onPrimary = onPrimary.toComposeColor()
    val surface = surface.toComposeColor()
    val onSurface = onSurface.toComposeColor()
    val surfaceVariant = surfaceVariant.toComposeColor()
    val outline = outline.toComposeColor()
    val error = error.toComposeColor()

    val primaryContainer = lerp(surface, primary, CONTAINER_TINT)
    fun surfaceBlend(fraction: Float) = lerp(surface, onSurface, fraction)

    val base = if (dark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onSurface,
        inversePrimary = lerp(primary, surface, INVERSE_PRIMARY_BLEND),
        secondary = primary,
        onSecondary = onPrimary,
        secondaryContainer = primaryContainer,
        onSecondaryContainer = onSurface,
        tertiary = primary,
        onTertiary = onPrimary,
        tertiaryContainer = primaryContainer,
        onTertiaryContainer = onSurface,
        background = surface,
        onBackground = onSurface,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = lerp(onSurface, surfaceVariant, ON_SURFACE_VARIANT_BLEND),
        surfaceTint = primary,
        inverseSurface = onSurface,
        inverseOnSurface = surface,
        error = error,
        // kommon accents are dark on light surfaces and light on dark ones, so the surface color
        // is the high-contrast "on" color for them.
        onError = surface,
        errorContainer = lerp(surface, error, CONTAINER_TINT),
        onErrorContainer = onSurface,
        outline = outline,
        outlineVariant = lerp(outline, surface, OUTLINE_VARIANT_BLEND),
        surfaceBright = if (dark) surfaceBlend(SURFACE_BRIGHT) else surface,
        surfaceDim = if (dark) surface else surfaceBlend(SURFACE_DIM),
        surfaceContainerLowest = surface,
        surfaceContainerLow = surfaceBlend(SURFACE_CONTAINER_LOW),
        surfaceContainer = surfaceBlend(SURFACE_CONTAINER),
        surfaceContainerHigh = surfaceBlend(SURFACE_CONTAINER_HIGH),
        surfaceContainerHighest = surfaceBlend(SURFACE_CONTAINER_HIGHEST),
    )
}

internal fun ColorSchemeTokens.toKmpColors(): KmpColors = KmpColors(
    success = success.toComposeColor(),
    warning = warning.toComposeColor(),
)

/**
 * Expands kommon's five type roles to the full Material scale.
 *
 * - display* <- display, headline* and titleLarge <- title
 * - titleMedium/titleSmall and labelLarge <- body size with the title/label weight
 * - body* <- body (bodySmall uses the label size at normal weight)
 * - labelMedium/labelSmall <- label
 *
 * `code` is deliberately *not* mapped to a Material slot: Material components read bodySmall for
 * supporting text, which must not render in monospace. Use `MaterialTheme.kmpTypography.code`.
 */
internal fun TypographyTokens.toMaterialTypography(): Typography {
    val displayStyle = display.toTextStyle()
    val titleStyle = title.toTextStyle()
    val bodyStyle = body.toTextStyle()
    val labelStyle = label.toTextStyle()
    val emphasizedBody = body.copy(weight = title.weight).toTextStyle()
    return Typography(
        displayLarge = displayStyle,
        displayMedium = displayStyle,
        displaySmall = displayStyle,
        headlineLarge = titleStyle,
        headlineMedium = titleStyle,
        headlineSmall = titleStyle,
        titleLarge = titleStyle,
        titleMedium = emphasizedBody,
        titleSmall = emphasizedBody,
        bodyLarge = bodyStyle,
        bodyMedium = bodyStyle,
        bodySmall = label.copy(weight = FontWeightToken.NORMAL).toTextStyle(),
        labelLarge = body.copy(weight = label.weight).toTextStyle(),
        labelMedium = labelStyle,
        labelSmall = labelStyle,
    )
}

internal fun TypographyTokens.toKmpTypography(): KmpTypography =
    KmpTypography(code = code.toTextStyle(fontFamily = FontFamily.Monospace))

internal fun TypeStyleToken.toTextStyle(fontFamily: FontFamily? = null): TextStyle = TextStyle(
    fontSize = fontSize.sp,
    lineHeight = lineHeight.sp,
    fontWeight = weight.toFontWeight(),
    fontFamily = fontFamily,
)

internal fun FontWeightToken.toFontWeight(): FontWeight = when (this) {
    FontWeightToken.NORMAL -> FontWeight.Normal
    FontWeightToken.MEDIUM -> FontWeight.Medium
    FontWeightToken.SEMI_BOLD -> FontWeight.SemiBold
    FontWeightToken.BOLD -> FontWeight.Bold
}

/** kommon has three radii; Material's extra-small and extra-large reuse the nearest one. */
internal fun ShapeTokens.toMaterialShapes(): Shapes = Shapes(
    extraSmall = RoundedCornerShape(smallRadius.dp),
    small = RoundedCornerShape(smallRadius.dp),
    medium = RoundedCornerShape(mediumRadius.dp),
    large = RoundedCornerShape(largeRadius.dp),
    extraLarge = RoundedCornerShape(largeRadius.dp),
)

internal fun SpacingTokens.toComposeSpacing(): ComposeSpacing = ComposeSpacing(
    none = none.dp,
    extraSmall = extraSmall.dp,
    small = small.dp,
    medium = medium.dp,
    large = large.dp,
    extraLarge = extraLarge.dp,
)

internal fun MotionTokens.toKmpMotion(): KmpMotion = KmpMotion(
    fastMillis = fastMilliseconds,
    standardMillis = standardMilliseconds,
    deliberateMillis = deliberateMilliseconds,
)
