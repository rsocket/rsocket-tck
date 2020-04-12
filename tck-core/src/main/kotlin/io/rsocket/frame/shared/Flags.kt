package io.rsocket.frame.shared

interface Flags {
    val value: Int
}

data class UntypedFlags(override val value: Int) : Flags {
    companion object {
        val Empty = UntypedFlags(0)
    }
}

abstract class TypedFlags(configuration: TypedFlagsBuilder.() -> Unit = {}) : Flags {
    private val flags: Set<Flag> = TypedFlagsBuilder().apply(configuration).flags
    override val value: Int get() = flags.fold(0) { acc, flag -> acc or flag.raw }
}

class TypedFlagsBuilder {
    private val _flags: MutableSet<Flag> = mutableSetOf()
    val flags: Set<Flag> get() = _flags

    fun Flag.set(): Unit = setIf(true)
    infix fun Flag.setIf(bool: Boolean) {
        require(position in 0..9) { "Flag position must be in range 0..9, but was '$position'" }
        if (bool) _flags += this
    }
}
