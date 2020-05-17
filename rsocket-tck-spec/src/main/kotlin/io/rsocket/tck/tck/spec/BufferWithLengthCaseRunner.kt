package io.rsocket.tck.tck.spec

import io.netty.buffer.*
import io.rsocket.frame.shared.*
import kotlinx.coroutines.*
import kotlin.time.*

class BufferWithLengthCaseRunner(
    private val transport: Transport,
    private val allocator: ByteBufAllocator
) {

    suspend fun send(buffer: ByteBuf) {
        val bufferWithLength = buffer.withLength(buffer.readableBytes(), allocator)
        transport.send(bufferWithLength)
    }

    suspend fun receive(): BufferWithLength {
        val buffer = transport.receive()
        return buffer.withLength()
    }
}

suspend fun BufferWithLengthCaseRunner.receive(duration: Duration): BufferWithLength = withTimeout(duration) { receive() }
suspend fun BufferWithLengthCaseRunner.receiveOrNull(duration: Duration): BufferWithLength? = withTimeoutOrNull(duration) { receive() }
