package io.rsocket.spec

import io.netty.buffer.*
import kotlinx.coroutines.*
import kotlin.time.*

// implement as transport with rsocket client/server
interface LowLevelCaseContext {
    suspend fun send(buffer: ByteBuf)

    suspend fun receive(): ByteBuf
}

suspend fun LowLevelCaseContext.receive(duration: Duration): ByteBuf = withTimeout(duration) { receive() }
suspend fun LowLevelCaseContext.receiveOrNull(duration: Duration): ByteBuf? = withTimeoutOrNull(duration) { receive() }
