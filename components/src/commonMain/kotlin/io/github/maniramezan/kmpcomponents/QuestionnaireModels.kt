package io.github.maniramezan.kmpcomponents

import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDate

/**
 * One selectable answer for a choice question in a [QuestionnaireView].
 *
 * @property id stable identity recorded in a [QuestionnaireAnswer.Choices] answer. Keep it unique
 *   within a question.
 * @property title the text shown on the option, rendered verbatim, so localize it on your side.
 */
@Immutable
public data class QuestionnaireOption(public val id: String, public val title: String) {
    /** An option whose identifier is its [title]. Use it when titles are already unique. */
    public constructor(title: String) : this(id = title, title = title)
}

/**
 * How the options of a choice or boolean [QuestionnaireQuestion] are presented. Presentation never
 * changes how many options can be selected; [QuestionnaireQuestion.kind] decides that.
 */
public enum class QuestionnaireChoiceStyle {
    /** Bullet rows for single-choice and boolean questions, checkmark rows for multiple choice. */
    AUTOMATIC,

    /** Full-width rows with a circular indicator that fills when selected. */
    BULLETS,

    /** Full-width rows with a square indicator that shows a check mark when selected. */
    CHECKMARKS,

    /** Compact chips that wrap onto more lines. */
    CHIPS,
}

/**
 * The input a [QuestionnaireQuestion] collects. Choice kinds record [QuestionnaireAnswer.Choices],
 * [Boolean] records [QuestionnaireAnswer.Boolean], [Number] records [QuestionnaireAnswer.Number],
 * [Date] records [QuestionnaireAnswer.Date], and [Text] records [QuestionnaireAnswer.Text]. A
 * [Custom] input can record any answer.
 */
public sealed interface QuestionnaireQuestionKind {
    /** Pick at most one of [options]. Tapping the selected option clears the answer. */
    @Immutable
    public data class SingleChoice(public val options: List<QuestionnaireOption>) : QuestionnaireQuestionKind

    /** Pick any number of [options]. The answer lists the selected identifiers in option order. */
    @Immutable
    public data class MultipleChoice(public val options: List<QuestionnaireOption>) : QuestionnaireQuestionKind

    /**
     * An explicit true or false answer that starts unanswered, so `false` is distinct from no answer.
     * Both titles are rendered verbatim; supply localized values such as "Yes" and "No". Choosing
     * [falseTitle] answers a required question.
     */
    @Immutable
    public data class Boolean(public val trueTitle: String, public val falseTitle: String) : QuestionnaireQuestionKind

    /**
     * A number inside [range], typed or stepped by [step]. [unitLabel] (for example "km") is shown
     * after the field. The questionnaire clamps the answer into [range] and snaps it to [step],
     * measured from the lower bound, when it submits. A non-positive or non-finite [step] does not
     * snap, and the stepper buttons then move by 1.
     *
     * The field keeps decimal digits and one decimal separator from the current locale; a leading
     * minus sign is allowed when [range] includes negative values. A question whose bounds are not
     * finite, or whose range is empty, is not shown.
     */
    @Immutable
    public data class Number(
        public val range: ClosedFloatingPointRange<Double>,
        public val step: Double = 1.0,
        public val unitLabel: String? = null,
    ) : QuestionnaireQuestionKind

    /**
     * A calendar day inside [range], shown as a date button that opens a calendar. Until the person
     * picks a day, the button shows today clamped into [range], and moving past the page records that
     * day. A question whose range is empty or reaches outside years 1 to 9999 is not shown.
     */
    @Immutable
    public data class Date(public val range: ClosedRange<LocalDate>) : QuestionnaireQuestionKind

    /**
     * Multi-line free-form text. [placeholder] shows while the field is empty. Surrounding whitespace
     * is trimmed on submit, and blank text is no answer.
     */
    @Immutable
    public data class Text(public val placeholder: String? = null) : QuestionnaireQuestionKind

    /**
     * Content built by the caller through [QuestionnaireView]'s `customInput`. The input reports an
     * answer when it is valid and `null` while it is incomplete. Without a `customInput`, these
     * questions are not shown.
     */
    public data object Custom : QuestionnaireQuestionKind
}

/**
 * One question, shown on its own page of a [QuestionnaireView]. Text is rendered verbatim, so
 * localize [prompt] and [detail] on your side.
 *
 * @property id stable identity; answers are keyed by it. Keep it unique within a questionnaire.
 * @property prompt the question, shown as the page heading.
 * @property kind the input the question collects.
 * @property detail optional supporting text below the prompt.
 * @property isRequired whether the person needs a valid answer to move past the page. Optional
 *   questions show the "Optional" caption and can be left blank.
 * @property choiceStyle how choice and boolean options are presented; other kinds ignore it.
 */
@Immutable
public data class QuestionnaireQuestion(
    public val id: String,
    public val prompt: String,
    public val kind: QuestionnaireQuestionKind,
    public val detail: String? = null,
    public val isRequired: Boolean = false,
    public val choiceStyle: QuestionnaireChoiceStyle = QuestionnaireChoiceStyle.AUTOMATIC,
)

/** A recorded answer. A question with no entry in the answers map is unanswered. */
public sealed interface QuestionnaireAnswer {
    /**
     * Selected option identifiers. Built-in choice questions keep option order and a single-choice
     * question holds at most one; a custom input keeps its own order, such as a ranking.
     */
    @Immutable
    public data class Choices(public val ids: List<String>) : QuestionnaireAnswer

    /** An explicit true or false answer. Both values count as answered. */
    @Immutable
    public data class Boolean(public val value: kotlin.Boolean) : QuestionnaireAnswer

    /** A numeric answer. */
    @Immutable
    public data class Number(public val value: Double) : QuestionnaireAnswer

    /** A calendar day, with no time of day or time zone. */
    @Immutable
    public data class Date(public val value: LocalDate) : QuestionnaireAnswer

    /** A free-form text answer. */
    @Immutable
    public data class Text(public val value: String) : QuestionnaireAnswer
}

/**
 * Labels for [QuestionnaireView]'s own text. Question, option, and placeholder text comes from each
 * [QuestionnaireQuestion].
 *
 * @property back the button that returns to the previous page.
 * @property next the button that moves to the next page.
 * @property done the button on the last page that submits the answers.
 * @property skip the button shown when the questionnaire has an `onSkip` action.
 * @property optional the caption under an optional question's prompt. Blank hides it.
 * @property increment accessible name and tooltip of the number stepper's increase button.
 * @property decrement accessible name and tooltip of the number stepper's decrease button.
 * @property chooseDate the action announced for the date button, which opens the calendar.
 * @property confirmDate the calendar dialog's confirm button.
 * @property cancelDate the calendar dialog's cancel button.
 * @property progress the caption above the progress bar, given the 1-based page number and the
 *   page count.
 */
@Immutable
public data class QuestionnaireLabels(
    public val back: String = "Back",
    public val next: String = "Next",
    public val done: String = "Done",
    public val skip: String = "Skip",
    public val optional: String = "Optional",
    public val increment: String = "Increase",
    public val decrement: String = "Decrease",
    public val chooseDate: String = "Choose date",
    public val confirmDate: String = "OK",
    public val cancelDate: String = "Cancel",
    public val progress: (page: Int, count: Int) -> String = { page, count -> "Question $page of $count" },
)
