package io.rsocket.tck.expect.frame

import io.rsocket.tck.expect.*
import io.rsocket.tck.frame.*
import strikt.api.*
import strikt.assertions.*

infix fun RequestResponseFrame.expect(expected: RequestResponseFrame): Unit = expectThat(this) {
    get("header", RequestResponseFrame::header).isEqualToHeader(expected.header, requestResponseFlagsAssertion)
    get("payload", RequestResponseFrame::payload).isEqualToPayload(expected.payload)
}

private val requestResponseFlagsAssertion: FlagsAssertion<RequestResponseFlags> = { expected ->
    get("metadata", RequestResponseFlags::metadata).isEqualTo(expected.metadata)
    get("follows", RequestResponseFlags::follows).isEqualTo(expected.follows)
}
