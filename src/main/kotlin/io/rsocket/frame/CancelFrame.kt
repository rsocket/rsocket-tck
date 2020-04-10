package io.rsocket.frame

import io.netty.buffer.*

object CancelFrame {
    fun encode(allocator: ByteBufAllocator, streamId: Int): FrameHeader = FrameHeader.encode(allocator, FrameType.CANCEL, 0, streamId)
}
