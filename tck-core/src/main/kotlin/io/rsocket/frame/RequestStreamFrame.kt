package io.rsocket.frame

import io.netty.buffer.*
import io.rsocket.frame.shared.*

data class RequestStreamFrame(
    override val header: FrameHeader<RequestStreamFlags>,
    val initialRequestN: Int,
    val payload: Payload
) : Frame<RequestStreamFlags>(FrameType.REQUEST_STREAM) {
    override fun buffer(allocator: ByteBufAllocator): ByteBuf {
        val header = headerBuffer(allocator) {
            writeInt(initialRequestN)
            payload.metadata?.length?.let(this::writeLength)
        }
        return allocator.compose(header, payload)
    }
}

fun RawFrame.asRequestStream(): RequestStreamFrame = typed(FrameType.REQUEST_STREAM) {
    val untypedFlags = header.flags.value
    val flags = RequestStreamFlags(
        metadata = untypedFlags check CommonFlag.Metadata,
        follows = untypedFlags check CommonFlag.Follows
    )
    val initialRequestN = readInt()
    val payload = readPayload(flags.metadata)
    RequestStreamFrame(
        header = header.withFlags(flags),
        initialRequestN = initialRequestN,
        payload = payload
    )
}

class RequestStreamFlags(
    val metadata: Boolean,
    val follows: Boolean
) : TypedFlags({
    CommonFlag.Metadata setIf metadata
    CommonFlag.Follows setIf follows
})