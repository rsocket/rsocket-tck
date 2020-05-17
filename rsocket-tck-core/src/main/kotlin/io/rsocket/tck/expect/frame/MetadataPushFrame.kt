package io.rsocket.tck.expect.frame

import io.rsocket.expect.*
import io.rsocket.frame.*
import strikt.api.*
import strikt.assertions.*

infix fun MetadataPushFrame.expect(expected: MetadataPushFrame): Unit = expectThat(this) {
    get("header", MetadataPushFrame::header).isEqualToHeader(expected.header, metadataPushFlagsAssertion)
    get("metadata", MetadataPushFrame::metadata).isEqualToBuffer(expected.metadata)
}

private val metadataPushFlagsAssertion: FlagsAssertion<MetadataPushFlags> = { expected ->
    get("metadata", MetadataPushFlags::metadata)
        .isTrue()
        .isEqualTo(expected.metadata)
}
