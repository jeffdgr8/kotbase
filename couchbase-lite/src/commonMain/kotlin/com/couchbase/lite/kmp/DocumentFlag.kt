@file:Suppress("NO_ACTUAL_FOR_EXPECT") // https://youtrack.jetbrains.com/issue/KT-42466

package com.couchbase.lite.kmp

/**
 * The flags enum describing the replicated document.
 */
public expect enum class DocumentFlag {

    /**
     * The current deleted status of the document.
     */
    DELETED,

    /**
     * The current access removed status of the document.
     */
    ACCESS_REMOVED
}
