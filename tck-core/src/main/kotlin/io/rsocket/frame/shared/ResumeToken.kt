package io.rsocket.frame.shared

import io.netty.buffer.*

data class ResumeToken(
    val token: ByteBuf,
    val length: Short = token.readableBytes().toShort()
)

fun ByteBuf.readResumeToken(): ResumeToken {
    val length = readShort()
    val token = readSlice(length.toInt() and 0xFFFF)
    return ResumeToken(
        token = token,
        length = length
    )
}