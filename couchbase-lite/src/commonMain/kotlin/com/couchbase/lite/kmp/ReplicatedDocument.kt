@file:Suppress("NO_ACTUAL_FOR_EXPECT") // https://youtrack.jetbrains.com/issue/KT-42466

package com.couchbase.lite.kmp

public expect class ReplicatedDocument {

    /**
     * The current document id.
     */
    public val id: String

    /**
     * The current status flag of the document. eg. deleted, access removed
     */
    public val flags: Set<DocumentFlag>

    /**
     * The current document replication error.
     */
    public val error: CouchbaseLiteException?
}
