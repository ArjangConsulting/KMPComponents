package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate

/** Routes [question] to the input for its kind. */
@Composable
internal fun QuestionnaireInput(
    question: QuestionnaireQuestion,
    answer: QuestionnaireAnswer?,
    onAnswerChange: (QuestionnaireAnswer?) -> Unit,
    labels: QuestionnaireLabels,
    today: LocalDate,
    customInput: QuestionnaireCustomInput?,
) {
    when (val kind = question.kind) {
        is QuestionnaireQuestionKind.SingleChoice ->
            QuestionnaireChoiceInput(kind.options, allowsMultiple = false, question.choiceStyle, answer, onAnswerChange)

        is QuestionnaireQuestionKind.MultipleChoice ->
            QuestionnaireChoiceInput(kind.options, allowsMultiple = true, question.choiceStyle, answer, onAnswerChange)

        is QuestionnaireQuestionKind.Boolean -> QuestionnaireBooleanInput(
            kind,
            question.choiceStyle,
            answer,
            onAnswerChange,
        )

        is QuestionnaireQuestionKind.Number -> QuestionnaireNumberInput(question, kind, answer, onAnswerChange, labels)

        is QuestionnaireQuestionKind.Date -> QuestionnaireDateInput(
            question,
            kind,
            answer,
            onAnswerChange,
            labels,
            today,
        )

        is QuestionnaireQuestionKind.Text -> QuestionnaireTextInput(
            question.prompt,
            kind.placeholder,
            answer,
            onAnswerChange,
        )

        QuestionnaireQuestionKind.Custom -> if (customInput != null) {
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
                customInput(question, answer, onAnswerChange)
            }
        }
    }
}

internal typealias QuestionnaireCustomInput =
    @Composable (
        question: QuestionnaireQuestion,
        answer: QuestionnaireAnswer?,
        onAnswerChange: (QuestionnaireAnswer?) -> Unit,
    ) -> Unit

/** Separate option rows, or wrapping chips for [QuestionnaireChoiceStyle.CHIPS]. */
@Composable
private fun QuestionnaireChoiceInput(
    options: List<QuestionnaireOption>,
    allowsMultiple: Boolean,
    style: QuestionnaireChoiceStyle,
    answer: QuestionnaireAnswer?,
    onAnswerChange: (QuestionnaireAnswer?) -> Unit,
) {
    val selected = QuestionnaireFlow.selectedIds(answer)
    val resolved = QuestionnaireFlow.resolvedChoiceStyle(style, allowsMultiple)
    val group = if (allowsMultiple) Modifier else Modifier.selectableGroup()
    val select = { option: QuestionnaireOption ->
        onAnswerChange(QuestionnaireFlow.toggling(option.id, answer, options, allowsMultiple))
    }
    if (resolved == QuestionnaireChoiceStyle.CHIPS) {
        FlowLayout(modifier = group) {
            options.forEach { option ->
                key(option.id) {
                    PillChip(label = option.title, isSelected = option.id in selected, onClick = { select(option) })
                }
            }
        }
    } else {
        Column(
            modifier = group.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small),
        ) {
            options.forEach { option ->
                key(option.id) {
                    QuestionnaireOptionRow(
                        title = option.title,
                        checkmarks = resolved == QuestionnaireChoiceStyle.CHECKMARKS,
                        selected = option.id in selected,
                        allowsMultiple = allowsMultiple,
                        onClick = { select(option) },
                    )
                }
            }
        }
    }
}

/** Explicit true and false options, so an unanswered boolean is distinct from `false`. */
@Composable
private fun QuestionnaireBooleanInput(
    kind: QuestionnaireQuestionKind.Boolean,
    style: QuestionnaireChoiceStyle,
    answer: QuestionnaireAnswer?,
    onAnswerChange: (QuestionnaireAnswer?) -> Unit,
) {
    val selection = (answer as? QuestionnaireAnswer.Boolean)?.let {
        QuestionnaireAnswer.Choices(listOf(if (it.value) TRUE_ID else FALSE_ID))
    }
    QuestionnaireChoiceInput(
        options = listOf(QuestionnaireOption(TRUE_ID, kind.trueTitle), QuestionnaireOption(FALSE_ID, kind.falseTitle)),
        allowsMultiple = false,
        style = style,
        answer = selection,
        onAnswerChange = { choice ->
            val id = (choice as? QuestionnaireAnswer.Choices)?.ids?.firstOrNull()
            onAnswerChange(id?.let { QuestionnaireAnswer.Boolean(it == TRUE_ID) })
        },
    )
}

private const val TRUE_ID = "true"
private const val FALSE_ID = "false"

/**
 * A full-width option. Selection shows as a filled indicator, a secondary container, and a stronger
 * outline, and is reported as a selected radio button or a checked checkbox.
 */
@Composable
private fun QuestionnaireOptionRow(
    title: String,
    checkmarks: Boolean,
    selected: Boolean,
    allowsMultiple: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.medium
    val container = if (selected) colors.secondaryContainer else colors.surfaceContainerLow
    val content = if (selected) colors.onSecondaryContainer else colors.onSurface
    val selection = if (allowsMultiple) {
        Modifier.toggleable(value = selected, role = Role.Checkbox, onValueChange = { onClick() })
    } else {
        Modifier.selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .keyboardFocusOutline(shape, content)
            .heightIn(min = minimumTouchTargetSize())
            .clip(shape)
            .background(container)
            .border(1.dp, if (selected) content else colors.outline, shape)
            .then(selection)
            .padding(horizontal = MaterialTheme.kmpSpacing.medium, vertical = MaterialTheme.kmpSpacing.small),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        QuestionnaireChoiceIndicator(
            checkmarks = checkmarks,
            selected = selected,
            color = if (selected) content else colors.onSurfaceVariant,
            markColor = container,
        )
        Text(text = title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, color = content)
    }
}

/** A decorative radio circle or checkbox square sized to the body text. */
@Composable
private fun QuestionnaireChoiceIndicator(checkmarks: Boolean, selected: Boolean, color: Color, markColor: Color) {
    Canvas(Modifier.size(glyphSize())) {
        val stroke = size.minDimension * INDICATOR_STROKE_FRACTION
        val inset = size.minDimension * INDICATOR_INSET_FRACTION
        if (checkmarks) {
            val corner = CornerRadius(size.minDimension * INDICATOR_CORNER_FRACTION)
            if (selected) {
                drawRoundRect(
                    color,
                    Offset(inset, inset),
                    Size(size.width - inset * 2, size.height - inset * 2),
                    corner,
                )
                val w = size.width
                val h = size.height
                val elbow = Offset(w * CHECK_ELBOW_X, h * CHECK_ELBOW_Y)
                drawLine(markColor, Offset(w * CHECK_START_X, h * CHECK_START_Y), elbow, stroke, StrokeCap.Round)
                drawLine(markColor, elbow, Offset(w * CHECK_END_X, h * CHECK_END_Y), stroke, StrokeCap.Round)
            } else {
                val edge = inset + stroke / 2f
                drawRoundRect(
                    color,
                    Offset(edge, edge),
                    Size(size.width - edge * 2, size.height - edge * 2),
                    corner,
                    style = Stroke(stroke),
                )
            }
        } else {
            val radius = size.minDimension / 2f - inset
            drawCircle(color, radius = radius - stroke / 2f, style = Stroke(stroke))
            if (selected) drawCircle(color, radius = radius * INDICATOR_DOT_FRACTION)
        }
    }
}

private const val INDICATOR_STROKE_FRACTION = 0.1f
private const val INDICATOR_INSET_FRACTION = 0.1f
private const val INDICATOR_CORNER_FRACTION = 0.15f
private const val INDICATOR_DOT_FRACTION = 0.5f
private const val CHECK_START_X = 0.28f
private const val CHECK_START_Y = 0.52f
private const val CHECK_ELBOW_X = 0.44f
private const val CHECK_ELBOW_Y = 0.67f
private const val CHECK_END_X = 0.73f
private const val CHECK_END_Y = 0.36f

/**
 * A number field with an optional unit suffix and decrease and increase buttons. Typing keeps the
 * text the person entered; the field shows the formatted answer whenever the answer changes for
 * another reason (the stepper, a reset, or a locale change).
 */
@Composable
private fun QuestionnaireNumberInput(
    question: QuestionnaireQuestion,
    kind: QuestionnaireQuestionKind.Number,
    answer: QuestionnaireAnswer?,
    onAnswerChange: (QuestionnaireAnswer?) -> Unit,
    labels: QuestionnaireLabels,
) {
    val localeTag = Locale.current.toLanguageTag()
    val separator = remember(localeTag) { platformDecimalSeparator() }
    val value = (answer as? QuestionnaireAnswer.Number)?.value?.takeIf { it.isFinite() }
    var typed by rememberSaveable(question.id) { mutableStateOf("") }
    val shown = if (QuestionnaireNumbers.parse(typed, separator) == value) {
        typed
    } else {
        value?.let { QuestionnaireNumbers.format(it, separator) }.orEmpty()
    }
    val allowsNegative = kind.range.start < 0.0 || (value != null && value < 0.0)
    val focusManager = LocalFocusManager.current
    val step = { increment: Boolean ->
        val next = QuestionnaireFlow.stepped(value, increment, kind.range, kind.step)
        typed = QuestionnaireNumbers.format(next, separator)
        onAnswerChange(QuestionnaireAnswer.Number(next))
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = shown,
            onValueChange = { edited ->
                val sanitized = QuestionnaireNumbers.sanitize(edited, separator, allowsNegative)
                typed = sanitized
                val parsed = QuestionnaireNumbers.parse(sanitized, separator)
                if (parsed != value) onAnswerChange(parsed?.let(QuestionnaireAnswer::Number))
            },
            modifier = Modifier.weight(1f).semantics { contentDescription = question.prompt },
            singleLine = true,
            suffix = kind.unitLabel?.takeIf { it.isNotBlank() }?.let { unit -> { Text(unit) } },
            // iOS's decimal pad has no minus key, so a range with negatives gets the full keyboard.
            keyboardOptions = KeyboardOptions(
                keyboardType = if (allowsNegative) KeyboardType.Text else KeyboardType.Decimal,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        )
        val iconSize = glyphSize()
        TooltipIconButton(
            label = labels.decrement,
            onClick = { step(false) },
            iconSize = iconSize,
            enabled = value == null || value > kind.range.start,
        ) {
            GlyphIcon(Glyph.MINUS, size = iconSize)
        }
        TooltipIconButton(
            label = labels.increment,
            onClick = { step(true) },
            iconSize = iconSize,
            enabled = value == null || value < kind.range.endInclusive,
        ) {
            GlyphIcon(Glyph.PLUS, size = iconSize)
        }
    }
}

/** A button showing the chosen day that opens a calendar dialog, like a compact date picker. */
@Composable
private fun QuestionnaireDateInput(
    question: QuestionnaireQuestion,
    kind: QuestionnaireQuestionKind.Date,
    answer: QuestionnaireAnswer?,
    onAnswerChange: (QuestionnaireAnswer?) -> Unit,
    labels: QuestionnaireLabels,
    today: LocalDate,
) {
    val shown = (QuestionnaireFlow.effectiveAnswer(answer, question, today) as? QuestionnaireAnswer.Date)?.value
        ?: today.coerceIn(kind.range)
    val localeTag = Locale.current.toLanguageTag()
    val text = remember(shown, localeTag) { formatQuestionnaireDate(shown) }
    var choosing by rememberSaveable(question.id) { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.small
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .keyboardFocusOutline(shape, colors.onSurface)
            .heightIn(min = minimumTouchTargetSize())
            .clip(shape)
            .border(1.dp, colors.outline, shape)
            .clickable(onClickLabel = labels.chooseDate, role = Role.Button) { choosing = true }
            .padding(horizontal = MaterialTheme.kmpSpacing.medium, vertical = MaterialTheme.kmpSpacing.small),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurface,
        )
        GlyphIcon(Glyph.CHEVRON_DOWN, color = colors.onSurfaceVariant)
    }
    if (choosing) {
        QuestionnaireDatePickerDialog(
            range = kind.range,
            initial = shown,
            labels = labels,
            onConfirm = { date ->
                choosing = false
                onAnswerChange(QuestionnaireAnswer.Date(date))
            },
            onDismiss = { choosing = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuestionnaireDatePickerDialog(
    range: ClosedRange<LocalDate>,
    initial: LocalDate,
    labels: QuestionnaireLabels,
    onConfirm: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val years = range.start.year..range.endInclusive.year
    val selectable = remember(range) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcDateOf(utcTimeMillis) in range

            override fun isSelectableYear(year: Int): Boolean = year in years
        }
    }
    val state = rememberDatePickerState(
        initialSelectedDateMillis = utcMillisOf(initial),
        yearRange = years,
        selectableDates = selectable,
    )
    val selectedMillis = state.selectedDateMillis
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            ThemeTextButton(
                text = labels.confirmDate,
                onClick = { selectedMillis?.let { onConfirm(utcDateOf(it).coerceIn(range)) } },
                enabled = selectedMillis != null,
            )
        },
        dismissButton = { ThemeTextButton(text = labels.cancelDate, onClick = onDismiss) },
    ) {
        DatePicker(state = state)
    }
}

private const val MILLIS_PER_DAY = 86_400_000L

/** Material's date picker works in UTC milliseconds at the start of the day. */
internal fun utcMillisOf(date: LocalDate): Long = date.toEpochDays() * MILLIS_PER_DAY

internal fun utcDateOf(utcMillis: Long): LocalDate = LocalDate.fromEpochDays(utcMillis.floorDiv(MILLIS_PER_DAY))

/** A multi-line text field named by the question's prompt. */
@Composable
private fun QuestionnaireTextInput(
    prompt: String,
    placeholder: String?,
    answer: QuestionnaireAnswer?,
    onAnswerChange: (QuestionnaireAnswer?) -> Unit,
) {
    OutlinedTextField(
        value = (answer as? QuestionnaireAnswer.Text)?.value.orEmpty(),
        onValueChange = { onAnswerChange(it.takeIf(String::isNotEmpty)?.let(QuestionnaireAnswer::Text)) },
        modifier = Modifier.fillMaxWidth().semantics { contentDescription = prompt },
        placeholder = placeholder?.takeIf { it.isNotBlank() }?.let { text -> { Text(text) } },
        minLines = TEXT_MIN_LINES,
    )
}

private const val TEXT_MIN_LINES = 3
