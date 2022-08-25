@file:Suppress("NO_ACTUAL_FOR_EXPECT") // https://youtrack.jetbrains.com/issue/KT-42466

package com.couchbase.lite.kmp

/**
 * Provides details about a Document change.
 */
public expect class DocumentChange {

    /**
     * The Database instance
     */
    public val database: Database

    /**
     * The changed document ID
     */
    public val documentID: String
}
