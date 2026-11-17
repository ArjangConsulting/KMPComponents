package io.github.maniramezan.kmpcomponents.sample

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.maniramezan.kmpcomponents.FormTextField
import io.github.maniramezan.kmpcomponents.MenuPicker
import io.github.maniramezan.kmpcomponents.QuestionnaireAnswer
import io.github.maniramezan.kmpcomponents.QuestionnaireChoiceStyle
import io.github.maniramezan.kmpcomponents.QuestionnaireLabels
import io.github.maniramezan.kmpcomponents.QuestionnaireOption
import io.github.maniramezan.kmpcomponents.QuestionnaireQuestion
import io.github.maniramezan.kmpcomponents.QuestionnaireQuestionKind
import io.github.maniramezan.kmpcomponents.QuestionnaireView
import io.github.maniramezan.kmpcomponents.SecondaryButton
import io.github.maniramezan.kmpcomponents.SectionCard
import io.github.maniramezan.kmpcomponents.ThemeCard
import io.github.maniramezan.kmpcomponents.ThemeSlider
import io.github.maniramezan.kmpcomponents.ThemeSwitch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlin.math.roundToInt
import kotlin.time.Clock

/** Independent examples make every input reachable; the last one chains all the others. */
internal enum class QuestionnaireExample(val title: String, val prompt: String, val detail: String) {
    SINGLE_CHOICE(
        "Single choice",
        "Which option do you prefer?",
        "Choose one option. Tap it again to clear your answer.",
    ),
    MULTIPLE_CHOICE("Multiple choice", "Which options apply?", "Select every option that applies."),
    FREE_TEXT(
        "Free-form text",
        "What would you like to add?",
        "Answer in your own words. Multiple lines are supported.",
    ),
    BOOLEAN(
        "Boolean choices",
        "Would you like to include this option?",
        "Choose Yes or No. Either choice answers the question.",
    ),
    NUMBER("Number", "How many items?", "Enter a number or use the buttons."),
    DATE("Date", "Which date do you prefer?", "The date starts at today. Open it to choose another day."),
    CUSTOM_COMPONENT(
        "Custom component example",
        "Which calendar date should be recorded?",
        "An inline calendar. Choose a date, then use Next or Done to continue.",
    ),
    RATING_SCALE(
        "Rating scale",
        "How would you rate this item?",
        "A labeled scale composed from a single-choice question.",
    ),
    ALL("Complete questionnaire", "", ""),
}

/** The sample's adjustable settings; the library's question model knows nothing about them. */
internal data class QuestionnaireExampleSettings(
    val prompt: String = "",
    val optionTitles: String = "Option A\nOption B\nOption C",
    val choiceStyle: QuestionnaireChoiceStyle = QuestionnaireChoiceStyle.AUTOMATIC,
    val required: Boolean = true,
    val showsDetail: Boolean = true,
    val placeholder: String = "Write your answer",
    val trueTitle: String = "Yes",
    val falseTitle: String = "No",
    val lowerBound: Double = 0.0,
    val upperBound: Double = 100.0,
    val step: Double = 5.0,
    val unit: String = "",
    val dateRangeMonths: Int = 6,
) {
    fun dateRange(today: LocalDate): ClosedRange<LocalDate> = today..today.plus(dateRangeMonths, DateTimeUnit.MONTH)

    fun questions(example: QuestionnaireExample, today: LocalDate): List<QuestionnaireQuestion> {
        val options = optionTitles.lines().map(String::trim).filter(String::isNotEmpty).map(::QuestionnaireOption)
        val examples = if (example ==
            QuestionnaireExample.ALL
        ) {
            QuestionnaireExample.entries - QuestionnaireExample.ALL
        } else {
            listOf(example)
        }
        return examples.map { item ->
            val kind = when (item) {
                QuestionnaireExample.SINGLE_CHOICE, QuestionnaireExample.ALL -> QuestionnaireQuestionKind.SingleChoice(
                    options,
                )

                QuestionnaireExample.MULTIPLE_CHOICE -> QuestionnaireQuestionKind.MultipleChoice(options)

                QuestionnaireExample.FREE_TEXT -> QuestionnaireQuestionKind.Text(placeholder.ifBlank { null })

                QuestionnaireExample.BOOLEAN -> QuestionnaireQuestionKind.Boolean(trueTitle, falseTitle)

                QuestionnaireExample.NUMBER ->
                    QuestionnaireQuestionKind.Number(lowerBound..upperBound, step, unit.ifBlank { null })

                QuestionnaireExample.DATE -> QuestionnaireQuestionKind.Date(dateRange(today))

                QuestionnaireExample.CUSTOM_COMPONENT -> QuestionnaireQuestionKind.Custom

                QuestionnaireExample.RATING_SCALE -> QuestionnaireQuestionKind.SingleChoice(
                    RATING_LABELS.mapIndexed { index, label ->
                        QuestionnaireOption("${index + 1}", "${index + 1} — $label")
                    },
                )
            }
            QuestionnaireQuestion(
                id = item.name,
                prompt = prompt.ifBlank { item.prompt },
                kind = kind,
                detail = item.detail.takeIf { showsDetail },
                isRequired = required,
                choiceStyle = choiceStyle,
            )
        }
    }

    private companion object {
        val RATING_LABELS = listOf("Very low", "Low", "Moderate", "High", "Very high")
    }
}

/** Every question kind, choice presentation, and a caller-supplied input in one questionnaire host. */
@Composable
fun QuestionnaireSection(today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())) {
    var example by remember { mutableStateOf(QuestionnaireExample.SINGLE_CHOICE) }
    var settings by remember { mutableStateOf(QuestionnaireExampleSettings()) }
    var allowsSkip by remember { mutableStateOf(true) }
    var submitTitle by remember { mutableStateOf("") }
    var generation by remember { mutableIntStateOf(0) }
    var answers by remember { mutableStateOf<Map<String, QuestionnaireAnswer>>(emptyMap()) }
    var lastEvent by remember { mutableStateOf("None yet") }
    val restart: () -> Unit = {
        answers = emptyMap()
        lastEvent = "None yet"
        generation++
    }

    SectionCard(title = "Questionnaire example") {
        MenuPicker(
            items = QuestionnaireExample.entries,
            selected = example,
            onSelect = {
                example = it
                restart()
            },
            label = "Example",
            itemLabel = QuestionnaireExample::title,
            modifier = Modifier.fillMaxWidth(),
        )
        ThemeCard(contentPadding = PaddingValues(0.dp)) {
            key(example, generation) {
                QuestionnaireView(
                    questions = settings.questions(example, today),
                    answers = answers,
                    onAnswersChange = { answers = it },
                    onSubmit = { lastEvent = "Submitted ${it.size} answer(s)" },
                    modifier = Modifier.fillMaxWidth().height(QUESTIONNAIRE_HEIGHT),
                    onSkip = if (allowsSkip) ({ lastEvent = "Skipped" }) else null,
                    labels = QuestionnaireLabels(done = submitTitle.ifBlank { "Done" }),
                    today = today,
                    customInput = { question, answer, onAnswerChange ->
                        QuestionnaireSampleCalendar(question.prompt, settings.dateRange(today), answer, onAnswerChange)
                    },
                )
            }
        }
        Text(
            text = "Last event: $lastEvent",
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            style = MaterialTheme.typography.bodySmall,
        )
    }

    SectionCard(title = "Question and presentation") {
        FormTextField(
            value = settings.prompt,
            onValueChange = { settings = settings.copy(prompt = it) },
            label = "Prompt (empty uses the example's)",
            modifier = Modifier.fillMaxWidth(),
        )
        ThemeSwitch(checked = settings.required, onCheckedChange = {
            settings = settings.copy(required = it)
        }, label = "Required answers")
        ThemeSwitch(checked = settings.showsDetail, onCheckedChange = {
            settings = settings.copy(showsDetail = it)
        }, label = "Detail text")
        MenuPicker(
            items = QuestionnaireChoiceStyle.entries,
            selected = settings.choiceStyle,
            onSelect = { settings = settings.copy(choiceStyle = it) },
            label = "Choice presentation",
            itemLabel = {
                when (it) {
                    QuestionnaireChoiceStyle.AUTOMATIC -> "Automatic rows"
                    QuestionnaireChoiceStyle.BULLETS -> "Bullet rows"
                    QuestionnaireChoiceStyle.CHECKMARKS -> "Checkmark rows"
                    QuestionnaireChoiceStyle.CHIPS -> "Wrapping chips"
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            "Presentation changes the indicators. The question kind controls single or multiple selection.",
            style = MaterialTheme.typography.bodySmall,
        )
        FormTextField(
            value = settings.optionTitles,
            onValueChange = { settings = settings.copy(optionTitles = it) },
            label = "Options (one per line)",
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
        )
        FormTextField(
            value = settings.placeholder,
            onValueChange = { settings = settings.copy(placeholder = it) },
            label = "Free-text placeholder",
            modifier = Modifier.fillMaxWidth(),
        )
        FormTextField(
            value = settings.trueTitle,
            onValueChange = { settings = settings.copy(trueTitle = it) },
            label = "True choice title",
            modifier = Modifier.fillMaxWidth(),
        )
        FormTextField(
            value = settings.falseTitle,
            onValueChange = { settings = settings.copy(falseTitle = it) },
            label = "False choice title",
            modifier = Modifier.fillMaxWidth(),
        )
    }

    SectionCard(title = "Number and date") {
        IntSlider("Lower bound", settings.lowerBound.roundToInt(), MIN_LOWER_BOUND..settings.upperBound.roundToInt()) {
            settings = settings.copy(lowerBound = it.toDouble())
        }
        IntSlider("Upper bound", settings.upperBound.roundToInt(), settings.lowerBound.roundToInt()..MAX_UPPER_BOUND) {
            settings = settings.copy(upperBound = it.toDouble())
        }
        IntSlider("Number step", settings.step.roundToInt(), 0..MAX_STEP) {
            settings =
                settings.copy(step = it.toDouble())
        }
        FormTextField(
            value = settings.unit,
            onValueChange = { settings = settings.copy(unit = it) },
            label = "Number unit",
            modifier = Modifier.fillMaxWidth(),
        )
        IntSlider("Date range in months", settings.dateRangeMonths, 1..MAX_DATE_MONTHS) {
            settings = settings.copy(dateRangeMonths = it)
        }
    }

    SectionCard(title = "Navigation and answers") {
        ThemeSwitch(checked = allowsSkip, onCheckedChange = { allowsSkip = it }, label = "Skip button")
        FormTextField(
            value = submitTitle,
            onValueChange = { submitTitle = it },
            label = "Submit title (empty uses Done)",
            modifier = Modifier.fillMaxWidth(),
        )
        SecondaryButton(text = "Restart", onClick = restart)
        Text(
            text = if (answers.isEmpty()) {
                "No answers"
            } else {
                answers.entries.sortedBy { it.key }.joinToString("\n") { (id, answer) -> "$id: $answer" }
            },
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

/** A slider over whole numbers whose label shows the current value. */
@Composable
private fun IntSlider(label: String, value: Int, range: IntRange, onValueChange: (Int) -> Unit) {
    ThemeSlider(
        value = value.toFloat(),
        onValueChange = { onValueChange(it.roundToInt()) },
        modifier = Modifier.fillMaxWidth(),
        valueRange = range.first.toFloat()..range.last.toFloat().coerceAtLeast(range.first.toFloat()),
        label = "$label: $value",
        valueDescription = "$value",
    )
}

/**
 * A caller-owned input: an inline calendar that records the day it shows as soon as it appears, and
 * each day the person picks after that.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun QuestionnaireSampleCalendar(
    label: String,
    range: ClosedRange<LocalDate>,
    answer: QuestionnaireAnswer?,
    onAnswerChange: (QuestionnaireAnswer?) -> Unit,
) {
    val shown = ((answer as? QuestionnaireAnswer.Date)?.value ?: range.start).coerceIn(range)
    val record by rememberUpdatedState(onAnswerChange)
    key(range) {
        val years = range.start.year..range.endInclusive.year
        val state = rememberDatePickerState(
            initialSelectedDateMillis = shown.utcMillis(),
            yearRange = years,
            selectableDates = remember {
                object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis.utcDate() in range

                    override fun isSelectableYear(year: Int): Boolean = year in years
                }
            },
        )
        LaunchedEffect(state) {
            snapshotFlow { state.selectedDateMillis }.collect { millis ->
                millis?.let { record(QuestionnaireAnswer.Date(it.utcDate().coerceIn(range))) }
            }
        }
        LaunchedEffect(shown) {
            if (state.selectedDateMillis != shown.utcMillis()) {
                state.selectedDateMillis = shown.utcMillis()
                state.displayedMonthMillis = shown.utcMillis()
            }
        }
        DatePicker(
            state = state,
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = label },
            title = null,
            headline = null,
            showModeToggle = false,
        )
    }
}

private const val MILLIS_PER_DAY = 86_400_000L

private fun LocalDate.utcMillis(): Long = toEpochDays() * MILLIS_PER_DAY

private fun Long.utcDate(): LocalDate = LocalDate.fromEpochDays(floorDiv(MILLIS_PER_DAY))

private val QUESTIONNAIRE_HEIGHT = 620.dp
private const val MIN_LOWER_BOUND = -100
private const val MAX_UPPER_BOUND = 1000
private const val MAX_STEP = 100
private const val MAX_DATE_MONTHS = 24
