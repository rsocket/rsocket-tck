package io.rsocket.spec

import io.netty.buffer.*
import kotlinx.coroutines.*
import org.spekframework.spek2.dsl.*
import org.spekframework.spek2.style.specification.*

//test against frames
fun Suite.case(description: String, skip: Skip = Skip.No, timeout: Long = delegate.defaultTimeout, body: suspend CaseContext.() -> Unit) {
    it(description, skip, timeout) {
        val context: LowLevelCaseContext by memoized()
        runBlocking {
            CaseContext(context, ByteBufAllocator.DEFAULT).body()
        }
    }
}

//test against low level buffers
fun Suite.lowLevelCase(
    description: String,
    skip: Skip = Skip.No,
    timeout: Long = delegate.defaultTimeout,
    body: suspend LowLevelCaseContext.() -> Unit
) {
    it(description, skip, timeout) {
        val context: LowLevelCaseContext by memoized()
        runBlocking {
            context.body()
        }
    }
}
