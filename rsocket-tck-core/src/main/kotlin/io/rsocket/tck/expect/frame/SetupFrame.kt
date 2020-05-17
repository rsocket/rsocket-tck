package io.rsocket.tck.expect.frame

import io.rsocket.tck.expect.*
import io.rsocket.tck.frame.*
import io.rsocket.tck.frame.shared.*
import strikt.api.*
import strikt.assertions.*

infix fun SetupFrame.expect(expected: SetupFrame): Unit = expectThat(this) {
    get("header", SetupFrame::header).isEqualToHeader(expected.header, setupFlagsAssertion)
    get("version", SetupFrame::version).isEqualToVersion(expected.version)
    get("keep alive", SetupFrame::keepAlive).isEqualTo(expected.keepAlive)
    get("resume token", SetupFrame::resumeToken).isEqualToResumeToken(expected.resumeToken)
    get("metadata mime type", SetupFrame::metadataMimeType).isEqualTo(expected.metadataMimeType)
    get("data mime type", SetupFrame::dataMimeType).isEqualTo(expected.dataMimeType)

    get("payload", SetupFrame::payload).isNotNull().and {
        isEqualToPayload(expected.payload!!)
    }
}

private val setupFlagsAssertion: FlagsAssertion<SetupFlags> = { expected ->
    get("resume", SetupFlags::resume).isEqualTo(expected.resume)
    get("lease", SetupFlags::lease).isEqualTo(expected.lease)
    get("metadata", SetupFlags::metadata).isEqualTo(expected.metadata)
}

private fun Assertion.Builder<KeepAlive>.isEqualTo(expected: KeepAlive): Assertion.Builder<KeepAlive> = apply {
    get("interval", KeepAlive::interval).isEqualTo(expected.interval)
    get("max lifetime", KeepAlive::maxLifetime).isEqualTo(expected.maxLifetime)
}

private fun Assertion.Builder<MimeType>.isEqualTo(expected: MimeType): Assertion.Builder<MimeType> = apply {
    get("length") { length.toInt() }.isEqualTo(expected.length.toInt())
    get("text", MimeType::text).isEqualTo(expected.text)
}
