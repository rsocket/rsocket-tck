/*
 * Copyright 2015-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.rsocket.frame

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

class ResumeFrameFlyweightTest {
    @Test
    fun testEncoding() {
        val tokenBytes = ByteArray(65000) { 1 }
        val token = bufferOf(*tokenBytes)
        val resumeFrame = ResumeFrame.encode(allocator, token, 21, 12)
        assertEquals(Version.CURRENT.value, resumeFrame.version)
        assertEquals(token, resumeFrame.token)
        assertEquals(21, resumeFrame.lastReceivedServerPos)
        assertEquals(12, resumeFrame.firstAvailableClientPos)
    }
}
