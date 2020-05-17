package io.rsocket.tck.frame

import io.netty.buffer.*


fun Int.checkFlag(flag: Int): Boolean = this and flag == flag

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