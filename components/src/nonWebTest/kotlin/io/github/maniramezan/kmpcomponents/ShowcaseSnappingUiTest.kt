package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class ShowcaseSnappingUiTest {
    /** The first visible item index and its scroll offset once the row settles. */
    private fun settleAfterPartialDrag(snapping: ShowcaseSnapping): Pair<Int, Int> {
        val state = LazyListState()
        var settled = -1 to -1
        runComposeUiTest {
            setContent {
                KmpTheme {
                    Box(Modifier.width(300.dp)) {
                        ShowcaseRow(
                            items = (1..10).toList(),
                            modifier = Modifier.testTag("row"),
                            itemWidth = ShowcaseItemWidth.Fixed(100.dp),
                            snapping = snapping,
                            state = state,
                        ) { Box(Modifier.height(80.dp)) { Text("Item $it") } }
                    }
                }
            }
            // A slow drag of about one and a half items leaves almost no fling velocity.
            onNodeWithTag("row").performTouchInput {
                down(Offset(centerX + 75.dp.toPx(), centerY))
                repeat(DRAG_STEPS) { moveBy(Offset(-165.dp.toPx() / DRAG_STEPS, 0f), delayMillis = 50) }
                up()
            }
            waitForIdle()
            settled = state.firstVisibleItemIndex to state.firstVisibleItemScrollOffset
        }
        return settled
    }

    @Test
    fun `item snapping settles on an item edge`() {
        val (index, offset) = settleAfterPartialDrag(ShowcaseSnapping.ITEM)
        assertTrue(index >= 1, "The drag did not scroll the row")
        assertEquals(0, offset)
    }

    @Test
    fun `free scrolling stops between items`() {
        val (index, offset) = settleAfterPartialDrag(ShowcaseSnapping.FREE)
        assertTrue(index >= 1, "The drag did not scroll the row")
        assertNotEquals(0, offset)
    }
}

private const val DRAG_STEPS = 20
