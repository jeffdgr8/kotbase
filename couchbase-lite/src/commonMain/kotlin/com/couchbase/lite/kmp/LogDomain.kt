package com.couchbase.lite.kmp

/**
 * Log domain
 */
public expect enum class LogDomain {
    DATABASE,
    QUERY,
    REPLICATOR,
    NETWORK,
    LISTENER;

    public companion object {
        public val ALL_DOMAINS: Set<LogDomain>
    }
}
