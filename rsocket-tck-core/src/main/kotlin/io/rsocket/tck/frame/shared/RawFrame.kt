package io.rsocket.tck.frame.shared

import io.netty.buffer.*
import io.rsocket.frame.*
import strikt.api.*
import strikt.assertions.*

data class RawFrame(
    val header: FrameHeader<UntypedFlags>,
    val type: FrameType,
    val buffer: ByteBuf
)

fun ByteBuf.frame(): RawFrame = preview {
    val streamId = readInt()
    val typeAndFlags = readShort().toInt() and 0xFFFF
    val flags = typeAndFlags and FrameHeader.FLAGS_MASK
    val frameType = FrameType.fromEncodedType(typeAndFlags shr FrameHeader.TYPE_SHIFT)
    RawFrame(
        header = FrameHeader(
            streamId = streamId,
            flags = UntypedFlags(flags)
        ),
        type = frameType,
        buffer = slice()
    )
}

inline fun <T> RawFrame.typed(type: FrameType, block: ByteBuf.() -> T): T {
    expectThat(this@typed.type)
        .describedAs("frame type")
        .isEqualTo(type)
    return buffer.preview(block)
}
