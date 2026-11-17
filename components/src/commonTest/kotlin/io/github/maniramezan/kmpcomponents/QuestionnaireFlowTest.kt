package io.github.maniramezan.kmpcomponents

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QuestionnaireFlowTest {
    private val today = LocalDate(2026, 6, 15)
    private val options =
        listOf(QuestionnaireOption("a", "A"), QuestionnaireOption("b", "B"), QuestionnaireOption("c", "C"))

    private fun single(required: Boolean = true) =
        QuestionnaireQuestion("single", "Pick", QuestionnaireQuestionKind.SingleChoice(options), isRequired = required)

    private fun multiple() = QuestionnaireQuestion(
        "multiple",
        "Pick",
        QuestionnaireQuestionKind.MultipleChoice(options),
    )

    private fun number(step: Double = 5.0) =
        QuestionnaireQuestion(
            "number",
            "How many",
            QuestionnaireQuestionKind.Number(0.0..100.0, step),
            isRequired = true,
        )

    private fun date(required: Boolean = true) = QuestionnaireQuestion(
        "date",
        "When",
        QuestionnaireQuestionKind.Date(LocalDate(2026, 7, 1)..LocalDate(2026, 12, 31)),
        isRequired = required,
    )

    private fun text(required: Boolean = true) =
        QuestionnaireQuestion("text", "Notes", QuestionnaireQuestionKind.Text(), isRequired = required)

    private fun boolean() = QuestionnaireQuestion(
        "bool",
        "Agree",
        QuestionnaireQuestionKind.Boolean("Yes", "No"),
        isRequired = true,
    )

    private fun custom() = QuestionnaireQuestion("custom", "Rank", QuestionnaireQuestionKind.Custom, isRequired = true)

    @Test
    fun `invalid questions are dropped and repeated options keep their first occurrence`() {
        val repeated = QuestionnaireQuestion(
            "repeated",
            "Pick",
            QuestionnaireQuestionKind.SingleChoice(
                listOf(QuestionnaireOption("a", "First"), QuestionnaireOption("a", "Second")),
            ),
            detail = "Detail",
            isRequired = true,
            choiceStyle = QuestionnaireChoiceStyle.CHIPS,
        )
        val questions = listOf(
            single(),
            single().copy(prompt = "Duplicate id"),
            QuestionnaireQuestion("empty", "Pick", QuestionnaireQuestionKind.MultipleChoice(emptyList())),
            QuestionnaireQuestion("infinite", "N", QuestionnaireQuestionKind.Number(0.0..Double.POSITIVE_INFINITY)),
            QuestionnaireQuestion("nan", "N", QuestionnaireQuestionKind.Number(Double.NaN..1.0)),
            QuestionnaireQuestion("inverted", "N", QuestionnaireQuestionKind.Number(5.0..1.0)),
            QuestionnaireQuestion(
                "emptyDates",
                "D",
                QuestionnaireQuestionKind.Date(LocalDate(2026, 2, 1)..LocalDate(2026, 1, 1)),
            ),
            QuestionnaireQuestion(
                "farDates",
                "D",
                QuestionnaireQuestionKind.Date(LocalDate(2026, 1, 1)..LocalDate(10000, 1, 1)),
            ),
            custom(),
            repeated,
            text(),
        )
        val valid = QuestionnaireFlow.validQuestions(questions, supportsCustomInput = false)
        assertEquals(listOf("single", "repeated", "text"), valid.map { it.id })
        assertEquals("Pick", valid.first().prompt)
        val repaired = valid[1]
        assertEquals(QuestionnaireQuestionKind.SingleChoice(listOf(QuestionnaireOption("a", "First"))), repaired.kind)
        assertEquals("Detail", repaired.detail)
        assertTrue(repaired.isRequired)
        assertEquals(QuestionnaireChoiceStyle.CHIPS, repaired.choiceStyle)
    }

    @Test
    fun `custom questions need an input builder`() {
        assertEquals(emptyList(), QuestionnaireFlow.validQuestions(listOf(custom()), supportsCustomInput = false))
        assertEquals(listOf(custom()), QuestionnaireFlow.validQuestions(listOf(custom()), supportsCustomInput = true))
        assertEquals(emptyList(), QuestionnaireFlow.validQuestions(emptyList(), supportsCustomInput = true))
    }

    @Test
    fun `required questions block advancing until answered and optional ones never do`() {
        assertFalse(QuestionnaireFlow.canAdvance(single(), null, today))
        assertTrue(QuestionnaireFlow.canAdvance(single(), QuestionnaireAnswer.Choices(listOf("b")), today))
        assertFalse(QuestionnaireFlow.canAdvance(single(), QuestionnaireAnswer.Choices(listOf("unknown")), today))
        assertFalse(QuestionnaireFlow.canAdvance(single(), QuestionnaireAnswer.Text("a"), today))
        assertTrue(QuestionnaireFlow.canAdvance(single(required = false), null, today))
        assertFalse(QuestionnaireFlow.canAdvance(text(), QuestionnaireAnswer.Text("   "), today))
        assertTrue(QuestionnaireFlow.canAdvance(text(required = false), QuestionnaireAnswer.Text("   "), today))
    }

    @Test
    fun `a required date advances on its displayed default clamped into the range`() {
        assertTrue(QuestionnaireFlow.canAdvance(date(), null, today))
        assertEquals(QuestionnaireAnswer.Date(LocalDate(2026, 7, 1)), QuestionnaireFlow.defaultAnswer(date(), today))
        assertEquals(
            QuestionnaireAnswer.Date(LocalDate(2026, 12, 31)),
            QuestionnaireFlow.defaultAnswer(date(), LocalDate(2027, 3, 1)),
        )
        assertNull(QuestionnaireFlow.defaultAnswer(single(), today))
    }

    @Test
    fun `booleans need an explicit answer and false is valid`() {
        assertFalse(QuestionnaireFlow.canAdvance(boolean(), null, today))
        assertTrue(QuestionnaireFlow.canAdvance(boolean(), QuestionnaireAnswer.Boolean(false), today))
        assertNull(QuestionnaireFlow.normalized(QuestionnaireAnswer.Choices(listOf("true")), boolean()))
    }

    @Test
    fun `the first unanswered required question is found again after an earlier answer is cleared`() {
        val questions = listOf(single(), text(required = false), number())
        val answers = mapOf<String, QuestionnaireAnswer>("number" to QuestionnaireAnswer.Number(10.0))
        assertEquals(0, QuestionnaireFlow.firstUnansweredRequiredIndex(questions, answers, today))
        val complete = answers + ("single" to QuestionnaireAnswer.Choices(listOf("a")))
        assertNull(QuestionnaireFlow.firstUnansweredRequiredIndex(questions, complete, today))
    }

    @Test
    fun `custom answers are normalized without assuming a default`() {
        val question = custom()
        assertNull(QuestionnaireFlow.defaultAnswer(question, today))
        assertFalse(QuestionnaireFlow.canAdvance(question, null, today))
        assertEquals(
            QuestionnaireAnswer.Text("x"),
            QuestionnaireFlow.normalized(QuestionnaireAnswer.Text("  x "), question),
        )
        assertNull(QuestionnaireFlow.normalized(QuestionnaireAnswer.Text("  "), question))
        assertNull(QuestionnaireFlow.normalized(QuestionnaireAnswer.Number(Double.NaN), question))
        assertEquals(
            QuestionnaireAnswer.Number(0.0),
            QuestionnaireFlow.normalized(QuestionnaireAnswer.Number(0.0), question),
        )
        assertEquals(
            QuestionnaireAnswer.Choices(listOf("c", "a")),
            QuestionnaireFlow.normalized(QuestionnaireAnswer.Choices(listOf("c", "a", "c")), question),
        )
        assertNull(QuestionnaireFlow.normalized(QuestionnaireAnswer.Choices(emptyList()), question))
        assertEquals(
            QuestionnaireAnswer.Boolean(false),
            QuestionnaireFlow.normalized(QuestionnaireAnswer.Boolean(false), question),
        )
        assertEquals(
            QuestionnaireAnswer.Date(today),
            QuestionnaireFlow.normalized(QuestionnaireAnswer.Date(today), question),
        )
    }

    @Test
    fun `choice answers keep known ids in option order`() {
        assertEquals(
            QuestionnaireAnswer.Choices(listOf("a", "c")),
            QuestionnaireFlow.normalized(QuestionnaireAnswer.Choices(listOf("c", "x", "a", "a")), multiple()),
        )
        assertEquals(
            QuestionnaireAnswer.Choices(listOf("c")),
            QuestionnaireFlow.normalized(QuestionnaireAnswer.Choices(listOf("x", "c", "a")), single()),
        )
        assertNull(QuestionnaireFlow.normalized(QuestionnaireAnswer.Choices(listOf("x")), multiple()))
    }

    @Test
    fun `numbers clamp into the range and snap to the step from the lower bound`() {
        fun normalized(value: Double, step: Double = 5.0) =
            QuestionnaireFlow.normalized(QuestionnaireAnswer.Number(value), number(step))
        assertEquals(QuestionnaireAnswer.Number(10.0), normalized(12.4))
        assertEquals(QuestionnaireAnswer.Number(15.0), normalized(12.5))
        assertEquals(QuestionnaireAnswer.Number(0.0), normalized(-3.0))
        assertEquals(QuestionnaireAnswer.Number(100.0), normalized(250.0))
        assertEquals(QuestionnaireAnswer.Number(12.4), normalized(12.4, step = 0.0))
        assertEquals(QuestionnaireAnswer.Number(12.4), normalized(12.4, step = Double.NaN))
        assertNull(normalized(Double.POSITIVE_INFINITY))
        assertEquals(7.0, QuestionnaireFlow.snapped(7.2, -5.0..10.0, 3.0))
        assertEquals(9.0, QuestionnaireFlow.snapped(9.9, 0.0..10.0, 3.0))
    }

    @Test
    fun `dates clamp and text trims`() {
        assertEquals(
            QuestionnaireAnswer.Date(LocalDate(2026, 12, 31)),
            QuestionnaireFlow.normalized(QuestionnaireAnswer.Date(LocalDate(2030, 1, 1)), date()),
        )
        assertEquals(
            QuestionnaireAnswer.Text("hi"),
            QuestionnaireFlow.normalized(QuestionnaireAnswer.Text("\n hi \t"), text()),
        )
        assertNull(QuestionnaireFlow.normalized(QuestionnaireAnswer.Number(1.0), text()))
    }

    @Test
    fun `submission keeps normalized answers to known questions and fills date defaults`() {
        val questions = listOf(single(), text(required = false), date(), number())
        val answers = mapOf(
            "single" to QuestionnaireAnswer.Choices(listOf("b")),
            "text" to QuestionnaireAnswer.Text("  "),
            "number" to QuestionnaireAnswer.Number(42.0),
            "stale" to QuestionnaireAnswer.Text("Removed question"),
        )
        assertEquals(
            mapOf(
                "single" to QuestionnaireAnswer.Choices(listOf("b")),
                "date" to QuestionnaireAnswer.Date(LocalDate(2026, 7, 1)),
                "number" to QuestionnaireAnswer.Number(40.0),
            ),
            QuestionnaireFlow.submission(questions, answers, today),
        )
    }

    @Test
    fun `submission records the date default when a stored date answer has the wrong type`() {
        val submitted = QuestionnaireFlow.submission(
            listOf(date()),
            mapOf("date" to QuestionnaireAnswer.Text("x")),
            today,
        )
        assertEquals(
            mapOf<String, QuestionnaireAnswer>("date" to QuestionnaireAnswer.Date(LocalDate(2026, 7, 1))),
            submitted,
        )
    }

    @Test
    fun `single-choice taps select and deselect`() {
        val selected = QuestionnaireFlow.toggling("a", null, options, allowsMultiple = false)
        assertEquals(QuestionnaireAnswer.Choices(listOf("a")), selected)
        assertEquals(
            QuestionnaireAnswer.Choices(listOf("b")),
            QuestionnaireFlow.toggling("b", selected, options, allowsMultiple = false),
        )
        assertNull(QuestionnaireFlow.toggling("a", selected, options, allowsMultiple = false))
        assertEquals(selected, QuestionnaireFlow.toggling("unknown", selected, options, allowsMultiple = false))
    }

    @Test
    fun `multiple-choice taps toggle and keep option order`() {
        var answer: QuestionnaireAnswer? = null
        answer = QuestionnaireFlow.toggling("c", answer, options, allowsMultiple = true)
        answer = QuestionnaireFlow.toggling("a", answer, options, allowsMultiple = true)
        assertEquals(QuestionnaireAnswer.Choices(listOf("a", "c")), answer)
        answer = QuestionnaireFlow.toggling("c", answer, options, allowsMultiple = true)
        assertEquals(QuestionnaireAnswer.Choices(listOf("a")), answer)
        assertNull(QuestionnaireFlow.toggling("a", answer, options, allowsMultiple = true))
    }

    @Test
    fun `automatic presentation resolves by cardinality and explicit styles are kept`() {
        assertEquals(
            QuestionnaireChoiceStyle.BULLETS,
            QuestionnaireFlow.resolvedChoiceStyle(QuestionnaireChoiceStyle.AUTOMATIC, allowsMultiple = false),
        )
        assertEquals(
            QuestionnaireChoiceStyle.CHECKMARKS,
            QuestionnaireFlow.resolvedChoiceStyle(QuestionnaireChoiceStyle.AUTOMATIC, allowsMultiple = true),
        )
        assertEquals(
            QuestionnaireChoiceStyle.CHIPS,
            QuestionnaireFlow.resolvedChoiceStyle(QuestionnaireChoiceStyle.CHIPS, allowsMultiple = true),
        )
        assertEquals(
            QuestionnaireChoiceStyle.CHECKMARKS,
            QuestionnaireFlow.resolvedChoiceStyle(QuestionnaireChoiceStyle.CHECKMARKS, allowsMultiple = false),
        )
    }

    @Test
    fun `stepping starts from the lower bound and stays in range`() {
        val range = -5.0..10.0
        assertEquals(-5.0, QuestionnaireFlow.stepped(null, increment = true, range, 3.0))
        assertEquals(-5.0, QuestionnaireFlow.stepped(Double.NaN, increment = false, range, 3.0))
        assertEquals(1.0, QuestionnaireFlow.stepped(-2.0, increment = true, range, 3.0))
        assertEquals(10.0, QuestionnaireFlow.stepped(9.0, increment = true, range, 3.0))
        assertEquals(-5.0, QuestionnaireFlow.stepped(-4.0, increment = false, range, 3.0))
        assertEquals(3.0, QuestionnaireFlow.stepped(2.0, increment = true, range, 0.0))
    }

    @Test
    fun `calendar days convert to and from Material's UTC milliseconds`() {
        val day = LocalDate(2026, 3, 8)
        assertEquals(day, utcDateOf(utcMillisOf(day)))
        assertEquals(day, utcDateOf(utcMillisOf(day) + 86_399_999L))
        assertEquals(LocalDate(1969, 12, 31), utcDateOf(-1L))
        assertEquals(0L, utcMillisOf(LocalDate(1970, 1, 1)))
    }
}
