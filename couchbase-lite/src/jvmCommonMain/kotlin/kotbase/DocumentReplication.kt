package kotbase

import kotbase.base.DelegatedClass
import com.couchbase.lite.DocumentReplication as CBLDocumentReplication

public actual class DocumentReplication
internal constructor(
    actual: CBLDocumentReplication,
    public actual val replicator: Replicator
) : DelegatedClass<CBLDocumentReplication>(actual) {

    public actual val isPush: Boolean
        get() = actual.isPush

    public actual val documents: List<ReplicatedDocument> by lazy {
        actual.documents.map { ReplicatedDocument(it) }
    }
}
