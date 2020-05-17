package io.rsocket.tck.expect.frame

import io.rsocket.expect.*
import io.rsocket.frame.*
import strikt.api.*
import strikt.assertions.*

infix fun LeaseFrame.expect(expected: LeaseFrame): Unit = expectThat(this) {
    get("header", LeaseFrame::header).isEqualToHeader(expected.header, leaseFlagsAssertion)
    get("ttl", LeaseFrame::ttl).isEqualTo(expected.ttl)
    get("number of requests", LeaseFrame::numberOfRequests).isEqualTo(expected.numberOfRequests)
    get("metadata", LeaseFrame::metadata).isEqualToBuffer(expected.metadata)
}

private val leaseFlagsAssertion: FlagsAssertion<LeaseFlags> = { expected ->
    get("metadata", LeaseFlags::metadata).isEqualTo(expected.metadata)
}
