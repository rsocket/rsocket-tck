package io.rsocket.frame

import io.netty.buffer.*

object RequestStreamFrame {

    fun encode(
        allocator: ByteBufAllocator,
        streamId: Int,
        fragmentFollows: Boolean,
        requestN: Long,
        data: ByteBuf,
        metadata: ByteBuf? = null
    ): RequestFrame = encode(
        allocator = allocator,
        streamId = streamId,
        fragmentFollows = fragmentFollows,
        requestN = if (requestN > Int.MAX_VALUE) Int.MAX_VALUE else requestN.toInt(),
        data = data,
        metadata = metadata
    )

    fun encode(
        allocator: ByteBufAllocator,
        streamId: Int,
        fragmentFollows: Boolean,
        requestN: Int,
        data: ByteBuf,
        metadata: ByteBuf? = null
    ): RequestFrame = RequestFrame.encode(
        allocator,
        frameType = FrameType.REQUEST_STREAM,
        streamId = streamId,
        fragmentFollows = fragmentFollows,
        complete = false,
        next = false,
        requestN = requestN,
        data = data,
        metadata = metadata
    )

}
