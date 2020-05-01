package io.rsocket.frame

import io.netty.buffer.*
import io.rsocket.expect.*
import io.rsocket.expect.frame.*
import io.rsocket.frame.shared.*
import org.junit.jupiter.api.*
import strikt.api.*
import strikt.assertions.*

class KeepAliveFrameTest {
    @Test
    fun canReadData() {
        val data = bufferOf(5, 4, 3)
        val keepAlive = KeepAliveFrame(
            FrameHeader(0, KeepAliveFlags(true)),
            3,
            data
        )

        val newKeepAlive = keepAlive.buffer(allocator).frame().asKeepAlive()

        keepAlive expect newKeepAlive

        expectThat(newKeepAlive) {
            get { lastReceivedPosition }.isEqualTo(3)
            get { data }.isEqualToBuffer(data)
        }
    }

    @Test
    fun testEncoding() {
        val keepAlive = KeepAliveFrame(
            FrameHeader(0, KeepAliveFlags(true)),
            0,
            TextBuffer("d")
        )

        val buffer = keepAlive.buffer(allocator)
        val newKeepAlive = buffer.frame().asKeepAlive()

        keepAlive expect newKeepAlive

        expectThat(ByteBufUtil.hexDump(buffer)).isEqualTo("000000000c80000000000000000064")
    }
}
