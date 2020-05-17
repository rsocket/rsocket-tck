package io.rsocket.tck.frame

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

class SetupFrameTest {
    @Test
    fun testEncoding() {
        val metadata = bufferOf(1, 2, 3, 4)
        val data = bufferOf(5, 4, 3)
        val setupFrame = SetupFrame.encode(
            allocator = allocator,
            lease = false,
            keepaliveInterval = 5,
            maxLifetime = 500,
            metadataMimeType = "metadata_type",
            dataMimeType = "data_type",
            data = data,
            metadata = metadata
        )
        assertEquals(FrameType.SETUP, setupFrame.header.frameType)
        assertFalse(setupFrame.resumeEnabled)
        assertNull(setupFrame.resumeToken)
        assertEquals("metadata_type", setupFrame.metadataMimeType)
        assertEquals("data_type", setupFrame.dataMimeType)
        assertEquals(metadata, setupFrame.metadata)
        assertEquals(data, setupFrame.data)
        assertEquals(Version.CURRENT, setupFrame.version)
        assertEquals(true, setupFrame.isSupportedVersion)
        assertEquals(5, setupFrame.keepAliveInterval)
        assertEquals(500, setupFrame.keepAliveMaxLifetime)
    }

    @Test
    fun testEncodingNoResume() {
        val setupFrame = SetupFrame.encode(
            allocator = allocator,
            lease = false,
            keepaliveInterval = 5,
            maxLifetime = 500,
            metadataMimeType = "metadata_type",
            dataMimeType = "data_type",
            data = bufferOf(5, 4, 3),
            metadata = bufferOf(1, 2, 3, 4)
        )
        assertEquals(FrameType.SETUP, setupFrame.header.frameType)
        assertFalse(setupFrame.resumeEnabled)
        assertNull(setupFrame.resumeToken)
    }

    @Test
    fun testEncodingResume() {
        val token = bufferOf(*ByteArray(65000) { 1 })
        val setupFrame = SetupFrame.encode(
            allocator = allocator,
            lease = true,
            keepaliveInterval = 5,
            maxLifetime = 500,
            metadataMimeType = "metadata_type",
            dataMimeType = "data_type",
            data = bufferOf(8, 9, 10),
            metadata = bufferOf(1, 2, 3, 4, 5, 6, 7),
            resumeToken = token
        )
        assertEquals(FrameType.SETUP, setupFrame.header.frameType)
        assertTrue(setupFrame.honorLease)
        assertTrue(setupFrame.resumeEnabled)
        assertEquals(token, setupFrame.resumeToken)
    }
}
