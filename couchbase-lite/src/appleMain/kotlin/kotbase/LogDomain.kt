package kotbase

import cocoapods.CouchbaseLite.*

public actual enum class LogDomain {
    DATABASE,
    QUERY,
    REPLICATOR,
    NETWORK,
    LISTENER;

    public val actual: CBLLogDomain
        get() = when (this) {
            DATABASE -> kCBLLogDomainDatabase
            QUERY -> kCBLLogDomainQuery
            REPLICATOR -> kCBLLogDomainReplicator
            NETWORK -> kCBLLogDomainNetwork
            LISTENER -> kCBLLogDomainListener
        }

    public actual companion object {

        public actual val ALL_DOMAINS: Set<LogDomain> =
            values().toSet()

        internal fun from(logDomain: CBLLogDomain): LogDomain {
            return when (logDomain) {
                kCBLLogDomainDatabase -> DATABASE
                kCBLLogDomainQuery -> QUERY
                kCBLLogDomainReplicator -> REPLICATOR
                kCBLLogDomainNetwork -> NETWORK
                kCBLLogDomainListener -> LISTENER
                else -> error("Unexpected CBLLogDomain")
            }
        }
    }
}

internal fun CBLLogDomain.toLogDomain(): Set<LogDomain> = buildSet {
    val domains = this@toLogDomain
    if (domains and kCBLLogDomainDatabase != 0UL) {
        add(LogDomain.DATABASE)
    }
    if (domains and kCBLLogDomainQuery != 0UL) {
        add(LogDomain.QUERY)
    }
    if (domains and kCBLLogDomainReplicator != 0UL) {
        add(LogDomain.REPLICATOR)
    }
    if (domains and kCBLLogDomainNetwork != 0UL) {
        add(LogDomain.NETWORK)
    }
    if (domains and kCBLLogDomainListener != 0UL) {
        add(LogDomain.LISTENER)
    }
}

internal fun Set<LogDomain>.toCBLLogDomain(): CBLLogDomain {
    var domains = 0UL
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
    return domains
}

// This constant is enterprise only in the Objective-C SDK, but not Java
// https://github.com/couchbase/couchbase-lite-ios/blob/master/Objective-C/CBLLogger.h#L32
private val kCBLLogDomainListener = 1UL shl 4
