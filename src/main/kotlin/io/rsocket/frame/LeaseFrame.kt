package io.rsocket.frame

import io.netty.buffer.*

inline class LeaseFrame(val buffer: ByteBuf) {
    val header: FrameHeader get() = FrameHeader(buffer, FrameType.LEASE)

    val ttl: Int
        get() {
            header //TODO
            return buffer.preview {
                skipBytes(FrameHeader.SIZE)
                readInt()
            }
        }

    val numRequests: Int
        get() {
            header //TODO
            return buffer.preview {
                skipBytes(FrameHeader.SIZE + Size.TTL)
                readInt()
            }
        }

    val metadata: ByteBuf
        get() = when (header.hasMetadata) {
            false -> Unpooled.EMPTY_BUFFER
            true  -> buffer.preview {
                skipBytes(FrameHeader.SIZE + Size.TTL + Size.NUM_REQUESTS)
                slice()
            }
        }

    private object Size {
        const val TTL: Int = Int.SIZE_BYTES
        const val NUM_REQUESTS: Int = Int.SIZE_BYTES
    }

    companion object {

        fun encode(allocator: ByteBufAllocator, ttl: Int, numRequests: Int, metadata: ByteBuf? = null): LeaseFrame {
            var flags = 0
            if (metadata != null) flags = flags or FrameHeader.Flags.M
            val header = FrameHeader.encode(allocator, FrameType.LEASE, flags) {
                writeInt(ttl)
                writeInt(numRequests)
            }.buffer
            return LeaseFrame(
                when (metadata) {
                    null -> header
                    else -> DataAndMetadata.encodeOnlyMetadata(allocator, header, metadata).buffer
                }
            )
        }

    }
}
