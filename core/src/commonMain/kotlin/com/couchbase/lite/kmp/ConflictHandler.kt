package com.couchbase.lite.kmp

public typealias ConflictHandler = (document: MutableDocument, oldDocument: Document?) -> Boolean
