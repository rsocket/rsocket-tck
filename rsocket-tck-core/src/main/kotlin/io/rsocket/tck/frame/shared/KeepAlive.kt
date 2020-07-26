package io.rsocket.tck.frame.shared

import io.netty.buffer.ByteBuf
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.milliseconds
import kotlin.time.toDuration

@Suppress("FunctionName")
fun KeepAlive(interval: Int, maxLifetime: Int): KeepAlive =
    KeepAlive(interval.toDuration(TimeUnit.MILLISECONDS), maxLifetime.toDuration(TimeUnit.MILLISECONDS))

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
