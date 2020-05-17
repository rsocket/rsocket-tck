package io.rsocket.tck.expect.frame

import io.rsocket.tck.expect.*
import io.rsocket.tck.frame.*
import strikt.api.*
import strikt.assertions.*

infix fun KeepAliveFrame.expect(expected: KeepAliveFrame): Unit = expectThat(this) {
    get("header", KeepAliveFrame::header).isEqualToHeader(expected.header, keepAliveFlagsAssertion)
    get("last received position", KeepAliveFrame::lastReceivedPosition).isEqualTo(expected.lastReceivedPosition)
    get("data", KeepAliveFrame::data).isEqualToBuffer(expected.data)
}

private val keepAliveFlagsAssertion: FlagsAssertion<KeepAliveFlags> = { expected ->
    get("respond", KeepAliveFlags::respond).isEqualTo(expected.respond)
}
