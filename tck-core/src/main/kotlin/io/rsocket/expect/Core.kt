package io.rsocket.expect

import io.netty.buffer.*
import strikt.api.*

infix fun <T : ByteBuf?> Assertion.Builder<T>.isEqualToBuffer(expected: T?): Assertion.Builder<T> =
    assert("is equal to %s", expected?.let(ByteBufUtil::prettyHexDump)) {
        when (it) {
            expected -> pass()
            else     -> fail(it?.let(ByteBufUtil::prettyHexDump), "found %s")
        }
    }
