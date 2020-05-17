package io.rsocket.tck.frame.shared

import io.netty.buffer.*

data class MimeType(
    val text: String,
    val length: Byte = ByteBufUtil.utf8Bytes(text).toByte()
)

fun ByteBuf.readMimeType(): MimeType {
    val length = readByte()
    val text = readSlice(length.toInt()).toString(Charsets.UTF_8)
    return MimeType(
        text = text,
        length = length
    )
}
