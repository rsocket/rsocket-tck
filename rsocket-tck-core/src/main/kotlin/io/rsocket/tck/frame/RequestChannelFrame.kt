package io.rsocket.tck.frame

import io.netty.buffer.*
import io.rsocket.tck.frame.shared.*

data class RequestChannelFrame(
    override val header: FrameHeader<RequestChannelFlags>,
    val initialRequestN: Int,
    val payload: Payload
) : Frame<RequestChannelFlags>(FrameType.REQUEST_CHANNEL) {
    override fun buffer(allocator: ByteBufAllocator): ByteBuf {
        val header = headerBuffer(allocator) {
            writeInt(initialRequestN)
            payload.metadata?.length?.let(this::writeLength)
        }
        return allocator.compose(header, payload)
    }
}

fun RawFrame.asRequestChannel(): RequestChannelFrame = typed(FrameType.REQUEST_CHANNEL) {
    val untypedFlags = header.flags.value
    val flags = RequestChannelFlags(
        metadata = untypedFlags check CommonFlag.Metadata,
        follows = untypedFlags check CommonFlag.Follows,
        complete = untypedFlags check CommonFlag.Complete
    )
    val initialRequestN = readInt()
    val payload = readPayload(flags.metadata)
    RequestChannelFrame(
        header = header.withFlags(flags),
        initialRequestN = initialRequestN,
        payload = payload
    )
}

class RequestChannelFlags(
    val metadata: Boolean,
    val follows: Boolean,
    val complete: Boolean
) : TypedFlags({
    CommonFlag.Metadata setIf metadata
    CommonFlag.Follows setIf follows
    CommonFlag.Complete setIf complete
})
