package io.rsocket.tck.frame

import io.netty.buffer.*
import io.rsocket.expect.frame.*
import io.rsocket.frame.shared.*
import org.junit.jupiter.api.*
import strikt.api.*
import strikt.assertions.*

class RequestNFrameTest {
    @Test
    fun testEncoding() {
        val requestN = RequestNFrame(
            FrameHeader(1, UntypedFlags.Empty),
            5
        )

        val buffer = requestN.buffer(allocator)
        val newRequestN = buffer.frame().asRequestN()

        requestN expect newRequestN

        expectThat(newRequestN) {
            get { this.requestN }.isEqualTo(5)
        }

        expectThat(ByteBufUtil.hexDump(buffer)).isEqualTo("00000001200000000005")
    }
}
