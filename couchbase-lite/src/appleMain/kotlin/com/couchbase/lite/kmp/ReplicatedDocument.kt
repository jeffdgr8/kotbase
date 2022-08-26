package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.CBLReplicatedDocument
import com.couchbase.lite.kmp.ext.toCouchbaseLiteException
import com.udobny.kmp.DelegatedClass

public actual class ReplicatedDocument
internal constructor(actual: CBLReplicatedDocument) :
    DelegatedClass<CBLReplicatedDocument>(actual) {

    public actual val id: String
        get() = actual.id

    public actual val flags: Set<DocumentFlag> by lazy {
        actual.flags.toDocumentFlags()
    }

    public actual val error: CouchbaseLiteException? by lazy {
        actual.error?.toCouchbaseLiteException()
    }
}
