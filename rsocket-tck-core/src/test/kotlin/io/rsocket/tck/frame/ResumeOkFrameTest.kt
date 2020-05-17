package io.rsocket.tck.frame

import io.rsocket.tck.expect.frame.*
import io.rsocket.tck.frame.shared.*
import org.junit.jupiter.api.*
import strikt.api.*
import strikt.assertions.*

class ResumeOkFrameTest {
    @Test
    fun testEncoding() {
        val resumeOk = ResumeOkFrame(
            FrameHeader(0, UntypedFlags.Empty),
            42
        )

        val newResumeOk = resumeOk.buffer(allocator).frame().asResumeOk()

        resumeOk expect newResumeOk

        expectThat(newResumeOk) {
            get { header.streamId }.isEqualTo(0)
            get { lastReceivedClientPosition }.isEqualTo(42)
        }
    }
}
