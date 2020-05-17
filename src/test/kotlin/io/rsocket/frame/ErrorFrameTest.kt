package io.rsocket.tck.frame

import io.netty.buffer.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

class ErrorFrameTest {
    @Test
    fun testEncode() {
        var frame = ErrorFrame.encode(allocator, 1, 0x00000201, TextBuffer("d")).buffer
        frame = FrameWithLength.encode(allocator, frame, frame.readableBytes()).buffer
        assertEquals("00000b000000012c000000020164", ByteBufUtil.hexDump(frame))
    }

    @Test
    fun testValues() {
        val frame = ErrorFrame.encode(allocator, 1, 0x00000201, TextBuffer("d"))
        assertEquals("d", frame.data.text())
        assertEquals(0x00000201, frame.code)
    }
}
