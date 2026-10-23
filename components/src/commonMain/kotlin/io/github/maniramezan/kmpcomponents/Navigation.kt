package io.github.maniramezan.kmpcomponents

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import kotlin.math.roundToInt
import androidx.compose.material3.LargeTopAppBar as MaterialLargeTopAppBar
import androidx.compose.material3.MediumTopAppBar as MaterialMediumTopAppBar
import androidx.compose.material3.TopAppBar as MaterialTopAppBar

/**
 * One destination of a [TabBar], [NavRail], or [AdaptiveNavScaffold]. Selection compares [value]
 * with the current selection, so reordering items keeps the selection.
 *
 * @param icon drawn with the item's state color already applied; pass an `Icon` without a tint.
 * @param label the visible name, or `null` for an icon-only destination.
 * @param badge optional content pinned to the icon's top-end corner, such as a [CountBadge].
 * @param contentDescription the spoken name; required when [label] is `null`, ignored otherwise.
 */
@Immutable
public data class TabBarItemData<T>(
    public val value: T,
    public val icon: @Composable () -> Unit,
    public val label: (@Composable () -> Unit)? = null,
    public val badge: (@Composable () -> Unit)? = null,
    public val enabled: Boolean = true,
    public val contentDescription: String = "",
)

/** Colors of a [TabBar] or [NavRail] destination in each state. */
@Immutable
public data class TabBarItemColors(
    public val selectedIconColor: Color,
    public val selectedTextColor: Color,
    public val selectedIndicatorColor: Color,
    public val unselectedIconColor: Color,
    public val unselectedTextColor: Color,
    public val disabledIconColor: Color,
    public val disabledTextColor: Color,
)

/** How a [TabBar] lays out its destinations. */
public enum class TabBarArrangement {
    /** Equal widths filling the bar, for three to five destinations. */
    EQUAL_WEIGHT,

    /** Content-sized destinations grouped in the middle. */
    CENTERED,
}

/** Defaults for [TabBar], [NavRail], and [AdaptiveNavScaffold]. Sizes match Material's bar. */
public object TabBarDefaults {
    /** Minimum height of the bar's content, excluding system bar insets. */
    public val MinHeight: Dp
        @Composable get() = minimumTouchTargetSize() + MaterialTheme.kmpSpacing.medium

    /** Width of the selected destination's indicator pill. */
    public val IndicatorWidth: Dp
        @Composable get() = MaterialTheme.kmpSpacing.extraLarge + MaterialTheme.kmpSpacing.large

    /** Height of the selected destination's indicator pill. */
    public val IndicatorHeight: Dp
        @Composable get() = MaterialTheme.kmpSpacing.extraLarge

    /** The arrangement used when none is given. */
    public val arrangement: TabBarArrangement = TabBarArrangement.EQUAL_WEIGHT

    /** The insets the bar pads for: the horizontal and bottom system bars. */
    public val windowInsets: WindowInsets
        @Composable get() = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)

    /**
     * Destination colors from the theme. The selected indicator is a primary-container pill; pass
     * [Color.Transparent] for tint-only selection.
     */
    @Composable
    public fun itemColors(
        selectedIconColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
        selectedTextColor: Color = MaterialTheme.colorScheme.primary,
        selectedIndicatorColor: Color = MaterialTheme.colorScheme.primaryContainer,
        unselectedIconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledIconColor: Color = unselectedIconColor.copy(alpha = DISABLED_ALPHA),
        disabledTextColor: Color = unselectedTextColor.copy(alpha = DISABLED_ALPHA),
    ): TabBarItemColors = TabBarItemColors(
        selectedIconColor = selectedIconColor,
        selectedTextColor = selectedTextColor,
        selectedIndicatorColor = selectedIndicatorColor,
        unselectedIconColor = unselectedIconColor,
        unselectedTextColor = unselectedTextColor,
        disabledIconColor = disabledIconColor,
        disabledTextColor = disabledTextColor,
    )
}

// Material 3's disabled content opacity.
private const val DISABLED_ALPHA = 0.38f
private const val INDICATOR_HIDDEN_SCALE = 0.6f

private val LocalTabBarArrangement = staticCompositionLocalOf { TabBarDefaults.arrangement }

/** A small top app bar on the surface color with primary-tinted navigation and action icons. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun ThemeTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) {
    MaterialTopAppBar(
        title = { Text(title) },
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        colors = topAppBarColors(),
    )
}

/** A medium (two-line) top app bar styled like [ThemeTopAppBar]. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun ThemeMediumTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) {
    MaterialMediumTopAppBar(
        title = { Text(title) },
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        colors = topAppBarColors(),
    )
}

/** A large (three-line) top app bar styled like [ThemeTopAppBar]. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun ThemeLargeTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) {
    MaterialLargeTopAppBar(
        title = { Text(title) },
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        colors = topAppBarColors(),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun topAppBarColors() = TopAppBarDefaults.topAppBarColors(
    containerColor = MaterialTheme.colorScheme.surface,
    titleContentColor = MaterialTheme.colorScheme.onSurface,
    navigationIconContentColor = MaterialTheme.colorScheme.primary,
    actionIconContentColor = MaterialTheme.colorScheme.primary,
)

/**
 * A bottom navigation bar built from [items]. With a [scrollBehavior] it hides while the content
 * scrolls down; attach the behavior's `nestedScrollConnection` to that content. For per-item
 * control use the overload that takes [TabBarItem]s.
 */
@Composable
public fun <T> TabBar(
    items: List<TabBarItemData<T>>,
    selection: T,
    onSelectionChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    windowInsets: WindowInsets = TabBarDefaults.windowInsets,
    arrangement: TabBarArrangement = TabBarDefaults.arrangement,
    colors: TabBarItemColors = TabBarDefaults.itemColors(),
    scrollBehavior: TabBarScrollBehavior? = null,
) {
    TabBar(modifier, containerColor, windowInsets, arrangement, scrollBehavior) {
        items.forEach { item ->
            TabBarItem(
                value = item.value,
                selection = selection,
                onSelectionChange = onSelectionChange,
                icon = item.icon,
                enabled = item.enabled,
                label = item.label,
                badge = item.badge,
                colors = colors,
                contentDescription = item.contentDescription,
            )
        }
    }
}

/**
 * A bottom navigation bar whose destinations are [TabBarItem]s in [content]. The destinations form
 * one selectable group. As a [scrollBehavior] hides the bar, the bar also reports a smaller height,
 * so a `Scaffold` gives the space back to the content.
 */
@Composable
public fun TabBar(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    windowInsets: WindowInsets = TabBarDefaults.windowInsets,
    arrangement: TabBarArrangement = TabBarDefaults.arrangement,
    scrollBehavior: TabBarScrollBehavior? = null,
    content: @Composable RowScope.() -> Unit,
) {
    Surface(
        color = containerColor,
        modifier = modifier
            .graphicsLayer { clip = true }
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                scrollBehavior?.updateHeightOffsetLimit(-placeable.height.toFloat())
                // Read in layout, so scrolling re-runs only this block instead of recomposing the bar.
                val offset = scrollBehavior?.heightOffset ?: 0f
                val height = (placeable.height + offset.roundToInt()).coerceIn(0, placeable.height)
                layout(placeable.width, height) { placeable.place(0, height - placeable.height) }
            },
    ) {
        CompositionLocalProvider(LocalTabBarArrangement provides arrangement) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(windowInsets)
                    .defaultMinSize(minHeight = TabBarDefaults.MinHeight)
                    .selectableGroup(),
                horizontalArrangement = when (arrangement) {
                    TabBarArrangement.EQUAL_WEIGHT -> Arrangement.Start

                    TabBarArrangement.CENTERED ->
                        Arrangement.spacedBy(MaterialTheme.kmpSpacing.medium, Alignment.CenterHorizontally)
                },
                verticalAlignment = Alignment.CenterVertically,
                content = content,
            )
        }
    }
}

/** One destination of a [TabBar]; see [TabBarItemData] for the parameters. */
@Composable
public fun <T> RowScope.TabBarItem(
    value: T,
    selection: T,
    onSelectionChange: (T) -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: (@Composable () -> Unit)? = null,
    badge: (@Composable () -> Unit)? = null,
    colors: TabBarItemColors = TabBarDefaults.itemColors(),
    contentDescription: String = "",
) {
    val selected = value == selection
    val spec = tween<Color>(MaterialTheme.kmpMotion.fastMillis)
    val iconColor by animateColorAsState(
        when {
            !enabled -> colors.disabledIconColor
            selected -> colors.selectedIconColor
            else -> colors.unselectedIconColor
        },
        spec,
        label = "tabIcon",
    )
    val textColor by animateColorAsState(
        when {
            !enabled -> colors.disabledTextColor
            selected -> colors.selectedTextColor
            else -> colors.unselectedTextColor
        },
        spec,
        label = "tabText",
    )
    val weighted = if (LocalTabBarArrangement.current ==
        TabBarArrangement.EQUAL_WEIGHT
    ) {
        modifier.weight(1f)
    } else {
        modifier
    }
    Column(
        modifier = weighted
            .keyboardFocusOutline()
            .selectable(selected = selected, enabled = enabled, role = Role.Tab) { onSelectionChange(value) }
            .heightIn(min = minimumTouchTargetSize())
            .then(
                if (label == null && contentDescription.isNotBlank()) {
                    Modifier.semantics { this.contentDescription = contentDescription }
                } else {
                    Modifier
                },
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.extraSmall, Alignment.CenterVertically),
    ) {
        Box(
            modifier = Modifier.size(TabBarDefaults.IndicatorWidth, TabBarDefaults.IndicatorHeight),
            contentAlignment = Alignment.Center,
        ) {
            SelectionIndicator(selected, colors.selectedIndicatorColor, Modifier.matchParentSize())
            CompositionLocalProvider(LocalContentColor provides iconColor) { BadgedIcon(icon, badge) }
        }
        if (label != null) {
            CompositionLocalProvider(
                LocalContentColor provides textColor,
                LocalTextStyle provides MaterialTheme.typography.labelSmall,
                content = label,
            )
        }
    }
}

/**
 * The selected destination's pill. It fades and scales in a fixed-size slot, so selecting a
 * destination never shifts the label.
 */
@Composable
private fun SelectionIndicator(visible: Boolean, color: Color, modifier: Modifier) {
    val spec = tween<Float>(MaterialTheme.kmpMotion.standardMillis)
    val alpha by animateFloatAsState(if (visible) 1f else 0f, spec, label = "indicatorAlpha")
    val scale by animateFloatAsState(if (visible) 1f else INDICATOR_HIDDEN_SCALE, spec, label = "indicatorScale")
    Box(
        modifier
            .graphicsLayer {
                this.alpha = alpha
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(color),
    )
}

/** [icon] with [badge] pinned just outside its top-end corner. */
@Composable
private fun BadgedIcon(icon: @Composable () -> Unit, badge: (@Composable () -> Unit)?) {
    if (badge == null) {
        icon()
        return
    }
    Box {
        icon()
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .offset(x = MaterialTheme.kmpSpacing.small, y = -MaterialTheme.kmpSpacing.small),
        ) { badge() }
    }
}

/** Material's primary tab row for switching between [tabs] at the same level. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun ThemeTabRow(
    tabs: List<String>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    PrimaryTabRow(selectedTabIndex = selectedIndex, modifier = modifier) {
        tabs.forEachIndexed { index, tab ->
            Tab(
                selected = index == selectedIndex,
                onClick = { onItemSelected(index) },
                modifier = Modifier.keyboardFocusOutline().heightIn(min = minimumTouchTargetSize()),
                text = { Text(tab) },
            )
        }
    }
}

/**
 * A side navigation rail for medium and wide windows, sharing [TabBarItemData] and
 * [TabBarItemColors] with [TabBar]. For a desktop-style labeled list, use [SidebarNavigation].
 */
@Composable
public fun <T> NavRail(
    items: List<TabBarItemData<T>>,
    selection: T,
    onSelectionChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    colors: TabBarItemColors = TabBarDefaults.itemColors(),
) {
    NavigationRail(modifier = modifier) {
        items.forEach { item ->
            NavigationRailItem(
                selected = item.value == selection,
                onClick = { onSelectionChange(item.value) },
                icon = { BadgedIcon(item.icon, item.badge) },
                modifier = (
                    if (item.label == null && item.contentDescription.isNotBlank()) {
                        Modifier.semantics { contentDescription = item.contentDescription }
                    } else {
                        Modifier
                    }
                    ).keyboardFocusOutline(),
                enabled = item.enabled,
                label = item.label,
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = colors.selectedIconColor,
                    selectedTextColor = colors.selectedTextColor,
                    indicatorColor = colors.selectedIndicatorColor,
                    unselectedIconColor = colors.unselectedIconColor,
                    unselectedTextColor = colors.unselectedTextColor,
                    disabledIconColor = colors.disabledIconColor,
                    disabledTextColor = colors.disabledTextColor,
                ),
            )
        }
    }
}

/**
 * A scaffold that shows a [TabBar] in compact windows (narrower than 600 dp) and a [NavRail] in
 * wider ones, with [topBar] above the content. [scrollBehavior] only applies to the tab bar; the
 * rail stays put.
 */
@Composable
public fun <T> AdaptiveNavScaffold(
    items: List<TabBarItemData<T>>,
    selection: T,
    onSelectionChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    colors: TabBarItemColors = TabBarDefaults.itemColors(),
    scrollBehavior: TabBarScrollBehavior? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    val windowWidth = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.width.toDp() }
    val wide = windowWidth >= CompactWindowMaxWidth
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = {
            if (!wide) {
                TabBar(
                    items = items,
                    selection = selection,
                    onSelectionChange = onSelectionChange,
                    colors = colors,
                    scrollBehavior = scrollBehavior,
                )
            }
        },
    ) { innerPadding ->
        if (wide) {
            Row(Modifier.padding(innerPadding)) {
                NavRail(items = items, selection = selection, onSelectionChange = onSelectionChange, colors = colors)
                Box(Modifier.weight(1f)) { content(PaddingValues()) }
            }
        } else {
            content(innerPadding)
        }
    }
}
