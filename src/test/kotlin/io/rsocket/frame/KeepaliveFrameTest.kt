package io.rsocket.frame

import io.netty.buffer.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

class KeepaliveFrameTest {
    @Test
    fun canReadData() {
        val data = bufferOf(5, 4, 3)
        val frame = KeepAliveFrame.encode(
            allocator = allocator,
            respondFlag = true,
            lastPosition = 3,
            data = data
        )
        assertTrue(frame.respondFlag)
        assertEquals(data, frame.data)
        assertEquals(3, frame.lastPosition)
    }

    @Test
    fun testEncoding() {
        var frame = KeepAliveFrame.encode(allocator, true, 0, TextBuffer("d")).buffer
        frame = FrameWithLength.encode(allocator, frame, frame.readableBytes()).buffer
        assertEquals("00000f000000000c80000000000000000064", ByteBufUtil.hexDump(frame))
    }
}
