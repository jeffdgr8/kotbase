package com.couchbase.lite.kmm

/**
 * Custom conflict resolution strategies implement this interface.
 */
public fun interface ConflictResolver {

    /**
     * Callback: called when there are conflicting changes in the local
     * and remote versions of a document during replication.
     *
     * @param conflict Description of the conflicting documents.
     * @return the resolved doc.
     */
    public fun resolve(conflict: Conflict): Document
}