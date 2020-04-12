package io.rsocket.frame

import io.netty.buffer.*
import io.rsocket.expect.frame.*
import io.rsocket.frame.shared.*
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
        ).buffer(allocator)
        expectThat(ByteBufUtil.hexDump(error)).isEqualTo("000000012c000000020164")
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
    }
}
