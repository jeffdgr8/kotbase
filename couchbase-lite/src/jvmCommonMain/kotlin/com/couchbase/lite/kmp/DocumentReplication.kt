package com.couchbase.lite.kmp

import com.udobny.kmp.DelegatedClass

public actual class DocumentReplication
internal constructor(
    actual: com.couchbase.lite.DocumentReplication,
    public actual val replicator: Replicator
) : DelegatedClass<com.couchbase.lite.DocumentReplication>(actual) {

    public actual val isPush: Boolean
        get() = actual.isPush

    public actual val documents: List<ReplicatedDocument> by lazy {
        actual.documents.map { ReplicatedDocument(it) }
    }
}
