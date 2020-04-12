package io.rsocket.frame.shared

import io.netty.buffer.*
import kotlin.time.*

data class KeepAlive(
    val interval: Duration,
    val maxLifetime: Duration
)

fun ByteBuf.readKeepAlive(): KeepAlive {
    val interval = readInt().milliseconds
    val maxLifetime = readInt().milliseconds
    return KeepAlive(
        interval = interval,
        maxLifetime = maxLifetime
    )
}
