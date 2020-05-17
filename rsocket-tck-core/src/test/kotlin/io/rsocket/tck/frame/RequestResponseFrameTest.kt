package io.rsocket.tck.frame

import io.rsocket.tck.expect.frame.*
import io.rsocket.tck.frame.shared.*
import org.junit.jupiter.api.*
import strikt.api.*
import strikt.assertions.*

class RequestResponseFrameTest {

    @Test
    fun requestResponseDataMetadata() {
        val requestResponse = RequestResponseFrame(
            FrameHeader(1, RequestResponseFlags(true, false)),
            Payload(
                PayloadMetadata(TextBuffer("md")),
                TextBuffer("d")
            )
        )

        val newRequestResponse = requestResponse.buffer(allocator).frame().asRequestResponse()

        requestResponse expect newRequestResponse

        expectThat(newRequestResponse) {
            get { payload.metadata?.value?.text() }.isEqualTo("md")
            get { payload.data.text() }.isEqualTo("d")
        }
    }

    @Test
    fun requestResponseData() {
        val requestResponse = RequestResponseFrame(
            FrameHeader(1, RequestResponseFlags(false, false)),
            Payload(null, TextBuffer("d"))
        )

        val newRequestResponse = requestResponse.buffer(allocator).frame().asRequestResponse()

        requestResponse expect newRequestResponse

        expectThat(newRequestResponse) {
            get { payload.metadata }.isEqualTo(null)
            get { payload.data.text() }.isEqualTo("d")
        }
    }

    @Test
    fun requestResponseMetadata() {
        val requestResponse = RequestResponseFrame(
            FrameHeader(1, RequestResponseFlags(true, false)),
            Payload(PayloadMetadata(TextBuffer("md")))
        )

        val newRequestResponse = requestResponse.buffer(allocator).frame().asRequestResponse()

        requestResponse expect newRequestResponse

        expectThat(newRequestResponse) {
            get { payload.metadata?.value?.text() }.isEqualTo("md")
            get { payload.data.readableBytes() }.isEqualTo(0)
        }
    }

}
