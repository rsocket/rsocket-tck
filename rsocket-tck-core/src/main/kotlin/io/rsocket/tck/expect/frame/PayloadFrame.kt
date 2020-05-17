package io.rsocket.tck.expect.frame

import io.rsocket.tck.expect.*
import io.rsocket.tck.frame.*
import strikt.api.*
import strikt.assertions.*

infix fun PayloadFrame.expect(expected: PayloadFrame): Unit = expectThat(this) {
    get("header", PayloadFrame::header).isEqualToHeader(expected.header, payloadFlagsAssertion)
    get("payload", PayloadFrame::payload).isEqualToPayload(expected.payload)
}

private val payloadFlagsAssertion: FlagsAssertion<PayloadFlags> = { expected ->
    get("metadata", PayloadFlags::metadata).isEqualTo(expected.metadata)
    get("follows", PayloadFlags::follows).isEqualTo(expected.follows)
    get("complete", PayloadFlags::complete).isEqualTo(expected.complete)
    get("next", PayloadFlags::next).isEqualTo(expected.next)

    assert("complete or next") {
        when (it.complete || it.next) {
            true  -> pass()
            false -> fail("at least complete or next must be true")
        }
    }
}
