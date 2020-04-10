package io.rsocket.frame

import io.netty.buffer.*

inline class DataAndMetadata(val buffer: ByteBuf) {
    private val reader: DataAndMetadataReader get() = DataAndMetadataReader(buffer)

    fun metadata(hasMetadata: Boolean): ByteBuf = buffer.preview {
        skipBytes(FrameHeader.SIZE)
        reader.metadata(hasMetadata)
    }

    fun data(hasMetadata: Boolean): ByteBuf = buffer.preview {
        skipBytes(FrameHeader.SIZE)
        reader.data(hasMetadata)
    }

    companion object {
        fun encode(allocator: ByteBufAllocator, header: ByteBuf, metadata: ByteBuf, data: ByteBuf): DataAndMetadata {
            header.writeLength(metadata.readableBytes())
            return DataAndMetadata(allocator.compose(header, metadata, data))
        }

        fun encodeOnlyData(allocator: ByteBufAllocator, header: ByteBuf, data: ByteBuf): DataAndMetadata =
            DataAndMetadata(allocator.compose(header, data))

        fun encodeOnlyMetadata(allocator: ByteBufAllocator, header: ByteBuf, metadata: ByteBuf): DataAndMetadata =
            DataAndMetadata(allocator.compose(header, metadata))
    }
}

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
inline class DataAndMetadataReader(val buffer: ByteBuf) {

    fun metadata(hasMetadata: Boolean): ByteBuf = when (hasMetadata) {
        true  -> buffer.readSlice(buffer.readLength())
        false -> Unpooled.EMPTY_BUFFER
    }

    fun data(hasMetadata: Boolean): ByteBuf {
        if (hasMetadata) buffer.skipBytes(buffer.readLength())
        return when (buffer.readableBytes() > 0) {
            true  -> buffer.readSlice(buffer.readableBytes())
            false -> Unpooled.EMPTY_BUFFER
        }
    }
}
