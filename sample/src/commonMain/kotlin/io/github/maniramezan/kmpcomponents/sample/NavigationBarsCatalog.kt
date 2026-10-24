package io.github.maniramezan.kmpcomponents.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import io.github.maniramezan.kmpcomponents.AdaptiveNavScaffold
import io.github.maniramezan.kmpcomponents.AppText
import io.github.maniramezan.kmpcomponents.AppTextStyle
import io.github.maniramezan.kmpcomponents.CountBadge
import io.github.maniramezan.kmpcomponents.FlipCard
import io.github.maniramezan.kmpcomponents.FlowLayout
import io.github.maniramezan.kmpcomponents.NavRail
import io.github.maniramezan.kmpcomponents.PageFooterStyle
import io.github.maniramezan.kmpcomponents.PageIndicator
import io.github.maniramezan.kmpcomponents.PageTitleAlignment
import io.github.maniramezan.kmpcomponents.PaginatedContent
import io.github.maniramezan.kmpcomponents.PaginationPage
import io.github.maniramezan.kmpcomponents.SecondaryButton
import io.github.maniramezan.kmpcomponents.SectionCard
import io.github.maniramezan.kmpcomponents.ShowcaseFeed
import io.github.maniramezan.kmpcomponents.ShowcaseItemWidth
import io.github.maniramezan.kmpcomponents.ShowcaseRow
import io.github.maniramezan.kmpcomponents.TabBar
import io.github.maniramezan.kmpcomponents.TabBarItem
import io.github.maniramezan.kmpcomponents.TabBarItemData
import io.github.maniramezan.kmpcomponents.ThemeCard
import io.github.maniramezan.kmpcomponents.ThemeLargeTopAppBar
import io.github.maniramezan.kmpcomponents.ThemeListItem
import io.github.maniramezan.kmpcomponents.ThemeMediumTopAppBar
import io.github.maniramezan.kmpcomponents.ThemeTabRow
import io.github.maniramezan.kmpcomponents.ThemeTopAppBar
import io.github.maniramezan.kmpcomponents.kmpSpacing
import io.github.maniramezan.kmpcomponents.rememberTabBarScrollBehavior
import kotlinx.coroutines.delay

/** Bars, paging, showcase feeds, flip cards, and text roles. */
@Composable
fun NavigationBarsSection() {
    BarsDemo()
    PagingDemo()
    FeedDemo()
}

private val destinations = listOf("Home", "Search", "Settings")

@Composable
private fun BarsDemo() {
    var selected by remember { mutableStateOf("Home") }
    var tab by remember { mutableIntStateOf(0) }
    val plus = rememberVectorPainter(PlusIcon)
    val items = destinations.map { name ->
        TabBarItemData(
            value = name,
            icon = { Icon(plus, null) },
            label = { Text(name) },
            badge = if (name == "Search") ({ CountBadge(2) }) else null,
        )
    }
    val scrollBehavior = rememberTabBarScrollBehavior()
    SectionCard(title = "App bars and navigation", description = "Scroll the list to hide the tab bar.") {
        ThemeTopAppBar(title = "Small app bar")
        ThemeMediumTopAppBar(title = "Medium app bar")
        ThemeLargeTopAppBar(title = "Large app bar")
        ThemeTabRow(tabs = listOf("All", "Unread", "Flagged"), selectedIndex = tab, onItemSelected = { tab = it })
        Column(Modifier.height(LIST_HEIGHT)) {
            Column(
                Modifier.weight(
                    1f,
                ).nestedScroll(scrollBehavior.nestedScrollConnection).verticalScroll(rememberScrollState()),
            ) {
                repeat(LIST_ROWS) {
                    ThemeListItem(headline = "Message ${it + 1}", supportingText = "From the $selected tab")
                }
            }
            TabBar(items = items, selection = selected, onSelectionChange = {
                selected = it
            }, scrollBehavior = scrollBehavior)
        }
        Box(Modifier.height(RAIL_HEIGHT)) {
            NavRail(items = items, selection = selected, onSelectionChange = {
                selected = it
            }, modifier = Modifier.fillMaxSize())
        }
        TabBar {
            destinations.forEach { name ->
                TabBarItem(
                    value = name,
                    selection = selected,
                    onSelectionChange = { selected = it },
                    icon = { Icon(plus, null) },
                    label = { Text(name) },
                )
            }
        }
        AdaptiveNavScaffold(
            items = items,
            selection = selected,
            onSelectionChange = { selected = it },
            modifier = Modifier.fillMaxWidth().height(ADAPTIVE_HEIGHT),
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("$selected example. Resize the window to switch navigation layout.")
            }
        }
        AppText("AppText in the TITLE role", style = AppTextStyle.TITLE)
        AppText("AppText in the LABEL_MEDIUM_SEMIBOLD role", style = AppTextStyle.LABEL_MEDIUM_SEMIBOLD)
    }
}

private val LIST_HEIGHT = 320.dp
private val RAIL_HEIGHT = 200.dp
private const val LIST_ROWS = 20

@Composable
private fun PagingDemo() {
    var page by remember { mutableIntStateOf(0) }
    SectionCard(title = "Paging and flip card", description = "Settled on page ${page + 1}.") {
        PaginatedContent(
            pages = listOf(PaginationPage("Welcome"), PaginationPage("Explore"), PaginationPage("Finish")),
            titleAlignment = PageTitleAlignment.CENTER,
            footerStyle = PageFooterStyle.PROGRESS,
            onPageChanged = { page = it },
            pagePositionDescription = { index, count -> "Step ${index + 1} of $count" },
        ) { _, item ->
            Box(Modifier.fillMaxWidth().height(PAGE_HEIGHT), contentAlignment = Alignment.Center) {
                Text("${item.title} page", style = MaterialTheme.typography.titleMedium)
            }
        }
        FlipCard(
            front = { Text("What is on the other side?", Modifier.align(Alignment.Center)) },
            back = { Text("An example answer", Modifier.align(Alignment.Center)) },
            modifier = Modifier.fillMaxWidth().height(PAGE_HEIGHT),
            frontLabel = "Question",
            backLabel = "Answer",
            flipLabel = "Flip card",
        )
        StandalonePageIndicator()
    }
}

@Composable
private fun StandalonePageIndicator() {
    var page by remember { mutableIntStateOf(0) }
    FlowLayout {
        SecondaryButton(text = "Previous", enabled = page > 0, onClick = { page-- })
        PageIndicator(
            pageCount = INDICATOR_PAGES,
            currentPage = page,
            pagePositionDescription = { index, count -> "Item ${index + 1} of $count" },
        )
        SecondaryButton(text = "Next", enabled = page < INDICATOR_PAGES - 1, onClick = { page++ })
    }
}

private const val INDICATOR_PAGES = 5

private val PAGE_HEIGHT = 120.dp

@Composable
private fun FeedDemo() {
    val loaded = remember { mutableStateListOf(*Array(PAGE_SIZE) { "Report ${it + 1}" }) }
    var loading by remember { mutableStateOf(false) }
    var requested by remember { mutableIntStateOf(0) }
    var showAll by remember { mutableStateOf(false) }
    LaunchedLoad(requested) {
        loading = true
        delay(LOAD_MILLIS)
        repeat(PAGE_SIZE) { loaded.add("Report ${loaded.size + 1}") }
        loading = false
    }
    SectionCard(title = "Showcase feed", description = "Horizontal rows plus a vertical section that loads more.") {
        ShowcaseRow(items = listOf("First", "Second", "Third")) { name ->
            ThemeCard { Text(name) }
        }
        ShowcaseFeed(Modifier.heightIn(max = FEED_HEIGHT)) {
            section(title = "Featured items", items = listOf("Alpha", "Beta", "Gamma", "Delta")) { name ->
                ThemeCard { Text(name) }
            }
            section(
                title = "Collections",
                items = if (showAll) listOf("Spring", "Summer", "Autumn", "Winter") else listOf("Spring", "Summer"),
                itemWidth = ShowcaseItemWidth.Fixed(COLLECTION_WIDTH),
                actionLabel = if (showAll) "Show fewer" else "See all",
                onAction = { showAll = !showAll },
            ) { name -> ThemeCard { Text(name) } }
            verticalSection(
                title = "Reports",
                items = loaded.toList(),
                key = "reports",
                hasMore = loaded.size < MAX_REPORTS,
                isLoadingMore = loading,
                onLoadMore = { if (!loading) requested++ },
            ) { name ->
                ThemeListItem(headline = name, modifier = Modifier.padding(horizontal = MaterialTheme.kmpSpacing.small))
            }
        }
    }
}

@Composable
private fun LaunchedLoad(trigger: Int, load: suspend () -> Unit) {
    LaunchedEffect(trigger) { if (trigger > 0) load() }
}

private const val PAGE_SIZE = 8
private const val MAX_REPORTS = 40
private const val LOAD_MILLIS = 800L
private val FEED_HEIGHT = 480.dp
private val COLLECTION_WIDTH = 160.dp

private val ADAPTIVE_HEIGHT = 320.dp
