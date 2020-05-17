package io.rsocket.tck.frame

import io.netty.buffer.*
import io.rsocket.frame.shared.*

data class RequestResponseFrame(
    override val header: FrameHeader<RequestResponseFlags>,
    val payload: Payload
) : Frame<RequestResponseFlags>(FrameType.REQUEST_RESPONSE) {
    override fun buffer(allocator: ByteBufAllocator): ByteBuf {
        val header = headerBuffer(allocator) {
            payload.metadata?.length?.let(this::writeLength)
        }
        return allocator.compose(header, payload)
    }
}

fun RawFrame.asRequestResponse(): RequestResponseFrame = typed(FrameType.REQUEST_RESPONSE) {
    val untypedFlags = header.flags.value
    val flags = RequestResponseFlags(
        metadata = untypedFlags check CommonFlag.Metadata,
        follows = untypedFlags check CommonFlag.Follows
    )
    val payload = readPayload(flags.metadata)
    RequestResponseFrame(
        header = header.withFlags(flags),
        payload = payload
    )
}

class RequestResponseFlags(
    val metadata: Boolean,
    val follows: Boolean
) : TypedFlags({
    CommonFlag.Metadata setIf metadata
    CommonFlag.Follows setIf follows
})