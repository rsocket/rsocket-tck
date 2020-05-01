package io.rsocket.frame

import io.netty.buffer.*
import io.rsocket.frame.shared.*

data class RequestNFrame(
    override val header: FrameHeader<UntypedFlags>,
    val requestN: Int
) : Frame<UntypedFlags>(FrameType.REQUEST_N) {
    override fun buffer(allocator: ByteBufAllocator): ByteBuf = headerBuffer(allocator) {
        writeInt(requestN)
    }
}

fun RawFrame.asRequestN(): RequestNFrame = typed(FrameType.REQUEST_N) {
    val requestN = readInt()
    RequestNFrame(
        header = header,
        requestN = requestN
    )
}
