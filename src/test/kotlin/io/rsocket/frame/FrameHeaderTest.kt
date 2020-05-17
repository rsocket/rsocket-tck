package io.rsocket.tck.frame

import io.netty.buffer.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

class FrameHeaderTest {
    @Test
    fun typeAndFlag() {
        val frameType = FrameType.REQUEST_FNF
        val flags = 951
        val header = FrameHeader.encode(ByteBufAllocator.DEFAULT, frameType, flags, 0)
        assertEquals(flags, header.flags)
        assertEquals(frameType, header.frameType)
    }

    @Test
    fun typeAndFlagTruncated() {
        val frameType = FrameType.SETUP
        val flags = 1975 // 1 bit too many
        val header = FrameHeader.encode(ByteBufAllocator.DEFAULT, frameType, flags, 0)
        assertNotEquals(flags, header.flags)
        assertEquals(flags and 1023, header.flags)
        assertEquals(frameType, header.frameType)
    }

    @Test
    fun streamId() {
        val header = FrameHeader.encode(ByteBufAllocator.DEFAULT, FrameType.REQUEST_FNF, 0, 322)
        assertEquals(322, header.streamId)
    }

    @Test
    fun hasFollowsFlag() {
        val header = FrameHeader.encode(ByteBufAllocator.DEFAULT, FrameType.REQUEST_FNF, FrameHeader.Flags.F, 0)
        assertTrue(header.hasFollows)
    }

    @Test
    fun frameTypeNext() {
        val header = FrameHeader.encode(ByteBufAllocator.DEFAULT, FrameType.PAYLOAD, FrameHeader.Flags.N, 0)
        assertEquals(FrameType.NEXT, header.frameType)
    }

    @Test
    fun frameTypeComplete() {
        val header = FrameHeader.encode(ByteBufAllocator.DEFAULT, FrameType.PAYLOAD, FrameHeader.Flags.C, 0)
        assertEquals(FrameType.COMPLETE, header.frameType)
    }

    @Test
    fun frameTypeNextComplete() {
        val header = FrameHeader.encode(ByteBufAllocator.DEFAULT, FrameType.PAYLOAD, FrameHeader.Flags.N or FrameHeader.Flags.C, 0)
        assertEquals(FrameType.NEXT_COMPLETE, header.frameType)
    }
}
