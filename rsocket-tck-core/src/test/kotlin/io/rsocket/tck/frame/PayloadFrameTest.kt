package io.rsocket.tck.frame

import io.rsocket.tck.expect.frame.*
import io.rsocket.tck.frame.shared.*
import org.junit.jupiter.api.*
import strikt.api.*
import strikt.assertions.*

class PayloadFrameTest {

    @Test
    fun nextCompleteDataMetadata() {
        val payload = PayloadFrame(
            FrameHeader(1, PayloadFlags(true, false, true, true)),
            Payload(PayloadMetadata(TextBuffer("md")), TextBuffer("d"))
        )

        val newPayload = payload.buffer(allocator).frame().asPayload()

        payload expect newPayload

        expectThat(newPayload) {
            get { this.payload.metadata?.value?.text() }.isEqualTo("md")
            get { this.payload.data.text() }.isEqualTo("d")
        }
    }

    @Test
    fun nextCompleteData() {
        val payload = PayloadFrame(
            FrameHeader(1, PayloadFlags(false, false, true, true)),
            Payload(null, TextBuffer("d"))
        )

        val newPayload = payload.buffer(allocator).frame().asPayload()

        payload expect newPayload

        expectThat(newPayload) {
            get { this.payload.metadata }.isEqualTo(null)
            get { this.payload.data.text() }.isEqualTo("d")
        }
    }

    @Test
    fun nextCompleteMetaData() {
        val payload = PayloadFrame(
            FrameHeader(1, PayloadFlags(true, false, true, true)),
            Payload(PayloadMetadata(TextBuffer("d")))
        )

        val newPayload = payload.buffer(allocator).frame().asPayload()

        payload expect newPayload

        expectThat(newPayload) {
            get { this.payload.metadata?.value?.text() }.isEqualTo("d")
            get { this.payload.data.readableBytes() }.isEqualTo(0)
        }
    }

    @Test
    fun nextDataMetadata() {
        val payload = PayloadFrame(
            FrameHeader(1, PayloadFlags(true, false, false, true)),
            Payload(PayloadMetadata(TextBuffer("md")), TextBuffer("d"))
        )

        val newPayload = payload.buffer(allocator).frame().asPayload()

        payload expect newPayload

        expectThat(newPayload) {
            get { this.payload.metadata?.value?.text() }.isEqualTo("md")
            get { this.payload.data.text() }.isEqualTo("d")
        }
    }

    @Test
    fun nextData() {
        val payload = PayloadFrame(
            FrameHeader(1, PayloadFlags(false, false, false, true)),
            Payload(null, TextBuffer("d"))
        )

        val newPayload = payload.buffer(allocator).frame().asPayload()

        payload expect newPayload

        expectThat(newPayload) {
            get { this.payload.metadata }.isEqualTo(null)
            get { this.payload.data.text() }.isEqualTo("d")
        }
    }

}
