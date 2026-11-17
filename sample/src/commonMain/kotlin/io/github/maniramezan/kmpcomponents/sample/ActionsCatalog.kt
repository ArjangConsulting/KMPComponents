package io.github.maniramezan.kmpcomponents.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import io.github.maniramezan.kmpcomponents.ActionPill
import io.github.maniramezan.kmpcomponents.ExpandableFAB
import io.github.maniramezan.kmpcomponents.FAB
import io.github.maniramezan.kmpcomponents.FABAction
import io.github.maniramezan.kmpcomponents.FABExpansionDirection
import io.github.maniramezan.kmpcomponents.LoadingIconButton
import io.github.maniramezan.kmpcomponents.PillChip
import io.github.maniramezan.kmpcomponents.PrimaryButton
import io.github.maniramezan.kmpcomponents.SecondaryButton
import io.github.maniramezan.kmpcomponents.SectionCard
import io.github.maniramezan.kmpcomponents.SegmentDensity
import io.github.maniramezan.kmpcomponents.SegmentSelectionIndicator
import io.github.maniramezan.kmpcomponents.SegmentWidthMode
import io.github.maniramezan.kmpcomponents.SegmentedContent
import io.github.maniramezan.kmpcomponents.SegmentedControl
import io.github.maniramezan.kmpcomponents.SegmentedItem
import io.github.maniramezan.kmpcomponents.ThemeExtendedFloatingActionButton
import io.github.maniramezan.kmpcomponents.ThemeIconButton
import io.github.maniramezan.kmpcomponents.ThemeSingleChoiceSegmentedButtonRow
import io.github.maniramezan.kmpcomponents.ThemeSwitch
import io.github.maniramezan.kmpcomponents.ThemeTextButton
import io.github.maniramezan.kmpcomponents.kmpSpacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Buttons, segmented controls, and pills. */
@Composable
fun ActionsSection() {
    ButtonsDemo()
    SegmentsDemo()
    PillsDemo()
    ExpandableFABDemo()
}

@Composable
private fun ExpandableFABDemo() {
    var expanded by remember { mutableStateOf(false) }
    var direction by remember { mutableStateOf(FABExpansionDirection.UP) }
    var showLabels by remember { mutableStateOf(true) }
    var lastAction by remember { mutableStateOf("None yet") }
    val plus = rememberVectorPainter(PlusIcon)
    val refresh = rememberVectorPainter(RefreshIcon)
    val actions = remember(plus, refresh) {
        listOf(
            FABAction(id = "new", label = "New item", icon = plus, onClick = { lastAction = "New item" }),
            FABAction(id = "refresh", label = "Refresh", icon = refresh, onClick = { lastAction = "Refresh" }),
        )
    }
    SectionCard(title = "ExpandableFAB", description = "Last action: $lastAction") {
        SegmentedControl(
            options = FABExpansionDirection.entries.map { it.name.lowercase().replaceFirstChar(Char::uppercase) },
            selectedIndex = direction.ordinal,
            onOptionSelected = { direction = FABExpansionDirection.entries[it] },
            widthMode = SegmentWidthMode.FIT,
        )
        ThemeSwitch(checked = showLabels, onCheckedChange = { showLabels = it }, label = "Show action labels")
        val alignment = when (direction) {
            FABExpansionDirection.UP -> Alignment.BottomEnd
            FABExpansionDirection.DOWN -> Alignment.TopEnd
            FABExpansionDirection.START -> Alignment.CenterEnd
            FABExpansionDirection.END -> Alignment.CenterStart
        }
        // A surface backdrop, like the screen a FAB floats over, so the action pills stand out from the card.
        Box(
            Modifier
                .fillMaxWidth()
                .height(FAB_DEMO_HEIGHT)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surface)
                .padding(MaterialTheme.kmpSpacing.small),
            contentAlignment = alignment,
        ) {
            ExpandableFAB(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                actions = actions,
                icon = plus,
                contentDescription = "More actions",
                expandedStateDescription = "Expanded",
                collapsedStateDescription = "Collapsed",
                direction = direction,
                showActionLabels = showLabels,
            )
        }
    }
}

private val FAB_DEMO_HEIGHT = 220.dp

@Composable
private fun ButtonsDemo() {
    var lastAction by remember { mutableStateOf("None yet") }
    var refreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val plus = rememberVectorPainter(PlusIcon)
    SectionCard(title = "Buttons", description = "Last action: $lastAction") {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            PrimaryButton(text = "Save", onClick = { lastAction = "Save" })
            SecondaryButton(text = "Secondary action", onClick = { lastAction = "Secondary action" })
            ThemeTextButton(text = "Cancel", onClick = { lastAction = "Cancel" })
            ThemeTextButton(text = "Delete all", onClick = { lastAction = "Delete all" }, destructive = true)
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            ThemeIconButton(icon = plus, contentDescription = "Add", onClick = { lastAction = "Add" })
            LoadingIconButton(
                loading = refreshing,
                icon = rememberVectorPainter(RefreshIcon),
                contentDescription = "Refresh example",
                onClick = {
                    refreshing = true
                    scope.launch {
                        delay(REFRESH_MILLIS)
                        refreshing = false
                        lastAction = "Refreshed"
                    }
                },
            )
            FAB(icon = plus, contentDescription = "Add item", onClick = { lastAction = "Add item" })
            ThemeExtendedFloatingActionButton(text = "Create", icon = plus, onClick = { lastAction = "Create" })
        }
    }
}

private const val REFRESH_MILLIS = 1_500L

@Composable
private fun SegmentsDemo() {
    var range by remember { mutableIntStateOf(1) }
    var tab by remember { mutableIntStateOf(0) }
    var format by remember { mutableIntStateOf(0) }
    SectionCard(title = "Segmented controls") {
        SegmentedControl(
            options = listOf("Day", "Week", "Month"),
            selectedIndex = range,
            onOptionSelected = { range = it },
        )
        SegmentedControl(
            options = listOf("First", "Second", "Third", "Fourth"),
            selectedIndex = tab,
            onOptionSelected = { tab = it },
            density = SegmentDensity.COMPACT,
            widthMode = SegmentWidthMode.FIT,
        )
        ThemeSingleChoiceSegmentedButtonRow(
            options = listOf("Small", "Medium", "Large"),
            selectedIndex = format,
            onOptionSelected = { format = it },
            modifier = Modifier.fillMaxWidth(),
        )
        SegmentedContent(
            items = listOf("Overview", "Details", "Activity", "More information", "History").map(::SegmentedItem),
            indicator = SegmentSelectionIndicator.UNDERLINE,
            backgroundColor = MaterialTheme.colorScheme.surface,
        ) { _, item -> Text("${item.title} panel", Modifier.padding(MaterialTheme.kmpSpacing.small)) }
        SegmentedContent(items = listOf("First", "Second").map(::SegmentedItem)) { _, item ->
            Text("${item.title} panel", Modifier.padding(MaterialTheme.kmpSpacing.small))
        }
    }
}

@Composable
private fun PillsDemo() {
    val filters = listOf("Info", "Warnings", "Errors")
    val active = remember { mutableStateListOf("Errors") }
    var docsOpened by remember { mutableIntStateOf(0) }
    SectionCard(title = "Pills", description = "Docs opened $docsOpened times") {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            filters.forEach { name ->
                PillChip(
                    label = name,
                    isSelected = name in active,
                    onClick = { if (name in active) active.remove(name) else active.add(name) },
                )
            }
            PillChip(label = "Static", isSelected = false)
            ActionPill(onClick = { docsOpened++ }) {
                Icon(
                    rememberVectorPainter(PlusIcon),
                    contentDescription = null,
                    modifier = Modifier.size(MaterialTheme.kmpSpacing.medium),
                )
                Text("Open docs")
            }
        }
    }
}
