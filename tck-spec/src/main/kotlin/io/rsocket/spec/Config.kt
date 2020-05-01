package io.rsocket.spec

import org.spekframework.spek2.dsl.*

//read from json/yaml
val config = Config(
    withLength = false,
    keys = listOf(
        "setup.reject",
        "setup.accept.streamId0"
    )
)

class Config(
    val withLength: Boolean,
    keys: List<String>
) {
    private val casesToRun = keys.flatMap(String::splitKeys)

    private fun check(key: String): Boolean {
        if (key in casesToRun) return true
        val keys = key.splitKeys()
        return casesToRun.any { it in keys }
    }

    fun skipIf(key: String): Skip = when (check(key)) {
        true  -> Skip.No
        false -> Skip.Yes("No key: $key")
    }
}

private fun String.splitKeys(): List<String> = split(".").fold(mutableListOf<String>()) { acc, nextKey ->
    acc += when (acc.isEmpty()) {
        true  -> nextKey
        false -> "${acc.last()}.$nextKey"
    }
    acc
}
