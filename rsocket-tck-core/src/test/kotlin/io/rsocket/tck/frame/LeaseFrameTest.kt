package io.rsocket.tck.frame

import io.rsocket.expect.frame.*
import io.rsocket.frame.shared.*
import org.junit.jupiter.api.*
import strikt.api.*
import strikt.assertions.*

class LeaseFrameTest {
    @Test
    fun leaseMetadata() {
        val lease = LeaseFrame(
            FrameHeader(0, LeaseFlags(true)),
            1,
            42,
            TextBuffer("md")
        )
        val newLease = lease.buffer(allocator).frame().asLease()

        lease expect newLease

        expectThat(newLease) {
            get { metadata?.text() }.isEqualTo("md")
        }
    }

    @Test
    fun leaseAbsentMetadata() {
        val lease = LeaseFrame(
            FrameHeader(0, LeaseFlags(false)),
            1,
            42,
            null
        )

        val newLease = lease.buffer(allocator).frame().asLease()

        lease expect newLease

        expectThat(newLease) {
            get { ttl }.isEqualTo(1)
            get { numberOfRequests }.isEqualTo(42)
            get { metadata }.isEqualTo(null)
        }
    }

}
