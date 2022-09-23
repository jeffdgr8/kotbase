package com.couchbase.lite.kmp

import kotlinx.cinterop.convert
import libcblite.*

public actual enum class LogDomain {
    DATABASE,
    QUERY,
    REPLICATOR,
    NETWORK,
    LISTENER;

    public actual companion object {

        public actual val ALL_DOMAINS: Set<LogDomain> =
            values().toSet()

        internal fun from(logDomain: CBLLogDomain): LogDomain {
            return when (logDomain.toUInt()) {
                kCBLLogDomainDatabase -> DATABASE
                kCBLLogDomainQuery -> QUERY
                kCBLLogDomainReplicator -> REPLICATOR
                kCBLLogDomainNetwork -> NETWORK
                kCBLLogDomainListener -> LISTENER
                else -> error("Unexpected CBLLogDomain")
            }
        }
    }

    internal val actual: CBLLogDomain
        get() = when (this) {
            DATABASE -> kCBLLogDomainDatabase
            QUERY -> kCBLLogDomainQuery
            REPLICATOR -> kCBLLogDomainReplicator
            NETWORK -> kCBLLogDomainNetwork
            LISTENER -> kCBLLogDomainListener
        }.convert()
}

// This constant is enterprise only in the C SDK, but not Java
// https://github.com/couchbase/couchbase-lite-ios/blob/master/Objective-C/CBLLogger.h#L32
private val kCBLLogDomainListener = 1U shl 4
