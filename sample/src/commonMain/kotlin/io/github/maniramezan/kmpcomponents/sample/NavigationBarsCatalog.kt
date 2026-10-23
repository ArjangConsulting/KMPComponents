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
import io.github.maniramezan.kmpcomponents.AppText
import io.github.maniramezan.kmpcomponents.AppTextStyle
import io.github.maniramezan.kmpcomponents.CountBadge
import io.github.maniramezan.kmpcomponents.FlipCard
import io.github.maniramezan.kmpcomponents.NavRail
import io.github.maniramezan.kmpcomponents.PageFooterStyle
import io.github.maniramezan.kmpcomponents.PageTitleAlignment
import io.github.maniramezan.kmpcomponents.PaginatedContent
import io.github.maniramezan.kmpcomponents.PaginationPage
import io.github.maniramezan.kmpcomponents.SectionCard
import io.github.maniramezan.kmpcomponents.ShowcaseFeed
import io.github.maniramezan.kmpcomponents.ShowcaseItemWidth
import io.github.maniramezan.kmpcomponents.TabBar
import io.github.maniramezan.kmpcomponents.TabBarItemData
import io.github.maniramezan.kmpcomponents.ThemeCard
import io.github.maniramezan.kmpcomponents.ThemeListItem
import io.github.maniramezan.kmpcomponents.ThemeMediumTopAppBar
import io.github.maniramezan.kmpcomponents.ThemeTabRow
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
        ThemeMediumTopAppBar(title = selected)
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
            pages = listOf(PaginationPage("Welcome"), PaginationPage("Connect"), PaginationPage("Record")),
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
            front = { Text("What does 404 mean?", Modifier.align(Alignment.Center)) },
            back = { Text("Not found", Modifier.align(Alignment.Center)) },
            modifier = Modifier.fillMaxWidth().height(PAGE_HEIGHT),
            frontLabel = "Question",
            backLabel = "Answer",
            flipLabel = "Flip card",
        )
    }
}

private val PAGE_HEIGHT = 120.dp

@Composable
private fun FeedDemo() {
    val loaded = remember { mutableStateListOf(*Array(PAGE_SIZE) { "Report ${it + 1}" }) }
    var loading by remember { mutableStateOf(false) }
    var requested by remember { mutableIntStateOf(0) }
    LaunchedLoad(requested) {
        loading = true
        delay(LOAD_MILLIS)
        repeat(PAGE_SIZE) { loaded.add("Report ${loaded.size + 1}") }
        loading = false
    }
    SectionCard(title = "Showcase feed", description = "Horizontal rows plus a vertical section that loads more.") {
        ShowcaseFeed(Modifier.heightIn(max = FEED_HEIGHT)) {
            section(title = "Recent projects", items = listOf("Payments", "Orders", "Users", "Webhooks")) { name ->
                ThemeCard { Text(name) }
            }
            section(
                title = "Devices",
                items = listOf("iPhone 16", "Pixel 9", "iPad Air", "Galaxy Tab"),
                itemWidth = ShowcaseItemWidth.Fixed(DEVICE_WIDTH),
                actionLabel = "See all",
                onAction = {},
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
private val DEVICE_WIDTH = 160.dp
