package io.github.maniramezan.kmpcomponents

import io.github.maniramezan.kmpcomponents.ChatComposerAction.Processing
import io.github.maniramezan.kmpcomponents.ChatComposerAction.Send
import io.github.maniramezan.kmpcomponents.ChatComposerAction.StartVoice
import io.github.maniramezan.kmpcomponents.ChatComposerAction.StopVoice
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ChatComposerActionTest {
    @Test
    fun `a draft sends and a blank draft without voice input cannot send`() {
        assertEquals(Send(enabled = true), resolveChatComposerAction("Hi", null, sendEnabled = true))
        assertEquals(Send(enabled = false), resolveChatComposerAction("Hi", null, sendEnabled = false))
        assertEquals(Send(enabled = false), resolveChatComposerAction("  \n", null, sendEnabled = true))
    }

    @Test
    fun `a blank draft with voice input shows the microphone`() {
        val idle = ChatComposerVoiceState.Idle
        assertEquals(StartVoice(enabled = true), resolveChatComposerAction("", idle, sendEnabled = true))
        assertEquals(StartVoice(enabled = false), resolveChatComposerAction("", idle, sendEnabled = false))
        val unavailable = ChatComposerVoiceState.Unavailable
        assertEquals(StartVoice(enabled = false), resolveChatComposerAction("", unavailable, sendEnabled = true))
        assertEquals(Send(enabled = true), resolveChatComposerAction("Typed", idle, sendEnabled = true))
    }

    @Test
    fun `listening and processing win over the draft`() {
        val listening = ChatComposerVoiceState.Listening(level = 0.4f)
        assertEquals(StopVoice(0.4f), resolveChatComposerAction("Transcript", listening, sendEnabled = false))
        assertEquals(StopVoice(1f), resolveChatComposerAction("", ChatComposerVoiceState.Listening(3f), true))
        assertEquals(StopVoice(0f), resolveChatComposerAction("", ChatComposerVoiceState.Listening(-1f), true))
        assertEquals(Processing, resolveChatComposerAction("Transcript", ChatComposerVoiceState.Processing, true))
    }

    @Test
    fun `stopping is always tappable and processing never is`() {
        assertTrue(Send(enabled = true).isTappable)
        assertFalse(Send(enabled = false).isTappable)
        assertFalse(StartVoice(enabled = false).isTappable)
        assertTrue(StopVoice(0f).isTappable)
        assertFalse(Processing.isTappable)
    }

    @Test
    fun `editing pauses only while the transcript is live`() {
        assertTrue(Send(enabled = true).allowsEditing)
        assertTrue(StartVoice(enabled = false).allowsEditing)
        assertFalse(StopVoice(0f).allowsEditing)
        assertFalse(Processing.allowsEditing)
    }

    @Test
    fun `the listening ring grows with the level and clamps`() {
        assertEquals(RING_REST_SCALE, chatComposerRingScale(0f))
        assertEquals(RING_REST_SCALE, chatComposerRingScale(-1f))
        assertEquals(RING_REST_SCALE + RING_MAX_GROWTH, chatComposerRingScale(1f))
        assertEquals(chatComposerRingScale(1f), chatComposerRingScale(5f))
        assertTrue(chatComposerRingScale(0.5f) < chatComposerRingScale(0.9f))
    }
}
