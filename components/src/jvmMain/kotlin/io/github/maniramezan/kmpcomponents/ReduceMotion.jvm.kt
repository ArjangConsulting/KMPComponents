package io.github.maniramezan.kmpcomponents

import androidx.compose.runtime.Composable

// Desktop JVM has no portable reduce-motion setting; LocalSkeletonShimmerEnabled turns it off.
@Composable
internal actual fun isReduceMotionEnabled(): Boolean = false
