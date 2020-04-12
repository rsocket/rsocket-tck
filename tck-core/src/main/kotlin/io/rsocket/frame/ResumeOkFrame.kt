package io.rsocket.frame

import io.netty.buffer.*
import io.rsocket.frame.shared.*

data class ResumeOkFrame(
    override val header: FrameHeader<UntypedFlags>,
    val lastReceivedClientPosition: Long
) : Frame<UntypedFlags>(FrameType.RESUME_OK) {

    override fun buffer(allocator: ByteBufAllocator): ByteBuf = headerBuffer(allocator) {
        writeLong(lastReceivedClientPosition)
    }
}

fun RawFrame.asResumeOk(): ResumeOkFrame = typed(FrameType.RESUME_OK) {
    val lastReceivedClientPosition = readLong()

    ResumeOkFrame(
        header = header,
        lastReceivedClientPosition = lastReceivedClientPosition
    )
}
