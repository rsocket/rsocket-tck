package io.rsocket.frame

import io.netty.buffer.*

object RequestStreamFrame {

    fun encode(
        allocator: ByteBufAllocator,
        streamId: Int,
        fragmentFollows: Boolean,
        requestN: Long,
        metadata: ByteBuf?,
        data: ByteBuf
    ): RequestFrame = encode(
        allocator = allocator,
        streamId = streamId,
        fragmentFollows = fragmentFollows,
        requestN = if (requestN > Int.MAX_VALUE) Int.MAX_VALUE else requestN.toInt(),
        metadata = metadata,
        data = data
    )

    fun encode(
        allocator: ByteBufAllocator,
        streamId: Int,
        fragmentFollows: Boolean,
        requestN: Int,
        metadata: ByteBuf?,
        data: ByteBuf
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
