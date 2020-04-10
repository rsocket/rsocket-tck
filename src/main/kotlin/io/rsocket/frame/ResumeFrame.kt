package io.rsocket.frame

import io.netty.buffer.*
import java.util.*

inline class ResumeFrame(val buffer: ByteBuf) {
    private val header: FrameHeader get() = FrameHeader(buffer, FrameType.RESUME)

    val version: Int
        get() {
            header //TODO
            return buffer.preview {
                skipBytes(FrameHeader.SIZE)
                readInt()
            }
        }

    val token: ByteBuf
        get() {
            header //TODO
            return buffer.preview {
                skipBytes(FrameHeader.SIZE + Size.VERSION)
                readSlice(readTokenLength())
            }
        }

    val lastReceivedServerPos: Long
        get() {
            header //TODO
            return buffer.preview {
                skipBytes(FrameHeader.SIZE + Size.VERSION)
                skipBytes(readTokenLength())
                readLong()
            }
        }

    val firstAvailableClientPos: Long
        get() {
            header //TODO
            return buffer.preview {
                skipBytes(FrameHeader.SIZE + Size.VERSION)
                skipBytes(readTokenLength())
                skipBytes(Size.LAST_RECEIVED_SERVER_POS)
                readLong()
            }
        }

    private fun readTokenLength(): Int = buffer.readShort().toInt() and 0xFFFF

    private object Size {
        const val VERSION: Int = Int.SIZE_BYTES
        const val LAST_RECEIVED_SERVER_POS: Int = Long.SIZE_BYTES
    }

    companion object {
        fun generateToken(): ByteBuf = Unpooled.buffer(16).apply {
            val uuid = UUID.randomUUID()
            writeLong(uuid.mostSignificantBits)
            writeLong(uuid.leastSignificantBits)
        }

        fun encode(allocator: ByteBufAllocator, token: ByteBuf, lastReceivedServerPos: Long, firstAvailableClientPos: Long): ResumeFrame =
            ResumeFrame(FrameHeader.encode(allocator, FrameType.RESUME, 0) {
                writeInt(Version.CURRENT.value)

                token.markReaderIndex()
                writeShort(token.readableBytes())
                writeBytes(token)
                token.resetReaderIndex()

                writeLong(lastReceivedServerPos)
                writeLong(firstAvailableClientPos)
            }.buffer)

    }
}