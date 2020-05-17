package io.rsocket.tck.frame

import io.netty.buffer.*

inline class KeepAliveFrame(val buffer: ByteBuf) {
    private val header: FrameHeader get() = FrameHeader(buffer, FrameType.KEEPALIVE)

    val respondFlag: Boolean get() = header.flags.checkFlag(Flags.R)

    val lastPosition: Long
        get() {
            header //TODO
            return buffer.preview {
                skipBytes(FrameHeader.SIZE)
                readLong()
            }
        }

    val data: ByteBuf
        get() {
            header //TODO
            return buffer.preview {
                skipBytes(FrameHeader.SIZE + Size.LAST_POSITION)
                slice()
            }
        }

    object Flags {

        /**
         * (R)espond: Set by the sender of the KEEPALIVE, to which the responder MUST reply with a
         * KEEPALIVE without the R flag set
         */
        const val R = 128
    }

    private object Size {
        const val LAST_POSITION: Int = Long.SIZE_BYTES
    }

    companion object {
        fun encode(allocator: ByteBufAllocator, respondFlag: Boolean, lastPosition: Long, data: ByteBuf): KeepAliveFrame {
            val flags = if (respondFlag) Flags.R else 0
            val header = FrameHeader.encode(allocator, FrameType.KEEPALIVE, flags, 0) {
                writeLong(if (lastPosition > 0) lastPosition else 0)
            }.buffer
            return KeepAliveFrame(DataAndMetadata.encodeOnlyData(allocator, header, data).buffer)
        }
    }
}