package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.width
import kotlin.test.Test
import kotlin.test.assertTrue

// A component's modifier goes on its root, so a parent's scope modifiers (align, weight) must reach
// the parent: the tooltip components wrap their content in Material's TooltipBox, which must not
// swallow them.
@OptIn(ExperimentalTestApi::class)
class TooltipLayoutTest {
    @Test
    fun `an icon button honors align in a box`() = runComposeUiTest {
        setContent {
            KmpTheme {
                Box(Modifier.size(300.dp).testTag("box")) {
                    ThemeIconButton(
                        icon = rememberVectorPainter(TestPlusIcon),
                        contentDescription = "Add",
                        onClick = {},
                        modifier = Modifier.align(Alignment.BottomEnd).testTag("button"),
                    )
                }
            }
        }
        val box = onNodeWithTag("box").getUnclippedBoundsInRoot()
        val button = onNodeWithTag("button").getUnclippedBoundsInRoot()
        assertTrue(button.right == box.right, "button right ${button.right} should meet box right ${box.right}")
        assertTrue(button.bottom == box.bottom, "button bottom ${button.bottom} should meet box bottom ${box.bottom}")
    }

    @Test
    fun `an icon button honors weight in a row`() = runComposeUiTest {
        setContent {
            KmpTheme {
                Row(Modifier.fillMaxWidth().testTag("row")) {
                    ThemeIconButton(
                        icon = rememberVectorPainter(TestPlusIcon),
                        contentDescription = "Add",
                        onClick = {},
                        modifier = Modifier.weight(1f).testTag("button"),
                    )
                }
            }
        }
        val row = onNodeWithTag("row").getUnclippedBoundsInRoot()
        val button = onNodeWithTag("button").getUnclippedBoundsInRoot()
        assertTrue(button.width == row.width, "button width ${button.width} should fill row width ${row.width}")
    }

    @Test
    fun `an info tooltip honors align in a box`() = runComposeUiTest {
        setContent {
            KmpTheme {
                Box(Modifier.size(300.dp).testTag("box")) {
                    InfoTooltip(
                        text = "Explains the field",
                        modifier = Modifier.align(Alignment.BottomEnd).testTag("tooltip"),
                    )
                }
            }
        }
        val box = onNodeWithTag("box").getUnclippedBoundsInRoot()
        val tooltip = onNodeWithTag("tooltip").getUnclippedBoundsInRoot()
        assertTrue(tooltip.right == box.right, "tooltip right ${tooltip.right} should meet box right ${box.right}")
        assertTrue(
            tooltip.bottom == box.bottom,
            "tooltip bottom ${tooltip.bottom} should meet box bottom ${box.bottom}",
        )
    }
}
