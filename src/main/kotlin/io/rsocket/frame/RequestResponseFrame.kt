package io.rsocket.tck.frame

import io.netty.buffer.*

object RequestResponseFrame {

    fun encode(
        allocator: ByteBufAllocator,
        streamId: Int,
        fragmentFollows: Boolean,
        data: ByteBuf,
        metadata: ByteBuf? = null
    ): RequestFrame = RequestFrame.encode(
        allocator = allocator,
        frameType = FrameType.REQUEST_RESPONSE,
        streamId = streamId,
        fragmentFollows = fragmentFollows,
        data = data,
        metadata = metadata
    )

}