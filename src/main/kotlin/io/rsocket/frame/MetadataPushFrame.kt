package io.rsocket.tck.frame

import io.netty.buffer.*

inline class MetadataPushFrame(val buffer: ByteBuf) {
    val metadata: ByteBuf
        get() = buffer.preview {
            skipBytes(FrameHeader.SIZE)
            slice()
        }

    companion object {
        fun encode(allocator: ByteBufAllocator, metadata: ByteBuf): MetadataPushFrame {
            val header = FrameHeader.encode(allocator, FrameType.METADATA_PUSH, FrameHeader.Flags.M).buffer
            return MetadataPushFrame(allocator.compose(header, metadata))
        }
    }
}
