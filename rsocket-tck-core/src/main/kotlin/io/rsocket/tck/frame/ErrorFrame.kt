package io.rsocket.tck.frame

import io.netty.buffer.*
import io.rsocket.tck.frame.shared.*

data class ErrorFrame(
    override val header: FrameHeader<UntypedFlags>,
    val code: ErrorCode,
    val data: ByteBuf
) : Frame<UntypedFlags>(FrameType.ERROR) {
    override fun buffer(allocator: ByteBufAllocator): ByteBuf {
        val header = headerBuffer(allocator) {
            writeInt(code.value)
        }
        return allocator.compose(header, data)
    }
}

fun RawFrame.asError(): ErrorFrame = typed(FrameType.ERROR) {
    val code = ErrorCode(readInt())
    val data = slice()
    ErrorFrame(
        header = header,
        code = code,
        data = data
    )
}
