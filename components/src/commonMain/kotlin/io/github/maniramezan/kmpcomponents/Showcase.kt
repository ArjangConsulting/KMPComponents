package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.lazy.grid.itemsIndexed as gridItemsIndexed

/** How wide each item of a [ShowcaseRow] is, which decides how much of the next item peeks in. */
public sealed interface ShowcaseItemWidth {
    /**
     * Each item takes [visibleFraction] of the row's usable width, so the next one peeks in at the
     * end edge. `1f` fills the row with no peek.
     */
    public data class Peek(public val visibleFraction: Float = DEFAULT_PEEK_FRACTION) : ShowcaseItemWidth

    /** Each item is exactly [width] wide. */
    public data class Fixed(public val width: Dp) : ShowcaseItemWidth

    /** Each item takes its own width. */
    public data object Wrap : ShowcaseItemWidth
}

private const val DEFAULT_PEEK_FRACTION = 0.85f

/** How a [ShowcaseRow] settles after a fling. */
public enum class ShowcaseSnapping {
    /** Settles with an item's start edge at the row's start padding, like a paged carousel. */
    ITEM,

    /** Stops wherever the fling ends. */
    FREE,
}

/**
 * A lazily composed horizontal carousel of [items], sized by [itemWidth]; by default the next item
 * peeks in to show the row scrolls. With [rows] above 1 and a [rowHeight] it becomes a
 * column-major grid of stacked rows ("top charts"). [snapping] decides whether a fling settles on
 * an item boundary (the default) or stops freely.
 */
@Composable
public fun <T> ShowcaseRow(
    items: List<T>,
    modifier: Modifier = Modifier,
    itemWidth: ShowcaseItemWidth = ShowcaseItemWidth.Peek(),
    rows: Int = 1,
    rowHeight: Dp? = null,
    snapping: ShowcaseSnapping = ShowcaseSnapping.ITEM,
    state: LazyListState = rememberLazyListState(),
    gridState: LazyGridState = rememberLazyGridState(),
    contentPadding: PaddingValues = PaddingValues(horizontal = MaterialTheme.kmpSpacing.medium),
    itemSpacing: Dp = MaterialTheme.kmpSpacing.small,
    key: ((index: Int, item: T) -> Any)? = null,
    itemContent: @Composable (item: T) -> Unit,
) {
    if (items.isEmpty()) return
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val layoutDirection = LocalLayoutDirection.current
        val padding = contentPadding.calculateStartPadding(layoutDirection) +
            contentPadding.calculateEndPadding(layoutDirection)
        val width: Dp? = when (itemWidth) {
            is ShowcaseItemWidth.Peek -> ((maxWidth - padding - itemSpacing) * itemWidth.visibleFraction)
                .coerceAtLeast(Dp.Hairline)

            is ShowcaseItemWidth.Fixed -> itemWidth.width

            ShowcaseItemWidth.Wrap -> null
        }
        val sized = if (width != null) Modifier.width(width) else Modifier
        if (rows > 1 && rowHeight != null) {
            LazyHorizontalGrid(
                rows = GridCells.Fixed(rows),
                state = gridState,
                flingBehavior = if (snapping == ShowcaseSnapping.ITEM) {
                    rememberSnapFlingBehavior(gridState, SnapPosition.Start)
                } else {
                    ScrollableDefaults.flingBehavior()
                },
                modifier = Modifier.height(rowHeight * rows + itemSpacing * (rows - 1)),
                contentPadding = contentPadding,
                horizontalArrangement = Arrangement.spacedBy(itemSpacing),
                verticalArrangement = Arrangement.spacedBy(itemSpacing),
            ) {
                gridItemsIndexed(items = items, key = key) { _, item ->
                    Box(sized.fillMaxHeight()) { itemContent(item) }
                }
            }
        } else {
            LazyRow(
                state = state,
                flingBehavior = if (snapping == ShowcaseSnapping.ITEM) {
                    rememberSnapFlingBehavior(state, SnapPosition.Start)
                } else {
                    ScrollableDefaults.flingBehavior()
                },
                contentPadding = contentPadding,
                horizontalArrangement = Arrangement.spacedBy(itemSpacing),
            ) {
                itemsIndexed(items = items, key = key) { _, item -> Box(sized) { itemContent(item) } }
            }
        }
    }
}

/** Declares the sections of a [ShowcaseFeed], like a `LazyColumn` DSL. */
public interface ShowcaseFeedScope {
    /**
     * A [SectionHeader] above a [ShowcaseRow]. Sections can use different item types.
     *
     * @param rowContentDescription optional spoken name for the row; `null` lets screen readers
     *   move through the items directly.
     */
    public fun <T> section(
        title: String,
        items: List<T>,
        key: Any? = null,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null,
        itemWidth: ShowcaseItemWidth = ShowcaseItemWidth.Peek(),
        rows: Int = 1,
        rowHeight: Dp? = null,
        snapping: ShowcaseSnapping = ShowcaseSnapping.ITEM,
        rowContentDescription: String? = null,
        itemKey: ((index: Int, item: T) -> Any)? = null,
        titleStyle: TextStyle? = null,
        itemContent: @Composable (item: T) -> Unit,
    )

    /**
     * A [SectionHeader] followed by [items] emitted straight into the feed, each lazily, so the
     * section can grow as the user scrolls. While [hasMore], a [LoadMoreFooter] calls [onLoadMore]
     * when it scrolls into view. Give each vertical section a [key] when a feed has several.
     */
    public fun <T> verticalSection(
        title: String,
        items: List<T>,
        key: Any? = null,
        itemKey: ((index: Int, item: T) -> Any)? = null,
        titleStyle: TextStyle? = null,
        hasMore: Boolean = false,
        isLoadingMore: Boolean = false,
        onLoadMore: (() -> Unit)? = null,
        itemContent: @Composable (item: T) -> Unit,
    )

    /** Any other content, such as a hero banner, placed directly in the feed. */
    public fun customSection(key: Any? = null, content: @Composable () -> Unit)
}

/**
 * An app-store style feed: a vertical list of sections that each scroll horizontally. Declare the
 * sections in [content] with [ShowcaseFeedScope].
 */
@Composable
public fun ShowcaseFeed(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(vertical = MaterialTheme.kmpSpacing.medium),
    verticalItemSpacing: Dp = MaterialTheme.kmpSpacing.large,
    content: ShowcaseFeedScope.() -> Unit,
) {
    val scope = ShowcaseFeedScopeImpl().apply(content)
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        state = state,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(verticalItemSpacing),
    ) {
        scope.entries.forEach { entry -> entry() }
    }
}

private class ShowcaseFeedScopeImpl : ShowcaseFeedScope {
    val entries = mutableListOf<LazyListScope.() -> Unit>()

    override fun <T> section(
        title: String,
        items: List<T>,
        key: Any?,
        actionLabel: String?,
        onAction: (() -> Unit)?,
        itemWidth: ShowcaseItemWidth,
        rows: Int,
        rowHeight: Dp?,
        snapping: ShowcaseSnapping,
        rowContentDescription: String?,
        itemKey: ((index: Int, item: T) -> Any)?,
        titleStyle: TextStyle?,
        itemContent: @Composable (item: T) -> Unit,
    ) {
        entries += {
            item(key = key) {
                Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small)) {
                    SectionHeader(
                        title = title,
                        modifier = Modifier.padding(horizontal = MaterialTheme.kmpSpacing.medium),
                        actionLabel = actionLabel,
                        onAction = onAction,
                        titleStyle = titleStyle,
                    )
                    ShowcaseRow(
                        items = items,
                        modifier = if (rowContentDescription != null) {
                            Modifier.semantics { contentDescription = rowContentDescription }
                        } else {
                            Modifier
                        },
                        itemWidth = itemWidth,
                        rows = rows,
                        rowHeight = rowHeight,
                        snapping = snapping,
                        key = itemKey,
                        itemContent = itemContent,
                    )
                }
            }
        }
    }

    override fun <T> verticalSection(
        title: String,
        items: List<T>,
        key: Any?,
        itemKey: ((index: Int, item: T) -> Any)?,
        titleStyle: TextStyle?,
        hasMore: Boolean,
        isLoadingMore: Boolean,
        onLoadMore: (() -> Unit)?,
        itemContent: @Composable (item: T) -> Unit,
    ) {
        entries += {
            item(key = key?.let { "$it#header" }) {
                SectionHeader(
                    title = title,
                    modifier = Modifier.padding(horizontal = MaterialTheme.kmpSpacing.medium),
                    titleStyle = titleStyle,
                )
            }
            itemsIndexed(items = items, key = itemKey) { _, item -> itemContent(item) }
            if (hasMore) {
                item(key = key?.let { "$it#load-more" }) {
                    LoadMoreFooter(isLoadingMore = isLoadingMore, onLoadMore = { onLoadMore?.invoke() })
                }
            }
        }
    }

    override fun customSection(key: Any?, content: @Composable () -> Unit) {
        entries += { item(key = key) { content() } }
    }
}
