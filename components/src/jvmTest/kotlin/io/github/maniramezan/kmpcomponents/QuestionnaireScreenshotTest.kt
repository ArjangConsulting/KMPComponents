package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.RoborazziOptions
import io.github.takahirom.roborazzi.captureRoboImage
import kotlinx.datetime.LocalDate
import kotlin.test.Test

// Golden images of each questionnaire input: choice presentations in one set, fields in the other.
@OptIn(ExperimentalTestApi::class)
class QuestionnaireScreenshotTest {
    @Test
    fun `questionnaire choices in light mode`() = capture("questionnaire-choices-light", ThemeMode.LIGHT) {
        ChoicesGallery()
    }

    @Test
    fun `questionnaire choices in dark mode`() = capture("questionnaire-choices-dark", ThemeMode.DARK) {
        ChoicesGallery()
    }

    @Test
    fun `questionnaire choices at double font scale`() =
        capture("questionnaire-choices-font-200", ThemeMode.LIGHT, fontScale = 2f) { ChoicesGallery() }

    @Test
    fun `questionnaire fields in light mode`() = capture("questionnaire-fields-light", ThemeMode.LIGHT) {
        FieldsGallery()
    }

    @Test
    fun `questionnaire fields in dark mode`() = capture("questionnaire-fields-dark", ThemeMode.DARK) { FieldsGallery() }

    @Test
    fun `questionnaire fields at double font scale`() =
        capture("questionnaire-fields-font-200", ThemeMode.LIGHT, fontScale = 2f) { FieldsGallery() }

    private fun capture(
        name: String,
        mode: ThemeMode,
        fontScale: Float = 1f,
        gallery: @Composable () -> Unit,
    ) = runDesktopComposeUiTest(
        width = WINDOW_WIDTH,
        height = if (fontScale >
            1f
        ) {
            TALL_WINDOW_HEIGHT
        } else {
            WINDOW_HEIGHT
        },
    ) {
        // The number and date text follow the default locale; pin it so the goldens match on every machine.
        java.util.Locale.setDefault(java.util.Locale.US)
        setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density.density, fontScale),
                LocalReduceMotionEnabled provides true,
            ) {
                KmpTheme(mode = mode) {
                    Surface(Modifier.width(GALLERY_WIDTH)) { gallery() }
                }
            }
        }
        onRoot().captureRoboImage("$SNAPSHOT_DIR/$name.png", roborazziOptions = OPTIONS)
    }

    private companion object {
        const val SNAPSHOT_DIR = "src/jvmTest/snapshots"
        const val WINDOW_WIDTH = 480
        const val WINDOW_HEIGHT = 1500
        const val TALL_WINDOW_HEIGHT = 3000
        val GALLERY_WIDTH = WINDOW_WIDTH.dp

        // Allow antialiasing noise between machines; real regressions change far more pixels.
        val OPTIONS = RoborazziOptions(compareOptions = RoborazziOptions.CompareOptions(changeThreshold = 0.01f))
    }
}

private val GalleryToday = LocalDate(2026, 6, 15)
private val GalleryOptions =
    listOf(
        QuestionnaireOption("a", "Option A"),
        QuestionnaireOption("b", "Option B"),
        QuestionnaireOption("c", "Option C"),
    )

@Composable
private fun GalleryQuestion(question: QuestionnaireQuestion, answer: QuestionnaireAnswer?) {
    QuestionnaireView(
        questions = listOf(question),
        answers = answer?.let { mapOf(question.id to it) }.orEmpty(),
        onAnswersChange = {},
        onSubmit = {},
        today = GalleryToday,
    )
}

@Composable
private fun ChoicesGallery() {
    Column(
        // A scrolling column gives each questionnaire unbounded height, so it wraps its page.
        Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.large),
    ) {
        GalleryQuestion(
            QuestionnaireQuestion(
                "single",
                "Which option do you prefer?",
                QuestionnaireQuestionKind.SingleChoice(GalleryOptions),
                detail = "Choose one option.",
                isRequired = true,
            ),
            QuestionnaireAnswer.Choices(listOf("b")),
        )
        GalleryQuestion(
            QuestionnaireQuestion(
                "multiple",
                "Which options apply?",
                QuestionnaireQuestionKind.MultipleChoice(GalleryOptions),
            ),
            QuestionnaireAnswer.Choices(listOf("a", "c")),
        )
        GalleryQuestion(
            QuestionnaireQuestion(
                "chips",
                "Which options apply?",
                QuestionnaireQuestionKind.MultipleChoice(GalleryOptions),
                isRequired = true,
                choiceStyle = QuestionnaireChoiceStyle.CHIPS,
            ),
            QuestionnaireAnswer.Choices(listOf("b")),
        )
        GalleryQuestion(
            QuestionnaireQuestion(
                "boolean",
                "Include this option?",
                QuestionnaireQuestionKind.Boolean("Yes", "No"),
                isRequired = true,
            ),
            null,
        )
    }
}

@Composable
private fun FieldsGallery() {
    Column(
        // A scrolling column gives each questionnaire unbounded height, so it wraps its page.
        Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.large),
    ) {
        GalleryQuestion(
            QuestionnaireQuestion(
                "number",
                "How far?",
                QuestionnaireQuestionKind.Number(0.0..100.0, step = 5.0, unitLabel = "km"),
                isRequired = true,
            ),
            QuestionnaireAnswer.Number(12.5),
        )
        GalleryQuestion(
            QuestionnaireQuestion(
                "date",
                "Which date do you prefer?",
                QuestionnaireQuestionKind.Date(GalleryToday..LocalDate(2026, 12, 31)),
                isRequired = true,
            ),
            null,
        )
        GalleryQuestion(
            QuestionnaireQuestion("text", "Anything else?", QuestionnaireQuestionKind.Text("Write your answer")),
            null,
        )
    }
}
