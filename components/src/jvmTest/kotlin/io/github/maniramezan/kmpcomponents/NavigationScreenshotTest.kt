package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.RoborazziOptions
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

// Golden images of the navigation family: lists, paging, showcase rows, flip cards, and bars.
@OptIn(ExperimentalTestApi::class)
class NavigationScreenshotTest {
    @Test
    fun `navigation in light mode`() = capture("navigation-light", ThemeMode.LIGHT)

    @Test
    fun `navigation in dark mode`() = capture("navigation-dark", ThemeMode.DARK)

    @Test
    fun `navigation at double font scale`() = capture("navigation-font-200", ThemeMode.LIGHT, fontScale = 2f)

    private fun capture(name: String, mode: ThemeMode, fontScale: Float = 1f) = runDesktopComposeUiTest(
        width = WINDOW_WIDTH,
        height = WINDOW_HEIGHT,
    ) {
        setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                KmpTheme(mode = mode) {
                    Surface(Modifier.width(GALLERY_WIDTH)) { NavigationGallery() }
                }
            }
        }
        onRoot().captureRoboImage("$SNAPSHOT_DIR/$name.png", roborazziOptions = OPTIONS)
    }

    private companion object {
        const val SNAPSHOT_DIR = "src/jvmTest/snapshots"
        const val WINDOW_WIDTH = 480
        const val WINDOW_HEIGHT = 2000
        val GALLERY_WIDTH = WINDOW_WIDTH.dp

        // Allow antialiasing noise between machines; real regressions change far more pixels.
        val OPTIONS = RoborazziOptions(compareOptions = RoborazziOptions.CompareOptions(changeThreshold = 0.01f))
    }
}

@Composable
private fun NavigationGallery() {
    val spacing = MaterialTheme.kmpSpacing
    val icon = rememberVectorPainter(TestPlusIcon)
    Column(verticalArrangement = Arrangement.spacedBy(spacing.small)) {
        ThemeTopAppBar(title = "Inbox", actions = {
            ThemeIconButton(icon = icon, contentDescription = "Add", onClick = {})
        })
        ThemeTabRow(tabs = listOf("All", "Unread", "Flagged"), selectedIndex = 1, onItemSelected = {})
        ThemeListItem(headline = "ThemeListItem", supportingText = "Supporting text")
        AppText("AppText TITLE", Modifier.padding(horizontal = spacing.medium), style = AppTextStyle.TITLE)
        AppText(
            "AppText LABEL_MEDIUM_BOLD",
            Modifier.padding(horizontal = spacing.medium),
            style = AppTextStyle.LABEL_MEDIUM_BOLD,
        )
        PaginatedContent(
            pages = listOf(PaginationPage("Intro"), PaginationPage("Setup"), PaginationPage("Done")),
            showScrollHint = false,
        ) { _, page ->
            Box(Modifier.fillMaxWidth().height(PAGE_HEIGHT), contentAlignment = Alignment.Center) { Text(page.title) }
        }
        ShowcaseRow(items = listOf("One", "Two", "Three")) { item ->
            ThemeCard { Text(item) }
        }
        FlipCard(
            front = { Text("Front", Modifier.align(Alignment.Center)) },
            back = { Text("Back", Modifier.align(Alignment.Center)) },
            modifier = Modifier.padding(horizontal = spacing.medium).fillMaxWidth().height(PAGE_HEIGHT),
        )
        TabBar(
            items = listOf("Home", "Search", "Settings").map { name ->
                TabBarItemData(
                    value = name,
                    icon = { Icon(icon, null) },
                    label = { Text(name) },
                    badge = if (name == "Search") ({ CountBadge(3) }) else null,
                )
            },
            selection = "Home",
            onSelectionChange = {},
        )
        Box(Modifier.height(RAIL_HEIGHT)) {
            NavRail(
                items = listOf("Home", "Search").map { name ->
                    TabBarItemData(name, icon = { Icon(icon, null) }, label = { Text(name) })
                },
                selection = "Search",
                onSelectionChange = {},
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

private val PAGE_HEIGHT = 80.dp
private val RAIL_HEIGHT = 200.dp
