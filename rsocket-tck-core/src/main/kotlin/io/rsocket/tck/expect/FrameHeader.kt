package io.rsocket.tck.expect

import io.rsocket.tck.frame.shared.*
import strikt.api.*
import strikt.assertions.*

typealias FlagsAssertion<T> = Assertion.Builder<T>.(expected: T) -> Unit

private fun Int.binary(): String = "%10s".format(toString(2)).replace(' ', '0')

infix fun <T : Flags> Assertion.Builder<FrameHeader<T>>.isEqualToHeader(
    expected: FrameHeader<T>
): Assertion.Builder<FrameHeader<T>> = apply {
    get("stream Id", FrameHeader<*>::streamId).isEqualTo(expected.streamId)
    get("flags value") { flags.value.binary() }.isEqualTo(expected.flags.value.binary())
}

fun <T : Flags> Assertion.Builder<FrameHeader<T>>.isEqualToHeader(
    expected: FrameHeader<T>,
    flagsAssertion: FlagsAssertion<T>
): Assertion.Builder<FrameHeader<T>> = apply {
    this isEqualToHeader expected
    get("flags", FrameHeader<T>::flags).flagsAssertion(expected.flags)
}
