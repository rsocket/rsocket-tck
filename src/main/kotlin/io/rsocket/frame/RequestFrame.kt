package io.rsocket.frame

import io.netty.buffer.*

inline class RequestFrame(val buffer: ByteBuf) {
    val header: FrameHeader get() = FrameHeader(buffer)
    val withInitial: RequestFrameWithInitial get() = RequestFrameWithInitial(buffer)
    private val reader: DataAndMetadataReader get() = DataAndMetadataReader(buffer)

    val data: ByteBuf
        get() {
            val hasMetadata = header.hasMetadata
            return buffer.preview {
                skipBytes(FrameHeader.SIZE)
                reader.data(hasMetadata)
            }
        }

    val metadata: ByteBuf
        get() {
            val hasMetadata = header.hasMetadata
            return buffer.preview {
                skipBytes(FrameHeader.SIZE)
                reader.metadata(hasMetadata)
            }
        }

    companion object {

        fun encode(
            allocator: ByteBufAllocator,
            frameType: FrameType,
            streamId: Int,
            fragmentFollows: Boolean,
            data: ByteBuf,
            metadata: ByteBuf? = null
        ): RequestFrame = encode(allocator, frameType, streamId, fragmentFollows, false, false, 0, data, metadata)

        fun encode(
            allocator: ByteBufAllocator,
            frameType: FrameType,
            streamId: Int,
            fragmentFollows: Boolean,
            complete: Boolean = false,
            next: Boolean = false,
            requestN: Int = 0,
            data: ByteBuf? = null,
            metadata: ByteBuf? = null
        ): RequestFrame {
            var flags = 0

            if (metadata != null) flags = flags or FrameHeader.Flags.M
            if (fragmentFollows) flags = flags or FrameHeader.Flags.F
            if (complete) flags = flags or FrameHeader.Flags.C
            if (next) flags = flags or FrameHeader.Flags.N

            val header = FrameHeader.encode(allocator, frameType, flags, streamId) {
                if (requestN > 0) writeInt(requestN)
            }.buffer

            return RequestFrame(
                when (data) {
                    null -> header
                    else -> when (metadata) {
                        null -> DataAndMetadata.encodeOnlyData(allocator, header, data)
                        else -> DataAndMetadata.encode(allocator, header, data, metadata)
                    }.buffer
                }
            )
        }

    }
}

inline class RequestFrameWithInitial(val buffer: ByteBuf) {
    val header: FrameHeader get() = FrameHeader(buffer)
    private val reader: DataAndMetadataReader get() = DataAndMetadataReader(buffer)

    val initialRequestN: Int
        get() = buffer.preview {
            skipBytes(FrameHeader.SIZE)
            readInt()
        }

    val data: ByteBuf
        get() {
            val hasMetadata = header.hasMetadata
            return buffer.preview {
                skipBytes(FrameHeader.SIZE + Size.INITIAL_REQUEST)
                reader.data(hasMetadata)
            }
        }

    val metadata: ByteBuf
        get() {
            val hasMetadata = header.hasMetadata
            return buffer.preview {
                skipBytes(FrameHeader.SIZE + Size.INITIAL_REQUEST)
                reader.metadata(hasMetadata)
            }
        }

    private object Size {
        const val INITIAL_REQUEST: Int = Int.SIZE_BYTES
    }

}
