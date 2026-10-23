package io.github.maniramezan.kmpcomponents

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalInspectionMode

/**
 * Whether the user asked the system to reduce motion (iOS Reduce Motion, Android animator duration
 * scale 0). Decorative animations such as the skeleton shimmer stay still when it is on.
 */
@Composable
internal expect fun isReduceMotionEnabled(): Boolean

/**
 * Overrides the system's reduced-motion preference for decorative looping animations and page
 * hints. `null` follows the system; desktop hosts can supply their own preference. Changes take
 * effect while the subtree remains composed.
 */
public val LocalReduceMotionEnabled: ProvidableCompositionLocal<Boolean?> = staticCompositionLocalOf { null }

/**
 * Whether decorative, looping animations (shimmer, typing dots, scroll hints) should run: not in
 * previews and not when the system reduces motion. Components show a still frame instead.
 */
@Composable
internal fun decorativeAnimationsEnabled(): Boolean =
    !LocalInspectionMode.current && !(LocalReduceMotionEnabled.current ?: isReduceMotionEnabled())
