package io.rsocket.spec

import io.netty.buffer.*
import kotlinx.coroutines.*
import org.spekframework.spek2.dsl.*
import org.spekframework.spek2.style.specification.*

private fun Suite.transportCase(
    description: String,
    skip: Skip = Skip.No,
    timeout: Long = delegate.defaultTimeout,
    body: suspend (Transport) -> Unit
) {
    it(description, skip, timeout) {
        val transport: Transport by memoized()
        runBlocking { body(transport) }
    }
}

//test against frames
fun Suite.case(
    description: String,
    skip: Skip = Skip.No,
    timeout: Long = delegate.defaultTimeout,
    body: suspend FrameCaseRunner.() -> Unit
) {
    transportCase(description, skip, timeout) {
        FrameCaseRunner(it, ByteBufAllocator.DEFAULT, config.withLength).body()
    }
}

//test against frames with length
fun Suite.withLengthCase(
    description: String,
    skip: Skip = Skip.No,
    timeout: Long = delegate.defaultTimeout,
    body: suspend FrameWithLengthCaseRunner.() -> Unit
) {
    transportCase(description, if (config.withLength) skip else Skip.Yes("Without length"), timeout) {
        FrameWithLengthCaseRunner(it, ByteBufAllocator.DEFAULT).body()
    }
}

//test against buffers
fun Suite.bufferCase(
    description: String,
    skip: Skip = Skip.No,
    timeout: Long = delegate.defaultTimeout,
    body: suspend BufferCaseRunner.() -> Unit
) {
    transportCase(description, skip, timeout) {
        BufferCaseRunner(it, ByteBufAllocator.DEFAULT, config.withLength).body()
    }
}

//test against buffers with length
fun Suite.bufferWithLengthCase(
    description: String,
    skip: Skip = Skip.No,
    timeout: Long = delegate.defaultTimeout,
    body: suspend BufferWithLengthCaseRunner.() -> Unit
) {
    transportCase(description, if (config.withLength) skip else Skip.Yes("Without length"), timeout) {
        BufferWithLengthCaseRunner(it, ByteBufAllocator.DEFAULT).body()
    }
}
