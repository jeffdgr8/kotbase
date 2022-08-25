package com.couchbase.lite.kmp

public actual enum class LogDomain {
    DATABASE,
    QUERY,
    REPLICATOR,
    NETWORK,
    LISTENER;

    public actual companion object {

        public actual val ALL_DOMAINS: Set<LogDomain> = values().toSet()

        internal fun from(logDomain: com.couchbase.lite.LogDomain): LogDomain {
            return when (logDomain) {
                com.couchbase.lite.LogDomain.DATABASE -> DATABASE
                com.couchbase.lite.LogDomain.QUERY -> QUERY
                com.couchbase.lite.LogDomain.REPLICATOR -> REPLICATOR
                com.couchbase.lite.LogDomain.NETWORK -> NETWORK
                com.couchbase.lite.LogDomain.LISTENER -> LISTENER
            }
        }
    }

    internal val actual: com.couchbase.lite.LogDomain
        get() = when (this) {
            DATABASE -> com.couchbase.lite.LogDomain.DATABASE
            QUERY -> com.couchbase.lite.LogDomain.QUERY
            REPLICATOR -> com.couchbase.lite.LogDomain.REPLICATOR
            NETWORK -> com.couchbase.lite.LogDomain.NETWORK
            LISTENER -> com.couchbase.lite.LogDomain.LISTENER
        }
}
