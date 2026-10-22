package io.github.maniramezan.kmpcomponents

import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** The Material minimum touch target, so tappable rows match Material controls on every platform. */
@Composable
@ReadOnlyComposable
internal fun minimumTouchTargetSize(): Dp = LocalMinimumInteractiveComponentSize.current

/** Material's compact window width limit; wider windows count as medium or expanded. */
internal val CompactWindowMaxWidth: Dp = 600.dp
