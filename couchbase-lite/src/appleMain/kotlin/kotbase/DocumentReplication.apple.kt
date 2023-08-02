package kotbase

import cocoapods.CouchbaseLite.CBLDocumentReplication
import cocoapods.CouchbaseLite.CBLReplicatedDocument
import kotbase.base.DelegatedClass

public actual class DocumentReplication
internal constructor(
    actual: CBLDocumentReplication,
    public actual val replicator: Replicator
) : DelegatedClass<CBLDocumentReplication>(actual) {

    public actual val isPush: Boolean
        get() = actual.isPush

    public actual val documents: List<ReplicatedDocument> by lazy {
        @Suppress("UNCHECKED_CAST")
        (actual.documents as List<CBLReplicatedDocument>).map { ReplicatedDocument(it) }
    }

    override fun toString(): String = "DocumentReplication{#${documents.size} @$replicator}"
}
