package io.rsocket.tck.frame

import io.netty.buffer.*

inline class RequestNFrame(val buffer: ByteBuf) {
    private val header: FrameHeader get() = FrameHeader(buffer, FrameType.REQUEST_N)
    val requestN: Int
        get() {
            header //TODO
            return buffer.preview {
                skipBytes(FrameHeader.SIZE)
                readInt()
            }
        }

    companion object {
        fun encode(allocator: ByteBufAllocator, streamId: Int, requestN: Int): RequestNFrame {
            require(requestN >= 1) { "request n is less than 1" }
            return RequestNFrame(FrameHeader.encode(allocator, FrameType.REQUEST_N, 0, streamId) {
                writeInt(requestN)
            }.buffer)
        }
    }
}
