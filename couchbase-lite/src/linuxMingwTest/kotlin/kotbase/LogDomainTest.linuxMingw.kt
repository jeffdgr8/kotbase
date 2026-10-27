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

import kotlinx.cinterop.convert
import libcblite.kCBLLogDomainNetwork
import libcblite.kCBLLogDomainReplicator
import kotlin.test.Test
import kotlin.test.assertEquals

class LogDomainTest {

    // The C SDK has no peer discovery or multipeer log domains,
    // logging with them must not throw
    @Test
    fun testAllDomainsMapToCDomain() {
        LogDomain.entries.forEach { it.actual }
        assertEquals(kCBLLogDomainNetwork.convert(), LogDomain.PEER_DISCOVERY.actual)
        assertEquals(kCBLLogDomainReplicator.convert(), LogDomain.MULTIPEER.actual)
    }
}
