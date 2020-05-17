package io.rsocket.tck.frame

import io.netty.buffer.*

inline class ExtensionFrame(val buffer: ByteBuf) {
    val header: FrameHeader get() = FrameHeader(buffer, FrameType.EXT)
    private val reader: DataAndMetadataReader get() = DataAndMetadataReader(buffer)

    val extendedType: Int
        get() {
            header //TODO it check frame type
            return buffer.preview {
                skipBytes(FrameHeader.SIZE)
                readInt()
            }
        }

    val data: ByteBuf
        get() {
            val hasMetadata = header.hasMetadata
            return buffer.preview {
                skipBytes(FrameHeader.SIZE + Size.EXTENDED_TYPE)
                reader.data(hasMetadata)
            }
        }

    val metadata: ByteBuf
        get() {
            val hasMetadata = header.hasMetadata
            return buffer.preview {
                skipBytes(FrameHeader.SIZE + Size.EXTENDED_TYPE)
                reader.metadata(hasMetadata)
            }
        }

    private object Size {
        const val EXTENDED_TYPE: Int = Int.SIZE_BYTES
    }

    companion object {

        fun encode(
            allocator: ByteBufAllocator,
            streamId: Int,
            extendedType: Int,
            data: ByteBuf? = null,
            metadata: ByteBuf? = null
        ): ExtensionFrame {
            var flags = FrameHeader.Flags.I

            if (metadata != null) flags = flags or FrameHeader.Flags.M

            val header = FrameHeader.encode(allocator, FrameType.EXT, flags, streamId) {
                writeInt(extendedType)
            }.buffer

            return ExtensionFrame(
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