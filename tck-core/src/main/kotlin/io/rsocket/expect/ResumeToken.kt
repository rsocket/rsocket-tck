package io.rsocket.expect

import io.rsocket.frame.shared.*
import strikt.api.*
import strikt.assertions.*

fun Assertion.Builder<ResumeToken?>.isEqualToResumeToken(expected: ResumeToken?): Assertion.Builder<ResumeToken?> = apply {
    get("length") { this?.length }.isEqualTo(expected?.length)
    get("token") { this?.token }.isEqualToBuffer(expected?.token)
}
