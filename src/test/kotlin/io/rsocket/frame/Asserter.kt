package io.rsocket.frame

import io.netty.buffer.*
import org.assertj.core.api.*
import org.assertj.core.error.*
import org.assertj.core.internal.*
import java.nio.charset.*

class FrameAssert(frame: ByteBuf) : AbstractAssert<FrameAssert, ByteBuf>(frame, FrameAssert::class.java) {
    private val failures = Failures.instance()
    private val header get() = FrameHeader(actual)

    fun hasMetadata(): FrameAssert {
        assertValid()
        if (!header.hasMetadata) {
            throw failures.failure(info, ShouldHave.shouldHave(actual, Condition("metadata present")))
        }
        return this
    }

    fun hasNoMetadata(): FrameAssert {
        assertValid()
        if (header.hasMetadata) {
            throw failures.failure(info, ShouldHave.shouldHave(actual, Condition("metadata absent")))
        }
        return this
    }

    fun hasMetadata(metadata: String, charset: Charset = Charsets.UTF_8): FrameAssert = hasMetadata(metadata.toByteArray(charset))

    fun hasMetadata(metadata: ByteArray): FrameAssert = hasMetadata(Unpooled.wrappedBuffer(metadata))

    fun hasMetadata(metadata: ByteBuf): FrameAssert {
        hasMetadata()
        val frameType = header.frameType
        val content = when {
            frameType === FrameType.METADATA_PUSH -> MetadataPushFrame(actual).metadata
            frameType.hasInitialRequestN          -> RequestFrame(actual).withInitial.metadata
            else                                  -> RequestFrame(actual).metadata
        }
        if (!ByteBufUtil.equals(content, metadata)) {
            throw failures.failure(info, ShouldBeEqual.shouldBeEqual(content, metadata, ByteBufRepresentation()))
        }
        return this
    }

    fun hasData(data: String, charset: Charset = Charsets.UTF_8): FrameAssert = hasData(data.toByteArray(charset))

    fun hasData(data: ByteArray): FrameAssert = hasData(Unpooled.wrappedBuffer(data))

    fun hasData(data: ByteBuf): FrameAssert {
        assertValid()

        val frameType = header.frameType
        val content = when {
            !frameType.canHaveData       -> throw failures.failure(
                info,
                BasicErrorMessageFactory(
                    "%nExpecting:  %n<%s>   %nto have data content but frame type  %n<%s> does not support data content",
                    actual,
                    frameType
                )
            )
            frameType.hasInitialRequestN -> RequestFrame(actual).withInitial.data
            else                         -> RequestFrame(actual).data
        }
        if (!ByteBufUtil.equals(content, data)) {
            throw failures.failure(info, ShouldBeEqual.shouldBeEqual(content, data, ByteBufRepresentation()))
        }
        return this
    }

    fun hasFragmentsFollow(): FrameAssert = hasFollows(true)

    fun hasNoFragmentsFollow(): FrameAssert = hasFollows(false)

    fun hasFollows(hasFollows: Boolean): FrameAssert {
        assertValid()
        if (header.hasFollows != hasFollows) {
            throw failures.failure(
                info,
                when {
                    hasFollows -> ShouldHave.shouldHave(actual, Condition("follows fragment present"))
                    else       -> ShouldNotHave.shouldNotHave(actual, Condition("follows fragment present"))
                }
            )
        }
        return this
    }

    fun typeOf(frameType: FrameType): FrameAssert {
        assertValid()
        val currentFrameType = header.frameType
        if (currentFrameType !== frameType) {
            throw failures.failure(info, ShouldBe.shouldBe(currentFrameType, Condition("frame of type [$frameType]")))
        }
        return this
    }

    fun hasStreamId(streamId: Int): FrameAssert {
        assertValid()
        val currentStreamId = header.streamId
        if (currentStreamId != streamId) {
            throw failures.failure(
                info,
                BasicErrorMessageFactory("%nExpecting streamId:%n<%s>%n to be equal %n<%s>", currentStreamId, streamId)
            )
        }
        return this
    }

    fun hasStreamIdZero(): FrameAssert = hasStreamId(0)

    fun hasClientSideStreamId(): FrameAssert {
        assertValid()
        val currentStreamId = header.streamId
        if (currentStreamId % 2 != 1) {
            throw failures.failure(
                info,
                BasicErrorMessageFactory(
                    "%nExpecting Client Side StreamId %nbut was "
                            + if (currentStreamId == 0) "Stream Id 0" else "Server Side Stream Id"
                )
            )
        }
        return this
    }

    fun hasServerSideStreamId(): FrameAssert {
        assertValid()
        val currentStreamId = header.streamId
        if (currentStreamId == 0 || currentStreamId % 2 != 0) {
            throw failures.failure(
                info,
                BasicErrorMessageFactory(
                    "%nExpecting %n  Server Side Stream Id %nbut was %n  "
                            + if (currentStreamId == 0) "Stream Id 0" else "Client Side Stream Id"
                )
            )
        }
        return this
    }

    fun hasPayloadSize(frameLength: Int): FrameAssert {
        assertValid()
        val currentFrameType = header.frameType
        val currentFrameLength: Int =
            actual.readableBytes() -
                    FrameHeader.SIZE -
                    (if (header.hasMetadata) 3 else 0) -
                    if (currentFrameType.hasInitialRequestN) Int.SIZE_BYTES else 0

        if (currentFrameLength != frameLength) {
            throw failures.failure(
                info,
                BasicErrorMessageFactory(
                    "%nExpecting %n<%s> %nframe payload size to be equal to  %n<%s>  %nbut was  %n<%s>",
                    actual,
                    frameLength,
                    currentFrameLength
                )
            )
        }
        return this
    }

    fun hasRequestN(n: Int): FrameAssert {
        assertValid()
        val currentFrameType = header.frameType
        val requestN = when {
            currentFrameType.hasInitialRequestN      -> RequestFrame(actual).withInitial.initialRequestN
            currentFrameType === FrameType.REQUEST_N -> RequestNFrame(actual).requestN
            else                                     ->
                throw failures.failure(
                    info,
                    BasicErrorMessageFactory(
                        "%nExpecting:  %n<%s>   %nto have requestN but frame type  %n<%s> does not support requestN",
                        actual,
                        currentFrameType
                    )
                )
        }
        if (requestN != n) {
            throw failures.failure(
                info,
                BasicErrorMessageFactory(
                    "%nExpecting:  %n<%s>   %nto have  %nrequestN(<%s>)  but got  %nrequestN(<%s>)",
                    actual,
                    n,
                    requestN
                )
            )
        }
        return this
    }

    private fun assertValid() {
        try {
            header
        } catch (t: Throwable) {
            throw failures.failure(info, ShouldBe.shouldBe(actual, Condition("a valid frame, but got exception [$t]")))
        }
    }
}