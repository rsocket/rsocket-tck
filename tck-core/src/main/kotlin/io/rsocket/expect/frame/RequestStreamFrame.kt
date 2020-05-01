package io.rsocket.expect.frame

import io.rsocket.expect.*
import io.rsocket.frame.*
import strikt.api.*
import strikt.assertions.*

infix fun RequestStreamFrame.expect(expected: RequestStreamFrame): Unit = expectThat(this) {
    get("header", RequestStreamFrame::header).isEqualToHeader(expected.header, requestStreamFlagsAssertion)
    get("initial request N", RequestStreamFrame::initialRequestN).isEqualTo(expected.initialRequestN)
    get("payload", RequestStreamFrame::payload).isEqualToPayload(expected.payload)
}

private val requestStreamFlagsAssertion: FlagsAssertion<RequestStreamFlags> = { expected ->
    get("metadata", RequestStreamFlags::metadata).isEqualTo(expected.metadata)
    get("follows", RequestStreamFlags::follows).isEqualTo(expected.follows)
}
