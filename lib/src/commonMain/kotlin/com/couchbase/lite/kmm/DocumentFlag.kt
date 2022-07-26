package com.couchbase.lite.kmm

/**
 * The flags enum describing the replicated document.
 */
@Suppress("NO_ACTUAL_FOR_EXPECT")
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
