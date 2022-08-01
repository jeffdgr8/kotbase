package com.couchbase.lite.kmm

public typealias ConflictHandler = (document: MutableDocument, oldDocument: Document?) -> Boolean
