package io.rsocket

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.ByteBufUtil
import io.rsocket.frames.SetupFrame
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets.UTF_8
import kotlin.test.BeforeTest
import kotlin.test.assertTrue

class SetupFrameSpec {
    private lateinit var frame: SetupFrame

    private val CAN_HAVE_DATA = 0x0000_000F
    private val CAN_HAVE_METADATA = 0x0000_0008

    @BeforeTest
    fun setUp() {
        frame = SetupFrame("data", 1)
    }

    @Test
    fun toByteBuf() {
        val buffer = frame.asRawBuffer()


        val expected = FrameBuilder()
            .withStream(0)
            .withFrameType(0x01)
            .withFlags(CAN_HAVE_DATA or CAN_HAVE_METADATA)
            .withMajorVersion(1)
            .withData("data")
            .build()

        assertTrue(ByteBufUtil.equals(buffer, expected))
    }

    class FrameBuilder {
        private val FRAME_TYPE_BITS: Int = 6
        private val FRAME_TYPE_SHIFT: Int = 16 - FRAME_TYPE_BITS

        private val allocator: ByteBufAllocator = ByteBufAllocator.DEFAULT
        private var stream: Int = 0
        private var type: Int = 0
        private var flags: Int = 0
        private var majorVersion: Int = 0
        private var minorVersion: Int = 0
        private var timeBetweenKeepalive: Int = 0
        private var maxLiveTime: Int = 0
        private var token: String? = null
        private var metadata: String? = null
        private var data: String? = null

        fun withStream(stream: Int): FrameBuilder {
            this.stream = stream
            return this
        }

        fun withFrameType(type: Int): FrameBuilder {
            this.type = type
            return this
        }

        fun withFlags(flags: Int): FrameBuilder {
            this.flags = flags
            return this
        }

        fun withMajorVersion(majorVersion: Int): FrameBuilder {
            this.majorVersion = majorVersion
            return this
        }

        fun withMinorVersion(minorVersion: Int): FrameBuilder {
            this.minorVersion = minorVersion
            return this
        }

        fun withTimeBetweenKeepalive(timeBetweenKeepalive: Int): FrameBuilder {
            this.timeBetweenKeepalive = timeBetweenKeepalive
            return this
        }

        fun withMaxLiveTime(maxLiveTime: Int): FrameBuilder {
            this.maxLiveTime = maxLiveTime
            return this
        }

        fun withToken(token: String): FrameBuilder {
            this.token = token
            return this
        }

        fun withMetadata(metadata: String): FrameBuilder {
            this.metadata = metadata
            return this
        }

        fun withData(data: String): FrameBuilder {
            this.data = data
            return this
        }

        fun build(): ByteBuf {
            val buffer = allocator.buffer()
            buffer.writeInt(stream)
                .writeShort(type shl FRAME_TYPE_SHIFT or flags)
                .writeShort(majorVersion)
                .writeShort(minorVersion)
                .writeInt(timeBetweenKeepalive)
                .writeInt(maxLiveTime)

            if (token != null) {
                buffer.writeShort(token!!.length)
                    .writeCharSequence(token, UTF_8)
            }

            if (metadata != null) {
                buffer.writeInt(metadata!!.length)
                    .writeCharSequence(metadata, UTF_8)
            }

            if (data != null) {
                buffer.writeInt(data!!.length)
                    .writeCharSequence(data, UTF_8)
            }

            return buffer
        }
    }
}
