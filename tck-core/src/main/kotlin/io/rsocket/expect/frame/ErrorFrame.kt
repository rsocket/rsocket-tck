package io.rsocket.expect.frame

import io.rsocket.expect.*
import io.rsocket.frame.*
import io.rsocket.frame.shared.*
import strikt.api.*
import strikt.assertions.*

infix fun ErrorFrame.expect(expected: ErrorFrame): Unit = expectThat(this) {
    get("header", ErrorFrame::header).isEqualToHeader(expected.header)
    get("error code", ErrorFrame::code).isEqualTo(expected.code)
    get("data") { data.text() }.isEqualTo(expected.data.text())
}

private val ErrorCode.prettyString: String
    get() = "0x%08X".format(value) + when (this) {
        is DefinedErrorCode -> "[$name]"
        else                -> "[Custom]"
    }

private fun Assertion.Builder<ErrorCode>.isEqualTo(expected: ErrorCode): Assertion.Builder<ErrorCode> =
    assert("is equal to %s", expected.prettyString) {
        when (it.value) {
            expected.value -> pass()
            else           -> fail(it.prettyString, "found %s")
        }
    }
