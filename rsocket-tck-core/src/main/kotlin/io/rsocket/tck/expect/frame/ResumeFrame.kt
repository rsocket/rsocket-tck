package io.rsocket.tck.expect.frame

import io.rsocket.expect.*
import io.rsocket.frame.*
import strikt.api.*
import strikt.assertions.*

infix fun ResumeFrame.expect(expected: ResumeFrame): Unit = expectThat(this) {
    get("header", ResumeFrame::header).isEqualToHeader(expected.header)
    get("version", ResumeFrame::version).isEqualToVersion(expected.version)
    get("resume token", ResumeFrame::resumeToken).isEqualToResumeToken(expected.resumeToken)
    get("last received server position", ResumeFrame::lastReceivedServerPosition).isEqualTo(expected.lastReceivedServerPosition)
    get("first available client position", ResumeFrame::firstAvailableClientPosition).isEqualTo(expected.firstAvailableClientPosition)
}
