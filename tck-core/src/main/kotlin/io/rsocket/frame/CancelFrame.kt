package io.rsocket.frame

import io.netty.buffer.*
import io.rsocket.frame.shared.*

data class CancelFrame(
    override val header: FrameHeader<UntypedFlags>
) : Frame<UntypedFlags>(FrameType.CANCEL) {
    override fun buffer(allocator: ByteBufAllocator): ByteBuf = headerBuffer(allocator)
}

fun RawFrame.asCancel(): CancelFrame = typed(FrameType.CANCEL) {
    CancelFrame(
        header = header
    )
}
