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
 * Returns the effective reduced-motion preference, so an app's own animations can follow the same
 * setting as the library's decorative ones.
 *
 * A non-null [LocalReduceMotionEnabled] wins over the system; when it is `null` this follows the
 * platform (iOS Reduce Motion, Android animator duration scale 0, the browser's
 * `prefers-reduced-motion`; desktop always reports `false`). The value recomposes callers when the
 * system setting changes on platforms that observe it, or when the provided override changes.
 */
@Composable
public fun rememberReduceMotionEnabled(): Boolean = LocalReduceMotionEnabled.current ?: isReduceMotionEnabled()

/**
 * Whether decorative, looping animations (shimmer, typing dots, scroll hints) should run: not in
 * previews and not when the system reduces motion. Components show a still frame instead.
 */
@Composable
internal fun decorativeAnimationsEnabled(): Boolean = !LocalInspectionMode.current && !rememberReduceMotionEnabled()
