package io.github.maniramezan.kmpcomponents

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion

/**
 * A row of dots marking the current position within [pageCount] pages, for carousels, onboarding
 * flows, and any pager. [PaginatedContent] uses it for its dots footer.
 *
 * The current page's dot is a wider pill as well as [activeColor], so the position does not rely on
 * color alone. The dots are hidden from accessibility services unless you supply
 * [pagePositionDescription]; the indicator then reads as one node (for example "Page 2 of 5") and
 * announces changes politely.
 *
 * @param pageCount total number of pages. Nothing is drawn when it is below 1.
 * @param currentPage zero-based index of the current page, clamped into range.
 * @param pagePositionDescription builds the localized announcement from the zero-based page index
 *   and the page count. Omit it, or return a blank string, to keep the dots decorative.
 * @param activeColor color of the current page's dot.
 * @param inactiveColor color of the other dots. The default keeps 3:1 contrast on surfaces.
 */
@Composable
public fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
    pagePositionDescription: ((pageIndex: Int, pageCount: Int) -> String)? = null,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.outlineVariant,
) {
    if (pageCount < 1) return
    val activePage = currentPage.coerceIn(0, pageCount - 1)
    val description = pagePositionDescription?.invoke(activePage, pageCount)
    val spacing = MaterialTheme.kmpSpacing
    val animate = !rememberReduceMotionEnabled()
    val duration = MaterialTheme.kmpMotion.fastMillis
    Row(
        modifier = modifier.clearAndSetSemantics {
            if (!description.isNullOrBlank()) {
                contentDescription = description
                liveRegion = LiveRegionMode.Polite
            }
        },
        horizontalArrangement = Arrangement.spacedBy(spacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val active = index == activePage
            val color by animateColorAsState(
                targetValue = if (active) activeColor else inactiveColor,
                animationSpec = if (animate) tween(duration) else snap(),
                label = "PageIndicatorDotColor",
            )
            val width by animateDpAsState(
                targetValue = if (active) spacing.small * ACTIVE_DOT_WIDTH_FACTOR else spacing.small,
                animationSpec = if (animate) tween(duration) else snap(),
                label = "PageIndicatorDotWidth",
            )
            Box(Modifier.width(width).height(spacing.small).clip(CircleShape).background(color))
        }
    }
}

/**
 * [PageIndicator] bound to a [PagerState]. The current dot follows [PagerState.currentPage], and the
 * page is read inside this composable, so the caller does not recompose as the pager moves.
 */
@Composable
public fun PageIndicator(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    pagePositionDescription: ((pageIndex: Int, pageCount: Int) -> String)? = null,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.outlineVariant,
) {
    PageIndicator(
        pageCount = pagerState.pageCount,
        currentPage = pagerState.currentPage,
        modifier = modifier,
        pagePositionDescription = pagePositionDescription,
        activeColor = activeColor,
        inactiveColor = inactiveColor,
    )
}

private const val ACTIVE_DOT_WIDTH_FACTOR = 2.5f
