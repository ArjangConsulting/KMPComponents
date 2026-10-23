package io.github.maniramezan.kmpcomponents.sample

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import io.github.maniramezan.kmpcomponents.SecondaryButton
import io.github.maniramezan.kmpcomponents.ThemeSwitch
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Lets the tester disable a picker while its popup has focus. */
@Composable
internal fun PickerAvailabilityControls(enabled: Boolean, onEnabledChange: (Boolean) -> Unit) {
    var waiting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    ThemeSwitch(checked = enabled, onCheckedChange = onEnabledChange, label = "Picker enabled")
    SecondaryButton(
        text = if (waiting) "Open the picker now" else "Disable picker in 3 seconds",
        enabled = enabled && !waiting,
        onClick = {
            waiting = true
            scope.launch {
                delay(DISABLE_DELAY_MILLIS)
                onEnabledChange(false)
                waiting = false
            }
        },
    )
    Text("Start the timer, then open the picker. Its menu closes when it becomes disabled.")
}

private const val DISABLE_DELAY_MILLIS = 3_000L
