package io.rsocket.tck.frame

import io.netty.buffer.*
import io.rsocket.tck.frame.shared.*

data class PayloadFrame(
    override val header: FrameHeader<PayloadFlags>,
    val payload: Payload
) : Frame<PayloadFlags>(FrameType.PAYLOAD) {
    override fun buffer(allocator: ByteBufAllocator): ByteBuf {
        val header = headerBuffer(allocator) {
            payload.metadata?.length?.let(this::writeLength)
        }
        return allocator.compose(header, payload)
    }
}

fun RawFrame.asPayload(): PayloadFrame = typed(FrameType.PAYLOAD) {
    val untypedFlags = header.flags.value
    val flags = PayloadFlags(
        metadata = untypedFlags check CommonFlag.Metadata,
        follows = untypedFlags check CommonFlag.Follows,
        complete = untypedFlags check CommonFlag.Complete,
        next = untypedFlags check CommonFlag.Next
    )
    val payload = readPayload(flags.metadata)
    PayloadFrame(
        header = header.withFlags(flags),
        payload = payload
    )
}

class PayloadFlags(
    val metadata: Boolean,
    val follows: Boolean,
    val complete: Boolean,
    val next: Boolean
) : TypedFlags({
    CommonFlag.Metadata setIf metadata
    CommonFlag.Follows setIf follows
    CommonFlag.Complete setIf complete
    CommonFlag.Next setIf next
})
