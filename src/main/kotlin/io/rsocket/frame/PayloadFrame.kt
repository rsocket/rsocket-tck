package io.rsocket.frame

import io.netty.buffer.*

object PayloadFrame {

    fun encode(
        allocator: ByteBufAllocator,
        streamId: Int,
        fragmentFollows: Boolean,
        complete: Boolean,
        next: Boolean,
        data: ByteBuf,
        metadata: ByteBuf? = null
    ): RequestFrame = RequestFrame.encode(
        allocator = allocator,
        frameType = FrameType.PAYLOAD,
        streamId = streamId,
        fragmentFollows = fragmentFollows,
        complete = complete,
        next = next,
        requestN = 0,
        data = data,
        metadata = metadata
    )

    fun encodeNextComplete(
        allocator: ByteBufAllocator,
        streamId: Int,
        data: ByteBuf,
        metadata: ByteBuf? = null
    ): RequestFrame = RequestFrame.encode(
        allocator = allocator,
        frameType = FrameType.PAYLOAD,
        streamId = streamId,
        fragmentFollows = false,
        complete = true,
        next = true,
        requestN = 0,
        data = data,
        metadata = metadata
    )

    fun encodeNext(
        allocator: ByteBufAllocator,
        streamId: Int,
        data: ByteBuf,
        metadata: ByteBuf? = null
    ): RequestFrame = RequestFrame.encode(
        allocator = allocator,
        frameType = FrameType.PAYLOAD,
        streamId = streamId,
        fragmentFollows = false,
        complete = false,
        next = true,
        requestN = 0,
        data = data,
        metadata = metadata
    )

    fun encodeComplete(
        allocator: ByteBufAllocator,
        streamId: Int
    ): RequestFrame = RequestFrame.encode(
        allocator = allocator,
        frameType = FrameType.PAYLOAD,
        streamId = streamId,
        fragmentFollows = false,
        complete = true,
        next = false,
        requestN = 0,
        data = null,
        metadata = null
    )

}
