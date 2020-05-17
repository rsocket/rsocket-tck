package io.rsocket.tck.frame

import io.netty.buffer.*
import io.rsocket.frame.shared.*
import kotlin.time.*

data class SetupFrame(
    override val header: FrameHeader<SetupFlags>,
    val version: Version,
    val keepAlive: KeepAlive,
    val resumeToken: ResumeToken?,
    val metadataMimeType: MimeType,
    val dataMimeType: MimeType,
    val payload: Payload
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
            writeByte(metadataMimeType.length.toInt())
            writeUtf8(metadataMimeType.text)
            writeByte(dataMimeType.length.toInt())
            writeUtf8(dataMimeType.text)
            payload.metadata?.length?.let(this::writeLength)
        }
        return allocator.compose(header, payload)
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
