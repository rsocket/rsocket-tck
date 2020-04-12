package io.rsocket.frame.shared

import kotlin.math.*

interface Flag {
    val position: Int

    val raw: Int get() = 2.0.pow(9 - position).toInt()
}

infix fun Int.check(flag: Int): Boolean = this and flag == flag
infix fun Int.check(flag: Flag): Boolean = check(flag.raw)

enum class CommonFlag(override val position: Int) : Flag {
    /** (I)gnore flag: a value of 0 indicates the protocol can't ignore this frame */
    Ignore(0),

    /** (M)etadata flag: a value of 1 indicates the frame contains metadata */
    Metadata(1),

    /** (F)ollows: More fragments follow this fragment (in case of fragmented REQUEST_x or PAYLOAD frames) */
    Follows(2),

    /** (C)omplete: bit to indicate stream completion */
    Complete(3),

    /** (N)ext: bit to indicate payload or metadata present */
    Next(4)
}
