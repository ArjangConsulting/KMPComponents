package io.github.maniramezan.kmpcomponents

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.math.abs
import kotlin.math.roundToInt

/** One page of a [PaginatedContent], with the [title] shown in its title row. */
@Immutable
public data class PaginationPage(public val title: String)

/** Where the current page's title sits in a [PaginatedContent] title row. */
public enum class PageTitleAlignment {
    /** At the start edge. */
    LEADING,

    /** At the end edge. */
    TRAILING,

    /** Centered; an incoming title slides from the start edge to the center as it settles. */
    CENTER,
}

/** Whether a [PaginatedContent] can be swiped back as well as forward. */
public enum class PageDirection {
    BIDIRECTIONAL,

    /** Forward only, for step-by-step flows. */
    UNIDIRECTIONAL,
}

/** The position footer under a [PaginatedContent]. */
public enum class PageFooterStyle { DOTS, PROGRESS, NONE }

/**
 * Horizontally paged content whose title row scrolls in step with the pages: each title is a page
 * width from its neighbors, so the next title slides in as the user swipes. This overload owns its
 * [PagerState].
 *
 * @param showScrollHint briefly nudges the first page on first display to show it can be swiped.
 *   Skipped when the system reduces motion.
 * @param pagePositionDescription builds the footer's spoken position, such as
 *   `{ index, count -> "Page ${index + 1} of $count" }`; `null` keeps the footer silent.
 */
@Composable
public fun PaginatedContent(
    pages: List<PaginationPage>,
    modifier: Modifier = Modifier,
    initialPage: Int = 0,
    titleAlignment: PageTitleAlignment = PageTitleAlignment.LEADING,
    direction: PageDirection = PageDirection.BIDIRECTIONAL,
    footerStyle: PageFooterStyle = PageFooterStyle.DOTS,
    showScrollHint: Boolean = true,
    onPageChanged: ((Int) -> Unit)? = null,
    pagePositionDescription: ((pageIndex: Int, pageCount: Int) -> String)? = null,
    pageTitle: @Composable (pageIndex: Int, page: PaginationPage, progress: Float) -> Unit =
        { _, page, progress -> DefaultPageTitle(page.title, progress) },
    pageContent: @Composable PagerScope.(pageIndex: Int, page: PaginationPage) -> Unit,
) {
    if (pages.isEmpty()) return
    val pagerState = rememberPagerState(initialPage = initialPage.coerceIn(0, pages.lastIndex)) { pages.size }
    PaginatedContent(
        pages = pages,
        pagerState = pagerState,
        modifier = modifier,
        titleAlignment = titleAlignment,
        direction = direction,
        footerStyle = footerStyle,
        showScrollHint = showScrollHint,
        onPageChanged = onPageChanged,
        pagePositionDescription = pagePositionDescription,
        pageTitle = pageTitle,
        pageContent = pageContent,
    )
}

/**
 * [PaginatedContent] driven by a caller-owned [pagerState], for jumping to pages from outside.
 * [onPageChanged] reports the settled page.
 */
@Composable
public fun PaginatedContent(
    pages: List<PaginationPage>,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    titleAlignment: PageTitleAlignment = PageTitleAlignment.LEADING,
    direction: PageDirection = PageDirection.BIDIRECTIONAL,
    footerStyle: PageFooterStyle = PageFooterStyle.DOTS,
    showScrollHint: Boolean = true,
    onPageChanged: ((Int) -> Unit)? = null,
    pagePositionDescription: ((pageIndex: Int, pageCount: Int) -> String)? = null,
    pageTitle: @Composable (pageIndex: Int, page: PaginationPage, progress: Float) -> Unit =
        { _, page, progress -> DefaultPageTitle(page.title, progress) },
    pageContent: @Composable PagerScope.(pageIndex: Int, page: PaginationPage) -> Unit,
) {
    if (pages.isEmpty()) return
    if (onPageChanged != null) {
        LaunchedEffect(pagerState, onPageChanged) {
            snapshotFlow { pagerState.settledPage }.distinctUntilChanged().collect { onPageChanged(it) }
        }
    }
    val hint = showScrollHint && pages.size > 1 && !isReduceMotionEnabled()
    val motion = MaterialTheme.kmpMotion
    if (hint) {
        LaunchedEffect(pagerState) {
            delay(motion.deliberateMillis.toLong())
            if (pagerState.currentPage < pages.lastIndex) {
                pagerState.animateScrollToPage(
                    page = pagerState.currentPage,
                    pageOffsetFraction = SCROLL_HINT_FRACTION,
                    animationSpec = tween(motion.deliberateMillis),
                )
                pagerState.animateScrollToPage(pagerState.currentPage, animationSpec = tween(motion.standardMillis))
            }
        }
    }
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val forwardOnly = remember(direction, isRtl) {
        if (direction == PageDirection.UNIDIRECTIONAL) ForwardOnlyNestedScrollConnection(isRtl) else null
    }
    val spacing = MaterialTheme.kmpSpacing
    Column(modifier = modifier.fillMaxWidth()) {
        ScrollingTitleRow(pages, pagerState, titleAlignment, pageTitle, Modifier.fillMaxWidth())
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().then(forwardOnly?.let { Modifier.nestedScroll(it) } ?: Modifier),
        ) { pageIndex -> pageContent(pageIndex, pages[pageIndex]) }
        if (pages.size > 1) {
            when (footerStyle) {
                PageFooterStyle.DOTS -> PageDots(
                    pageCount = pages.size,
                    pagerState = pagerState,
                    describe = pagePositionDescription,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = spacing.small),
                )

                PageFooterStyle.PROGRESS -> PageProgress(
                    pageCount = pages.size,
                    pagerState = pagerState,
                    describe = pagePositionDescription,
                    modifier = Modifier.fillMaxWidth().padding(
                        top = spacing.small,
                        start = spacing.medium,
                        end = spacing.medium,
                    ),
                )

                PageFooterStyle.NONE -> Unit
            }
        }
    }
}

private const val SCROLL_HINT_FRACTION = 0.25f

/** Swallows backward drags and flings so a unidirectional pager can only move forward. */
private class ForwardOnlyNestedScrollConnection(private val isRtl: Boolean) : NestedScrollConnection {
    private fun isBackward(deltaX: Float): Boolean = if (isRtl) deltaX < 0f else deltaX > 0f

    // The pager's own settle animation is not user input, so a cancelled swipe still snaps back.
    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset =
        if (isBackward(available.x) && source == NestedScrollSource.UserInput) Offset(available.x, 0f) else Offset.Zero

    override suspend fun onPreFling(available: Velocity): Velocity =
        if (isBackward(available.x)) Velocity(available.x, 0f) else Velocity.Zero
}

/**
 * The title row. Each title occupies a page-wide slot that scrolls with the pager. In center
 * alignment the current title's anchor moves from the start edge to the center as it settles.
 */
@Composable
private fun ScrollingTitleRow(
    pages: List<PaginationPage>,
    pagerState: PagerState,
    titleAlignment: PageTitleAlignment,
    pageTitle: @Composable (pageIndex: Int, page: PaginationPage, progress: Float) -> Unit,
    modifier: Modifier,
) {
    // A stable state per page keeps scroll frames from recreating the title lambdas.
    val progressStates = remember { mutableMapOf<Int, MutableFloatState>() }
    val spacing = MaterialTheme.kmpSpacing
    SubcomposeLayout(
        modifier.clipToBounds().padding(horizontal = spacing.medium, vertical = spacing.small),
    ) { constraints ->
        val currentPage = pagerState.currentPage
        val continuousPage = currentPage + pagerState.currentPageOffsetFraction
        val width = constraints.maxWidth
        val visible = ((currentPage - 1)..(currentPage + 1)).filter { it in pages.indices }
        val measured = visible.associateWith { index ->
            val progress = titleProgress(index, continuousPage)
            val state = progressStates.getOrPut(index) { mutableFloatStateOf(progress) }
            state.floatValue = progress
            subcompose("title_$index") {
                val value by state
                Box { pageTitle(index, pages[index], value) }
            }.first().measure(constraints.copy(minWidth = 0))
        }
        val height = measured.values.maxOfOrNull { it.height } ?: 0
        layout(width, height) {
            measured.forEach { (index, placeable) ->
                val x = titleOffset(index - continuousPage, width, placeable.width, titleAlignment)
                // placeRelative mirrors in right-to-left layouts.
                placeable.placeRelative(x, (height - placeable.height) / 2)
            }
        }
    }
}

/** How selected a title is: `1f` on the current page, `0f` a page or more away. */
internal fun titleProgress(
    index: Int,
    continuousPage: Float,
): Float = (1f - abs(index - continuousPage)).coerceIn(0f, 1f)

/** The title's x position in a row [rowWidth] wide when it is [pageOffset] pages from current. */
internal fun titleOffset(pageOffset: Float, rowWidth: Int, titleWidth: Int, alignment: PageTitleAlignment): Int {
    val slot = pageOffset * rowWidth
    val anchor = when (alignment) {
        PageTitleAlignment.LEADING -> 0f
        PageTitleAlignment.TRAILING -> (rowWidth - titleWidth).toFloat()
        PageTitleAlignment.CENTER -> (rowWidth - titleWidth) / 2f * (1f - abs(pageOffset)).coerceIn(0f, 1f)
    }
    return (slot + anchor).roundToInt()
}

@Composable
private fun DefaultPageTitle(title: String, progress: Float) {
    val colors = MaterialTheme.colorScheme
    val selected = progress > HALF
    val container by animateColorAsState(if (selected) colors.primary else Color.Transparent, label = "titleContainer")
    val text by animateColorAsState(if (selected) colors.onPrimary else colors.onSurfaceVariant, label = "titleText")
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(container)
            .padding(horizontal = MaterialTheme.kmpSpacing.medium, vertical = MaterialTheme.kmpSpacing.extraSmall),
        contentAlignment = Alignment.Center,
    ) {
        Text(title, style = MaterialTheme.typography.titleSmall, color = text)
    }
}

@Composable
private fun PageDots(
    pageCount: Int,
    pagerState: PagerState,
    describe: ((Int, Int) -> String)?,
    modifier: Modifier,
) {
    // Reading the page here keeps recomposition inside the footer.
    val current = pagerState.currentPage
    val description = describe?.invoke(current, pageCount)
    Row(
        modifier = modifier.positionSemantics(description),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val color by animateColorAsState(
                if (index == current) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                label = "dot",
            )
            Box(Modifier.size(MaterialTheme.kmpSpacing.small).clip(CircleShape).background(color))
        }
    }
}

@Composable
private fun PageProgress(
    pageCount: Int,
    pagerState: PagerState,
    describe: ((Int, Int) -> String)?,
    modifier: Modifier,
) {
    val current = pagerState.currentPage
    LinearProgressIndicator(
        // The offset is read in the progress lambda, so scrolling redraws without recomposing.
        progress = { ((current + pagerState.currentPageOffsetFraction) / (pageCount - 1)).coerceIn(0f, 1f) },
        modifier = modifier.clip(CircleShape).positionSemantics(describe?.invoke(current, pageCount)),
    )
}

private fun Modifier.positionSemantics(description: String?): Modifier = if (description == null) {
    this
} else {
    semantics {
        contentDescription = description
        liveRegion = LiveRegionMode.Polite
    }
}

private const val HALF = 0.5f
