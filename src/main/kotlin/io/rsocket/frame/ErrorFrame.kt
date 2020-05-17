package io.rsocket.tck.frame

import io.netty.buffer.*

inline class ErrorFrame(val buffer: ByteBuf) {

    val code: Int
        get() = buffer.preview {
            skipBytes(FrameHeader.SIZE)
            readInt()
        }

    val data: ByteBuf
        get() = buffer.preview {
            skipBytes(FrameHeader.SIZE + Size.CODE)
            slice()
        }

    val dataUtf8: String get() = data.toString(Charsets.UTF_8)

    private object Size {
        const val CODE: Int = Int.SIZE_BYTES
    }

    companion object {
        fun encode(allocator: ByteBufAllocator, streamId: Int, errorCode: Int, data: ByteBuf? = null): ErrorFrame {
            val header = FrameHeader.encode(allocator, FrameType.ERROR, 0, streamId) {
                writeInt(errorCode)
            }.buffer
            val errorData = data ?: ByteBufUtil.writeUtf8(allocator, "")
            return ErrorFrame(allocator.compose(header, errorData))
        }
    }
}
