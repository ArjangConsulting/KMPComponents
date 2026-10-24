package io.github.maniramezan.kmpcomponents

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay

/**
 * Reveals [text] a character at a time, at [charsPerSecond], instead of showing it all at once.
 *
 * Built for streamed content such as an assistant reply that grows [text] over several
 * recompositions. When [text] grows, the reveal continues from where it is and catches up; when
 * [text] is not an extension of what is already shown, the reveal restarts from the beginning.
 * Surrogate pairs (most emoji) are revealed whole.
 *
 * The full [text] is returned at once when the system reduces motion
 * ([rememberReduceMotionEnabled]). The returned [State] is the revealed prefix, rendered however the
 * caller likes. Screen readers read whatever is on screen, so give the finished message, not each
 * intermediate prefix, to any live region.
 *
 * @param text the full target text; it may grow between recompositions.
 * @param charsPerSecond reveal speed. Values below 1 are treated as 1.
 */
@Composable
public fun rememberTypewriterReveal(text: String, charsPerSecond: Int = DEFAULT_CHARS_PER_SECOND): State<String> {
    val revealed = remember { mutableStateOf("") }
    val animate = !rememberReduceMotionEnabled()
    LaunchedEffect(text, charsPerSecond, animate) {
        if (!animate) {
            revealed.value = text
            return@LaunchedEffect
        }
        if (!text.startsWith(revealed.value)) revealed.value = ""
        val delayMillis = MILLIS_PER_SECOND / charsPerSecond.coerceAtLeast(1)
        while (revealed.value.length < text.length) {
            delay(delayMillis)
            revealed.value = text.substring(0, nextRevealLength(text, revealed.value.length))
        }
    }
    return revealed
}

/**
 * The prefix length after revealing one more character of [text] beyond [revealedLength], keeping
 * a surrogate pair together.
 */
internal fun nextRevealLength(text: String, revealedLength: Int): Int {
    if (revealedLength >= text.length) return text.length
    val next = revealedLength + 1
    val splitsPair = next < text.length && text[revealedLength].isHighSurrogate() && text[next].isLowSurrogate()
    return if (splitsPair) next + 1 else next
}

private const val DEFAULT_CHARS_PER_SECOND = 60
private const val MILLIS_PER_SECOND = 1_000L
