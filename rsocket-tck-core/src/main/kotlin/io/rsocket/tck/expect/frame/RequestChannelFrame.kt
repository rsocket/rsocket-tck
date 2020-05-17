package io.rsocket.tck.expect.frame

import io.rsocket.expect.*
import io.rsocket.frame.*
import strikt.api.*
import strikt.assertions.*

infix fun RequestChannelFrame.expect(expected: RequestChannelFrame): Unit = expectThat(this) {
    get("header", RequestChannelFrame::header).isEqualToHeader(expected.header, requestChannelFlagsAssertion)
    get("initial request N", RequestChannelFrame::initialRequestN).isEqualTo(expected.initialRequestN)
    get("payload", RequestChannelFrame::payload).isEqualToPayload(expected.payload)
}

private val requestChannelFlagsAssertion: FlagsAssertion<RequestChannelFlags> = { expected ->
    get("metadata", RequestChannelFlags::metadata).isEqualTo(expected.metadata)
    get("follows", RequestChannelFlags::follows).isEqualTo(expected.follows)
    get("complete", RequestChannelFlags::complete).isEqualTo(expected.complete)
}
