package io.rsocket.tck.frame

import io.netty.buffer.*

fun ByteBuf.prettyString(): String {
    val frameHeader = FrameHeader(this)
    val frameType = frameHeader.frameType
    val streamId = frameHeader.streamId
    val payload = StringBuilder()

    payload
        .append("\nFrame => Stream ID: ")
        .append(streamId)
        .append(" Type: ")
        .append(frameType)
        .append(" Flags: 0b")
        .append(Integer.toBinaryString(frameHeader.flags))
        .append(" Length: " + readableBytes())

    if (frameHeader.hasMetadata) {
        payload.append("\nMetadata:\n")

        ByteBufUtil.appendPrettyHexDump(payload, getMetadata(frameType))
    }

    payload.append("\nData:\n")
    ByteBufUtil.appendPrettyHexDump(payload, getData(frameType))

    return payload.toString()

}

private fun ByteBuf.getMetadata(frameType: FrameType): ByteBuf {
    val requestFrame = RequestFrame(this)
    return if (requestFrame.header.hasMetadata) when (frameType) {
        FrameType.REQUEST_STREAM,
        FrameType.REQUEST_CHANNEL  -> requestFrame.withInitial.metadata
        FrameType.REQUEST_FNF,
        FrameType.REQUEST_RESPONSE -> requestFrame.metadata
        // Payload and synthetic types
        FrameType.PAYLOAD,
        FrameType.NEXT,
        FrameType.NEXT_COMPLETE,
        FrameType.COMPLETE         -> requestFrame.metadata
        FrameType.METADATA_PUSH    -> MetadataPushFrame(this).metadata
        FrameType.SETUP            -> SetupFrame(this).metadata
        FrameType.LEASE            -> LeaseFrame(this).metadata
        else                       -> Unpooled.EMPTY_BUFFER
    } else Unpooled.EMPTY_BUFFER
}

private fun ByteBuf.getData(frameType: FrameType): ByteBuf {
    val requestFrame = RequestFrame(this)
    return when (frameType) {
        FrameType.REQUEST_STREAM,
        FrameType.REQUEST_CHANNEL  -> requestFrame.withInitial.data
        FrameType.REQUEST_FNF,
        FrameType.REQUEST_RESPONSE -> requestFrame.data
        // Payload and synthetic types
        FrameType.PAYLOAD,
        FrameType.NEXT,
        FrameType.NEXT_COMPLETE,
        FrameType.COMPLETE         -> requestFrame.data
        FrameType.SETUP            -> SetupFrame(this).data
        else                       -> Unpooled.EMPTY_BUFFER
    }
}
