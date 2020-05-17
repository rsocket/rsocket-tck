package io.rsocket.tck.tck.spec

import io.netty.buffer.*
import kotlinx.coroutines.*
import kotlin.time.*

class BufferCaseRunner(
    private val transport: Transport,
    allocator: ByteBufAllocator,
    withLength: Boolean
) {

    private val withLengthRunner = when (withLength) {
        true -> BufferWithLengthCaseRunner(transport, allocator)
        else -> null
    }

    suspend fun send(buffer: ByteBuf) {
        withLengthRunner?.send(buffer) ?: transport.send(buffer)
    }

    suspend fun receive(): ByteBuf {
        return withLengthRunner?.receive()?.buffer ?: transport.receive()
    }
}

suspend fun BufferCaseRunner.receive(duration: Duration): ByteBuf = withTimeout(duration) { receive() }
suspend fun BufferCaseRunner.receiveOrNull(duration: Duration): ByteBuf? = withTimeoutOrNull(duration) { receive() }
