package io.github.maniramezan.kmpcomponents

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/**
 * A paged questionnaire that shows one [QuestionnaireQuestion] at a time, with a progress caption and
 * bar above it and Back, Next, and Skip buttons below.
 *
 * Each question kind gets its own input: option rows (or wrapping chips) for choices, explicit
 * options for booleans, a field with decrease and increase buttons for numbers, a date button that
 * opens a calendar for dates, a multi-line field for text, and [customInput] for
 * [QuestionnaireQuestionKind.Custom]. Next is disabled on a required question until it has a valid
 * answer. The last page shows [QuestionnaireLabels.done] instead: if an earlier required question has
 * lost its answer, Done returns to it; otherwise Done normalizes the answers (trimmed text, numbers
 * clamped and snapped, displayed date defaults filled in, answers to unknown questions dropped),
 * passes them to [onAnswersChange], and then to [onSubmit].
 *
 * The answers are hoisted: every edit calls [onAnswersChange] with a new map, and a question with no
 * entry is unanswered. Seed [answers] to prefill. Text and numbers are stored as typed until submit.
 * The current page is internal, saved state; recreate the questionnaire (for example with `key`) to
 * start again from the first page.
 *
 * Questions with a repeated id, choice questions without options, number or date questions with an
 * invalid range, and custom questions without a [customInput] are not shown. With no questions left,
 * only the Done (and Skip) buttons show, and Done submits an empty map.
 *
 * Give the questionnaire a bounded height (a screen, sheet, or fixed-height card) to keep the buttons
 * in view while the page scrolls. Inside an unbounded parent, such as a vertically scrolling column,
 * it grows to fit its content instead. The host owns system bar and keyboard insets. Pages slide in
 * the reading direction, or switch at once when the system reduces motion. The progress caption is
 * announced politely when the page changes, and the prompt is a heading.
 *
 * @param questions the questions, in display order.
 * @param answers the current answers, keyed by question id.
 * @param onAnswersChange called with the full, updated answers map after each edit and on submit.
 * @param onSubmit called from the last page with the normalized answers. Unanswered optional
 *   questions have no entry.
 * @param onSkip when not `null`, a Skip button on every page calls it. Skipping does not submit or
 *   clear the answers.
 * @param labels the questionnaire's own text, including the progress caption.
 * @param today the day a date question shows before it is answered; pass a fixed day for previews and
 *   tests.
 * @param customInput builds the input for each [QuestionnaireQuestionKind.Custom] question from the
 *   question, its current answer, and a callback that records an answer (or `null` while the input is
 *   incomplete). The caller owns its validation, labels, and accessibility. Recorded answers are
 *   normalized like the built-in kinds: text is trimmed, numbers must be finite, and choice ids are
 *   deduplicated in their given order.
 */
@Composable
public fun QuestionnaireView(
    questions: List<QuestionnaireQuestion>,
    answers: Map<String, QuestionnaireAnswer>,
    onAnswersChange: (Map<String, QuestionnaireAnswer>) -> Unit,
    onSubmit: (Map<String, QuestionnaireAnswer>) -> Unit,
    modifier: Modifier = Modifier,
    onSkip: (() -> Unit)? = null,
    labels: QuestionnaireLabels = QuestionnaireLabels(),
    today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    customInput: (
        @Composable (
            question: QuestionnaireQuestion,
            answer: QuestionnaireAnswer?,
            onAnswerChange: (QuestionnaireAnswer?) -> Unit,
        ) -> Unit
    )? = null,
) {
    val supportsCustomInput = customInput != null
    val pages = remember(questions, supportsCustomInput) {
        QuestionnaireFlow.validQuestions(questions, supportsCustomInput)
    }
    var currentIndex by rememberSaveable { mutableIntStateOf(0) }
    val index = currentIndex.coerceIn(0, (pages.size - 1).coerceAtLeast(0))
    val question = pages.getOrNull(index)
    val focusManager = LocalFocusManager.current
    val canAdvance = question == null || QuestionnaireFlow.canAdvance(question, answers[question.id], today)
    val isLastPage = index >= pages.lastIndex

    val advance = advance@{
        focusManager.clearFocus()
        var updated = answers
        if (question != null) {
            if (!QuestionnaireFlow.canAdvance(question, answers[question.id], today)) return@advance
            // Record the day the date button shows, so moving on keeps it as the answer.
            val fallback = if (answers[question.id] == null) QuestionnaireFlow.defaultAnswer(question, today) else null
            if (fallback != null) updated = answers + (question.id to fallback)
        }
        if (!isLastPage) {
            if (updated !== answers) onAnswersChange(updated)
            currentIndex = index + 1
            return@advance
        }
        val unanswered = QuestionnaireFlow.firstUnansweredRequiredIndex(pages, updated, today)
        if (unanswered != null) {
            if (updated !== answers) onAnswersChange(updated)
            currentIndex = unanswered
            return@advance
        }
        val submitted = QuestionnaireFlow.submission(pages, updated, today)
        onAnswersChange(submitted)
        onSubmit(submitted)
    }

    BoxWithConstraints(modifier = modifier) {
        val bounded = constraints.hasBoundedHeight
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (bounded) Modifier.fillMaxHeight() else Modifier)
                .padding(vertical = MaterialTheme.kmpSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.medium),
        ) {
            if (question != null) {
                QuestionnaireProgress(
                    caption = labels.progress(index + 1, pages.size),
                    fraction = (index + 1).toFloat() / pages.size,
                    modifier = Modifier.padding(horizontal = MaterialTheme.kmpSpacing.medium),
                )
                QuestionnairePages(
                    page = QuestionnairePage(index, question),
                    bounded = bounded,
                    modifier = if (bounded) Modifier.weight(1f) else Modifier,
                ) { shown ->
                    QuestionnairePageContent(
                        question = shown,
                        answer = answers[shown.id],
                        onAnswerChange = { answer ->
                            onAnswersChange(if (answer == null) answers - shown.id else answers + (shown.id to answer))
                        },
                        labels = labels,
                        today = today,
                        customInput = customInput,
                    )
                }
            }
            QuestionnaireControls(
                backLabel = labels.back.takeIf { index > 0 },
                advanceLabel = if (isLastPage) labels.done else labels.next,
                canAdvance = canAdvance,
                skipLabel = labels.skip.takeIf { onSkip != null && it.isNotBlank() },
                onBack = {
                    focusManager.clearFocus()
                    currentIndex = index - 1
                },
                onAdvance = advance,
                onSkip = { onSkip?.invoke() },
                modifier = Modifier.padding(horizontal = MaterialTheme.kmpSpacing.medium),
            )
        }
    }
}

/** The page shown, with its position so a page change knows its direction. */
private data class QuestionnairePage(val index: Int, val question: QuestionnaireQuestion)

/** Slides pages in the reading direction, scrolling each page on its own when the height is bounded. */
@Composable
private fun QuestionnairePages(
    page: QuestionnairePage,
    bounded: Boolean,
    modifier: Modifier,
    content: @Composable (QuestionnaireQuestion) -> Unit,
) {
    val pageModifier = @Composable {
        val scroll = if (bounded) Modifier.fillMaxSize().verticalScroll(rememberScrollState()) else Modifier
        scroll.padding(horizontal = MaterialTheme.kmpSpacing.medium)
    }
    if (!decorativeAnimationsEnabled()) {
        Box(modifier) {
            // A new page gets a fresh scroll position, as it does when animated.
            key(page.question.id) {
                Box(pageModifier()) { content(page.question) }
            }
        }
        return
    }
    val enterMillis = MaterialTheme.kmpMotion.standardMillis
    val exitMillis = MaterialTheme.kmpMotion.fastMillis
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    AnimatedContent(
        targetState = page,
        modifier = modifier,
        transitionSpec = {
            val forward = targetState.index >= initialState.index
            // Forward pages enter from the end edge: the right in left-to-right layouts.
            val fromEnd = forward != rtl
            ContentTransform(
                targetContentEnter =
                slideInHorizontally(tween(enterMillis)) { width -> if (fromEnd) width else -width } +
                    fadeIn(tween(enterMillis)),
                initialContentExit = fadeOut(tween(exitMillis)),
                sizeTransform = null,
            )
        },
        contentKey = { it.question.id },
        label = "Questionnaire",
    ) { target ->
        Box(pageModifier()) { content(target.question) }
    }
}

/**
 * "Question N of M" above a bar. Both are replaced by one element named by the caption, which is
 * announced politely when it changes; with a blank caption the header is silent.
 */
@Composable
private fun QuestionnaireProgress(caption: String, fraction: Float, modifier: Modifier) {
    val progress = fraction.coerceIn(0f, 1f)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clearAndSetSemantics {
                if (caption.isNotBlank()) {
                    contentDescription = caption
                    liveRegion = LiveRegionMode.Polite
                    progressBarRangeInfo = ProgressBarRangeInfo(progress, 0f..1f)
                }
            },
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.extraSmall),
    ) {
        if (caption.isNotBlank()) {
            Text(
                caption,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
    }
}

/** The prompt, detail, optional caption, and input for one question. */
@Composable
private fun QuestionnairePageContent(
    question: QuestionnaireQuestion,
    answer: QuestionnaireAnswer?,
    onAnswerChange: (QuestionnaireAnswer?) -> Unit,
    labels: QuestionnaireLabels,
    today: LocalDate,
    customInput: QuestionnaireCustomInput?,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.medium),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.extraSmall)) {
            Text(
                text = question.prompt,
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            question.detail?.takeIf { it.isNotBlank() }?.let { detail ->
                Text(
                    detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (!question.isRequired && labels.optional.isNotBlank()) {
                Text(
                    labels.optional,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        QuestionnaireInput(question, answer, onAnswerChange, labels, today, customInput)
    }
}

/** Back and Next (or Done) side by side, with Skip below them. */
@Composable
private fun QuestionnaireControls(
    backLabel: String?,
    advanceLabel: String,
    canAdvance: Boolean,
    skipLabel: String?,
    onBack: () -> Unit,
    onAdvance: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small)) {
            if (backLabel != null) SecondaryButton(text = backLabel, onClick = onBack, modifier = Modifier.weight(1f))
            PrimaryButton(
                text = advanceLabel,
                onClick = onAdvance,
                modifier = Modifier.weight(1f),
                enabled = canAdvance,
            )
        }
        if (skipLabel != null) ThemeTextButton(text = skipLabel, onClick = onSkip, modifier = Modifier.fillMaxWidth())
    }
}
