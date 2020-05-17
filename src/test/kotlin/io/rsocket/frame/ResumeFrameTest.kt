package io.rsocket.tck.frame

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

class ResumeFrameTest {
    @Test
    fun testEncoding() {
        val tokenBytes = ByteArray(65000) { 1 }
        val token = bufferOf(*tokenBytes)
        val resumeFrame = ResumeFrame.encode(allocator, token, 21, 12)
        assertEquals(Version.CURRENT.value, resumeFrame.version)
        assertEquals(token, resumeFrame.token)
        assertEquals(21, resumeFrame.lastReceivedServerPos)
        assertEquals(12, resumeFrame.firstAvailableClientPos)
    }
}
