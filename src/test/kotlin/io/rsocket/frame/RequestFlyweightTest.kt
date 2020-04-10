package io.rsocket.frame

class RequestFlyweightTest {
    //    @Test
    //    fun testEncoding() {
    //        var frame = RequestStreamFrameF(
    //            ByteBufAllocator.DEFAULT,
    //            1,
    //            false,
    //            1,
    //            Unpooled.copiedBuffer("md", Charsets.UTF_8),
    //            Unpooled.copiedBuffer("d", Charsets.UTF_8)
    //        ).input.bb
    //        frame = FrameLengthF(ByteBufAllocator.DEFAULT, frame.readableBytes(), frame).input.bb
    //        assertEquals("000010000000011900000000010000026d6464", ByteBufUtil.hexDump(frame))
    //        frame.release()
    //    }
    //
    //    @Test
    //    fun testEncodingWithEmptyMetadata() {
    //        var frame = RequestStreamFrameF(
    //            ByteBufAllocator.DEFAULT,
    //            1,
    //            false,
    //            1,
    //            Unpooled.EMPTY_BUFFER,
    //            Unpooled.copiedBuffer("d", Charsets.UTF_8)
    //        ).input.bb
    //        frame = FrameLengthF(ByteBufAllocator.DEFAULT, frame.readableBytes(), frame).input.bb
    //        assertEquals("00000e0000000119000000000100000064", ByteBufUtil.hexDump(frame))
    //        frame.release()
    //    }
    //
    //    @Test
    //    fun testEncodingWithNullMetadata() {
    //        var frame = RequestStreamFrameF(
    //            ByteBufAllocator.DEFAULT,
    //            1,
    //            false,
    //            1,
    //            null,
    //            Unpooled.copiedBuffer("d", Charsets.UTF_8)
    //        ).input.bb
    //        frame = FrameLengthF(ByteBufAllocator.DEFAULT, frame.readableBytes(), frame).input.bb
    //        assertEquals("00000b0000000118000000000164", ByteBufUtil.hexDump(frame))
    //        frame.release()
    //    }
    //
    //    @Test
    //    fun requestResponseDataMetadata() {
    //        val request = RequestResponseFrameF(
    //            ByteBufAllocator.DEFAULT,
    //            1,
    //            false,
    //            Unpooled.copiedBuffer("md", Charsets.UTF_8),
    //            Unpooled.copiedBuffer("d", Charsets.UTF_8)
    //        )
    //        val frameHeader = FrameHeaderF(request.input.bb)
    //        val data: String? = request.data.toString(Charsets.UTF_8)
    //        val metadata: String? = request.metadata.toString(Charsets.UTF_8)
    //        assertTrue(frameHeader.hasMetadata)
    //        assertEquals("d", data)
    //        assertEquals("md", metadata)
    //        request.input.bb.release()
    //    }
    //
    //    @Test
    //    fun requestResponseData() {
    //        val request = RequestResponseFrameF(
    //            ByteBufAllocator.DEFAULT,
    //            1,
    //            false,
    //            null,
    //            Unpooled.copiedBuffer("d", Charsets.UTF_8)
    //        )
    //        val frameHeader = FrameHeaderF(request.input.bb)
    //        val data = request.data.toString(Charsets.UTF_8)
    //        val metadata = request.metadata
    //        assertFalse(frameHeader.hasMetadata)
    //        assertEquals("d", data)
    //        assertTrue(metadata.readableBytes() == 0)
    //        request.input.bb.release()
    //    }
    //
    //    @Test
    //    fun requestResponseMetadata() {
    //        val request = RequestResponseFrameF(
    //            ByteBufAllocator.DEFAULT,
    //            1,
    //            false,
    //            Unpooled.copiedBuffer("md", Charsets.UTF_8),
    //            Unpooled.EMPTY_BUFFER
    //        )
    //        val frameHeader = FrameHeaderF(request.input.bb)
    //        val data = request.data
    //        val metadata = request.metadata.toString(Charsets.UTF_8)
    //        assertTrue(frameHeader.hasMetadata)
    //        assertTrue(data.readableBytes() == 0)
    //        assertEquals("md", metadata)
    //        request.input.bb.release()
    //    }
    //
    //    @Test
    //    fun requestStreamDataMetadata() {
    //        val request = RequestStreamFrameF(
    //            ByteBufAllocator.DEFAULT,
    //            1,
    //            false,
    //            Integer.MAX_VALUE + 1L,
    //            Unpooled.copiedBuffer("md", Charsets.UTF_8),
    //            Unpooled.copiedBuffer("d", Charsets.UTF_8)
    //        )
    //        val frameHeader = FrameHeaderF(request.input.bb)
    //        val actualRequest = request.initialRequestN
    //        val data = request.data.toString(Charsets.UTF_8)
    //        val metadata = request.metadata.toString(Charsets.UTF_8)
    //        assertTrue(frameHeader.hasMetadata)
    //        assertEquals(Integer.MAX_VALUE, actualRequest)
    //        assertEquals("md", metadata)
    //        assertEquals("d", data)
    //        request.input.bb.release()
    //    }
    //
    //    @Test
    //    fun requestStreamData() {
    //        val request = RequestStreamFrameF(
    //            ByteBufAllocator.DEFAULT,
    //            1,
    //            false,
    //            42,
    //            null,
    //            Unpooled.copiedBuffer("d", Charsets.UTF_8)
    //        )
    //        val frameHeader = FrameHeaderF(request.input.bb)
    //        val actualRequest = request.initialRequestN
    //        val data = request.data.toString(Charsets.UTF_8)
    //        val metadata = request.metadata
    //        assertFalse(frameHeader.hasMetadata)
    //        assertEquals(42, actualRequest)
    //        assertTrue(metadata.readableBytes() == 0)
    //        assertEquals("d", data)
    //        request.input.bb.release()
    //    }
    //
    //    @Test
    //    fun requestStreamMetadata() {
    //        val request = RequestStreamFrameF(
    //            ByteBufAllocator.DEFAULT,
    //            1,
    //            false,
    //            42,
    //            Unpooled.copiedBuffer("md", Charsets.UTF_8),
    //            Unpooled.EMPTY_BUFFER
    //        )
    //        val frameHeader = FrameHeaderF(request.input.bb)
    //        val actualRequest = request.initialRequestN
    //        val data = request.data
    //        val metadata = request.metadata.toString(Charsets.UTF_8)
    //        assertTrue(frameHeader.hasMetadata)
    //        assertEquals(42, actualRequest)
    //        assertTrue(data.readableBytes() == 0)
    //        assertEquals("md", metadata)
    //        request.input.bb.release()
    //    }
    //
    //    @Test
    //    fun requestFnfDataAndMetadata() {
    //        val request = RequestFireAndForgetFrameF(
    //            ByteBufAllocator.DEFAULT,
    //            1,
    //            false,
    //            Unpooled.copiedBuffer("md", Charsets.UTF_8),
    //            Unpooled.copiedBuffer("d", Charsets.UTF_8)
    //        )
    //        val frameHeader = FrameHeaderF(request.input.bb)
    //        val data = request.data.toString(Charsets.UTF_8)
    //        val metadata = request.metadata.toString(Charsets.UTF_8)
    //        assertTrue(frameHeader.hasMetadata)
    //        assertEquals("d", data)
    //        assertEquals("md", metadata)
    //        request.input.bb.release()
    //    }
    //
    //    @Test
    //    fun requestFnfData() {
    //        val request = RequestFireAndForgetFrameF(
    //            ByteBufAllocator.DEFAULT,
    //            1,
    //            false,
    //            null,
    //            Unpooled.copiedBuffer("d", Charsets.UTF_8)
    //        )
    //        val frameHeader = FrameHeaderF(request.input.bb)
    //        val data = request.data.toString(Charsets.UTF_8)
    //        val metadata = request.metadata
    //        assertFalse(frameHeader.hasMetadata)
    //        assertEquals("d", data)
    //        assertTrue(metadata.readableBytes() == 0)
    //        request.input.bb.release()
    //    }
    //
    //    @Test
    //    fun requestFnfMetadata() {
    //        val request = RequestFireAndForgetFrameF(
    //            ByteBufAllocator.DEFAULT,
    //            1,
    //            false,
    //            Unpooled.copiedBuffer("md", Charsets.UTF_8),
    //            Unpooled.EMPTY_BUFFER
    //        )
    //        val frameHeader = FrameHeaderF(request.input.bb)
    //        val data = request.data
    //        val metadata = request.metadata.toString(Charsets.UTF_8)
    //        assertTrue(frameHeader.hasMetadata)
    //        assertEquals("md", metadata)
    //        assertTrue(data.readableBytes() == 0)
    //        request.input.bb.release()
    //    }
}
