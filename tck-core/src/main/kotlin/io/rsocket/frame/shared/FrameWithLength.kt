package io.rsocket.frame.shared

import io.netty.buffer.*

data class BufferWithLength(
    val buffer: ByteBuf,
    val length: Int = buffer.readableBytes()
)

data class FrameWithLength(
    val frame: RawFrame,
    val length: Int = FrameHeader.SIZE + frame.buffer.readableBytes()
)

fun ByteBuf.withLength(length: Int, allocator: ByteBufAllocator): ByteBuf {
    val lengthHeader = allocator.buffer {
        writeLength(length)
    }
    return allocator.compose(lengthHeader, this)
}

fun ByteBuf.withLength(): BufferWithLength = preview {
    val length = readLength()
    val buffer = slice()
    BufferWithLength(
        buffer = buffer,
        length = length
    )
}

fun BufferWithLength.frame(): FrameWithLength {
    val frame = buffer.frame()
    return FrameWithLength(
        frame = frame,
        length = length
    )
}