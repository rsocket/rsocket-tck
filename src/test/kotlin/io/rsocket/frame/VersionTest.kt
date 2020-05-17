package io.rsocket.tck.frame

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

class VersionTest {
    @Test
    fun simple() {
        val version = Version(1, 0)
        assertEquals(1, version.major)
        assertEquals(0, version.minor)
        assertEquals(0x00010000, version.value)
        assertEquals("1.0", version.toString())
    }

    @Test
    fun complex() {
        val version = Version(0x1234, 0x5678)
        assertEquals(0x1234, version.major)
        assertEquals(0x5678, version.minor)
        assertEquals(0x12345678, version.value)
        assertEquals("4660.22136", version.toString())
    }

    @Test
    fun noShortOverflow() {
        val version = Version(43210, 43211)
        assertEquals(43210, version.major)
        assertEquals(43211, version.minor)
    }
}
