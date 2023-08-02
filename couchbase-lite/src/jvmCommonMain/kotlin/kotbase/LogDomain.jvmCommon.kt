package kotbase

import com.couchbase.lite.LogDomain as CBLLogDomain

public actual enum class LogDomain {
    DATABASE,
    QUERY,
    REPLICATOR,
    NETWORK,
    LISTENER;

    public val actual: CBLLogDomain
        get() = when (this) {
            DATABASE -> CBLLogDomain.DATABASE
            QUERY -> CBLLogDomain.QUERY
            REPLICATOR -> CBLLogDomain.REPLICATOR
            NETWORK -> CBLLogDomain.NETWORK
            LISTENER -> CBLLogDomain.LISTENER
        }

    public actual companion object {

        public actual val ALL_DOMAINS: Set<LogDomain> = values().toSet()

        internal fun from(logDomain: CBLLogDomain): LogDomain = when (logDomain) {
            CBLLogDomain.DATABASE -> DATABASE
            CBLLogDomain.QUERY -> QUERY
            CBLLogDomain.REPLICATOR -> REPLICATOR
            CBLLogDomain.NETWORK -> NETWORK
            CBLLogDomain.LISTENER -> LISTENER
        }
    }
}
