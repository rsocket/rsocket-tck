package io.rsocket.expect

import io.rsocket.frame.shared.*
import strikt.api.*
import strikt.assertions.*

fun Assertion.Builder<Payload>.isEqualToPayload(expected: Payload): Assertion.Builder<Payload> = apply {
    get("metadata", Payload::metadata).isEqualTo(expected.metadata)
    get("data", Payload::data).isEqualTo(expected.data)
}

private fun Assertion.Builder<PayloadMetadata?>.isEqualTo(expected: PayloadMetadata?): Assertion.Builder<PayloadMetadata?> = apply {
    get("length") { this?.length }.isEqualTo(expected?.length)
    get("value") { this?.value }.isEqualToBuffer(expected?.value)
}
