package io.rsocket.tck.tck.spec

import io.netty.buffer.*
import io.rsocket.frame.*
import io.rsocket.frame.shared.*
import kotlinx.coroutines.*
import kotlin.time.*

class FrameWithLengthCaseRunner(
    transport: Transport,
    private val allocator: ByteBufAllocator
) {
    private val runner = BufferWithLengthCaseRunner(transport, allocator)

    suspend fun send(frame: Frame<*>) {
        runner.send(frame.buffer(allocator))
    }

    suspend fun receive(): FrameWithLength {
        return runner.receive().frame()
    }
}

suspend fun FrameWithLengthCaseRunner.receive(duration: Duration): FrameWithLength = withTimeout(duration) { receive() }
suspend fun FrameWithLengthCaseRunner.receiveOrNull(duration: Duration): FrameWithLength? = withTimeoutOrNull(duration) { receive() }
