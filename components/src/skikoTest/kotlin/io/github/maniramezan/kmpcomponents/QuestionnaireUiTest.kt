package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalTestApi::class)
class QuestionnaireUiTest {
    private val today = LocalDate(2026, 6, 15)
    private val options = listOf(QuestionnaireOption("a", "Option A"), QuestionnaireOption("b", "Option B"))
    private var answers by mutableStateOf<Map<String, QuestionnaireAnswer>>(emptyMap())
    private var submitted: Map<String, QuestionnaireAnswer>? = null
    private var skips = 0

    private fun choice(id: String = "choice", required: Boolean = true) =
        QuestionnaireQuestion(
            id,
            "Pick one $id",
            QuestionnaireQuestionKind.SingleChoice(options),
            isRequired = required,
        )

    private fun ComposeUiTest.setQuestionnaire(
        questions: List<QuestionnaireQuestion>,
        skippable: Boolean = false,
        bounded: Boolean = true,
        reduceMotion: Boolean = true,
        customInput: (
            @androidx.compose.runtime.Composable (
                QuestionnaireQuestion,
                QuestionnaireAnswer?,
                (QuestionnaireAnswer?) -> Unit,
            ) -> Unit
        )? = null,
    ) {
        setContent {
            CompositionLocalProvider(LocalReduceMotionEnabled provides reduceMotion) {
                KmpTheme {
                    QuestionnaireView(
                        questions = questions,
                        answers = answers,
                        onAnswersChange = { answers = it },
                        onSubmit = { submitted = it },
                        modifier = if (bounded) Modifier.height(HEIGHT) else Modifier,
                        onSkip = if (skippable) ({ skips++ }) else null,
                        labels = QuestionnaireLabels(done = "Submit"),
                        today = today,
                        customInput = customInput,
                    )
                }
            }
        }
    }

    @Test
    fun `a required choice gates Next and Back keeps the selection`() = runComposeUiTest {
        setQuestionnaire(listOf(choice(), QuestionnaireQuestion("notes", "Notes", QuestionnaireQuestionKind.Text())))
        onNodeWithText("Pick one choice").assert(isHeading())
        onNodeWithText("Next").assertIsNotEnabled()
        onNodeWithText("Option A").assertIsNotSelected().performClick()
        onNodeWithText("Option A").assertIsSelected()
        onNodeWithText("Next").assertIsEnabled().performClick()
        onNodeWithText("Optional").assertExists()
        onNode(hasSetTextAction()).assertExists()
        onNodeWithText("Back").performClick()
        onNodeWithText("Option A").assertIsSelected().performClick()
        onNodeWithText("Option A").assertIsNotSelected()
        onNodeWithText("Next").assertIsNotEnabled()
        assertEquals(emptyMap(), answers)
    }

    @Test
    fun `the progress caption names the page and is announced politely`() = runComposeUiTest {
        setQuestionnaire(listOf(choice("first", required = false), choice("second", required = false)))
        val polite = SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite)
        onNode(polite and hasContentDescription("Question 1 of 2")).assertExists()
        onNodeWithText("Next").performClick()
        onNode(polite and hasContentDescription("Question 2 of 2")).assertExists()
        onNodeWithText("Submit").assertExists()
    }

    @Test
    fun `false is an explicit boolean answer`() = runComposeUiTest {
        setQuestionnaire(
            listOf(
                QuestionnaireQuestion(
                    "bool",
                    "Continue?",
                    QuestionnaireQuestionKind.Boolean("Yes", "No"),
                    isRequired = true,
                ),
            ),
        )
        onNodeWithText("Submit").assertIsNotEnabled()
        onNodeWithText("No").performClick()
        onNodeWithText("No").assertIsSelected()
        onNodeWithText("Submit").performClick()
        assertEquals(mapOf<String, QuestionnaireAnswer>("bool" to QuestionnaireAnswer.Boolean(false)), submitted)
    }

    @Test
    fun `multiple choice rows toggle like checkboxes and submit in option order`() = runComposeUiTest {
        setQuestionnaire(
            listOf(QuestionnaireQuestion("many", "Pick", QuestionnaireQuestionKind.MultipleChoice(options))),
        )
        onNodeWithText("Option B").assertIsOff().performClick()
        onNodeWithText("Option B").assertIsOn()
        onNodeWithText("Option A").performClick()
        onNodeWithText("Submit").performClick()
        assertEquals(
            mapOf<String, QuestionnaireAnswer>("many" to QuestionnaireAnswer.Choices(listOf("a", "b"))),
            submitted,
        )
    }

    @Test
    fun `chips keep the question's selection rules`() = runComposeUiTest {
        setQuestionnaire(
            listOf(
                QuestionnaireQuestion(
                    "chips",
                    "Pick",
                    QuestionnaireQuestionKind.SingleChoice(options),
                    choiceStyle = QuestionnaireChoiceStyle.CHIPS,
                ),
            ),
        )
        onNodeWithText("Option A").performClick()
        onNodeWithText("Option B").performClick()
        onNodeWithText("Option A").assertIsNotSelected()
        onNodeWithText("Option B").assertIsSelected()
        onNodeWithText("Submit").performClick()
        assertEquals(mapOf<String, QuestionnaireAnswer>("chips" to QuestionnaireAnswer.Choices(listOf("b"))), submitted)
    }

    @Test
    fun `text needs content and is trimmed on submit`() = runComposeUiTest {
        setQuestionnaire(
            listOf(
                QuestionnaireQuestion("text", "Notes", QuestionnaireQuestionKind.Text("Write here"), isRequired = true),
            ),
        )
        onNodeWithContentDescription("Notes").performTextInput("   ")
        onNodeWithText("Submit").assertIsNotEnabled()
        onNodeWithContentDescription("Notes").performTextInput("value  ")
        assertEquals(QuestionnaireAnswer.Text("   value  "), answers["text"])
        onNodeWithText("Submit").performClick()
        assertEquals(mapOf<String, QuestionnaireAnswer>("text" to QuestionnaireAnswer.Text("value")), submitted)
    }

    @Test
    fun `the stepper starts at the lower bound and stops at the ends`() = runComposeUiTest {
        setQuestionnaire(
            listOf(
                QuestionnaireQuestion(
                    "number",
                    "Amount",
                    QuestionnaireQuestionKind.Number(-5.0..1.0, 3.0),
                    isRequired = true,
                ),
            ),
        )
        onNodeWithText("Submit").assertIsNotEnabled()
        onNodeWithContentDescription("Increase").performClick()
        assertEquals(QuestionnaireAnswer.Number(-5.0), answers["number"])
        onNodeWithContentDescription("Amount").assertTextEquals("-5")
        onNodeWithContentDescription("Decrease").assertIsNotEnabled()
        onNodeWithContentDescription("Increase").performClick()
        onNodeWithContentDescription("Increase").performClick()
        onNodeWithContentDescription("Amount").assertTextEquals("1")
        onNodeWithContentDescription("Increase").assertIsNotEnabled()
        onNodeWithContentDescription("Decrease").performClick()
        onNodeWithText("Submit").performClick()
        assertEquals(mapOf<String, QuestionnaireAnswer>("number" to QuestionnaireAnswer.Number(-2.0)), submitted)
    }

    @Test
    fun `typed numbers drop stray characters and snap on submit`() = runComposeUiTest {
        setQuestionnaire(
            listOf(QuestionnaireQuestion("number", "Amount", QuestionnaireQuestionKind.Number(0.0..100.0, 5.0), "km")),
        )
        onNodeWithText("km").assertExists()
        onNodeWithContentDescription("Amount").performTextReplacement("-1a2")
        onNodeWithContentDescription("Amount").assertTextEquals("12")
        assertEquals(QuestionnaireAnswer.Number(12.0), answers["number"])
        onNodeWithContentDescription("Amount").performTextReplacement("")
        assertNull(answers["number"])
        onNodeWithContentDescription("Amount").performTextReplacement("13")
        onNodeWithText("Submit").performClick()
        assertEquals(mapOf<String, QuestionnaireAnswer>("number" to QuestionnaireAnswer.Number(15.0)), submitted)
    }

    @Test
    fun `the number field follows answers replaced by the caller`() = runComposeUiTest {
        answers = mapOf("number" to QuestionnaireAnswer.Number(-7.0))
        setQuestionnaire(
            listOf(QuestionnaireQuestion("number", "Amount", QuestionnaireQuestionKind.Number(0.0..100.0))),
        )
        // A negative answer keeps its sign until submission clamps it.
        onNodeWithContentDescription("Amount").assertTextEquals("-7")
        answers = mapOf("number" to QuestionnaireAnswer.Number(42.0))
        onNodeWithContentDescription("Amount").assertTextEquals("42")
        answers = emptyMap()
        onNodeWithContentDescription("Amount").assertTextEquals("")
    }

    @Test
    fun `a date shows its default and records it when moving on`() = runComposeUiTest {
        val range = LocalDate(2026, 7, 1)..LocalDate(2026, 12, 31)
        setQuestionnaire(
            listOf(
                QuestionnaireQuestion("date", "When", QuestionnaireQuestionKind.Date(range), isRequired = true),
                choice(required = false),
            ),
        )
        onNodeWithText(formatQuestionnaireDate(LocalDate(2026, 7, 1))).assertIsDisplayed()
        onNodeWithText("Next").assertIsEnabled().performClick()
        assertEquals(QuestionnaireAnswer.Date(LocalDate(2026, 7, 1)), answers["date"])
    }

    @Test
    fun `the calendar dialog confirms or cancels the chosen day`() = runComposeUiTest {
        answers = mapOf("date" to QuestionnaireAnswer.Date(LocalDate(2026, 8, 20)))
        val range = LocalDate(2026, 7, 1)..LocalDate(2026, 12, 31)
        setQuestionnaire(listOf(QuestionnaireQuestion("date", "When", QuestionnaireQuestionKind.Date(range))))
        onNodeWithText(formatQuestionnaireDate(LocalDate(2026, 8, 20))).performClick()
        onNodeWithText("Cancel").performClick()
        onNodeWithText("OK").assertDoesNotExist()
        onNodeWithText(formatQuestionnaireDate(LocalDate(2026, 8, 20))).performClick()
        onNodeWithText("OK").performClick()
        onNodeWithText("OK").assertDoesNotExist()
        assertEquals(QuestionnaireAnswer.Date(LocalDate(2026, 8, 20)), answers["date"])
    }

    @Test
    fun `a custom input records answers through its callback`() = runComposeUiTest {
        val question = QuestionnaireQuestion("custom", "Rank", QuestionnaireQuestionKind.Custom, isRequired = true)
        setQuestionnaire(listOf(question)) { _, answer, onAnswerChange ->
            ThemeTextButton(text = "Answer", onClick = {
                onAnswerChange(QuestionnaireAnswer.Choices(listOf("x", "y", "x")))
            })
            Text(if (answer == null) "Unanswered" else "Answered")
        }
        onNodeWithText("Unanswered").assertExists()
        onNodeWithText("Submit").assertIsNotEnabled()
        onNodeWithText("Answer").performClick()
        onNodeWithText("Answered").assertExists()
        onNodeWithText("Submit").performClick()
        assertEquals(
            mapOf<String, QuestionnaireAnswer>("custom" to QuestionnaireAnswer.Choices(listOf("x", "y"))),
            submitted,
        )
    }

    @Test
    fun `custom questions are not shown without an input builder`() = runComposeUiTest {
        setQuestionnaire(listOf(QuestionnaireQuestion("custom", "Rank", QuestionnaireQuestionKind.Custom), choice()))
        onNodeWithText("Rank").assertDoesNotExist()
        onNodeWithText("Pick one choice").assertExists()
        onNodeWithContentDescription("Question 1 of 1").assertExists()
    }

    @Test
    fun `Done returns to an earlier required question whose answer was cleared`() = runComposeUiTest {
        setQuestionnaire(listOf(choice("first"), choice("second")))
        onNodeWithText("Option A").performClick()
        onNodeWithText("Next").performClick()
        onNodeWithText("Option B").performClick()
        answers = answers - "first"
        onNodeWithText("Submit").performClick()
        onNodeWithText("Pick one first").assertExists()
        assertNull(submitted)
    }

    @Test
    fun `pages slide in and out when motion is allowed`() = runComposeUiTest {
        setQuestionnaire(
            listOf(choice("first", required = false), choice("second", required = false)),
            reduceMotion = false,
        )
        onNodeWithText("Next").performClick()
        waitForIdle()
        onNodeWithText("Pick one second").assertIsDisplayed()
        onNodeWithText("Pick one first").assertDoesNotExist()
        onNodeWithText("Back").performClick()
        waitForIdle()
        onNodeWithText("Pick one first").assertIsDisplayed()
        onNodeWithText("Pick one second").assertDoesNotExist()
    }

    @Test
    fun `Skip reports without submitting`() = runComposeUiTest {
        setQuestionnaire(listOf(choice()), skippable = true)
        onNodeWithText("Skip").performClick()
        assertEquals(1, skips)
        assertNull(submitted)
    }

    @Test
    fun `no Skip button shows without an action`() = runComposeUiTest {
        setQuestionnaire(listOf(choice()))
        onNodeWithText("Skip").assertDoesNotExist()
    }

    @Test
    fun `an empty questionnaire submits no answers`() = runComposeUiTest {
        setQuestionnaire(emptyList())
        onNodeWithText("Next").assertDoesNotExist()
        onNodeWithText("Submit").performClick()
        assertEquals(emptyMap(), submitted)
    }

    @Test
    fun `an unbounded questionnaire grows to fit its page`() = runComposeUiTest {
        setQuestionnaire(listOf(choice()), bounded = false)
        onNodeWithText("Option B").assertIsDisplayed()
        onNodeWithText("Submit").assertIsDisplayed()
    }

    private companion object {
        val HEIGHT = 560.dp
    }
}
