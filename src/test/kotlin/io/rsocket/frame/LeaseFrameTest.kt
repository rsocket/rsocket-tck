package io.rsocket.frame

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

class LeaseFrameTest {
    @Test
    fun leaseMetadata() {
        val metadata = TextBuffer("md")
        val ttl = 1
        val numRequests = 42
        val lease = LeaseFrame.encode(allocator, ttl, numRequests, metadata)
        assertTrue(lease.header.hasMetadata)
        assertEquals(ttl, lease.ttl)
        assertEquals(numRequests, lease.numRequests)
        assertEquals(metadata, lease.metadata)
    }

    @Test
    fun leaseAbsentMetadata() {
        val ttl = 1
        val numRequests = 42
        val lease = LeaseFrame.encode(allocator, ttl, numRequests, null)
        assertFalse(lease.header.hasMetadata)
        assertEquals(ttl, lease.ttl)
        assertEquals(numRequests, lease.numRequests)
        assertEquals(0, lease.metadata.readableBytes())
    }

}
