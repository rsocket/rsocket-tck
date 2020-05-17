package io.rsocket.tck.expect.frame

import io.rsocket.tck.expect.*
import io.rsocket.tck.frame.*
import strikt.api.*

infix fun CancelFrame.expect(expected: CancelFrame): Unit = expectThat(this) {
    get("header", CancelFrame::header).isEqualToHeader(expected.header)
}
