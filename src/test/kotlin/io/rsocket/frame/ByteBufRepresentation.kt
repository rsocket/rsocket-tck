package io.rsocket.frame

import io.netty.buffer.*
import org.assertj.core.presentation.*

class ByteBufRepresentation : StandardRepresentation() {
    override fun fallbackToStringOf(`object`: Any): String =
        if (`object` is ByteBuf) ByteBufUtil.prettyHexDump(`object`) else super.fallbackToStringOf(`object`)
}
