package io.rsocket.frame

import io.netty.buffer.*

inline class ResumeOkFrame(val buffer: ByteBuf) {
    private val header get() = FrameHeader(buffer, FrameType.RESUME_OK)
    val lastReceivedClientPos: Long
        get() {
            header //TODO
            return buffer.preview {
                skipBytes(FrameHeader.SIZE)
                readLong()
            }
        }

    companion object {

        fun encode(allocator: ByteBufAllocator, lastReceivedClientPos: Long): ResumeOkFrame =
            ResumeOkFrame(FrameHeader.encode(allocator, FrameType.RESUME_OK, 0) {
                writeLong(lastReceivedClientPos)
            }.buffer)
    }
}