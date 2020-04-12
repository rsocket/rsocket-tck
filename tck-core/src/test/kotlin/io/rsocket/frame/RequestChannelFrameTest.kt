package io.rsocket.frame

import io.netty.buffer.*
import io.rsocket.expect.frame.*
import io.rsocket.frame.shared.*
import org.junit.jupiter.api.*
import strikt.api.*
import strikt.assertions.*

class RequestChannelFrameTest {
    @Test
    fun testEncoding() {
        val requestChannel = RequestChannelFrame(
            FrameHeader(1, RequestChannelFlags(true, false, false)),
            1,
            Payload(
                PayloadMetadata(TextBuffer("md")),
                TextBuffer("d")
            )
        )

        val buffer = requestChannel.buffer(allocator)
        val newRequestChannel = buffer.frame().asRequestChannel()

        requestChannel expect newRequestChannel

        expectThat(newRequestChannel) {
            get { initialRequestN }.isEqualTo(1)
            get { payload.metadata?.value?.text() }.isEqualTo("md")
            get { payload.data.text() }.isEqualTo("d")
        }
    }

    @Test
    fun testEncodingWithEmptyMetadata() {
        val requestChannel = RequestChannelFrame(
            FrameHeader(1, RequestChannelFlags(true, false, false)),
            1,
            Payload(
                PayloadMetadata(Unpooled.EMPTY_BUFFER),
                TextBuffer("d")
            )
        )

        val buffer = requestChannel.buffer(allocator)
        val newRequestChannel = buffer.frame().asRequestChannel()

        requestChannel expect newRequestChannel

        expectThat(newRequestChannel) {
            get { initialRequestN }.isEqualTo(1)
            get { payload.metadata?.value?.readableBytes() }.isEqualTo(0)
            get { payload.metadata?.length }.isEqualTo(0)
            get { payload.data.text() }.isEqualTo("d")
        }
    }

    @Test
    fun testEncodingWithNullMetadata() {
        val requestChannel = RequestChannelFrame(
            FrameHeader(1, RequestChannelFlags(false, false, false)),
            1,
            Payload(null, TextBuffer("d"))
        )

        val buffer = requestChannel.buffer(allocator)
        val newRequestChannel = buffer.frame().asRequestChannel()

        requestChannel expect newRequestChannel

        expectThat(newRequestChannel) {
            get { initialRequestN }.isEqualTo(1)
            get { payload.metadata }.isEqualTo(null)
            get { payload.data.text() }.isEqualTo("d")
        }
    }

    @Test
    fun testEncodingWithCompleteFlag() {
        val requestChannel = RequestChannelFrame(
            FrameHeader(1, RequestChannelFlags(false, false, true)),
            1,
            Payload(null, TextBuffer("d"))
        )

        val buffer = requestChannel.buffer(allocator)
        val newRequestChannel = buffer.frame().asRequestChannel()

        requestChannel expect newRequestChannel

        expectThat(newRequestChannel) {
            get { header.flags.complete }.isEqualTo(true)
            get { initialRequestN }.isEqualTo(1)
            get { payload.metadata }.isEqualTo(null)
            get { payload.data.text() }.isEqualTo("d")
        }
    }

}
