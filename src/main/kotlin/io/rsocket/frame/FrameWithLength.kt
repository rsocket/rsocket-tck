package io.rsocket.frame

import io.netty.buffer.*

inline class FrameWithLength(val buffer: ByteBuf) {

    val length: Int get() = buffer.preview { readLength() }

    val frame: ByteBuf
        get() = buffer.preview {
            skipBytes(Size.LENGTH)
            slice()
        }

    private object Size {
        const val LENGTH: Int = 3
    }

    companion object {
        const val MASK: Int = 0xFFFFFF

        fun encode(allocator: ByteBufAllocator, frame: ByteBuf, length: Int): FrameWithLength =
            FrameWithLength(allocator.compose(allocator.buffer { writeLength(length) }, frame))
    }
}

internal fun ByteBuf.readLength(): Int {
    val b = readByte().toInt() and 0xFF shl 16
    val b1 = readByte().toInt() and 0xFF shl 8
    val b2 = readByte().toInt() and 0xFF
    return b or b1 or b2
}

internal fun ByteBuf.writeLength(length: Int): ByteBuf {
    require(length and FrameWithLength.MASK.inv() == 0) { "Length is larger than 24 bits" }
    // Write each byte separately in reverse order, this mean we can write 1 << 23 without overflowing.
    writeByte(length shr 16)
    writeByte(length shr 8)
    writeByte(length)
    return this
}
