package io.github.maniramezan.kmpcomponents.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import io.github.maniramezan.kmpcomponents.KmpTheme
import io.github.maniramezan.kmpcomponents.LocalReduceMotionEnabled
import io.github.maniramezan.kmpcomponents.kmpSpacing
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class CatalogUiTest {
    @Test
    fun `every section lays out at normal and double text sizes in both directions`() = runDesktopComposeUiTest(
        width = 560,
        height = 900,
    ) {
        var section by mutableStateOf(CatalogSection.FORMS)
        var fontScale by mutableStateOf(1f)
        var layoutDirection by mutableStateOf(LayoutDirection.Ltr)
        setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density.density, fontScale),
                LocalLayoutDirection provides layoutDirection,
                LocalReduceMotionEnabled provides true,
            ) {
                KmpTheme {
                    Column(
                        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.medium),
                    ) { CatalogExamples(section) }
                }
            }
        }
        listOf(LayoutDirection.Ltr, LayoutDirection.Rtl).forEach { direction ->
            layoutDirection = direction
            listOf(1f, 2f).forEach { scale ->
                fontScale = scale
                CatalogSection.entries.forEach { example ->
                    section = example
                    waitForIdle()
                    onRoot().assertExists()
                }
            }
        }
    }

    @Test
    fun `catalog navigation opens a section and replaces the previous examples`() = runDesktopComposeUiTest(
        width = 560,
        height = 900,
    ) {
        val reportedAppearances = mutableListOf<Boolean>()
        setContent { ComponentCatalog(onDarkThemeChanged = reportedAppearances::add) }
        onNodeWithText("Forms and validation").performScrollTo().assertIsDisplayed()
        onNodeWithText("Forms").performScrollTo().performClick()
        onNodeWithText(CatalogSection.STATUS.label).performScrollTo().performClick()
        onNodeWithText("Status messages and badges").performScrollTo().assertIsDisplayed()
        onNodeWithText("Forms and validation").assertDoesNotExist()
        onNodeWithText("Appearance and accessibility").performScrollTo().performClick()
        onNodeWithText("Light").performScrollTo().performClick()
        waitForIdle()
        assertFalse(reportedAppearances.last())
        onNodeWithText("Dark").performScrollTo().performClick()
        waitForIdle()
        assertTrue(reportedAppearances.last())
        onNode(hasText("Right-to-left layout")).performScrollTo().performClick()
        onNodeWithText("Status messages and badges").performScrollTo().assertIsDisplayed()
    }
}
