package io.github.maniramezan.kmpcomponents.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import io.github.maniramezan.kmpcomponents.Avatar
import io.github.maniramezan.kmpcomponents.AvatarSize
import io.github.maniramezan.kmpcomponents.ContentRow
import io.github.maniramezan.kmpcomponents.FlowLayout
import io.github.maniramezan.kmpcomponents.FormTextField
import io.github.maniramezan.kmpcomponents.InlineProgressIndicator
import io.github.maniramezan.kmpcomponents.KeyValueRow
import io.github.maniramezan.kmpcomponents.LazyList
import io.github.maniramezan.kmpcomponents.LoadMoreFooter
import io.github.maniramezan.kmpcomponents.MenuPicker
import io.github.maniramezan.kmpcomponents.NoticeCard
import io.github.maniramezan.kmpcomponents.ProgressIndicator
import io.github.maniramezan.kmpcomponents.SearchField
import io.github.maniramezan.kmpcomponents.SectionCard
import io.github.maniramezan.kmpcomponents.SectionHeader
import io.github.maniramezan.kmpcomponents.StatusTone
import io.github.maniramezan.kmpcomponents.ThemeCheckbox
import io.github.maniramezan.kmpcomponents.ThemeListItem
import io.github.maniramezan.kmpcomponents.ThemeSwitch
import io.github.maniramezan.kmpcomponents.ThemeTriStateCheckbox
import io.github.maniramezan.kmpcomponents.kmpTypography

/** Headers, rows, avatars, search, pickers, progress, notices, and toggle rows. */
@Composable
fun EssentialsSection() {
    ListsDemo()
    AvatarsDemo()
    LazyListDemo()
    FormsDemo()
    FeedbackDemo()
}

@Composable
private fun ListsDemo() {
    val items = remember { mutableStateListOf("Alpha", "Beta", "Gamma", "Delta") }
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf("Alpha") }
    val shown = items.filter { it.contains(query, ignoreCase = true) }
    SectionCard(title = "Lists and search") {
        SectionHeader(
            title = "Example items",
            subtitle = "${shown.size} of ${items.size} shown",
            actionLabel = "Add",
            onAction = { items.add("Item ${items.size + 1}") },
        )
        SearchField(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth())
        shown.forEach { name ->
            ContentRow(
                title = name,
                modifier = Modifier.fillMaxWidth(),
                secondaryText = "Example",
                supportingText = "Select or remove this item",
                leadingContent = { Avatar(name = name, size = AvatarSize.SMALL) },
                onClick = { selected = name },
                selected = selected == name,
                trailingContent = { TextButton(onClick = { items.remove(name) }) { Text("Remove") } },
            )
        }
    }
}

@Composable
private fun AvatarsDemo() {
    SectionCard(
        title = "Avatars",
        description = "Initials come from the first and last words of the name; sizes follow the text size.",
    ) {
        FlowLayout {
            Avatar(name = "Example Person", size = AvatarSize.SMALL)
            Avatar(name = "Another Example")
            Avatar(name = "Third Example Name", size = AvatarSize.LARGE)
            Avatar(name = "Image avatar", size = AvatarSize.LARGE) {
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.tertiary))
            }
        }
    }
}

@Composable
private fun LazyListDemo() {
    var showLoading by remember { mutableStateOf(false) }
    var footerEntries by remember { mutableStateOf(0) }
    SectionCard(title = "Lazy list and load-more footer") {
        ThemeSwitch(checked = showLoading, onCheckedChange = { showLoading = it }, label = "Show loading footer")
        Text("Footer entered $footerEntries times")
        LazyList(Modifier.height(LAZY_LIST_HEIGHT)) {
            items(8) { ThemeListItem(headline = "Item ${it + 1}", supportingText = "Example list content") }
            item { LoadMoreFooter(isLoadingMore = showLoading, onLoadMore = { footerEntries++ }) }
        }
    }
}

private val LAZY_LIST_HEIGHT = 220.dp

private enum class Language(val label: String) { KOTLIN("Kotlin"), SWIFT("Swift"), TYPESCRIPT("TypeScript") }

@Composable
private fun FormsDemo() {
    var language by remember { mutableStateOf(Language.KOTLIN) }
    var pickerEnabled by remember { mutableStateOf(true) }
    var exampleText by remember { mutableStateOf("") }
    var notifications by remember { mutableStateOf(true) }
    val options = remember { mutableStateListOf(true, false, true) }
    val allState = when {
        options.all { it } -> ToggleableState.On
        options.none { it } -> ToggleableState.Off
        else -> ToggleableState.Indeterminate
    }
    SectionCard(title = "Pickers and toggles") {
        FormTextField(
            value = exampleText,
            onValueChange = { exampleText = it },
            label = "Example text",
            modifier = Modifier.fillMaxWidth(),
        )
        MenuPicker(
            items = Language.entries,
            selected = language,
            onSelect = { language = it },
            label = "Language",
            itemLabel = Language::label,
            enabled = pickerEnabled,
            modifier = Modifier.fillMaxWidth(),
        )
        PickerAvailabilityControls(enabled = pickerEnabled, onEnabledChange = { pickerEnabled = it })
        ThemeSwitch(
            checked = notifications,
            onCheckedChange = { notifications = it },
            label = "Example switch",
            modifier = Modifier.fillMaxWidth(),
            supportingText = "Changes this example’s local toggle state",
            tooltip = "Use the switch or its label to toggle the example.",
        )
        ThemeTriStateCheckbox(
            state = allState,
            onClick = {
                val next = allState != ToggleableState.On
                options.indices.forEach { options[it] = next }
            },
            label = "All options",
        )
        listOf("First option", "Second option", "Third option").forEachIndexed { index, name ->
            ThemeCheckbox(checked = options[index], onCheckedChange = { options[index] = it }, label = name)
        }
        KeyValueRow(key = "Language", value = language.label, modifier = Modifier.fillMaxWidth())
        KeyValueRow(
            key = "Identifier",
            value = "example.${language.name.lowercase()}",
            modifier = Modifier.fillMaxWidth(),
            valueStyle = MaterialTheme.kmpTypography.code,
        )
    }
}

@Composable
private fun FeedbackDemo() {
    var noticeVisible by remember { mutableStateOf(true) }
    var actionResult by remember { mutableStateOf("No action selected") }
    SectionCard(title = "Progress and notices", description = actionResult) {
        InlineProgressIndicator(label = "Working…")
        ProgressIndicator(label = "Example progress", modifier = Modifier.fillMaxWidth(), progress = PROGRESS)
        if (noticeVisible) {
            NoticeCard(
                message = "An example operation failed.",
                tone = StatusTone.ERROR,
                title = "Example error",
                actionLabel = "Retry",
                onAction = { actionResult = "Retry selected" },
                onDismiss = { noticeVisible = false },
            )
        } else {
            TextButton(onClick = { noticeVisible = true }) { Text("Show notice again") }
        }
        NoticeCard(
            message = "Review this example warning.",
            tone = StatusTone.WARNING,
            actionLabel = "Acknowledge",
            onAction = { actionResult = "Warning acknowledged" },
        )
    }
}

private const val PROGRESS = 0.4f
