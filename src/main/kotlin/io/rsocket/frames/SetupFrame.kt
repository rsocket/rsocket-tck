package io.rsocket.frames

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import java.nio.charset.StandardCharsets

class SetupFrame(private val allocator: ByteBufAllocator, val data: String, val majorVersion: Int) {
    private val FRAME_TYPE_BITS: Int = 6
    private val FRAME_TYPE_SHIFT: Int = 16 - FRAME_TYPE_BITS

    constructor(data: String, majorVersion: Int) : this(ByteBufAllocator.DEFAULT, data, majorVersion)

    fun asRawBuffer(): ByteBuf {
        val buffer = allocator.buffer()
        buffer.writeInt(0)
            .writeShort(0x01 shl FRAME_TYPE_SHIFT or 0)
            .writeShort(majorVersion)
            .writeShort(0)
            .writeInt(0)
            .writeInt(0)

        buffer.writeInt(data.length)
            .writeCharSequence(data, StandardCharsets.UTF_8)

        return buffer
    }
}
