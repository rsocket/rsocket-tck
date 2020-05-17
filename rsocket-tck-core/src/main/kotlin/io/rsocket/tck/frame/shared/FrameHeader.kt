package io.rsocket.tck.frame.shared

import io.rsocket.tck.frame.*

data class FrameHeader<F : Flags>(
    val streamId: Int,
    val flags: F
) {
    companion object {
        const val SIZE: Int = Int.SIZE_BYTES + Short.SIZE_BYTES
        const val FLAGS_MASK: Int = 1023
        const val TYPE_SHIFT: Int = 16 - FrameType.SIZE
    }

    fun <NF : Flags> withFlags(flags: NF): FrameHeader<NF> = FrameHeader(streamId, flags)
}
