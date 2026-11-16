package io.github.maniramezan.kmpcomponents.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.maniramezan.kmpcomponents.AdaptiveContentContainer
import io.github.maniramezan.kmpcomponents.AsyncContent
import io.github.maniramezan.kmpcomponents.AsyncContentState
import io.github.maniramezan.kmpcomponents.BottomSheet
import io.github.maniramezan.kmpcomponents.DisclosureCard
import io.github.maniramezan.kmpcomponents.ErrorState
import io.github.maniramezan.kmpcomponents.FlowLayout
import io.github.maniramezan.kmpcomponents.LoadingState
import io.github.maniramezan.kmpcomponents.OverlayCard
import io.github.maniramezan.kmpcomponents.PrimaryButton
import io.github.maniramezan.kmpcomponents.SecondaryButton
import io.github.maniramezan.kmpcomponents.SectionCard
import io.github.maniramezan.kmpcomponents.SegmentedControl
import io.github.maniramezan.kmpcomponents.Skeleton
import io.github.maniramezan.kmpcomponents.SkeletonBlock
import io.github.maniramezan.kmpcomponents.ThemeCard
import io.github.maniramezan.kmpcomponents.ThemeDialog
import io.github.maniramezan.kmpcomponents.ThemeHorizontalDivider
import io.github.maniramezan.kmpcomponents.ThemeListItem
import io.github.maniramezan.kmpcomponents.ThemeSnackbar
import io.github.maniramezan.kmpcomponents.ThemeSurface
import io.github.maniramezan.kmpcomponents.ThemeTextButton
import io.github.maniramezan.kmpcomponents.ThemeVerticalDivider
import io.github.maniramezan.kmpcomponents.Toast
import io.github.maniramezan.kmpcomponents.ToastHost
import io.github.maniramezan.kmpcomponents.TonalBadge
import io.github.maniramezan.kmpcomponents.kmpSpacing
import io.github.maniramezan.kmpcomponents.rememberToastHostState
import io.github.maniramezan.kmpcomponents.skeletonShimmer
import kotlinx.coroutines.launch

/** Cards, overlays, dialogs, toasts, placeholders, and layout helpers. */
@Composable
fun ContainersSection() {
    CardsDemo()
    OverlaysDemo()
    PlaceholdersDemo()
    AsyncContentDemo()
}

@Composable
private fun CardsDemo() {
    var opened by remember { mutableStateOf(0) }
    var expanded by remember { mutableStateOf(false) }
    SectionCard(title = "Cards and layout") {
        ThemeCard(onClick = { opened++ }) {
            Text("ThemeCard", style = MaterialTheme.typography.titleMedium)
            Text("Opened $opened times", style = MaterialTheme.typography.bodySmall)
        }
        ThemeSurface { Text("ThemeSurface: a flat, surface-colored container.") }
        DisclosureCard(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            summary = { Text("Advanced settings") },
            detail = { Text("Additional example content appears here.") },
        )
        ThemeHorizontalDivider()
        Row(Modifier.height(DIVIDER_HEIGHT)) {
            Text("Start")
            ThemeVerticalDivider(Modifier.padding(horizontal = MaterialTheme.kmpSpacing.small))
            Text("End")
        }
        FlowLayout {
            listOf("One", "Two", "Three", "Four", "Five", "Six", "Seven").forEach { TonalBadge(it) }
        }
        AdaptiveContentContainer(Modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
            Text(
                "AdaptiveContentContainer caps this text at the form width on wide windows.",
                Modifier.background(
                    MaterialTheme.colorScheme.secondaryContainer,
                ).padding(MaterialTheme.kmpSpacing.small),
            )
        }
        Box(Modifier.fillMaxWidth().height(OVERLAY_HEIGHT).background(MaterialTheme.colorScheme.tertiary)) {
            OverlayCard(Modifier.align(Alignment.BottomStart).padding(MaterialTheme.kmpSpacing.small)) {
                Text("OverlayCard on media")
            }
        }
    }
}

private val OVERLAY_HEIGHT = 120.dp

@Composable
private fun OverlaysDemo() {
    var showDialog by remember { mutableStateOf(false) }
    var showSheet by remember { mutableStateOf(false) }
    val toasts = rememberToastHostState()
    val scope = rememberCoroutineScope()
    var toastResult by remember { mutableStateOf("") }
    SectionCard(title = "Dialogs, sheets, and toasts", description = toastResult.ifEmpty { null }) {
        FlowLayout {
            SecondaryButton(text = "Dialog", onClick = { showDialog = true })
            SecondaryButton(text = "Bottom sheet", onClick = { showSheet = true })
            SecondaryButton(
                text = "Toast",
                onClick = {
                    scope.launch {
                        toastResult =
                            "Toast: ${toasts.showToast("Example toast", actionLabel = "Undo")}"
                    }
                },
            )
        }
        Toast(message = "Toast placed inline", actionLabel = "Undo", onAction = {
            toastResult =
                "Inline toast action selected"
        })
        ThemeSnackbar(message = "Example item removed", actionLabel = "Undo", onAction = {
            toastResult =
                "Snackbar action selected"
        })
        Box(Modifier.fillMaxWidth().height(TOAST_AREA_HEIGHT)) { ToastHost(hostState = toasts) }
    }
    if (showDialog) {
        ThemeDialog(
            title = "Export finished",
            text = "The example operation has finished.",
            confirmText = "OK",
            onConfirm = { showDialog = false },
            onDismissRequest = { showDialog = false },
        )
    }
    if (showSheet) {
        BottomSheet(onDismissRequest = { showSheet = false }) {
            Text("Bottom sheet", style = MaterialTheme.typography.titleLarge)
            Text("Always opens fully expanded.")
            PrimaryButton(text = "Close", onClick = { showSheet = false })
        }
    }
}

private val TOAST_AREA_HEIGHT = 96.dp

private enum class PaneState(val label: String) { LOADING("Loading"), SKELETON("Skeleton"), ERROR("Error") }

@Composable
private fun PlaceholdersDemo() {
    var state by remember { mutableStateOf(PaneState.SKELETON) }
    SectionCard(title = "Placeholders") {
        SegmentedControl(
            options = PaneState.entries.map { it.label },
            selectedIndex = state.ordinal,
            onOptionSelected = { state = PaneState.entries[it] },
        )
        when (state) {
            PaneState.LOADING -> LoadingState(label = "Loading reports")

            PaneState.SKELETON -> Column(
                modifier = Modifier.skeletonShimmer(),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small),
            ) {
                repeat(SKELETON_ROWS) {
                    SkeletonBlock(height = SKELETON_HEADER_HEIGHT, width = SKELETON_HEADER_WIDTH)
                    Skeleton()
                }
            }

            PaneState.ERROR -> ErrorState(
                title = "Could not load reports",
                message = "The server did not respond.",
                action = { ThemeTextButton(text = "Retry", onClick = { state = PaneState.LOADING }) },
            )
        }
    }
}

private enum class AsyncDemoState(val label: String) {
    LOADING("Loading"),
    EMPTY("Empty"),
    ERROR("Error"),
    CONTENT("Content"),
}

@Composable
private fun AsyncContentDemo() {
    var shown by remember { mutableStateOf(AsyncDemoState.CONTENT) }
    val state: AsyncContentState<List<String>> = when (shown) {
        AsyncDemoState.LOADING -> AsyncContentState.Loading
        AsyncDemoState.EMPTY -> AsyncContentState.Empty
        AsyncDemoState.ERROR -> AsyncContentState.Error()
        AsyncDemoState.CONTENT -> AsyncContentState.Content(listOf("First report", "Second report"))
    }
    SectionCard(title = "AsyncContent") {
        SegmentedControl(
            options = AsyncDemoState.entries.map { it.label },
            selectedIndex = shown.ordinal,
            onOptionSelected = { shown = AsyncDemoState.entries[it] },
        )
        AsyncContent(
            state = state,
            loadingLabel = "Loading reports",
            emptyTitle = "No reports yet",
            errorTitle = "Could not load reports",
            errorMessage = "The server did not respond.",
            emptyMessage = "Reports you create appear here.",
            emptyAction = { ThemeTextButton(text = "Create report", onClick = { shown = AsyncDemoState.CONTENT }) },
            errorAction = { ThemeTextButton(text = "Retry", onClick = { shown = AsyncDemoState.LOADING }) },
        ) { reports ->
            Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small)) {
                reports.forEach { ThemeListItem(headline = it) }
            }
        }
    }
}

private const val SKELETON_ROWS = 3
private val SKELETON_HEADER_HEIGHT = 24.dp
private val SKELETON_HEADER_WIDTH = 160.dp

private val DIVIDER_HEIGHT = 40.dp
