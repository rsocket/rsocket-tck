package io.rsocket.tck.frame

import io.netty.buffer.*
import io.rsocket.frame.shared.*

data class MetadataPushFrame(
    override val header: FrameHeader<MetadataPushFlags>,
    val metadata: ByteBuf
) : Frame<MetadataPushFlags>(FrameType.METADATA_PUSH) {
    override fun buffer(allocator: ByteBufAllocator): ByteBuf {
        val header = headerBuffer(allocator)
        return allocator.compose(header, metadata)
    }
}

fun RawFrame.asMetadataPush(): MetadataPushFrame = typed(FrameType.METADATA_PUSH) {
    val flags = MetadataPushFlags(metadata = header.flags.value check CommonFlag.Metadata)
    val metadata = slice()
    MetadataPushFrame(
        header = header.withFlags(flags),
        metadata = metadata
    )
}

data class MetadataPushFlags(
    val metadata: Boolean
) : TypedFlags({
    CommonFlag.Metadata setIf metadata
})
