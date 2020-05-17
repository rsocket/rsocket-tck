package io.rsocket.tck.frame

import io.rsocket.expect.*
import io.rsocket.expect.frame.*
import io.rsocket.frame.shared.*
import org.junit.jupiter.api.*
import strikt.api.*
import strikt.assertions.*

class ResumeFrameTest {
    @Test
    fun testEncoding() {
        val token = bufferOf(*ByteArray(65000) { 1 })
        val resume = ResumeFrame(
            FrameHeader(0, UntypedFlags.Empty),
            Version.Current,
            ResumeToken(token),
            21,
            12
        )

        val newResume = resume.buffer(allocator).frame().asResume()

        resume expect newResume

        expectThat(newResume) {
            get { resumeToken.token }.isEqualToBuffer(token)
            get { lastReceivedServerPosition }.isEqualTo(21)
            get { firstAvailableClientPosition }.isEqualTo(12)
        }

    }
}
