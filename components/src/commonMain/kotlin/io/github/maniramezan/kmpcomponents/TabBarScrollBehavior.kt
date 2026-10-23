package io.github.maniramezan.kmpcomponents

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource

/**
 * Lets a [TabBar] hide as content scrolls down and come back as it scrolls up, like Material's
 * `TopAppBarScrollBehavior` for a bottom bar. Attach [nestedScrollConnection] to the scrolling
 * content with `Modifier.nestedScroll` and pass this to the bar. Create it with
 * [rememberTabBarScrollBehavior].
 */
@Stable
public class TabBarScrollBehavior internal constructor() {
    /** How far the bar is pushed down, in pixels: `0f` fully shown, [heightOffsetLimit] hidden. */
    public var heightOffset: Float by mutableFloatStateOf(0f)
        internal set

    /** The bar's measured height, negated; kept up to date by [TabBar]. */
    public var heightOffsetLimit: Float by mutableFloatStateOf(0f)
        internal set

    /** `0f` when the bar is fully shown, `1f` when fully hidden. */
    public val collapsedFraction: Float
        get() = if (heightOffset == 0f) 0f else (heightOffset / heightOffsetLimit).coerceIn(0f, 1f)

    internal fun updateHeightOffsetLimit(limit: Float) {
        heightOffsetLimit = limit
        heightOffset = heightOffset.coerceIn(limit, 0f)
    }

    /** Attach to the scrolling content that drives the bar. */
    public val nestedScrollConnection: NestedScrollConnection = object : NestedScrollConnection {
        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
            val limit = heightOffsetLimit
            if (limit != 0f) heightOffset = (heightOffset + consumed.y).coerceIn(limit, 0f)
            return Offset.Zero
        }
    }
}

/** Remembers a [TabBarScrollBehavior]. */
@Composable
public fun rememberTabBarScrollBehavior(): TabBarScrollBehavior = remember { TabBarScrollBehavior() }
