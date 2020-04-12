package io.rsocket.frame

import io.netty.buffer.*
import io.rsocket.frame.shared.*

abstract class Frame<F : Flags>(val type: FrameType) {
    abstract val header: FrameHeader<F>

    protected fun headerBuffer(allocator: ByteBufAllocator, configuration: ByteBuf.() -> Unit): ByteBuf = allocator.buffer {
        writeInt(header.streamId)
        writeShort(header.flags.value or (type.encodedType shl FrameHeader.TYPE_SHIFT))
        configuration()
    }

    abstract fun buffer(allocator: ByteBufAllocator): ByteBuf
}
