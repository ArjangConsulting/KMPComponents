package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.RoborazziOptions
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

// Golden images of the app-essentials family, kept apart from `ScreenshotTest`'s gallery so each
// family's goldens stay small enough to review.
@OptIn(ExperimentalTestApi::class)
class EssentialsScreenshotTest {
    @Test
    fun `essentials in light mode`() = capture("essentials-light", ThemeMode.LIGHT)

    @Test
    fun `essentials in dark mode`() = capture("essentials-dark", ThemeMode.DARK)

    @Test
    fun `essentials at double font scale`() = capture("essentials-font-200", ThemeMode.LIGHT, fontScale = 2f)

    private fun capture(name: String, mode: ThemeMode, fontScale: Float = 1f) = runDesktopComposeUiTest(
        width = WINDOW_WIDTH,
        height = WINDOW_HEIGHT,
    ) {
        setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                KmpTheme(mode = mode) {
                    Surface(Modifier.width(GALLERY_WIDTH)) { EssentialsGallery() }
                }
            }
        }
        onRoot().captureRoboImage("$SNAPSHOT_DIR/$name.png", roborazziOptions = OPTIONS)
    }

    private companion object {
        const val SNAPSHOT_DIR = "src/jvmTest/snapshots"
        const val WINDOW_WIDTH = 480
        const val WINDOW_HEIGHT = 2000
        val GALLERY_WIDTH = WINDOW_WIDTH.dp

        // Allow antialiasing noise between machines; real regressions change far more pixels.
        val OPTIONS = RoborazziOptions(compareOptions = RoborazziOptions.CompareOptions(changeThreshold = 0.01f))
    }
}

@Composable
private fun EssentialsGallery() {
    val spacing = MaterialTheme.kmpSpacing
    Column(Modifier.padding(spacing.medium), verticalArrangement = Arrangement.spacedBy(spacing.small)) {
        SectionHeader(
            title = "Example items",
            subtitle = "SectionHeader with subtitle",
            actionLabel = "Add",
            onAction = {},
        )
        ContentRow(
            title = "Selected row",
            secondaryText = "Example",
            supportingText = "Select this item",
            selected = true,
        )
        ContentRow(
            title = "Clickable row",
            supportingText = "With a trailing action",
            onClick = {},
            trailingContent = { TextButton(onClick = {}) { Text("Remove") } },
        )
        SearchField(value = "", onValueChange = {}, modifier = Modifier.fillMaxWidth())
        SearchField(value = "alpha", onValueChange = {}, modifier = Modifier.fillMaxWidth())
        MenuPicker(
            items = listOf("Kotlin", "Swift"),
            selected = "Kotlin",
            onSelect = {},
            label = "Language",
            itemLabel = { it },
            modifier = Modifier.fillMaxWidth(),
        )
        InlineProgressIndicator(label = "Working")
        ProgressIndicator(label = "Example progress", modifier = Modifier.fillMaxWidth(), progress = 0.4f)
        NoticeCard(message = "Example notice", title = "Info", actionLabel = "Details", onAction = {})
        NoticeCard(
            message = "Example error",
            tone = StatusTone.ERROR,
            actionLabel = "Retry",
            onAction = {},
            onDismiss = {},
        )
        ThemeSwitch(
            checked = true,
            onCheckedChange = {},
            label = "Example switch",
            modifier = Modifier.fillMaxWidth(),
            supportingText = "ThemeSwitch with a tooltip",
            tooltip = "Explains this example switch",
        )
        ThemeCheckbox(checked = false, onCheckedChange = {}, label = "Include option")
        ThemeTriStateCheckbox(state = ToggleableState.Indeterminate, onClick = {}, label = "3 of 5 selected")
        KeyValueRow(key = "Identifier", value = "example", modifier = Modifier.fillMaxWidth())
        KeyValueRow(
            key = "Path",
            value = "example/item",
            modifier = Modifier.fillMaxWidth(),
            valueStyle = MaterialTheme.kmpTypography.code,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(spacing.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Avatar(name = "Ada Lovelace", size = AvatarSize.SMALL)
            Avatar(name = "Grace Hopper")
            Avatar(name = "Wren Wu", size = AvatarSize.LARGE)
            Avatar(name = "")
        }
        PageIndicator(pageCount = 5, currentPage = 1)
    }
}
