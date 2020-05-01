package io.rsocket.expect.frame

import io.rsocket.expect.*
import io.rsocket.frame.*
import strikt.api.*
import strikt.assertions.*

infix fun ExtensionFrame.expect(expected: ExtensionFrame): Unit = expectThat(this) {
    get("header", ExtensionFrame::header).isEqualToHeader(expected.header, extensionFlagsAssertion)
    get("extended type", ExtensionFrame::extendedType).isEqualTo(expected.extendedType)
    get("data", ExtensionFrame::data).isEqualToBuffer(expected.data)
}

private val extensionFlagsAssertion: FlagsAssertion<ExtensionFrameFlags> = { expected ->
    get("ignore", ExtensionFrameFlags::ignore).isEqualTo(expected.ignore)
    get("metadata", ExtensionFrameFlags::metadata).isEqualTo(expected.metadata)
}
