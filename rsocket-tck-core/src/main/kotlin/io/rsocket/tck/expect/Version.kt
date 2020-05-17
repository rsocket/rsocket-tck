package io.rsocket.tck.expect

import io.rsocket.tck.frame.shared.*
import strikt.api.*
import strikt.assertions.*

fun Assertion.Builder<Version>.isEqualToVersion(expected: Version): Assertion.Builder<Version> = apply {
    get("major", Version::major).isEqualTo(expected.major)
    get("minor", Version::minor).isEqualTo(expected.minor)
}
