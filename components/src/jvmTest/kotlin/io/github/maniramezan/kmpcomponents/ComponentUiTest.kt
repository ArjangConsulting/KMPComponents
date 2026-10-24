package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.maniramezan.kommon.designsystem.KommonDesignTokens
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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
    fun `clearing a secret resets its visibility`() = runComposeUiTest {
        var secret by mutableStateOf("first-secret")
        setContent {
            KmpTheme { SecretTextField(value = secret, onValueChange = { secret = it }, label = "API key") }
        }
        onNodeWithText("Show").performClick()
        onNodeWithText("Hide").assertIsDisplayed()
        secret = ""
        waitForIdle()
        onNodeWithText("Show").assertIsDisplayed()
        secret = "next-secret"
        waitForIdle()
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
        onNodeWithText(ProviderConfigurationLabels().invalidUrlError, useUnmergedTree = true)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
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
    fun `theme mode selector fits multiword labels at normal and double font scales`() = runDesktopComposeUiTest(
        width = 1600,
        height = 300,
    ) {
        val labels = ThemeModeLabels(system = "Use system settings")
        var fontScale by mutableStateOf(1f)
        var mode by mutableStateOf(ThemeMode.SYSTEM)
        setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                KmpTheme {
                    ThemeModeSelector(selected = mode, onSelect = { mode = it }, labels = labels)
                }
            }
        }
        listOf(1f, 2f).forEach { scale ->
            fontScale = scale
            ThemeMode.entries.forEach { selected ->
                mode = selected
                waitForIdle()
                ThemeMode.entries.forEach { option ->
                    val label = labels.labelFor(option)
                    val text = onNodeWithText(label, useUnmergedTree = true)
                    val results = mutableListOf<TextLayoutResult>()
                    text.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
                    assertEquals(1, results.single().lineCount, "$label at font scale $scale")
                    assertFalse(results.single().hasVisualOverflow, "$label at font scale $scale")
                    val textBounds = text.fetchSemanticsNode().boundsInRoot
                    val buttonBounds = onNodeWithText(label).fetchSemanticsNode().boundsInRoot
                    assertTrue(textBounds.left >= buttonBounds.left, "$label starts inside its button")
                    assertTrue(textBounds.right <= buttonBounds.right, "$label ends inside its button")
                }
            }
        }
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

    @Test
    fun `secret field icon toggle is labeled with the next action`() = runComposeUiTest {
        setContent {
            KmpTheme {
                SecretTextField(
                    value = "sk-123",
                    onValueChange = {},
                    label = "API key",
                    showLabel = "Show key",
                    hideLabel = "Hide key",
                    revealToggle = SecretRevealToggle.ICON,
                )
            }
        }
        onNodeWithContentDescription("Show key").performClick()
        onNodeWithContentDescription("Hide key").assertIsDisplayed()
    }

    @Test
    fun `field and tooltip keep spacing and usable icon targets at large text sizes`() = runDesktopComposeUiTest(
        width = 600,
        height = 300,
    ) {
        var fontScale by mutableStateOf(1f)
        var expectedGap = 0.dp
        var expectedPadding = 0.dp
        setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                KmpTheme {
                    val spacing = MaterialTheme.kmpSpacing
                    expectedGap = spacing.small
                    expectedPadding = spacing.medium
                    val labelTopPadding = with(LocalDensity.current) {
                        MaterialTheme.typography.bodySmall.lineHeight.toDp() / 2
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(spacing.medium),
                        horizontalArrangement = Arrangement.spacedBy(spacing.small),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SecretTextField(
                            value = "secret",
                            onValueChange = {},
                            label = "Secret",
                            modifier = Modifier.weight(1f),
                            revealToggle = SecretRevealToggle.ICON,
                        )
                        InfoTooltip("More information", modifier = Modifier.padding(top = labelTopPadding))
                    }
                }
            }
        }
        listOf(1f, 2f).forEach { scale ->
            fontScale = scale
            waitForIdle()
            val tooltip = onNodeWithContentDescription("More information")
            tooltip.assertWidthIsAtLeast(48.dp).assertHeightIsAtLeast(48.dp)
            val fieldBounds = onNode(hasSetTextAction()).getUnclippedBoundsInRoot()
            val tooltipBounds = tooltip.getUnclippedBoundsInRoot()
            val rootBounds = onRoot().getUnclippedBoundsInRoot()
            assertTrue(tooltipBounds.left - fieldBounds.right >= expectedGap)
            assertTrue(rootBounds.right - tooltipBounds.right >= expectedPadding)
            val revealBounds = onNodeWithContentDescription("Show").getUnclippedBoundsInRoot()
            val fieldCenter = (revealBounds.top + revealBounds.bottom) / 2
            val tooltipCenter = (tooltipBounds.top + tooltipBounds.bottom) / 2
            assertEquals(fieldCenter.value, tooltipCenter.value, 1f)
            tooltip.requestFocus().assertIsFocused()
            onNodeWithContentDescription("Show").performClick()
            onNodeWithContentDescription("Hide").performClick()
        }
    }

    @Test
    fun `form field error replaces supporting text and is announced`() = runComposeUiTest {
        var error by mutableStateOf<String?>(null)
        setContent {
            KmpTheme {
                FormTextField(
                    value = "",
                    onValueChange = {},
                    label = "Name",
                    placeholder = "Your name",
                    supportingText = "Shown on invoices",
                    errorText = error,
                )
            }
        }
        onNodeWithText("Shown on invoices", useUnmergedTree = true)
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.LiveRegion))
        error = "Required"
        waitForIdle()
        onAllNodes(hasText("Shown on invoices"), useUnmergedTree = true).assertCountEquals(0)
        onNodeWithText("Required", useUnmergedTree = true)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
    }

    @Test
    fun `only warning and error status messages are announced`() = runComposeUiTest {
        setContent {
            KmpTheme {
                Column {
                    StatusTone.entries.forEach { tone ->
                        StatusMessage(message = tone.name, tone = tone, title = "T-$tone")
                    }
                }
            }
        }
        StatusTone.entries.forEach { tone ->
            val announced = tone == StatusTone.WARNING || tone == StatusTone.ERROR
            val matcher = if (announced) {
                SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite)
            } else {
                SemanticsMatcher.keyNotDefined(SemanticsProperties.LiveRegion)
            }
            onNode(hasText(tone.name) and hasText("T-$tone")).assert(matcher)
        }
    }

    @Test
    fun `kmp theme provides the tokens for the chosen mode`() = runComposeUiTest {
        val tokens = KommonDesignTokens.default
        var surface: Color? = null
        var spacing: ComposeSpacing? = null
        setContent {
            KmpTheme(mode = ThemeMode.DARK, tokens = tokens) {
                surface = MaterialTheme.colorScheme.surface
                spacing = MaterialTheme.kmpSpacing
            }
        }
        waitForIdle()
        assertEquals(tokens.darkColors.surface.toComposeColor(), surface)
        assertEquals(tokens.spacing.toComposeSpacing(), spacing)
    }

    @Test
    fun `kmp spacing outside a theme falls back to kommon defaults`() = runComposeUiTest {
        var spacing: ComposeSpacing? = null
        setContent { spacing = MaterialTheme.kmpSpacing }
        waitForIdle()
        assertEquals(KommonDesignTokens.default.spacing.toComposeSpacing(), spacing)
    }
}
