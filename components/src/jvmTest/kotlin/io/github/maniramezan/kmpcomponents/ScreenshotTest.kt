package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.RoborazziOptions
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

// Golden images of every component, checked by `./gradlew :components:verifyRoborazziJvm` and updated
// with `recordRoborazziJvm`. They catch token-mapping and layout regressions that assertions miss.
@OptIn(ExperimentalTestApi::class)
class ScreenshotTest {
    @Test
    fun `gallery in light mode`() = capture("gallery-light", ThemeMode.LIGHT)

    @Test
    fun `gallery in dark mode`() = capture("gallery-dark", ThemeMode.DARK)

    @Test
    fun `gallery at double font scale`() = capture("gallery-font-200", ThemeMode.LIGHT, fontScale = 2f)

    private fun capture(name: String, mode: ThemeMode, fontScale: Float = 1f) = runDesktopComposeUiTest(
        width = WINDOW_WIDTH,
        height = WINDOW_HEIGHT,
    ) {
        setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                KmpTheme(mode = mode) {
                    Surface(Modifier.width(GALLERY_WIDTH)) { Gallery() }
                }
            }
        }
        onRoot().captureRoboImage("$SNAPSHOT_DIR/$name.png", roborazziOptions = OPTIONS)
    }

    private companion object {
        const val SNAPSHOT_DIR = "src/jvmTest/snapshots"
        const val WINDOW_WIDTH = 480
        const val WINDOW_HEIGHT = 2400
        val GALLERY_WIDTH = WINDOW_WIDTH.dp

        // Allow antialiasing noise between machines; real regressions change far more pixels.
        val OPTIONS = RoborazziOptions(compareOptions = RoborazziOptions.CompareOptions(changeThreshold = 0.01f))
    }
}

@Composable
private fun Gallery() {
    val spacing = MaterialTheme.kmpSpacing
    Column(Modifier.padding(spacing.medium), verticalArrangement = Arrangement.spacedBy(spacing.small)) {
        SectionCard(title = "Actions") {
            OutlinedButton(onClick = {}) { Text("Delete example item…") }
            OutlinedButton(onClick = {}, enabled = false) { Text("Disabled action") }
        }
        SectionCard(title = "Section", description = "SectionCard with a description") {
            ThemeModeSelector(selected = ThemeMode.SYSTEM, onSelect = {})
            ThemeModeDropdown(selected = ThemeMode.DARK, onSelect = {}, label = "Color scheme")
        }
        FormTextField(value = "", onValueChange = {}, label = "Name", errorText = "Required")
        SecretTextField(value = "secret", onValueChange = {}, label = "Password")
        val labelTopPadding = with(LocalDensity.current) { MaterialTheme.typography.bodySmall.lineHeight.toDp() / 2 }
        Row(
            modifier = Modifier.fillMaxWidth(),
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
            InfoTooltip("Show or hide the secret.", modifier = Modifier.padding(top = labelTopPadding))
        }
        StatusTone.entries.forEach { tone ->
            StatusMessage(
                message = "${tone.name.lowercase().replaceFirstChar(Char::uppercase)} message",
                modifier = Modifier.fillMaxWidth(),
                tone = tone,
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(spacing.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TonalBadge("Info")
            TonalBadge("Error", tone = StatusTone.ERROR)
            CountBadge(120)
            InfoTooltip("Info")
        }
        CheckStatusRow(CheckStatus.Success("Check succeeded"))
        CheckStatusRow(CheckStatus.Failure("Example error"))
        TabChipRow {
            TabChip(label = "First tab", selected = true, onClick = {}, onClose = {})
            TabChip(label = "Second tab", selected = false, onClick = {})
        }
        SelectableCard(selected = true, onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("Selected card") }
        Row(Modifier.height(IntrinsicSize.Min)) {
            SidebarNavigation(modifier = Modifier.width(SIDEBAR_WIDTH)) {
                SidebarItem(label = "General", selected = true, onClick = {})
                SidebarItem(label = "Other", selected = false, onClick = {})
            }
            EmptyState(title = "Nothing here", modifier = Modifier.weight(1f), message = "EmptyState")
        }
        HorizontalSplitPane(
            split = 0.4f,
            onSplitChange = {},
            modifier = Modifier.fillMaxWidth().height(SPLIT_HEIGHT),
            leading = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Leading") } },
            trailing = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Trailing") } },
        )
    }
}

private val SIDEBAR_WIDTH = 200.dp
private val SPLIT_HEIGHT = 60.dp
