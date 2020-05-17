package io.rsocket.tck.frame

import io.netty.buffer.*
import io.rsocket.frame.shared.*

data class LeaseFrame(
    override val header: FrameHeader<LeaseFlags>,
    val ttl: Int,
    val numberOfRequests: Int,
    val metadata: ByteBuf?
) : Frame<LeaseFlags>(FrameType.LEASE) {
    override fun buffer(allocator: ByteBufAllocator): ByteBuf {
        val header = headerBuffer(allocator) {
            writeInt(ttl)
            writeInt(numberOfRequests)
        }
        return when (metadata) {
            null -> header
            else -> allocator.compose(header, metadata)
        }
    }
}

fun RawFrame.asLease(): LeaseFrame = typed(FrameType.LEASE) {
    val flags = LeaseFlags(metadata = header.flags.value check CommonFlag.Metadata)
    val ttl = readInt()
    val numberOfRequests = readInt()
    val metadata = if (flags.metadata) slice() else null
    LeaseFrame(
        header = header.withFlags(flags),
        ttl = ttl,
        numberOfRequests = numberOfRequests,
        metadata = metadata
    )
}

data class LeaseFlags(
    val metadata: Boolean
) : TypedFlags({
    CommonFlag.Metadata setIf metadata
})