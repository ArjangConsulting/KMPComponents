package io.github.maniramezan.kmpcomponents

import kotlinx.datetime.LocalDate
import kotlin.math.floor

/**
 * Pure paging, validation, and normalization rules for [QuestionnaireView], kept out of the
 * composables so tests can call them directly.
 */
internal object QuestionnaireFlow {
    private const val MIN_DATE_YEAR = 1
    private const val MAX_DATE_YEAR = 9999

    /**
     * The questions the view can show, in order. Drops a question whose id repeats an earlier one, a
     * choice question with no options, a number question with non-finite bounds or an empty range, a
     * date question with an empty range or one outside years 1 to 9999 (the calendar's limits), and a
     * custom question when [supportsCustomInput] is `false`. Repeated option ids keep their first
     * occurrence.
     */
    fun validQuestions(
        questions: List<QuestionnaireQuestion>,
        supportsCustomInput: Boolean,
    ): List<QuestionnaireQuestion> = questions.distinctBy { it.id }.mapNotNull { question ->
        when (val kind = question.kind) {
            is QuestionnaireQuestionKind.SingleChoice -> kind.options.distinctBy { it.id }
                .takeIf { it.isNotEmpty() }
                ?.let { question.copy(kind = QuestionnaireQuestionKind.SingleChoice(it)) }

            is QuestionnaireQuestionKind.MultipleChoice -> kind.options.distinctBy { it.id }
                .takeIf { it.isNotEmpty() }
                ?.let { question.copy(kind = QuestionnaireQuestionKind.MultipleChoice(it)) }

            is QuestionnaireQuestionKind.Number -> question.takeIf {
                kind.range.start.isFinite() && kind.range.endInclusive.isFinite() && !kind.range.isEmpty()
            }

            is QuestionnaireQuestionKind.Date -> question.takeIf {
                !kind.range.isEmpty() &&
                    kind.range.start.year >= MIN_DATE_YEAR &&
                    kind.range.endInclusive.year <= MAX_DATE_YEAR
            }

            QuestionnaireQuestionKind.Custom -> question.takeIf { supportsCustomInput }

            is QuestionnaireQuestionKind.Boolean, is QuestionnaireQuestionKind.Text -> question
        }
    }

    /** The answer a question shows before the person touches it: today clamped for a date, else `null`. */
    fun defaultAnswer(question: QuestionnaireQuestion, today: LocalDate): QuestionnaireAnswer? =
        (question.kind as? QuestionnaireQuestionKind.Date)?.let { QuestionnaireAnswer.Date(today.coerceIn(it.range)) }

    /** The normalized stored answer, or the question's default when nothing valid is stored. */
    fun effectiveAnswer(
        answer: QuestionnaireAnswer?,
        question: QuestionnaireQuestion,
        today: LocalDate,
    ): QuestionnaireAnswer? = answer?.let { normalized(it, question) } ?: defaultAnswer(question, today)

    /** Whether the person may leave the page: always when optional, and with a valid answer when required. */
    fun canAdvance(question: QuestionnaireQuestion, answer: QuestionnaireAnswer?, today: LocalDate): Boolean =
        !question.isRequired || effectiveAnswer(answer, question, today) != null

    /** The first required question that still needs an answer, or `null` when the answers can be submitted. */
    fun firstUnansweredRequiredIndex(
        questions: List<QuestionnaireQuestion>,
        answers: Map<String, QuestionnaireAnswer>,
        today: LocalDate,
    ): Int? = questions.indexOfFirst { !canAdvance(it, answers[it.id], today) }.takeIf { it >= 0 }

    /**
     * Cleans [answer] for [question], or returns `null` when nothing valid remains.
     *
     * - Choices keep known option ids in option order; a single choice keeps the first known one.
     * - Numbers must be finite; they are clamped into the range and snapped to the step.
     * - Dates are clamped into the range.
     * - Text is trimmed, and blank text is no answer.
     * - Custom answers: text is trimmed, numbers must be finite, and choice ids are deduplicated in
     *   their given order.
     * - An answer of the wrong type for a built-in kind is no answer.
     */
    fun normalized(answer: QuestionnaireAnswer, question: QuestionnaireQuestion): QuestionnaireAnswer? =
        when (val kind = question.kind) {
            is QuestionnaireQuestionKind.SingleChoice -> (answer as? QuestionnaireAnswer.Choices)
                ?.ids
                ?.firstOrNull { id -> kind.options.any { it.id == id } }
                ?.let { QuestionnaireAnswer.Choices(listOf(it)) }

            is QuestionnaireQuestionKind.MultipleChoice -> (answer as? QuestionnaireAnswer.Choices)?.let { selection ->
                val selected = selection.ids.toSet()
                kind.options.map { it.id }.filter { it in selected }
                    .takeIf { it.isNotEmpty() }
                    ?.let(QuestionnaireAnswer::Choices)
            }

            is QuestionnaireQuestionKind.Boolean -> answer as? QuestionnaireAnswer.Boolean

            is QuestionnaireQuestionKind.Number -> (answer as? QuestionnaireAnswer.Number)
                ?.value
                ?.takeIf { it.isFinite() }
                ?.let { QuestionnaireAnswer.Number(snapped(it, kind.range, kind.step)) }

            is QuestionnaireQuestionKind.Date -> (answer as? QuestionnaireAnswer.Date)
                ?.let { QuestionnaireAnswer.Date(it.value.coerceIn(kind.range)) }

            is QuestionnaireQuestionKind.Text -> normalizedText(answer)

            QuestionnaireQuestionKind.Custom -> when (answer) {
                is QuestionnaireAnswer.Text -> normalizedText(answer)

                is QuestionnaireAnswer.Number -> answer.takeIf { it.value.isFinite() }

                is QuestionnaireAnswer.Choices -> answer.ids.distinct()
                    .takeIf { it.isNotEmpty() }
                    ?.let(QuestionnaireAnswer::Choices)

                is QuestionnaireAnswer.Boolean, is QuestionnaireAnswer.Date -> answer
            }
        }

    /**
     * The answers handed back on submit: the normalized answer, or the displayed default for a date,
     * for each of [questions]. Invalid answers and answers to unknown ids are dropped.
     */
    fun submission(
        questions: List<QuestionnaireQuestion>,
        answers: Map<String, QuestionnaireAnswer>,
        today: LocalDate,
    ): Map<String, QuestionnaireAnswer> = buildMap {
        questions.forEach { question ->
            effectiveAnswer(answers[question.id], question, today)?.let { put(question.id, it) }
        }
    }

    /** The option ids [answer] selects. */
    fun selectedIds(answer: QuestionnaireAnswer?): Set<String> =
        (answer as? QuestionnaireAnswer.Choices)?.ids.orEmpty().toSet()

    /**
     * The answer after tapping [optionId]. A single-choice tap selects the option, or clears the answer
     * when it was the only selection. A multiple-choice tap toggles the option and keeps option order.
     * An empty selection is no answer, and an unknown id changes nothing.
     */
    fun toggling(
        optionId: String,
        answer: QuestionnaireAnswer?,
        options: List<QuestionnaireOption>,
        allowsMultiple: Boolean,
    ): QuestionnaireAnswer? {
        if (options.none { it.id == optionId }) return answer
        val selected = selectedIds(answer)
        val ids = if (allowsMultiple) {
            val updated = if (optionId in selected) selected - optionId else selected + optionId
            options.map { it.id }.filter { it in updated }
        } else if (selected == setOf(optionId)) {
            emptyList()
        } else {
            listOf(optionId)
        }
        return ids.takeIf { it.isNotEmpty() }?.let(QuestionnaireAnswer::Choices)
    }

    /** [style] with [QuestionnaireChoiceStyle.AUTOMATIC] resolved to bullets or checkmarks. */
    fun resolvedChoiceStyle(style: QuestionnaireChoiceStyle, allowsMultiple: Boolean): QuestionnaireChoiceStyle =
        when {
            style != QuestionnaireChoiceStyle.AUTOMATIC -> style
            allowsMultiple -> QuestionnaireChoiceStyle.CHECKMARKS
            else -> QuestionnaireChoiceStyle.BULLETS
        }

    /**
     * The value after one stepper press, clamped into [range]. An empty or non-finite value starts at
     * the lower bound. A non-positive or non-finite [step] moves by 1.
     */
    fun stepped(
        value: Double?,
        increment: Boolean,
        range: ClosedFloatingPointRange<Double>,
        step: Double,
    ): Double {
        if (value == null || !value.isFinite()) return range.start
        val delta = step.takeIf { it.isFinite() && it > 0.0 } ?: 1.0
        return (value + if (increment) delta else -delta).coerceIn(range)
    }

    /**
     * [value] clamped into [range] and snapped to the nearest [step] from the lower bound, with halves
     * rounding up. A non-positive or non-finite [step] only clamps.
     */
    fun snapped(value: Double, range: ClosedFloatingPointRange<Double>, step: Double): Double {
        val clamped = value.coerceIn(range)
        if (!step.isFinite() || step <= 0.0) return clamped
        val steps = floor((clamped - range.start) / step + HALF)
        val result = range.start + steps * step
        return if (result.isFinite()) result.coerceIn(range) else clamped
    }

    private fun normalizedText(answer: QuestionnaireAnswer): QuestionnaireAnswer.Text? =
        (answer as? QuestionnaireAnswer.Text)?.value?.trim()?.takeIf { it.isNotEmpty() }?.let(QuestionnaireAnswer::Text)

    private const val HALF = 0.5
}
