package io.rsocket.frame

import io.rsocket.expect.*
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

        expectThat(newSetup) {
            get { version }.isEqualTo(Version.Current)
            get { keepAlive.interval.inMilliseconds }.isEqualTo(5.0)
            get { keepAlive.maxLifetime.inMilliseconds }.isEqualTo(500.0)
            get { metadataMimeType.text }.isEqualTo("metadata_type")
            get { dataMimeType.text }.isEqualTo("data_type")
            get { resumeToken }.isEqualTo(null)
            get { payload.metadata?.value }.isEqualToBuffer(bufferOf(1, 2, 3, 4))
            get { payload.data }.isEqualToBuffer(bufferOf(5, 4, 3))
        }
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

        expectThat(newSetup) {
            get { resumeToken?.token }.isEqualTo(token)
            get { payload.data }.isEqualTo(bufferOf(5, 4, 3))
        }
    }
}
