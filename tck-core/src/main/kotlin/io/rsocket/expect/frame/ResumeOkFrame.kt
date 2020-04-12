package io.rsocket.expect.frame

import io.rsocket.expect.*
import io.rsocket.frame.*
import strikt.api.*
import strikt.assertions.*

infix fun ResumeOkFrame.expect(expected: ResumeOkFrame): Unit = expectThat(this) {
    get("header", ResumeOkFrame::header).isEqualToHeader(expected.header)
    get("last received client position", ResumeOkFrame::lastReceivedClientPosition).isEqualTo(expected.lastReceivedClientPosition)
}
