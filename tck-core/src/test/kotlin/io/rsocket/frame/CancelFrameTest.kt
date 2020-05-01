package io.rsocket.frame

import io.rsocket.expect.frame.*
import io.rsocket.frame.shared.*
import org.junit.jupiter.api.*
import strikt.api.*
import strikt.assertions.*

class CancelFrameTest {
    @Test
    fun testEncoding() {
        val cancel = CancelFrame(
            FrameHeader(1, UntypedFlags.Empty)
        )

        val buffer = cancel.buffer(allocator)
        val newCancel = buffer.frame().asCancel()

        cancel expect newCancel

        expectThat(newCancel) {
            get { header.streamId }.isEqualTo(1)
        }
    }

    @Test
    fun testEncodingStreamIdAny() {
        val cancel = CancelFrame(
            FrameHeader(12345, UntypedFlags.Empty)
        )

        val buffer = cancel.buffer(allocator)
        val newCancel = buffer.frame().asCancel()

        cancel expect newCancel

        expectThat(newCancel) {
            get { header.streamId }.isEqualTo(12345)
        }
    }
}
