package com.couchbase.lite.kmm

import com.udobny.kmm.DelegatedClass

public actual class ReplicatedDocument
internal constructor(actual: com.couchbase.lite.ReplicatedDocument) :
    DelegatedClass<com.couchbase.lite.ReplicatedDocument>(actual) {

    public actual val id: String
        get() = actual.id

    public actual val flags: Set<DocumentFlag>
        get() = actual.flags

    public actual val error: CouchbaseLiteException?
        get() = actual.error
}