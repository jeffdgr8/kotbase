package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.CBLDocumentReplication
import cocoapods.CouchbaseLite.CBLReplicatedDocument
import com.udobny.kmm.DelegatedClass

public actual class DocumentReplication
internal constructor(actual: CBLDocumentReplication) :
    DelegatedClass<CBLDocumentReplication>(actual) {

    public actual val replicator: Replicator by lazy {
        Replicator(actual.replicator)
    }

    public actual val isPush: Boolean
        get() = actual.isPush

    public actual val documents: List<ReplicatedDocument> by lazy {
        @Suppress("UNCHECKED_CAST")
        (actual.documents as List<CBLReplicatedDocument>).map { ReplicatedDocument(it) }
    }
}
