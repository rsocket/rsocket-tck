package io.rsocket.frame

import io.netty.buffer.*
import io.rsocket.frame.shared.*

class ExtensionFrame(
    override val header: FrameHeader<ExtensionFrameFlags>,
    val extendedType: Int, //TODO replace by some typed value ?
    val data: ByteBuf? //TODO check if may be use Payload
) : Frame<ExtensionFrameFlags>(FrameType.EXT) {
    override fun buffer(allocator: ByteBufAllocator): ByteBuf {
        val header = headerBuffer(allocator) {
            writeInt(extendedType)
        }
        return when (data) {
            null -> header
            else -> allocator.compose(header, data)
        }
    }
}

fun RawFrame.asExtension(): ExtensionFrame = typed(FrameType.EXT) {
    val untypedFlags = header.flags.value
    val flags = ExtensionFrameFlags(
        ignore = untypedFlags check CommonFlag.Ignore,
        metadata = untypedFlags check CommonFlag.Metadata
    )
    val extendedType = readInt()
    val data = slice()
    ExtensionFrame(
        header = header.withFlags(flags),
        extendedType = extendedType,
        data = data
    )
}

data class ExtensionFrameFlags(
    val ignore: Boolean,
    val metadata: Boolean
) : TypedFlags({
    CommonFlag.Ignore setIf ignore
    CommonFlag.Metadata setIf metadata
})
