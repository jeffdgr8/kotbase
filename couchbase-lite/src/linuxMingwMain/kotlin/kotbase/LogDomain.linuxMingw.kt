/*
 * Copyright 2022-2023 Jeff Lockhart
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
import libcblite.*

public actual enum class LogDomain {
    DATABASE,
    QUERY,
    REPLICATOR,
    NETWORK,
    LISTENER;

    internal val actual: CBLLogDomain
        get() = when (this) {
            DATABASE -> kCBLLogDomainDatabase
            QUERY -> kCBLLogDomainQuery
            REPLICATOR -> kCBLLogDomainReplicator
            NETWORK -> kCBLLogDomainNetwork
            LISTENER -> kCBLLogDomainListener
        }.convert()

    public actual companion object {

        public actual val ALL_DOMAINS: Set<LogDomain> = entries.toSet()

        internal fun from(logDomain: CBLLogDomain): LogDomain = when (logDomain.toUInt()) {
            kCBLLogDomainDatabase -> DATABASE
            kCBLLogDomainQuery -> QUERY
            kCBLLogDomainReplicator -> REPLICATOR
            kCBLLogDomainNetwork -> NETWORK
            kCBLLogDomainListener -> LISTENER
            else -> error("Unexpected CBLLogDomain ($logDomain)")
        }
    }
}

// This constant is enterprise only in the C SDK, but not Java
// https://github.com/couchbase/couchbase-lite-ios/blob/master/Objective-C/CBLLogger.h#L32
private val kCBLLogDomainListener = 1U shl 4
