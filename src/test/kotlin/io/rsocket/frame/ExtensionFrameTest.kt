package io.rsocket.tck.frame

import io.netty.buffer.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

class ExtensionFrameTest {
    @Test
    fun extensionDataMetadata() {
        val metadata = TextBuffer("md")
        val data = TextBuffer("d")
        val extendedType = 1
        val extension = ExtensionFrame.encode(allocator, 1, extendedType, data, metadata)
        assertTrue(extension.header.hasMetadata)
        assertEquals(extendedType, extension.extendedType)
        assertEquals(metadata, extension.metadata)
        assertEquals(data, extension.data)
    }

    @Test
    fun extensionData() {
        val data = TextBuffer("d")
        val extendedType = 1
        val extension = ExtensionFrame.encode(allocator, 1, extendedType, data, null)
        assertFalse(extension.header.hasMetadata)
        assertEquals(extendedType, extension.extendedType)
        assertEquals(0, extension.metadata.readableBytes())
        assertEquals(data, extension.data)
    }

    @Test
    fun extensionMetadata() {
        val metadata = TextBuffer("md")
        val extendedType = 1
        val extension = ExtensionFrame.encode(allocator, 1, extendedType, Unpooled.EMPTY_BUFFER, metadata)
        assertTrue(extension.header.hasMetadata)
        assertEquals(extendedType, extension.extendedType)
        assertEquals(0, extension.data.readableBytes())
        assertEquals(metadata, extension.metadata)
    }

}
