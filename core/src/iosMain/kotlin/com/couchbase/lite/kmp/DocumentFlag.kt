package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.CBLDocumentFlags
import cocoapods.CouchbaseLite.kCBLDocumentFlagsAccessRemoved
import cocoapods.CouchbaseLite.kCBLDocumentFlagsDeleted

public actual enum class DocumentFlag {
    DELETED,
    ACCESS_REMOVED
}

internal fun CBLDocumentFlags.toDocumentFlags(): Set<DocumentFlag> = buildSet {
    val flags = this@toDocumentFlags
    if (flags and kCBLDocumentFlagsDeleted != 0UL) {
        add(DocumentFlag.DELETED)
    }
    if (flags and kCBLDocumentFlagsAccessRemoved != 0UL) {
        add(DocumentFlag.ACCESS_REMOVED)
    }
}

internal fun Set<DocumentFlag>.toCBLDocumentFlags(): CBLDocumentFlags {
    var flags = 0UL
    if (contains(DocumentFlag.DELETED)) {
        flags = flags or kCBLDocumentFlagsDeleted
    }
    if (contains(DocumentFlag.ACCESS_REMOVED)) {
        flags = flags or kCBLDocumentFlagsAccessRemoved
    }
    return flags
}
