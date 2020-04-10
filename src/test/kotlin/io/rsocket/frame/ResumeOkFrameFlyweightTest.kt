package io.rsocket.frame

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

class ResumeOkFrameFlyweightTest {
    @Test
    fun testEncoding() {
        val resumeOkFrame = ResumeOkFrame.encode(allocator, 42)
        assertEquals(42, resumeOkFrame.lastReceivedClientPos)
    }
}
