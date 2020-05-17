package io.rsocket.tck.frame.shared

import io.netty.buffer.*

@Suppress("FunctionName")
fun Version(major: Int, minor: Int): Version = Version((major shl 16) or (minor and 0xFFFF))

data class Version(val value: Int) {
    val major: Int get() = value shr 16 and 0xFFFF
    val minor: Int get() = value and 0xFFFF
    override fun toString(): String = "$major.$minor"

    companion object {
        val Current: Version = Version(1, 0)
    }
}

fun ByteBuf.readVersion(): Version = Version(readInt())