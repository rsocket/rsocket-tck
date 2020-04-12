package io.rsocket.frame

import io.netty.buffer.*

object RequestChannelFrame {

    fun encode(
        allocator: ByteBufAllocator,
        streamId: Int,
        fragmentFollows: Boolean,
        complete: Boolean,
        requestN: Long,
        data: ByteBuf,
        metadata: ByteBuf? = null
    ): RequestFrameWithInitial = RequestFrame.encode(
        allocator = allocator,
        frameType = FrameType.REQUEST_CHANNEL,
        streamId = streamId,
        fragmentFollows = fragmentFollows,
        complete = complete,
        next = false,
        requestN = if (requestN > Int.MAX_VALUE) Int.MAX_VALUE else requestN.toInt(),
        data = data,
        metadata = metadata
    ).withInitial

}