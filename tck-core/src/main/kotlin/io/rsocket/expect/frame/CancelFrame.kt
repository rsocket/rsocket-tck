package io.rsocket.expect.frame

import io.rsocket.expect.*
import io.rsocket.frame.*
import strikt.api.*

infix fun CancelFrame.expect(expected: CancelFrame): Unit = expectThat(this) {
    get("header", CancelFrame::header).isEqualToHeader(expected.header)
}
