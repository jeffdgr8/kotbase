package com.couchbase.lite.kmp

/**
 * Interface delegate that takes Document input parameter and bool output parameter
 * Document push and pull will be allowed if output is true, otherwise, Document
 * push and pull will not be allowed.
 **/
public typealias ReplicationFilter = (document: Document, flags: Set<DocumentFlag>) -> Boolean
