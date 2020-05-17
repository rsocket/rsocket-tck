package io.rsocket.tck.frame

import io.netty.buffer.*
import io.rsocket.frame.shared.*

data class ResumeFrame(
    override val header: FrameHeader<UntypedFlags>,
    val version: Version,
    val resumeToken: ResumeToken,
    val lastReceivedServerPosition: Long,
    val firstAvailableClientPosition: Long
) : Frame<UntypedFlags>(FrameType.RESUME) {

    override fun buffer(allocator: ByteBufAllocator): ByteBuf = headerBuffer(allocator) {
        writeInt(version.value)
        writeShort(resumeToken.length.toInt())
        resumeToken.token.preview { this@headerBuffer.writeBytes(this) }
        writeLong(lastReceivedServerPosition)
        writeLong(firstAvailableClientPosition)
    }
}

fun RawFrame.asResume(): ResumeFrame = typed(FrameType.RESUME) {
    val version = readVersion()
    val resumeToken = readResumeToken()
    val lastReceivedServerPosition = readLong()
    val firstAvailableClientPosition = readLong()

    ResumeFrame(
        header = header,
        version = version,
        resumeToken = resumeToken,
        lastReceivedServerPosition = lastReceivedServerPosition,
        firstAvailableClientPosition = firstAvailableClientPosition
    )
}
