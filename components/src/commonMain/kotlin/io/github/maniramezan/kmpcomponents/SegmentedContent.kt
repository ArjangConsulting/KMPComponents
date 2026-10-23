package io.github.maniramezan.kmpcomponents

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.LayoutDirection

/** One segment of a [SegmentedContent]. Custom title slots may ignore [title]. */
@Immutable
public data class SegmentedItem(public val title: String)

/** How a [SegmentedContent] marks the selected segment. */
public enum class SegmentSelectionIndicator {
    /** A filled pill behind the selected segment on a tinted bar. */
    PILL,

    /** A Material-tab underline under the selected segment. */
    UNDERLINE,

    /** No marker; the title slot styles selection itself. */
    NONE,
}

/** Whether [SegmentedContent] segments share the width evenly when they fit. */
public enum class SegmentFitMode {
    /** Split the width evenly when the titles fit; scroll when they don't. */
    EVEN_WHEN_FITS,

    /** Size each segment to its title; scroll when they overflow. */
    INTRINSIC,
}

/**
 * A tab-style segment bar above caller-supplied content that crossfades when the selection changes.
 * When the titles overflow, the bar scrolls, keeps the selected segment in view, and fades the edge
 * that has more segments. This overload keeps the selection itself (saved across recreation).
 *
 * @param unselectedSegmentColor fill behind unselected segments; transparent lets
 *   [backgroundColor] show through.
 */
@Composable
public fun SegmentedContent(
    items: List<SegmentedItem>,
    modifier: Modifier = Modifier,
    initialSelectedIndex: Int = 0,
    indicator: SegmentSelectionIndicator = SegmentSelectionIndicator.PILL,
    fitMode: SegmentFitMode = SegmentFitMode.EVEN_WHEN_FITS,
    widthMode: SegmentWidthMode = SegmentWidthMode.FILL,
    density: SegmentDensity = SegmentDensity.REGULAR,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    unselectedSegmentColor: Color = Color.Transparent,
    onSelectionChanged: ((Int) -> Unit)? = null,
    segmentTitle: @Composable (index: Int, item: SegmentedItem, isSelected: Boolean) -> Unit =
        { _, item, isSelected -> DefaultSegmentTitle(item.title, isSelected, indicator) },
    segmentContent: @Composable (index: Int, item: SegmentedItem) -> Unit,
) {
    if (items.isEmpty()) return
    var selectedIndex by rememberSaveable { mutableIntStateOf(initialSelectedIndex.coerceIn(0, items.lastIndex)) }
    SegmentedContent(
        items = items,
        selectedIndex = selectedIndex,
        onSelectionChanged = { index ->
            selectedIndex = index
            onSelectionChanged?.invoke(index)
        },
        modifier = modifier,
        indicator = indicator,
        fitMode = fitMode,
        widthMode = widthMode,
        density = density,
        backgroundColor = backgroundColor,
        unselectedSegmentColor = unselectedSegmentColor,
        segmentTitle = segmentTitle,
        segmentContent = segmentContent,
    )
}

/**
 * [SegmentedContent] whose selection the caller owns through [selectedIndex]. Outgoing content keeps
 * its item until the crossfade ends, including when that item is removed from [items].
 */
@Composable
public fun SegmentedContent(
    items: List<SegmentedItem>,
    selectedIndex: Int,
    onSelectionChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    indicator: SegmentSelectionIndicator = SegmentSelectionIndicator.PILL,
    fitMode: SegmentFitMode = SegmentFitMode.EVEN_WHEN_FITS,
    widthMode: SegmentWidthMode = SegmentWidthMode.FILL,
    density: SegmentDensity = SegmentDensity.REGULAR,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    unselectedSegmentColor: Color = Color.Transparent,
    segmentTitle: @Composable (index: Int, item: SegmentedItem, isSelected: Boolean) -> Unit =
        { _, item, isSelected -> DefaultSegmentTitle(item.title, isSelected, indicator) },
    segmentContent: @Composable (index: Int, item: SegmentedItem) -> Unit,
) {
    if (items.isEmpty()) return
    val safeIndex = selectedIndex.coerceIn(0, items.lastIndex)
    val duration = MaterialTheme.kmpMotion.fastMillis
    val fill = if (widthMode == SegmentWidthMode.FILL) Modifier.fillMaxWidth() else Modifier
    Column(modifier = modifier.then(fill)) {
        SegmentBar(
            items = items,
            selectedIndex = safeIndex,
            onSelectionChanged = onSelectionChanged,
            style = SegmentStyle(indicator, density, backgroundColor, unselectedSegmentColor),
            fitMode = fitMode,
            fillWidth = widthMode == SegmentWidthMode.FILL,
            segmentTitle = segmentTitle,
        )
        AnimatedContent(
            targetState = safeIndex to items[safeIndex],
            modifier = fill,
            transitionSpec = { fadeIn(tween(duration)) togetherWith fadeOut(tween(duration)) },
            label = "segmentContent",
        ) { (index, item) -> segmentContent(index, item) }
    }
}

/** How a segment bar lays its segments out. */
internal enum class SegmentLayout { EVEN, SCROLLABLE, INTRINSIC }

/**
 * Chooses the bar layout. Even widths need a full-width bar that asked for them and an equal share
 * wide enough for the widest title ([fitsEvenly]), so no title is cut off. Otherwise a full-width
 * bar or titles that do not [fit] side by side scroll, and a fitting content-width bar hugs them.
 */
internal fun segmentLayout(
    fitMode: SegmentFitMode,
    fillWidth: Boolean,
    fitsEvenly: Boolean,
    fits: Boolean,
): SegmentLayout =
    when {
        fitMode == SegmentFitMode.EVEN_WHEN_FITS && fillWidth && fitsEvenly -> SegmentLayout.EVEN
        fillWidth || !fits -> SegmentLayout.SCROLLABLE
        else -> SegmentLayout.INTRINSIC
    }

@Immutable
private data class SegmentStyle(
    val indicator: SegmentSelectionIndicator,
    val density: SegmentDensity,
    val backgroundColor: Color,
    val unselectedSegmentColor: Color,
)

/** The default segment label, colored for the selection [indicator] it sits on. */
@Composable
private fun DefaultSegmentTitle(title: String, isSelected: Boolean, indicator: SegmentSelectionIndicator) {
    val colors = MaterialTheme.colorScheme
    val selectedColor = if (indicator == SegmentSelectionIndicator.PILL) colors.onPrimary else colors.primary
    val color by animateColorAsState(if (isSelected) selectedColor else colors.onSurfaceVariant, label = "segmentTitle")
    Text(
        title,
        style = MaterialTheme.typography.labelLarge,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun SegmentBar(
    items: List<SegmentedItem>,
    selectedIndex: Int,
    onSelectionChanged: (Int) -> Unit,
    style: SegmentStyle,
    fitMode: SegmentFitMode,
    fillWidth: Boolean,
    segmentTitle: @Composable (Int, SegmentedItem, Boolean) -> Unit,
) {
    val spacing = MaterialTheme.kmpSpacing
    val gapPx = with(LocalDensity.current) { spacing.extraSmall.roundToPx() }
    val shape = if (style.indicator == SegmentSelectionIndicator.PILL) CircleShape else RectangleShape
    Box(
        modifier = (if (fillWidth) Modifier.fillMaxWidth() else Modifier)
            .background(style.backgroundColor, shape)
            .clip(shape),
    ) {
        SubcomposeLayout(Modifier.padding(spacing.extraSmall)) { constraints ->
            // Measure the titles once, unplaced and hidden from accessibility, to decide the layout.
            val intrinsic = subcompose("intrinsic") {
                items.forEachIndexed { index, item ->
                    Box(
                        Modifier.clearAndSetSemantics {
                        }.padding(horizontal = spacing.medium, vertical = spacing.small),
                    ) {
                        segmentTitle(index, item, index == selectedIndex)
                    }
                }
            }.map { it.measure(constraints.copy(minWidth = 0, maxWidth = Constraints.Infinity)) }
            val gaps = gapPx * (items.size - 1).coerceAtLeast(0)
            val totalWidth = intrinsic.sumOf { it.width } + gaps
            val evenWidth = (intrinsic.maxOfOrNull { it.width } ?: 0) * items.size + gaps
            val layout = segmentLayout(
                fitMode = fitMode,
                fillWidth = fillWidth,
                fitsEvenly = evenWidth <= constraints.maxWidth,
                fits = totalWidth <= constraints.maxWidth,
            )
            val placeables = subcompose("main") {
                when (layout) {
                    SegmentLayout.EVEN -> Row(
                        Modifier.fillMaxWidth().selectableGroup(),
                        horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall),
                    ) {
                        items.forEachIndexed { index, item ->
                            SegmentSlot(
                                index,
                                item,
                                index == selectedIndex,
                                style,
                                segmentTitle,
                                Modifier.weight(1f),
                                fillWidth = true,
                            ) {
                                onSelectionChanged(index)
                            }
                        }
                    }

                    SegmentLayout.SCROLLABLE -> ScrollableSegmentRow(
                        items,
                        selectedIndex,
                        onSelectionChanged,
                        style,
                        segmentTitle,
                    )

                    SegmentLayout.INTRINSIC -> Row(
                        Modifier.selectableGroup(),
                        horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall),
                    ) {
                        items.forEachIndexed { index, item ->
                            SegmentSlot(index, item, index == selectedIndex, style, segmentTitle) {
                                onSelectionChanged(index)
                            }
                        }
                    }
                }
            }.map { it.measure(constraints) }
            val width = if (fillWidth) {
                constraints.maxWidth
            } else {
                totalWidth.coerceIn(
                    constraints.minWidth,
                    constraints.maxWidth,
                )
            }
            layout(width, placeables.maxOfOrNull { it.height } ?: 0) { placeables.forEach { it.placeRelative(0, 0) } }
        }
    }
}

@Composable
private fun ScrollableSegmentRow(
    items: List<SegmentedItem>,
    selectedIndex: Int,
    onSelectionChanged: (Int) -> Unit,
    style: SegmentStyle,
    segmentTitle: @Composable (Int, SegmentedItem, Boolean) -> Unit,
) {
    val spacing = MaterialTheme.kmpSpacing
    val state = rememberLazyListState()
    // Scroll the selection in with the previous segment peeking, so more content is obvious.
    val peekPx = with(LocalDensity.current) { spacing.extraLarge.roundToPx() }
    LaunchedEffect(selectedIndex) { state.animateScrollToItem(selectedIndex, scrollOffset = -peekPx) }
    val showLeading by remember {
        derivedStateOf {
            state.firstVisibleItemIndex > 0 ||
                state.firstVisibleItemScrollOffset > 0
        }
    }
    val showTrailing by remember {
        derivedStateOf {
            val last = state.layoutInfo.visibleItemsInfo.lastOrNull()
            last != null &&
                (last.index < items.lastIndex || last.offset + last.size > state.layoutInfo.viewportEndOffset)
        }
    }
    val leadingAlpha by animateFloatAsState(if (showLeading) 1f else 0f, label = "leadingFade")
    val trailingAlpha by animateFloatAsState(if (showTrailing) 1f else 0f, label = "trailingFade")
    val fadeWidthPx = with(LocalDensity.current) { spacing.large.toPx() }
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    LazyRow(
        state = state,
        modifier = Modifier
            .fillMaxWidth()
            .selectableGroup()
            .edgeFade({ leadingAlpha }, { trailingAlpha }, style.backgroundColor, fadeWidthPx, isRtl),
        contentPadding = PaddingValues(horizontal = spacing.extraSmall),
        horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall),
    ) {
        itemsIndexed(items, key = { index, _ -> index }) { index, item ->
            SegmentSlot(index, item, index == selectedIndex, style, segmentTitle) { onSelectionChanged(index) }
        }
    }
}

/**
 * Fades the start and end edges into [color]. The alphas are lambdas read while drawing, so the
 * fade animates without recomposing the row; left and right swap in right-to-left layouts.
 */
private fun Modifier.edgeFade(
    leadingAlpha: () -> Float,
    trailingAlpha: () -> Float,
    color: Color,
    fadeWidthPx: Float,
    isRtl: Boolean,
): Modifier = drawWithContent {
    drawContent()
    val leftAlpha = if (isRtl) trailingAlpha() else leadingAlpha()
    val rightAlpha = if (isRtl) leadingAlpha() else trailingAlpha()
    val w = fadeWidthPx.coerceAtMost(size.width)
    if (leftAlpha > 0f) {
        drawRect(
            brush = Brush.horizontalGradient(listOf(color, Color.Transparent), startX = 0f, endX = w),
            size = Size(w, size.height),
            alpha = leftAlpha,
        )
    }
    if (rightAlpha > 0f) {
        drawRect(
            brush = Brush.horizontalGradient(
                listOf(Color.Transparent, color),
                startX = size.width - w,
                endX = size.width,
            ),
            topLeft = Offset(size.width - w, 0f),
            size = Size(w, size.height),
            alpha = rightAlpha,
        )
    }
}

/**
 * One segment. The tappable area and the highlight are the same box, so highlighting a segment
 * always covers its whole tap target. Regular density keeps the Material minimum height; compact
 * only keeps the minimum width, so the bar really is shorter.
 */
@Composable
private fun SegmentSlot(
    index: Int,
    item: SegmentedItem,
    isSelected: Boolean,
    style: SegmentStyle,
    segmentTitle: @Composable (Int, SegmentedItem, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    fillWidth: Boolean = false,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val spacing = MaterialTheme.kmpSpacing
    val background by animateColorAsState(
        when {
            style.indicator == SegmentSelectionIndicator.PILL && isSelected -> colors.primary
            style.indicator != SegmentSelectionIndicator.NONE -> style.unselectedSegmentColor
            else -> Color.Transparent
        },
        label = "segmentBackground",
    )
    val underline by animateColorAsState(
        if (style.indicator == SegmentSelectionIndicator.UNDERLINE && isSelected) colors.primary else Color.Transparent,
        label = "segmentUnderline",
    )
    val shape = if (style.indicator == SegmentSelectionIndicator.PILL) CircleShape else RectangleShape
    val minSize = minimumTouchTargetSize()
    val sized = (if (fillWidth) Modifier.fillMaxWidth() else Modifier.width(IntrinsicSize.Max).widthIn(min = minSize))
        .then(if (style.density == SegmentDensity.COMPACT) Modifier else Modifier.heightIn(min = minSize))
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .selectable(
                isSelected,
                interactionSource = interactionSource,
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            )
            .then(sized)
            .clip(shape)
            .background(background)
            .indication(interactionSource, LocalIndication.current)
            .padding(
                horizontal = spacing.medium,
                vertical = if (style.density == SegmentDensity.COMPACT) spacing.extraSmall else spacing.small,
            ),
        contentAlignment = Alignment.Center,
    ) {
        segmentTitle(index, item, isSelected)
        if (style.indicator == SegmentSelectionIndicator.UNDERLINE) {
            // matchParentSize follows the slot's resolved size; fillMaxWidth would be 0 in unbounded rows.
            Box(Modifier.matchParentSize(), contentAlignment = Alignment.BottomCenter) {
                Box(Modifier.fillMaxWidth().height(spacing.extraSmall).background(underline))
            }
        }
    }
}
