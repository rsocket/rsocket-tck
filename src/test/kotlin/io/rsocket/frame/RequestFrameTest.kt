package io.rsocket.tck.frame

import io.netty.buffer.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

class RequestFrameTest {
    @Test
    fun testEncoding() {
        var frame = RequestStreamFrame.encode(
            allocator,
            1,
            false,
            1,
            TextBuffer("d"),
            TextBuffer("md")
        ).buffer
        frame = FrameWithLength.encode(allocator, frame, frame.readableBytes()).buffer
        assertEquals("000010000000011900000000010000026d6464", ByteBufUtil.hexDump(frame))
        frame.release()
    }

    @Test
    fun testEncodingWithEmptyMetadata() {
        var frame = RequestStreamFrame.encode(
            allocator,
            1,
            false,
            1,
            TextBuffer("d"),
            Unpooled.EMPTY_BUFFER
        ).buffer
        frame = FrameWithLength.encode(allocator, frame, frame.readableBytes()).buffer
        assertEquals("00000e0000000119000000000100000064", ByteBufUtil.hexDump(frame))
        frame.release()
    }

    @Test
    fun testEncodingWithNullMetadata() {
        var frame = RequestStreamFrame.encode(
            allocator,
            1,
            false,
            1,
            TextBuffer("d"),
            null
        ).buffer
        frame = FrameWithLength.encode(allocator, frame, frame.readableBytes()).buffer
        assertEquals("00000b0000000118000000000164", ByteBufUtil.hexDump(frame))
        frame.release()
    }

    @Test
    fun requestResponseDataMetadata() {
        val request = RequestResponseFrame.encode(
            allocator,
            1,
            false,
            Unpooled.copiedBuffer("d", Charsets.UTF_8),
            Unpooled.copiedBuffer("md", Charsets.UTF_8)
        )
        assertTrue(request.header.hasMetadata)
        assertEquals("d", request.data.toString(Charsets.UTF_8))
        assertEquals("md", request.metadata.toString(Charsets.UTF_8))
        request.buffer.release()
    }

    @Test
    fun requestResponseData() {
        val request = RequestResponseFrame.encode(
            allocator,
            1,
            false,
            Unpooled.copiedBuffer("d", Charsets.UTF_8),
            null
        )
        assertFalse(request.header.hasMetadata)
        assertEquals("d", request.data.toString(Charsets.UTF_8))
        assertEquals(0, request.metadata.readableBytes())
        request.buffer.release()
    }

    @Test
    fun requestResponseMetadata() {
        val request = RequestResponseFrame.encode(
            allocator,
            1,
            false,
            Unpooled.EMPTY_BUFFER,
            Unpooled.copiedBuffer("md", Charsets.UTF_8)
        )
        assertTrue(request.header.hasMetadata)
        assertEquals(0, request.data.readableBytes())
        assertEquals("md", request.metadata.toString(Charsets.UTF_8))
        request.buffer.release()
    }

    @Test
    fun requestStreamDataMetadata() {
        val request = RequestStreamFrame.encode(
            allocator,
            1,
            false,
            Integer.MAX_VALUE + 1L,
            Unpooled.copiedBuffer("d", Charsets.UTF_8),
            Unpooled.copiedBuffer("md", Charsets.UTF_8)
        )
        assertTrue(request.header.hasMetadata)
        assertEquals(Integer.MAX_VALUE, request.initialRequestN)
        assertEquals("md", request.metadata.toString(Charsets.UTF_8))
        assertEquals("d", request.data.toString(Charsets.UTF_8))
        request.buffer.release()
    }

    @Test
    fun requestStreamData() {
        val request = RequestStreamFrame.encode(
            allocator,
            1,
            false,
            42,
            Unpooled.copiedBuffer("d", Charsets.UTF_8),
            null
        )
        assertFalse(request.header.hasMetadata)
        assertEquals(42, request.initialRequestN)
        assertEquals(0, request.metadata.readableBytes())
        assertEquals("d", request.data.toString(Charsets.UTF_8))
        request.buffer.release()
    }

    @Test
    fun requestStreamMetadata() {
        val request = RequestStreamFrame.encode(
            allocator,
            1,
            false,
            42,
            Unpooled.EMPTY_BUFFER,
            Unpooled.copiedBuffer("md", Charsets.UTF_8)
        )
        assertTrue(request.header.hasMetadata)
        assertEquals(42, request.initialRequestN)
        assertEquals(0, request.data.readableBytes())
        assertEquals("md", request.metadata.toString(Charsets.UTF_8))
        request.buffer.release()
    }

    @Test
    fun requestFnfDataAndMetadata() {
        val request = RequestFireAndForgetFrame.encode(
            allocator,
            1,
            false,
            Unpooled.copiedBuffer("d", Charsets.UTF_8),
            Unpooled.copiedBuffer("md", Charsets.UTF_8)
        )
        assertTrue(request.header.hasMetadata)
        assertEquals("d", request.data.toString(Charsets.UTF_8))
        assertEquals("md", request.metadata.toString(Charsets.UTF_8))
        request.buffer.release()
    }

    @Test
    fun requestFnfData() {
        val request = RequestFireAndForgetFrame.encode(
            allocator,
            1,
            false,
            Unpooled.copiedBuffer("d", Charsets.UTF_8),
            null
        )
        assertFalse(request.header.hasMetadata)
        assertEquals("d", request.data.toString(Charsets.UTF_8))
        assertEquals(0, request.metadata.readableBytes())
        request.buffer.release()
    }

    @Test
    fun requestFnfMetadata() {
        val request = RequestFireAndForgetFrame.encode(
            allocator,
            1,
            false,
            Unpooled.EMPTY_BUFFER,
            Unpooled.copiedBuffer("md", Charsets.UTF_8)
        )
        assertTrue(request.header.hasMetadata)
        assertEquals("md", request.metadata.toString(Charsets.UTF_8))
        assertEquals(0, request.data.readableBytes())
        request.buffer.release()
    }
}
