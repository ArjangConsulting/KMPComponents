package io.github.maniramezan.kmpcomponents

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals

// Desktop-only because runComposeUiTest needs a Skiko runtime; Android host tests have no renderer.
@OptIn(ExperimentalTestApi::class)
class ComponentUiTest {
    @Test
    fun `secret field toggles between show and hide`() = runComposeUiTest {
        setContent {
            KmpTheme { SecretTextField(value = "sk-123", onValueChange = {}, label = "API key") }
        }
        onNodeWithText("Show").performClick()
        onNodeWithText("Hide").assertIsDisplayed()
        onNodeWithText("Hide").performClick()
        onNodeWithText("Show").assertIsDisplayed()
    }

    @Test
    fun `provider form reports edits through onStateChange`() = runComposeUiTest {
        var state by mutableStateOf(ProviderConfigurationState())
        setContent {
            KmpTheme { ProviderConfigurationForm(state = state, onStateChange = { state = it }) }
        }
        onNode(hasSetTextAction() and hasText("Model")).performTextInput("large")
        waitForIdle()
        assertEquals("large", state.model)
    }

    @Test
    fun `provider form shows validation errors only when asked`() = runComposeUiTest {
        var showErrors by mutableStateOf(false)
        setContent {
            KmpTheme {
                ProviderConfigurationForm(
                    state = ProviderConfigurationState(baseUrl = "example.com"),
                    onStateChange = {},
                    showValidationErrors = showErrors,
                )
            }
        }
        onAllNodes(hasText("Required")).assertCountEquals(0)
        showErrors = true
        waitForIdle()
        onNodeWithText(ProviderConfigurationLabels().invalidUrlError).assertIsDisplayed()
        onAllNodes(hasText("Required")).assertCountEquals(3)
    }

    @Test
    fun `theme mode selector reports the chosen mode`() = runComposeUiTest {
        var mode by mutableStateOf(ThemeMode.SYSTEM)
        setContent { KmpTheme(mode = mode) { ThemeModeSelector(selected = mode, onSelect = { mode = it }) } }
        onNodeWithText("Dark").performClick()
        waitForIdle()
        assertEquals(ThemeMode.DARK, mode)
    }

    @Test
    fun `section card renders title description and content`() = runComposeUiTest {
        setContent {
            KmpTheme {
                SectionCard(title = "Provider", description = "Where requests go") {
                    StatusMessage("Connected", tone = StatusTone.SUCCESS)
                }
            }
        }
        onNodeWithText("Provider").assertIsDisplayed()
        onNodeWithText("Where requests go").assertIsDisplayed()
        onNodeWithText("Connected").assertIsDisplayed()
    }
}
