package io.rsocket.expect.frame

import io.rsocket.expect.*
import io.rsocket.frame.*
import strikt.api.*
import strikt.assertions.*

infix fun RequestFireAndForgetFrame.expect(expected: RequestFireAndForgetFrame): Unit = expectThat(this) {
    get("header", RequestFireAndForgetFrame::header).isEqualToHeader(expected.header, requestFireAndForgetFrameFlagsAssertion)
    get("payload", RequestFireAndForgetFrame::payload).isEqualToPayload(expected.payload)
}

private val requestFireAndForgetFrameFlagsAssertion: FlagsAssertion<RequestFireAndForgetFlags> = { expected ->
    get("metadata", RequestFireAndForgetFlags::metadata).isEqualTo(expected.metadata)
    get("follows", RequestFireAndForgetFlags::follows).isEqualTo(expected.follows)
}
