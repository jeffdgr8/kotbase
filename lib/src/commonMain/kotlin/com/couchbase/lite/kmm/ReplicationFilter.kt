package com.couchbase.lite.kmm

/**
 * Interface delegate that takes Document input parameter and bool output parameter
 * Document push and pull will be allowed if output is true, otherwise, Document
 * push and pull will not be allowed.
 **/
public fun interface ReplicationFilter {

    public fun filtered(document: Document, flags: Set<DocumentFlag>): Boolean
}
