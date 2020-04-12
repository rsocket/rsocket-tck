package io.rsocket.frame

import io.netty.buffer.*
import io.rsocket.expect.frame.*
import io.rsocket.frame.shared.*
import org.junit.jupiter.api.*
import strikt.api.*
import strikt.assertions.*

class RequestStreamFrameTest {
    @Test
    fun testEncoding() {
        val requestStream = RequestStreamFrame(
            FrameHeader(1, RequestStreamFlags(true, false)),
            1,
            Payload(
                PayloadMetadata(TextBuffer("md")),
                TextBuffer("d")
            )
        )

        val buffer = requestStream.buffer(allocator)
        val newRequestStream = buffer.frame().asRequestStream()

        requestStream expect newRequestStream

        expectThat(newRequestStream) {
            get { initialRequestN }.isEqualTo(1)
            get { payload.metadata?.value?.text() }.isEqualTo("md")
            get { payload.data.text() }.isEqualTo("d")
        }

        expectThat(ByteBufUtil.hexDump(buffer)).isEqualTo("000000011900000000010000026d6464")
    }

    @Test
    fun testEncodingWithEmptyMetadata() {
        val requestStream = RequestStreamFrame(
            FrameHeader(1, RequestStreamFlags(true, false)),
            1,
            Payload(
                PayloadMetadata(Unpooled.EMPTY_BUFFER),
                TextBuffer("d")
            )
        )

        val buffer = requestStream.buffer(allocator)
        val newRequestStream = buffer.frame().asRequestStream()

        requestStream expect newRequestStream

        expectThat(newRequestStream) {
            get { initialRequestN }.isEqualTo(1)
            get { payload.metadata?.value?.readableBytes() }.isEqualTo(0)
            get { payload.metadata?.length }.isEqualTo(0)
            get { payload.data.text() }.isEqualTo("d")
        }

        expectThat(ByteBufUtil.hexDump(buffer)).isEqualTo("0000000119000000000100000064")
    }

    @Test
    fun testEncodingWithNullMetadata() {
        val requestStream = RequestStreamFrame(
            FrameHeader(1, RequestStreamFlags(false, false)),
            1,
            Payload(null, TextBuffer("d"))
        )

        val buffer = requestStream.buffer(allocator)
        val newRequestStream = buffer.frame().asRequestStream()

        requestStream expect newRequestStream

        expectThat(newRequestStream) {
            get { initialRequestN }.isEqualTo(1)
            get { payload.metadata }.isEqualTo(null)
            get { payload.data.text() }.isEqualTo("d")
        }

        expectThat(ByteBufUtil.hexDump(buffer)).isEqualTo("0000000118000000000164")
    }

}
