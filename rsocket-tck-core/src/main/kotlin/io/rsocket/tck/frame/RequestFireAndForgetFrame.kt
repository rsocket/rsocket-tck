package io.rsocket.tck.frame

import io.netty.buffer.*
import io.rsocket.frame.shared.*

data class RequestFireAndForgetFrame(
    override val header: FrameHeader<RequestFireAndForgetFlags>,
    val payload: Payload
) : Frame<RequestFireAndForgetFlags>(FrameType.REQUEST_FNF) {
    override fun buffer(allocator: ByteBufAllocator): ByteBuf {
        val header = headerBuffer(allocator) {
            payload.metadata?.length?.let(this::writeLength)
        }
        return allocator.compose(header, payload)
    }
}

fun RawFrame.asRequestFireAndForget(): RequestFireAndForgetFrame = typed(FrameType.REQUEST_FNF) {
    val untypedFlags = header.flags.value
    val flags = RequestFireAndForgetFlags(
        metadata = untypedFlags check CommonFlag.Metadata,
        follows = untypedFlags check CommonFlag.Follows
    )
    val payload = readPayload(flags.metadata)
    RequestFireAndForgetFrame(
        header = header.withFlags(flags),
        payload = payload
    )
}

class RequestFireAndForgetFlags(
    val metadata: Boolean,
    val follows: Boolean
) : TypedFlags({
    CommonFlag.Metadata setIf metadata
    CommonFlag.Follows setIf follows
})