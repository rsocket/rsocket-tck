package io.rsocket.frame

import io.netty.buffer.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

class PayloadFlyweightTest {

    @Test
    fun nextCompleteDataMetadata() {
        val nextComplete = PayloadFrame.encodeNextComplete(allocator, 1, data, metadata)
        assertEquals(dataString, nextComplete.data.text())
        assertEquals(metadataString, nextComplete.metadata.text())
    }

    @Test
    fun nextCompleteData() {
        val nextComplete = PayloadFrame.encodeNextComplete(allocator, 1, data)
        assertEquals(dataString, nextComplete.data.text())
        assertEquals(0, nextComplete.metadata.readableBytes())
    }

    @Test
    fun nextCompleteMetaData() {
        val nextComplete = PayloadFrame.encodeNextComplete(allocator, 1, Unpooled.EMPTY_BUFFER, metadata)
        assertEquals(0, nextComplete.data.readableBytes())
        assertEquals(metadataString, nextComplete.metadata.text())
    }

    @Test
    fun nextDataMetadata() {
        val next = PayloadFrame.encodeNext(allocator, 1, data, metadata)
        assertEquals(dataString, next.data.text())
        assertEquals(metadataString, next.metadata.text())
    }

    @Test
    fun nextData() {
        val next = PayloadFrame.encodeNext(allocator, 1, data)
        assertEquals(dataString, next.data.text())
        assertEquals(0, next.metadata.readableBytes())
    }

    private companion object {
        const val dataString: String = "d"
        val data: ByteBuf get() = TextBuffer(dataString)

        const val metadataString: String = "md"
        val metadata: ByteBuf get() = TextBuffer(metadataString)

    }
}
