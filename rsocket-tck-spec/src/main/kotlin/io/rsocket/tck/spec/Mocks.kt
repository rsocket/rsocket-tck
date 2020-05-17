package io.rsocket.tck.spec

import io.netty.buffer.*
import io.rsocket.tck.frame.*
import io.rsocket.tck.frame.shared.*
import kotlinx.coroutines.*
import kotlin.time.*

object MockErrorTransport : Transport {
    override suspend fun send(buffer: ByteBuf) {

    }

    override suspend fun receive(): ByteBuf = ErrorFrame(
        header = FrameHeader(
            streamId = 0,
            flags = UntypedFlags.Empty
        ),
        code = DefinedErrorCode.RejectedSetup,
        data = TextBuffer("failed?")
    ).buffer(ByteBufAllocator.DEFAULT)
}

object MockDelayTransport : Transport {
    override suspend fun send(buffer: ByteBuf) {

    }

    override suspend fun receive(): ByteBuf {
        delay(5.seconds)
        error("")
    }
}
