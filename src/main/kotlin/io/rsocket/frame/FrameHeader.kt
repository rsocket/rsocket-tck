package io.rsocket.tck.frame

import io.netty.buffer.*
import java.lang.Boolean.*

@Suppress("FunctionName")
fun FrameHeader(input: ByteBuf, frameType: FrameType): FrameHeader {
    val header = FrameHeader(input)
    header.checkFrameType(frameType)
    return header
}

inline class FrameHeader(val buffer: ByteBuf) {

    val streamId: Int get() = buffer.preview { readInt() }

    private val typeAndFlags: Int
        get() = buffer.preview {
            skipBytes(Size.STREAM_ID)
            readShort().toInt() and 0xFFFF
        }

    val flags: Int get() = typeAndFlags and FRAME_FLAGS_MASK

    val hasFollows: Boolean get() = flags.checkFlag(Flags.F)
    val hasMetadata: Boolean get() = flags.checkFlag(Flags.M)

    /**
     * faster version of [.frameType] which does not replace PAYLOAD with synthetic type
     */
    val nativeFrameType: FrameType get() = FrameType.fromEncodedType(typeAndFlags shr FRAME_TYPE_SHIFT)

    val frameType: FrameType
        get() {
            val typeAndFlags = typeAndFlags
            return when (val result = FrameType.fromEncodedType(typeAndFlags shr FRAME_TYPE_SHIFT)) {
                FrameType.PAYLOAD -> {
                    val flags = typeAndFlags and FRAME_FLAGS_MASK
                    val complete = flags.checkFlag(Flags.C)
                    val next = flags.checkFlag(Flags.N)
                    when {
                        next && complete -> FrameType.NEXT_COMPLETE
                        complete         -> FrameType.COMPLETE
                        next             -> FrameType.NEXT
                        else             -> throw IllegalArgumentException("Payload must set either or both of NEXT and COMPLETE.")
                    }
                }
                else              -> result
            }
        }

    fun checkFrameType(anotherType: FrameType) {
        if (disableFrameTypeCheck) return

        val typeInFrame = nativeFrameType //TODO
        check(typeInFrame === anotherType) { "expected $anotherType, but saw $typeInFrame" }
    }

    object Flags {
        /** (I)gnore flag: a value of 0 indicates the protocol can't ignore this frame  */
        const val I = 512

        /** (M)etadata flag: a value of 1 indicates the frame contains metadata  */
        const val M = 256

        /**
         * (F)ollows: More fragments follow this fragment (in case of fragmented REQUEST_x or PAYLOAD
         * frames)
         */
        const val F = 128

        /** (C)omplete: bit to indicate stream completion ([Subscriber.onComplete])  */
        const val C = 64

        /** (N)ext: bit to indicate payload or metadata present ([Subscriber.onNext])  */
        const val N = 32
    }

    private object Size {
        const val STREAM_ID: Int = Int.SIZE_BYTES
        const val TYPE_AND_FLAGS: Int = Short.SIZE_BYTES
    }

    companion object {

        const val SIZE: Int = Size.STREAM_ID + Size.TYPE_AND_FLAGS

        private const val FRAME_FLAGS_MASK: Int = 1023
        private const val FRAME_TYPE_BITS: Int = 6
        const val FRAME_TYPE_SHIFT: Int = 16 - FRAME_TYPE_BITS

        private val disableFrameTypeCheck: Boolean = getBoolean("io.rsocket.frames.disableFrameTypeCheck")


        fun encode(
            allocator: ByteBufAllocator,
            frameType: FrameType,
            flags: Int,
            streamId: Int = 0,
            configuration: ByteBuf.() -> Unit = {}
        ): FrameHeader {
            check(frameType.canHaveMetadata || !flags.checkFlag(FrameHeader.Flags.M)) { "bad value for metadata flag" }
            val typeAndFlags = frameType.encodedType shl FrameHeader.FRAME_TYPE_SHIFT or flags
            return FrameHeader(allocator.buffer {
                writeInt(streamId)
                writeShort(typeAndFlags)
                configuration()
            })
        }
    }
}
