package io.rsocket.tck.frame

import io.netty.buffer.*
import io.rsocket.tck.expect.frame.*
import io.rsocket.tck.frame.shared.*
import org.junit.jupiter.api.*
import strikt.api.*
import strikt.assertions.*

class ErrorFrameTest {
    @Test
    fun testEncode() {
        val error = ErrorFrame(
            FrameHeader(1, UntypedFlags.Empty),
            DefinedErrorCode.ApplicationError,
            TextBuffer("d")
        )
        val buffer = error.buffer(allocator)
        val newError = buffer.frame().asError()

        error expect newError

        expectThat(ByteBufUtil.hexDump(buffer)).isEqualTo("000000012c000000020164")
    }

    @Test
    fun testValues() {
        val error = ErrorFrame(
            FrameHeader(1, UntypedFlags.Empty),
            DefinedErrorCode.ApplicationError,
            TextBuffer("d")
        )
        val newError = error.buffer(allocator).frame().asError()

        error expect newError

        expectThat(newError) {
            get { code.value }.isEqualTo(0x00000201)
            get { data.text() }.isEqualTo("d")
        }
    }
}
