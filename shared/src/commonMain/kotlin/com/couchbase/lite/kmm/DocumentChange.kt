package com.couchbase.lite.kmm

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
