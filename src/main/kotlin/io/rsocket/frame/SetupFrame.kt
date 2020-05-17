package io.rsocket.tck.frame

import io.netty.buffer.*

inline class SetupFrame(val buffer: ByteBuf) {
    val header: FrameHeader get() = FrameHeader(buffer, FrameType.SETUP)
    private val reader: DataAndMetadataReader get() = DataAndMetadataReader(buffer)

    val honorLease: Boolean get() = header.flags.checkFlag(Flags.HONOR_LEASE)
    val resumeEnabled: Boolean get() = header.flags.checkFlag(Flags.RESUME_ENABLE)

    val version: Version
        get() {
            header //TODO
            return buffer.preview {
                skipBytes(FrameHeader.SIZE)
                Version(readInt())
            }
        }

    val isSupportedVersion: Boolean get() = version == Version.CURRENT

    val keepAliveInterval: Int
        get() = buffer.preview {
            skipBytes(FrameHeader.SIZE + Size.VERSION)
            readInt()
        }

    val keepAliveMaxLifetime: Int
        get() = buffer.preview {
            skipBytes(FrameHeader.SIZE + Size.VERSION + Size.KEEPALIVE_INTERVAL)
            readInt()
        }

    val resumeTokenLength: Int
        get() = buffer.preview {
            skipBytes(FrameHeader.SIZE + Size.VERSION + Size.KEEPALIVE_INTERVAL + Size.KEEPALIVE_MAX_LIFETIME)
            readTokenLength()
        }

    val resumeToken: ByteBuf?
        get() = when (resumeEnabled) {
            false -> null
            true  -> buffer.preview {
                skipBytes(FrameHeader.SIZE + Size.VERSION + Size.KEEPALIVE_INTERVAL + Size.KEEPALIVE_MAX_LIFETIME)
                readSlice(readTokenLength())
            }
        }

    val metadataMimeType: String
        get() = buffer.preview {
            skipToMimeType()
            readSlice(readByte().toInt()).toString(Charsets.UTF_8)
        }

    val dataMimeType: String
        get() = buffer.preview {
            skipToMimeType()
            skipBytes(readByte().toInt()) //skip metadata mime type
            readSlice(readByte().toInt()).toString(Charsets.UTF_8)
        }

    val metadata: ByteBuf
        get() {
            val hasMetadata = header.hasMetadata
            return buffer.preview {
                skipToPayload()
                reader.metadata(hasMetadata)
            }
        }

    val data: ByteBuf
        get() {
            val hasMetadata = header.hasMetadata
            return buffer.preview {
                skipToPayload()
                reader.data(hasMetadata)
            }
        }

    private fun skipToMimeType() {
        if (resumeEnabled) buffer.skipBytes(Size.RESUME_TOKEN_LENGTH + resumeTokenLength)
        buffer.skipBytes(FrameHeader.SIZE + Size.VERSION + Size.KEEPALIVE_INTERVAL + Size.KEEPALIVE_MAX_LIFETIME)
    }

    private fun skipToPayload() {
        skipToMimeType()
        buffer.skipBytes(buffer.readByte().toInt()) //metadataMimeType
        buffer.skipBytes(buffer.readByte().toInt()) //dataMimeType
    }

    private fun readTokenLength(): Int = buffer.readShort().toInt() and 0xFFFF

    object Flags {

        /** A flag used to indicate that the client will honor LEASE sent by the server  */
        const val HONOR_LEASE = 64

        /**
         * A flag used to indicate that the client requires connection resumption, if possible (the frame
         * contains a Resume Identification Token)
         */
        const val RESUME_ENABLE = 128

    }

    private object Size {
        const val VERSION: Int = Int.SIZE_BYTES
        const val KEEPALIVE_INTERVAL: Int = Int.SIZE_BYTES
        const val KEEPALIVE_MAX_LIFETIME: Int = Int.SIZE_BYTES
        const val RESUME_TOKEN_LENGTH: Int = Short.SIZE_BYTES
    }

    companion object {

        fun encode(
            allocator: ByteBufAllocator,
            lease: Boolean,
            keepaliveInterval: Int,
            maxLifetime: Int,
            metadataMimeType: String,
            dataMimeType: String,
            data: ByteBuf,
            metadata: ByteBuf?,
            resumeToken: ByteBuf? = null
        ): SetupFrame {

            @Suppress("NAME_SHADOWING")
            val resumeToken = resumeToken?.takeIf { it.readableBytes() > 0 }

            var flags = 0
            if (resumeToken != null) flags = flags or SetupFrame.Flags.RESUME_ENABLE
            if (lease) flags = flags or SetupFrame.Flags.HONOR_LEASE
            if (metadata != null) flags = flags or FrameHeader.Flags.M

            val header = FrameHeader.encode(allocator, FrameType.SETUP, flags) {
                writeInt(Version.CURRENT.value)
                writeInt(keepaliveInterval)
                writeInt(maxLifetime)

                resumeToken?.let {
                    it.markReaderIndex()
                    writeShort(it.readableBytes())
                    writeBytes(it)
                    it.resetReaderIndex()
                }

                // Write metadata mime-type
                writeByte(ByteBufUtil.utf8Bytes(metadataMimeType))
                ByteBufUtil.writeUtf8(this, metadataMimeType)


                // Write data mime-type
                writeByte(ByteBufUtil.utf8Bytes(dataMimeType))
                ByteBufUtil.writeUtf8(this, dataMimeType)
            }.buffer

            return SetupFrame(
                when (metadata) {
                    null -> DataAndMetadata.encodeOnlyData(allocator, header, data)
                    else -> DataAndMetadata.encode(allocator, header, data, metadata)
                }.buffer
            )
        }

    }
}