package io.rsocket.spec

import io.netty.buffer.*

// implement as transport with rsocket client/server
interface Transport {
    suspend fun send(buffer: ByteBuf)
    suspend fun receive(): ByteBuf
}