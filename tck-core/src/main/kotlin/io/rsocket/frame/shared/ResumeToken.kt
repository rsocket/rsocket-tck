package io.rsocket.frame.shared

import io.netty.buffer.*

data class ResumeToken(
    val token: ByteBuf,
    val length: Short = token.readableBytes().toShort()
)