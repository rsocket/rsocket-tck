package io.rsocket.tck.frame

import io.netty.buffer.*

val allocator: ByteBufAllocator = ByteBufAllocator.DEFAULT

@Suppress("TestFunctionName")
fun TextBuffer(text: String): ByteBuf = ByteBufUtil.writeUtf8(allocator, text)

fun ByteBuf.text(): String = toString(Charsets.UTF_8)

fun bufferOf(vararg bytes: Byte): ByteBuf = Unpooled.copiedBuffer(bytes)
