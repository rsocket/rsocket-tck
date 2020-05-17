package io.rsocket.tck.frame

import io.netty.buffer.*
import io.rsocket.tck.frame.shared.*

class KeepAliveFrame(
    override val header: FrameHeader<KeepAliveFlags>,
    val lastReceivedPosition: Long,
    val data: ByteBuf
) : Frame<KeepAliveFlags>(FrameType.KEEPALIVE) {
    override fun buffer(allocator: ByteBufAllocator): ByteBuf {
        val header = headerBuffer(allocator) {
            writeLong(lastReceivedPosition)
        }
        return allocator.compose(header, data)
    }
}

fun RawFrame.asKeepAlive(): KeepAliveFrame = typed(FrameType.KEEPALIVE) {
    val flags = KeepAliveFlags(respond = header.flags.value check KeepAliveRespondFlag)
    val lastReceivedPosition = readLong()
    val data = slice()
    KeepAliveFrame(
        header = header.withFlags(flags),
        lastReceivedPosition = lastReceivedPosition,
        data = data
    )
}

object KeepAliveRespondFlag : Flag {
    override val position: Int = 2
}

data class KeepAliveFlags(
    val respond: Boolean
) : TypedFlags({
    KeepAliveRespondFlag setIf respond
})