package com.couchbase.lite.kmp

public actual enum class ReplicatorType {
    PUSH_AND_PULL,
    PUSH,
    PULL;

    internal val actual: com.couchbase.lite.ReplicatorType
        get() = when (this) {
            PUSH_AND_PULL -> com.couchbase.lite.ReplicatorType.PUSH_AND_PULL
            PUSH -> com.couchbase.lite.ReplicatorType.PUSH
            PULL -> com.couchbase.lite.ReplicatorType.PULL
        }

    internal companion object {

        internal fun from(replicatorType: com.couchbase.lite.ReplicatorType): ReplicatorType {
            return when (replicatorType) {
                com.couchbase.lite.ReplicatorType.PUSH_AND_PULL -> PUSH_AND_PULL
                com.couchbase.lite.ReplicatorType.PUSH -> PUSH
                com.couchbase.lite.ReplicatorType.PULL -> PULL
            }
        }
    }
}
