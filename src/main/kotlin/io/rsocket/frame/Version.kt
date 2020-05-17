package io.rsocket.tck.frame

@Suppress("FunctionName")
fun Version(major: Int, minor: Int): Version = Version((major shl 16) or (minor and 0xFFFF))

inline class Version(val value: Int) {
    val major: Int get() = value shr 16 and 0xFFFF
    val minor: Int get() = value and 0xFFFF
    override fun toString(): String = "$major.$minor"

    companion object {
        val CURRENT: Version = Version(1, 0)
    }
}
