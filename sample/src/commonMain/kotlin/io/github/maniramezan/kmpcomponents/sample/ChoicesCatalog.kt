package io.github.maniramezan.kmpcomponents.sample

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.maniramezan.kmpcomponents.KeyValueRow
import io.github.maniramezan.kmpcomponents.RadioGroup
import io.github.maniramezan.kmpcomponents.SecondaryButton
import io.github.maniramezan.kmpcomponents.SectionCard
import io.github.maniramezan.kmpcomponents.SelectionList
import io.github.maniramezan.kmpcomponents.SelectionListContent
import io.github.maniramezan.kmpcomponents.SelectionListNode
import io.github.maniramezan.kmpcomponents.ThemeSlider
import io.github.maniramezan.kmpcomponents.ThemeTextButton
import kotlin.math.roundToInt

/** Radio groups, sliders, and selection lists. */
@Composable
fun ChoicesSection() {
    ControlsDemo()
    SelectionListDemo()
}

@Composable
private fun ControlsDemo() {
    var scheme by remember { mutableIntStateOf(1) }
    var timeout by remember { mutableFloatStateOf(DEFAULT_TIMEOUT) }
    SectionCard(title = "Radio group and slider") {
        RadioGroup(options = listOf("HTTP", "HTTPS"), selectedIndex = scheme, onOptionSelected = { scheme = it })
        val seconds = timeout.roundToInt()
        ThemeSlider(
            value = timeout,
            onValueChange = { timeout = it },
            valueRange = 0f..MAX_TIMEOUT,
            steps = TIMEOUT_STEPS,
            label = "Timeout",
            valueDescription = "$seconds seconds",
        )
        Text("Timeout: $seconds s", style = MaterialTheme.typography.bodyMedium)
    }
}

private const val DEFAULT_TIMEOUT = 30f
private const val MAX_TIMEOUT = 60f
private const val TIMEOUT_STEPS = 11

private val languages = listOf(
    SelectionListNode(
        id = "en",
        title = "English",
        children = listOf(
            SelectionListNode("en-US", "United States", subtitle = "en-US"),
            SelectionListNode("en-GB", "United Kingdom", subtitle = "en-GB"),
        ),
    ),
    SelectionListNode("de", "Deutsch", subtitle = "de"),
    SelectionListNode("es", "Español", subtitle = "es"),
    SelectionListNode(
        id = "pt",
        title = "Português",
        children = listOf(
            SelectionListNode("pt-BR", "Brasil", subtitle = "pt-BR"),
            SelectionListNode("pt-PT", "Portugal", subtitle = "pt-PT"),
        ),
    ),
)

@Composable
private fun SelectionListDemo() {
    var language by remember { mutableStateOf<String?>("en-GB") }
    var extras by remember { mutableStateOf(setOf("de")) }
    var showSheet by remember { mutableStateOf(false) }
    SectionCard(
        title = "Selection list",
        description = "SelectionListContent inline (multiple choice) and SelectionList in a bottom sheet " +
            "(single choice, dismisses on tap).",
    ) {
        KeyValueRow(key = "Language", value = language ?: "None", modifier = Modifier.fillMaxWidth())
        SecondaryButton(text = "Choose language…", onClick = { showSheet = true })
        SelectionListContent(
            title = "Also translate to",
            nodes = languages,
            selectedIds = extras,
            onSelect = { id -> extras = if (id in extras) extras - id else extras + id },
            modifier = Modifier.heightIn(max = INLINE_LIST_HEIGHT),
            isSearchable = true,
            confirmButton = { ThemeTextButton(text = "Clear", onClick = { extras = emptySet() }) },
        )
    }
    if (showSheet) {
        SelectionList(
            title = "Language",
            nodes = languages,
            selectedId = language,
            onSelect = { language = it },
            onDismissRequest = { showSheet = false },
            isSearchable = true,
        )
    }
}

private val INLINE_LIST_HEIGHT = 360.dp
