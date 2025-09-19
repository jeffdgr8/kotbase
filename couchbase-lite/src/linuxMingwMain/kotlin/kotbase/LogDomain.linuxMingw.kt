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

        public actual val ALL: Set<LogDomain> = entries.toSet()

        @Deprecated(
            "Use LogDomain.ALL",
            ReplaceWith("LogDomain.ALL")
        )
        public actual val ALL_DOMAINS: Set<LogDomain>
            get() = ALL

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

internal fun UShort.toLogDomain(): Set<LogDomain> = buildSet {
    val domains = this@toLogDomain.toUInt()
    if (domains and kCBLLogDomainDatabase != 0U) {
        add(LogDomain.DATABASE)
    }
    if (domains and kCBLLogDomainQuery != 0U) {
        add(LogDomain.QUERY)
    }
    if (domains and kCBLLogDomainReplicator != 0U) {
        add(LogDomain.REPLICATOR)
    }
    if (domains and kCBLLogDomainNetwork != 0U) {
        add(LogDomain.NETWORK)
    }
    if (domains and kCBLLogDomainListener != 0U) {
        add(LogDomain.LISTENER)
    }
}

internal fun Set<LogDomain>.toCBLLogDomain(): UShort {
    var domains = 0U
    if (contains(LogDomain.DATABASE)) {
        domains = domains or kCBLLogDomainDatabase
    }
    if (contains(LogDomain.QUERY)) {
        domains = domains or kCBLLogDomainQuery
    }
    if (contains(LogDomain.REPLICATOR)) {
        domains = domains or kCBLLogDomainReplicator
    }
    if (contains(LogDomain.NETWORK)) {
        domains = domains or kCBLLogDomainNetwork
    }
    if (contains(LogDomain.LISTENER)) {
        domains = domains or kCBLLogDomainListener
    }
    return domains.toUShort()
}

// This constant is enterprise only in the C SDK, but not Java
// https://github.com/couchbase/couchbase-lite-ios/blob/master/Objective-C/CBLLogger.h#L32
private val kCBLLogDomainListener = 1U shl 4
