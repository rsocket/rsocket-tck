package io.rsocket.spec

import io.netty.buffer.*
import io.rsocket.frame.*
import io.rsocket.frame.shared.*
import kotlinx.coroutines.*
import kotlin.time.*

object MockErrorContext : LowLevelCaseContext {
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

object MockDelayContext : LowLevelCaseContext {
    override suspend fun send(buffer: ByteBuf) {

    }

    override suspend fun receive(): ByteBuf {
        delay(5.seconds)
        error("")
    }
}
