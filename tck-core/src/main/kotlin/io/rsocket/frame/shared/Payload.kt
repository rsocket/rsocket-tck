package io.rsocket.frame.shared

import io.netty.buffer.*

data class Payload(
    val metadata: PayloadMetadata? = null,
    val data: ByteBuf = Unpooled.EMPTY_BUFFER
)

data class PayloadMetadata(
    val value: ByteBuf,
    val length: Int = value.readableBytes()
)
