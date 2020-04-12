package io.rsocket.frame

import io.rsocket.expect.frame.*
import io.rsocket.frame.shared.*
import org.junit.jupiter.api.*
import strikt.api.*
import strikt.assertions.*
import kotlin.time.*

class SetupFrameTest {
    @Test
    fun testEncoding() {
        val setup = SetupFrame(
            FrameHeader(0, SetupFlags(false, false, true)),
            Version.Current,
            KeepAlive(5.milliseconds, 500.milliseconds),
            null,
            MimeType("metadata_type"),
            MimeType("data_type"),
            Payload(PayloadMetadata(bufferOf(1, 2, 3, 4)), bufferOf(5, 4, 3))
        )

        val newSetup = setup.buffer(allocator).frame().asSetup()

        setup expect newSetup
    }

    @Test
    fun testEncodingResume() {
        val token = bufferOf(*ByteArray(65000) { 1 })
        val setup = SetupFrame(
            FrameHeader(0, SetupFlags(true, false, true)),
            Version.Current,
            KeepAlive(5.milliseconds, 500.milliseconds),
            ResumeToken(token),
            MimeType("metadata_type"),
            MimeType("data_type"),
            Payload(PayloadMetadata(bufferOf(1, 2, 3, 4)), bufferOf(5, 4, 3))
        )

        val newSetup = setup.buffer(allocator).frame().asSetup()

        setup expect newSetup
        expectThat(newSetup.resumeToken?.token).isEqualTo(token)
    }
}
