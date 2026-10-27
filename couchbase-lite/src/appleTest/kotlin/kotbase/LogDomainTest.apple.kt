/*
 * Copyright 2026 Jeff Lockhart
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
package kotbase

import swiftPMImport.CouchbaseLite.kCBLLogDomainMDNS
import swiftPMImport.CouchbaseLite.kCBLLogDomainPeerDiscovery
import swiftPMImport.CouchbaseLite.CBLLogLevel.kCBLLogLevelInfo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class LogDomainTest {

    @Test
    fun testMDNSDomainIsPeerDiscovery() {
        assertEquals(LogDomain.PEER_DISCOVERY, LogDomain.from(kCBLLogDomainMDNS))
        assertEquals(setOf(LogDomain.PEER_DISCOVERY), kCBLLogDomainMDNS.toLogDomain())
    }

    @Test
    fun testPeerDiscoveryIncludesMDNS() {
        val domains = setOf(LogDomain.PEER_DISCOVERY).toCBLLogDomain()
        assertNotEquals(0UL, domains and kCBLLogDomainPeerDiscovery)
        assertNotEquals(0UL, domains and kCBLLogDomainMDNS)
    }

    // Couchbase Lite calls the logger with mDNS logs during peer discovery,
    // throwing from this callback crashes the app
    @Suppress("DEPRECATION")
    @Test
    fun testLoggerReceivesMDNSLogs() {
        val domains = mutableListOf<LogDomain>()
        val logger = object : Logger {
            override val level = LogLevel.DEBUG
            override fun log(level: LogLevel, domain: LogDomain, message: String) {
                domains.add(domain)
            }
        }
        logger.convert().logWithLevel(kCBLLogLevelInfo, kCBLLogDomainMDNS, "mDNS")
        assertEquals(listOf(LogDomain.PEER_DISCOVERY), domains)
    }

    @Test
    fun testUnknownDomainDoesNotThrow() {
        assertEquals(LogDomain.DATABASE, LogDomain.from(1UL shl 8))
    }
}
