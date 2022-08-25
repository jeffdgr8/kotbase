package com.couchbase.lite.kmp

import com.udobny.kmp.DelegatedClass

public actual class DocumentReplication
internal constructor(actual: com.couchbase.lite.DocumentReplication) :
    DelegatedClass<com.couchbase.lite.DocumentReplication>(actual) {

    public actual val replicator: Replicator by lazy {
        Replicator(actual.replicator)
    }

    public actual val isPush: Boolean
        get() = actual.isPush

    public actual val documents: List<ReplicatedDocument> by lazy {
        actual.documents.map { ReplicatedDocument(it) }
    }
}
