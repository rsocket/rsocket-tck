package io.rsocket.frame.shared

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
    val frameType = when (val result = FrameType.fromEncodedType(typeAndFlags shr FrameHeader.TYPE_SHIFT)) {
        FrameType.PAYLOAD -> {
            val complete = flags check CommonFlag.Complete
            val next = flags check CommonFlag.Next
            when {
                next && complete -> FrameType.NEXT_COMPLETE
                complete         -> FrameType.COMPLETE
                next             -> FrameType.NEXT
                else             -> throw IllegalArgumentException("Payload must set either or both of NEXT and COMPLETE.")
            }
        }
        else              -> result
    }
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
