@file:Suppress("NO_ACTUAL_FOR_EXPECT") // https://youtrack.jetbrains.com/issue/KT-42466

package com.couchbase.lite.kmp

/**
 * ConcurrencyControl type used when saving or deleting a document.
 */
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
