package io.rsocket.tck.frame

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.rsocket.tck.frame.shared.*
import kotlin.time.DurationUnit

fun create(
    header: FrameHeader<SetupFlags>,
    version: Version,
    keepAlive: KeepAlive,
    dataMimeType: MimeType
): SetupFrame = SetupFrame(header, version, keepAlive, null, null, dataMimeType, null)

data class SetupFrame(
    override val header: FrameHeader<SetupFlags>,
    val version: Version,
    val keepAlive: KeepAlive,
    val resumeToken: ResumeToken? = null,
    val metadataMimeType: MimeType? = null,
    val dataMimeType: MimeType,
    val payload: Payload? = null
) : Frame<SetupFlags>(FrameType.SETUP) {

    override fun buffer(allocator: ByteBufAllocator): ByteBuf {
        val header = headerBuffer(allocator) {
            writeInt(version.value)
            writeInt(keepAlive.interval.toInt(DurationUnit.MILLISECONDS))
            writeInt(keepAlive.maxLifetime.toInt(DurationUnit.MILLISECONDS))
            resumeToken?.let {
                writeShort(it.length.toInt())
                it.token.preview { this@headerBuffer.writeBytes(this) }
            }
            if (metadataMimeType != null) {
                writeByte(metadataMimeType.length.toInt())
                writeUtf8(metadataMimeType.text)
            }
            writeByte(dataMimeType.length.toInt())
            writeUtf8(dataMimeType.text)

            payload?.metadata?.length?.let(this::writeLength)
        }
        return allocator.compose(header, payload)
    }

    companion object {
        fun decode(buffer: ByteBuf): SetupFrame {
            return buffer.frame().asSetup()
        }
    }
}

fun RawFrame.asSetup(): SetupFrame = typed(FrameType.SETUP) {
    val untypedFlags = header.flags.value
    val flags = SetupFlags(
        resume = untypedFlags check SetupFlag.ResumeEnable,
        lease = untypedFlags check SetupFlag.HonorLease,
        metadata = untypedFlags check CommonFlag.Metadata
    )

    val version = readVersion()
    val keepAlive = readKeepAlive()

    val resumeToken = if (flags.resume) readResumeToken() else null

    val metadataMimeType = readMimeType()
    val dataMimeType = readMimeType()

    val payload = readPayload(flags.metadata)

    SetupFrame(
        header = header.withFlags(flags),
        version = version,
        keepAlive = keepAlive,
        resumeToken = resumeToken,
        metadataMimeType = metadataMimeType,
        dataMimeType = dataMimeType,
        payload = payload
    )
}

enum class SetupFlag(override val position: Int) : Flag {
    /**
     * A flag used to indicate that the client requires connection resumption,
     * if possible (the frame contains a Resume Identification Token)
     */
    ResumeEnable(2),

    /** A flag used to indicate that the client will honor LEASE sent by the server  */
    HonorLease(3)
}

data class SetupFlags(
    val resume: Boolean,
    val lease: Boolean,
    val metadata: Boolean
) : TypedFlags({
    SetupFlag.ResumeEnable setIf resume
    SetupFlag.HonorLease setIf lease
    CommonFlag.Metadata setIf metadata
})
