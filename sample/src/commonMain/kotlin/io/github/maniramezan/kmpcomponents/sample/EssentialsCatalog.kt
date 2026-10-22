package io.github.maniramezan.kmpcomponents.sample

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.state.ToggleableState
import io.github.maniramezan.kmpcomponents.ContentRow
import io.github.maniramezan.kmpcomponents.InlineProgressIndicator
import io.github.maniramezan.kmpcomponents.KeyValueRow
import io.github.maniramezan.kmpcomponents.MenuPicker
import io.github.maniramezan.kmpcomponents.NoticeCard
import io.github.maniramezan.kmpcomponents.ProgressIndicator
import io.github.maniramezan.kmpcomponents.SearchField
import io.github.maniramezan.kmpcomponents.SectionCard
import io.github.maniramezan.kmpcomponents.SectionHeader
import io.github.maniramezan.kmpcomponents.StatusTone
import io.github.maniramezan.kmpcomponents.ThemeCheckbox
import io.github.maniramezan.kmpcomponents.ThemeSwitch
import io.github.maniramezan.kmpcomponents.ThemeTriStateCheckbox
import io.github.maniramezan.kmpcomponents.kmpTypography

/** Headers, rows, search, pickers, progress, notices, and toggle rows. */
@Composable
fun EssentialsSection() {
    ListsDemo()
    FormsDemo()
    FeedbackDemo()
}

@Composable
private fun ListsDemo() {
    val items = remember { mutableStateListOf("Users", "Orders", "Payments", "Webhooks") }
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf("Users") }
    val shown = items.filter { it.contains(query, ignoreCase = true) }
    SectionCard(title = "Lists and search") {
        SectionHeader(
            title = "Endpoints",
            subtitle = "${shown.size} of ${items.size} shown",
            actionLabel = "Add",
            onAction = { items.add("Endpoint ${items.size + 1}") },
        )
        SearchField(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth())
        shown.forEach { name ->
            ContentRow(
                title = name,
                modifier = Modifier.fillMaxWidth(),
                secondaryText = "GET",
                supportingText = "/${name.lowercase()}",
                onClick = { selected = name },
                selected = selected == name,
                trailingContent = { TextButton(onClick = { items.remove(name) }) { Text("Remove") } },
            )
        }
    }
}

private enum class Language(val label: String) { KOTLIN("Kotlin"), SWIFT("Swift"), TYPESCRIPT("TypeScript") }

@Composable
private fun FormsDemo() {
    var language by remember { mutableStateOf(Language.KOTLIN) }
    var record by remember { mutableStateOf(true) }
    val headers = remember { mutableStateListOf(true, false, true) }
    val allState = when {
        headers.all { it } -> ToggleableState.On
        headers.none { it } -> ToggleableState.Off
        else -> ToggleableState.Indeterminate
    }
    SectionCard(title = "Pickers and toggles") {
        MenuPicker(
            items = Language.entries,
            selected = language,
            onSelect = { language = it },
            label = "Language",
            itemLabel = Language::label,
            modifier = Modifier.fillMaxWidth(),
        )
        ThemeSwitch(
            checked = record,
            onCheckedChange = { record = it },
            label = "Record traffic",
            modifier = Modifier.fillMaxWidth(),
            supportingText = "Saves each request to the project",
            tooltip = "Recorded requests can be replayed as mocks.",
        )
        ThemeTriStateCheckbox(
            state = allState,
            onClick = {
                val next = allState != ToggleableState.On
                headers.indices.forEach { headers[it] = next }
            },
            label = "All headers",
        )
        listOf("Accept", "Authorization", "Content-Type").forEachIndexed { index, name ->
            ThemeCheckbox(checked = headers[index], onCheckedChange = { headers[index] = it }, label = name)
        }
        KeyValueRow(key = "Language", value = language.label, modifier = Modifier.fillMaxWidth())
        KeyValueRow(
            key = "Output",
            value = "build/generated/${language.name.lowercase()}",
            modifier = Modifier.fillMaxWidth(),
            valueStyle = MaterialTheme.kmpTypography.code,
        )
    }
}

@Composable
private fun FeedbackDemo() {
    var noticeVisible by remember { mutableStateOf(true) }
    SectionCard(title = "Progress and notices") {
        InlineProgressIndicator(label = "Booting device…")
        ProgressIndicator(label = "Downloading runtime", modifier = Modifier.fillMaxWidth(), progress = PROGRESS)
        if (noticeVisible) {
            NoticeCard(
                message = "The device stopped responding.",
                tone = StatusTone.ERROR,
                title = "Device disconnected",
                actionLabel = "Retry",
                onAction = {},
                onDismiss = { noticeVisible = false },
            )
        } else {
            TextButton(onClick = { noticeVisible = true }) { Text("Show notice again") }
        }
        NoticeCard(
            message = "Add an AI provider to generate tests.",
            tone = StatusTone.WARNING,
            actionLabel = "Open settings",
            onAction = {},
        )
    }
}

private const val PROGRESS = 0.4f
