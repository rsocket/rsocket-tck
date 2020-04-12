package io.rsocket.frame.shared

import io.netty.buffer.*

inline fun <T> ByteBuf.preview(block: ByteBuf.() -> T): T {
    markReaderIndex()
    try {
        return block()
    } finally {
        resetReaderIndex()
    }
}

inline fun ByteBufAllocator.buffer(block: ByteBuf.() -> Unit): ByteBuf = buffer().apply(block)

fun ByteBufAllocator.compose(vararg components: ByteBuf): ByteBuf =
    compositeBuffer(components.size).addComponents(true, *components)

fun ByteBuf.writeUtf8(text: String): ByteBuf = apply {
    ByteBufUtil.writeUtf8(this, text)
}

@Suppress("TestFunctionName")
fun TextBuffer(text: String, allocator: ByteBufAllocator = ByteBufAllocator.DEFAULT): ByteBuf = ByteBufUtil.writeUtf8(allocator, text)

fun ByteBuf.text(): String = toString(Charsets.UTF_8)

fun bufferOf(vararg bytes: Byte): ByteBuf = Unpooled.copiedBuffer(bytes)

fun ByteBuf.readLength(): Int {
    val b = readByte().toInt() and 0xFF shl 16
    val b1 = readByte().toInt() and 0xFF shl 8
    val b2 = readByte().toInt() and 0xFF
    return b or b1 or b2
}

fun ByteBuf.writeLength(length: Int): ByteBuf {
    require(length and 0xFFFFFF.inv() == 0) { "Length is larger than 24 bits" }
    // Write each byte separately in reverse order, this mean we can write 1 << 23 without overflowing.
    writeByte(length shr 16)
    writeByte(length shr 8)
    writeByte(length)
    return this
}
