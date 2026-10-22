package io.github.maniramezan.kmpcomponents

import androidx.compose.runtime.Composable

/**
 * Whether the user asked the system to reduce motion (iOS Reduce Motion, Android animator duration
 * scale 0). Decorative animations such as the skeleton shimmer stay still when it is on.
 */
@Composable
internal expect fun isReduceMotionEnabled(): Boolean
