package io.github.maniramezan.kmpcomponents.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import io.github.maniramezan.kmpcomponents.ActionPill
import io.github.maniramezan.kmpcomponents.FAB
import io.github.maniramezan.kmpcomponents.LoadingIconButton
import io.github.maniramezan.kmpcomponents.PillChip
import io.github.maniramezan.kmpcomponents.PrimaryButton
import io.github.maniramezan.kmpcomponents.SecondaryButton
import io.github.maniramezan.kmpcomponents.SectionCard
import io.github.maniramezan.kmpcomponents.SegmentDensity
import io.github.maniramezan.kmpcomponents.SegmentWidthMode
import io.github.maniramezan.kmpcomponents.SegmentedControl
import io.github.maniramezan.kmpcomponents.ThemeExtendedFloatingActionButton
import io.github.maniramezan.kmpcomponents.ThemeIconButton
import io.github.maniramezan.kmpcomponents.ThemeSingleChoiceSegmentedButtonRow
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
}

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
            SecondaryButton(text = "Test connection", onClick = { lastAction = "Test connection" })
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
                contentDescription = "Refresh providers",
                onClick = {
                    refreshing = true
                    scope.launch {
                        delay(REFRESH_MILLIS)
                        refreshing = false
                        lastAction = "Refreshed"
                    }
                },
            )
            FAB(icon = plus, contentDescription = "New project", onClick = { lastAction = "New project" })
            ThemeExtendedFloatingActionButton(text = "Record", icon = plus, onClick = { lastAction = "Record" })
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
            options = listOf("Headers", "Body", "Cookies", "Query"),
            selectedIndex = tab,
            onOptionSelected = { tab = it },
            density = SegmentDensity.COMPACT,
            widthMode = SegmentWidthMode.FIT,
        )
        ThemeSingleChoiceSegmentedButtonRow(
            options = listOf("JSON", "YAML", "XML"),
            selectedIndex = format,
            onOptionSelected = { format = it },
            modifier = Modifier.fillMaxWidth(),
        )
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
