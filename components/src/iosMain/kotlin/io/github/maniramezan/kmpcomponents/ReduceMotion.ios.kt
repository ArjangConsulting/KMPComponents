package io.github.maniramezan.kmpcomponents

import androidx.compose.runtime.Composable
import platform.UIKit.UIAccessibilityIsReduceMotionEnabled

@Composable
internal actual fun isReduceMotionEnabled(): Boolean = UIAccessibilityIsReduceMotionEnabled()
