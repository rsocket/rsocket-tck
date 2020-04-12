package io.rsocket.spec

import io.netty.buffer.*
import io.rsocket.frame.*
import io.rsocket.frame.shared.*
import kotlinx.coroutines.*
import kotlin.time.*

class CaseContext(
    private val lowLevelCaseContext: LowLevelCaseContext,
    private val allocator: ByteBufAllocator
) {

    suspend fun send(frame: Frame<*>) {
        lowLevelCaseContext.send(frame.buffer(allocator = allocator))
    }

    suspend fun receive(): RawFrame = lowLevelCaseContext.receive().frame()
}

suspend fun CaseContext.receive(duration: Duration): RawFrame = withTimeout(duration) { receive() }
suspend fun CaseContext.receiveOrNull(duration: Duration): RawFrame? = withTimeoutOrNull(duration) { receive() }
