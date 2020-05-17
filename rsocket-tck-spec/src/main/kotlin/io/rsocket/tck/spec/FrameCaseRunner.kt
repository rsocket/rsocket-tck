package io.rsocket.tck.spec

import io.netty.buffer.*
import io.rsocket.tck.frame.*
import io.rsocket.tck.frame.shared.*
import kotlinx.coroutines.*
import kotlin.time.*

class FrameCaseRunner(
    private val transport: Transport,
    private val allocator: ByteBufAllocator,
    withLength: Boolean
) {

    private val withLengthRunner = when (withLength) {
        true -> FrameWithLengthCaseRunner(transport, allocator)
        else -> null
    }

    suspend fun send(frame: Frame<*>) {
        withLengthRunner?.send(frame) ?: transport.send(frame.buffer(allocator))
    }

    suspend fun receive(): RawFrame {
        return withLengthRunner?.receive()?.frame ?: transport.receive().frame()
    }
}

suspend fun FrameCaseRunner.receive(duration: Duration): RawFrame = withTimeout(duration) { receive() }
suspend fun FrameCaseRunner.receiveOrNull(duration: Duration): RawFrame? = withTimeoutOrNull(duration) { receive() }
