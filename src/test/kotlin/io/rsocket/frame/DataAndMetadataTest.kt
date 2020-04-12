package io.rsocket.frame

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

class DataAndMetadataTest {

    @Test
    fun testEncodeData() {
        val header = FrameHeader.encode(allocator, FrameType.PAYLOAD, 0, 1).buffer
        val dataString = "_I'm data_"
        val data = TextBuffer(dataString)
        val dam = DataAndMetadata.encodeOnlyData(allocator, header, data)
        val decodedData = dam.data(false)
        val decodedDataString = decodedData.text()
        assertEquals(dataString, decodedDataString)
    }

    @Test
    fun testEncodeMetadata() {
        val header = FrameHeader.encode(allocator, FrameType.PAYLOAD, 0, 1).buffer
        val metadataString = "_I'm metadata_"
        val metadata = TextBuffer(metadataString)
        val dam = DataAndMetadata.encodeOnlyMetadata(allocator, header, metadata)
        val decodedMetadata = dam.metadata(false)
        assertEquals(0, decodedMetadata.readableBytes())
    }

    @Test
    fun testEncodeDataAndMetadata() {
        val dataString = "_I'm data_"
        val data = TextBuffer(dataString)
        val metadataString = "_I'm metadata_"
        val metadata = TextBuffer(metadataString)
        val header = FrameHeader.encode(allocator, FrameType.REQUEST_RESPONSE, 0, 1).buffer
        val dam = DataAndMetadata.encode(allocator, header, data, metadata)
        val decodedData = dam.data(true)
        val decodedMetadata = dam.metadata(true)
        assertEquals(FrameType.REQUEST_RESPONSE, FrameHeader(dam.buffer).frameType)
        assertEquals("_I'm data_", decodedData.text())
        assertEquals("_I'm metadata_", decodedMetadata.text())
    }
}
