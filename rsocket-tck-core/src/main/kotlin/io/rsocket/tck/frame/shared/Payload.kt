package io.rsocket.tck.frame.shared

import io.netty.buffer.*

data class Payload(
    val metadata: PayloadMetadata? = null,
    val data: ByteBuf = Unpooled.EMPTY_BUFFER
)

data class PayloadMetadata(
    val value: ByteBuf,
    val length: Int = value.readableBytes()
)

fun ByteBufAllocator.compose(header: ByteBuf, payload: Payload): ByteBuf = when (payload.metadata) {
    null -> compose(header, payload.data)
    else -> compose(header, payload.metadata.value, payload.data)
}

fun ByteBuf.readPayloadMetadata(): PayloadMetadata {
    val length = readLength()
    val metadata = readSlice(length)
    return PayloadMetadata(
        value = metadata,
        length = length
    )
}

fun ByteBuf.readPayload(hasMetadata: Boolean): Payload {
    val payloadMetadata = if (hasMetadata) readPayloadMetadata() else null
    val data = readableBytes().takeIf { it > 0 }?.let(this::readSlice) ?: Unpooled.EMPTY_BUFFER
    return Payload(
        metadata = payloadMetadata,
        data = data
    )
}
