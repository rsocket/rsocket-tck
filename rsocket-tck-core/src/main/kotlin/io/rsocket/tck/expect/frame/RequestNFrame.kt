package io.rsocket.tck.expect.frame

import io.rsocket.tck.expect.*
import io.rsocket.tck.frame.*
import strikt.api.*
import strikt.assertions.*

infix fun RequestNFrame.expect(expected: RequestNFrame): Unit = expectThat(this) {
    get("header", RequestNFrame::header).isEqualToHeader(expected.header)
    get("request N", RequestNFrame::requestN).isEqualTo(expected.requestN)
}
