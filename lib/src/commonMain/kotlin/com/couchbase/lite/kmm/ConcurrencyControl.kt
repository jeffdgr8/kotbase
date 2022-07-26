package com.couchbase.lite.kmm

/**
 * ConcurrencyControl type used when saving or deleting a document.
 */
@Suppress("NO_ACTUAL_FOR_EXPECT")
public expect enum class ConcurrencyControl {

    /**
     * The last write operation will win if there is a conflict.
     */
    LAST_WRITE_WINS,

    /**
     * The operation will fail if there is a conflict.
     */
    FAIL_ON_CONFLICT
}
