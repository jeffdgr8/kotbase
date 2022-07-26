package com.couchbase.lite.kmm

/**
 * Log domain
 */
@Suppress("NO_ACTUAL_FOR_EXPECT")
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
