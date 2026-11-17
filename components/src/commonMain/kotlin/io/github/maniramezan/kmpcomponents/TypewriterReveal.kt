package io.github.maniramezan.kmpcomponents

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameMillis
import kotlinx.coroutines.flow.first

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
 * @param charsPerSecond reveal speed. Values below 1 are treated as 1. The reveal follows elapsed
 *   frame time, so speeds above the display's frame rate reveal several characters per frame.
 */
@Composable
public fun rememberTypewriterReveal(text: String, charsPerSecond: Int = DEFAULT_CHARS_PER_SECOND): State<String> {
    val revealed = remember { mutableStateOf("") }
    val animate = !rememberReduceMotionEnabled()
    val target = rememberUpdatedState(text)
    // Keyed on the speed only: a stream that grows the text faster than one character's delay must
    // not restart the timer, or nothing is revealed until the stream pauses.
    LaunchedEffect(charsPerSecond, animate) {
        if (!animate) {
            snapshotFlow { target.value }.collect { revealed.value = it }
            return@LaunchedEffect
        }
        val millisPerChar = MILLIS_PER_SECOND.toDouble() / charsPerSecond.coerceAtLeast(1)
        var owedChars = 0.0
        var lastFrame = withFrameMillis { it }
        while (true) {
            val full = target.value
            if (!full.startsWith(revealed.value)) revealed.value = ""
            if (revealed.value.length >= full.length) {
                snapshotFlow { target.value }.first { it != full }
                owedChars = 0.0
                lastFrame = withFrameMillis { it }
                continue
            }
            val frame = withFrameMillis { it }
            owedChars += (frame - lastFrame) / millisPerChar
            lastFrame = frame
            val steps = owedChars.toInt()
            if (steps > 0) {
                owedChars -= steps
                revealed.value = revealedPrefix(target.value, revealed.value, steps)
            }
        }
    }
    return revealed
}

/**
 * [current] extended by [steps] characters of [full], or restarted from the beginning of [full]
 * when [current] is not a prefix of it. One allocation per frame, however many characters a frame
 * reveals.
 */
internal fun revealedPrefix(full: String, current: String, steps: Int): String {
    var length = if (full.startsWith(current)) current.length else 0
    repeat(steps) { length = nextRevealLength(full, length) }
    return full.substring(0, length)
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
