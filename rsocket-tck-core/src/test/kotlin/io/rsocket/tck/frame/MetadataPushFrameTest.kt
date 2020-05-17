package io.rsocket.tck.frame

import io.netty.buffer.*
import io.rsocket.tck.expect.frame.*
import io.rsocket.tck.frame.shared.*
import org.junit.jupiter.api.*
import strikt.api.*
import strikt.assertions.*

class MetadataPushFrameTest {
    @Test
    fun encodeMetadata() {
        val metadataPush = MetadataPushFrame(
            FrameHeader(0, MetadataPushFlags(true)),
            TextBuffer("md")
        )
        val newMetadataPush = metadataPush.buffer(allocator).frame().asMetadataPush()

        metadataPush expect newMetadataPush

        expectThat(newMetadataPush) {
            get { metadata.text() }.isEqualTo("md")
        }
    }

    @Test
    fun encodeEmptyMetadata() {
        val metadataPush = MetadataPushFrame(
            FrameHeader(0, MetadataPushFlags(true)),
            Unpooled.EMPTY_BUFFER
        )

        val newMetadataPush = metadataPush.buffer(allocator).frame().asMetadataPush()

        metadataPush expect newMetadataPush

        expectThat(newMetadataPush) {
            get { metadata.readableBytes() }.isEqualTo(0)
        }
    }

}
