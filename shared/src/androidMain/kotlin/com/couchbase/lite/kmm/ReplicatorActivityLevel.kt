package com.couchbase.lite.kmm

public actual enum class ReplicatorActivityLevel {
    STOPPED,
    OFFLINE,
    CONNECTING,
    IDLE,
    BUSY;

    internal val actual: com.couchbase.lite.ReplicatorActivityLevel
        get() = when (this) {
            STOPPED -> com.couchbase.lite.ReplicatorActivityLevel.STOPPED
            OFFLINE -> com.couchbase.lite.ReplicatorActivityLevel.OFFLINE
            CONNECTING -> com.couchbase.lite.ReplicatorActivityLevel.CONNECTING
            IDLE -> com.couchbase.lite.ReplicatorActivityLevel.IDLE
            BUSY -> com.couchbase.lite.ReplicatorActivityLevel.BUSY
        }

    internal companion object {

        internal fun from(activityLevel: com.couchbase.lite.ReplicatorActivityLevel): ReplicatorActivityLevel {
            return when (activityLevel) {
                com.couchbase.lite.ReplicatorActivityLevel.STOPPED -> STOPPED
                com.couchbase.lite.ReplicatorActivityLevel.OFFLINE -> OFFLINE
                com.couchbase.lite.ReplicatorActivityLevel.CONNECTING -> CONNECTING
                com.couchbase.lite.ReplicatorActivityLevel.IDLE -> IDLE
                com.couchbase.lite.ReplicatorActivityLevel.BUSY -> BUSY
            }
        }
    }
}
