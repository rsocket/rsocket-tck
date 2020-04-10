package io.rsocket.frame

import io.netty.buffer.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

class RequestNFrameFlyweightTest {
    @Test
    fun testEncoding() {
        var frame = RequestNFrame.encode(allocator, 1, 5).buffer
        frame = FrameWithLength.encode(allocator, frame, frame.readableBytes()).buffer
        assertEquals("00000a00000001200000000005", ByteBufUtil.hexDump(frame))
    }
}
