package io.rsocket.frame

import io.netty.buffer.*

object RequestFireAndForgetFrame {

    fun encode(
        allocator: ByteBufAllocator,
        streamId: Int,
        fragmentFollows: Boolean,
        metadata: ByteBuf?,
        data: ByteBuf
    ): RequestFrame = RequestFrame.encode(
        allocator = allocator,
        frameType = FrameType.REQUEST_FNF,
        streamId = streamId,
        fragmentFollows = fragmentFollows,
        data = data,
        metadata = metadata
    )

}