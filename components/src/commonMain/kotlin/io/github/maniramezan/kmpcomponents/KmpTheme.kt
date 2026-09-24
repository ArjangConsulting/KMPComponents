package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import io.github.maniramezan.kommon.designsystem.KommonDesignTokens
import io.github.maniramezan.kommon.designsystem.ThemeTokens

/**
 * Applies [tokens] as a Material 3 theme plus the kommon-only values that Material has no slot for
 * ([kmpSpacing], [kmpColors], [kmpMotion], [kmpTypography]).
 *
 * Material color roles that kommon does not define are derived from the nearest kommon role, so
 * the Material baseline palette never leaks into rendered components. See `TokenAdapters.kt`.
 */
@Composable
public fun KmpTheme(
    mode: ThemeMode = ThemeMode.SYSTEM,
    tokens: ThemeTokens = KommonDesignTokens.default,
    content: @Composable () -> Unit,
) {
    val dark = mode.isDark(isSystemInDarkTheme())
    val colorTokens = if (dark) tokens.darkColors else tokens.lightColors
    val colorScheme = remember(colorTokens, dark) { colorTokens.toMaterialColorScheme(dark) }
    val kmpColors = remember(colorTokens) { colorTokens.toKmpColors() }
    val typography = remember(tokens.typography) { tokens.typography.toMaterialTypography() }
    val kmpTypography = remember(tokens.typography) { tokens.typography.toKmpTypography() }
    val shapes = remember(tokens.shapes) { tokens.shapes.toMaterialShapes() }
    val spacing = remember(tokens.spacing) { tokens.spacing.toComposeSpacing() }
    val motion = remember(tokens.motion) { tokens.motion.toKmpMotion() }

    CompositionLocalProvider(
        LocalKmpSpacing provides spacing,
        LocalKmpColors provides kmpColors,
        LocalKmpTypography provides kmpTypography,
        LocalKmpMotion provides motion,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            shapes = shapes,
            typography = typography,
            content = content,
        )
    }
}

/** kommon spacing scale converted to [Dp]. Read it with `MaterialTheme.kmpSpacing`. */
@Immutable
public data class ComposeSpacing(
    public val none: Dp,
    public val extraSmall: Dp,
    public val small: Dp,
    public val medium: Dp,
    public val large: Dp,
    public val extraLarge: Dp,
)

/** Semantic colors kommon defines that have no Material 3 role. */
@Immutable
public data class KmpColors(public val success: Color, public val warning: Color)

/** Text styles kommon defines that have no Material 3 role. */
@Immutable
public data class KmpTypography(
    /** Monospace style for code, identifiers, and other literal values. */
    public val code: TextStyle,
)

/** Animation durations in milliseconds, suitable for `tween(durationMillis = ...)`. */
@Immutable
public data class KmpMotion(
    public val fastMillis: Int,
    public val standardMillis: Int,
    public val deliberateMillis: Int,
)

public val LocalKmpSpacing: ProvidableCompositionLocal<ComposeSpacing> =
    staticCompositionLocalOf { KommonDesignTokens.default.spacing.toComposeSpacing() }

public val LocalKmpColors: ProvidableCompositionLocal<KmpColors> =
    staticCompositionLocalOf { KommonDesignTokens.default.lightColors.toKmpColors() }

public val LocalKmpTypography: ProvidableCompositionLocal<KmpTypography> =
    staticCompositionLocalOf { KommonDesignTokens.default.typography.toKmpTypography() }

public val LocalKmpMotion: ProvidableCompositionLocal<KmpMotion> =
    staticCompositionLocalOf { KommonDesignTokens.default.motion.toKmpMotion() }

public val MaterialTheme.kmpSpacing: ComposeSpacing
    @Composable @ReadOnlyComposable
    get() = LocalKmpSpacing.current

public val MaterialTheme.kmpColors: KmpColors
    @Composable @ReadOnlyComposable
    get() = LocalKmpColors.current

public val MaterialTheme.kmpTypography: KmpTypography
    @Composable @ReadOnlyComposable
    get() = LocalKmpTypography.current

public val MaterialTheme.kmpMotion: KmpMotion
    @Composable @ReadOnlyComposable
    get() = LocalKmpMotion.current
