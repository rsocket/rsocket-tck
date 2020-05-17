package io.rsocket.tck.frame

import io.rsocket.tck.expect.frame.*
import io.rsocket.tck.frame.shared.*
import org.junit.jupiter.api.*
import strikt.api.*
import strikt.assertions.*

class RequestFireAndForgetFrameTest {

    @Test
    fun requestFnfDataAndMetadata() {
        val requestFireAndForget = RequestFireAndForgetFrame(
            FrameHeader(1, RequestFireAndForgetFlags(true, false)),
            Payload(
                PayloadMetadata(TextBuffer("md")),
                TextBuffer("d")
            )
        )

        val newRequestFireAndForget = requestFireAndForget.buffer(allocator).frame().asRequestFireAndForget()

        requestFireAndForget expect newRequestFireAndForget

        expectThat(newRequestFireAndForget) {
            get { payload.metadata?.value?.text() }.isEqualTo("md")
            get { payload.data.text() }.isEqualTo("d")
        }
    }

    @Test
    fun requestFnfData() {
        val requestFireAndForget = RequestFireAndForgetFrame(
            FrameHeader(1, RequestFireAndForgetFlags(false, false)),
            Payload(null, TextBuffer("d"))
        )

        val newRequestFireAndForget = requestFireAndForget.buffer(allocator).frame().asRequestFireAndForget()

        requestFireAndForget expect newRequestFireAndForget

        expectThat(newRequestFireAndForget) {
            get { payload.metadata }.isEqualTo(null)
            get { payload.data.text() }.isEqualTo("d")
        }
    }

    @Test
    fun requestFnfMetadata() {
        val requestFireAndForget = RequestFireAndForgetFrame(
            FrameHeader(1, RequestFireAndForgetFlags(true, false)),
            Payload(PayloadMetadata(TextBuffer("md")))
        )
        val newRequestFireAndForget = requestFireAndForget.buffer(allocator).frame().asRequestFireAndForget()

        requestFireAndForget expect newRequestFireAndForget

        expectThat(newRequestFireAndForget) {
            get { payload.metadata?.value?.text() }.isEqualTo("md")
            get { payload.data.readableBytes() }.isEqualTo(0)
        }
    }
}
