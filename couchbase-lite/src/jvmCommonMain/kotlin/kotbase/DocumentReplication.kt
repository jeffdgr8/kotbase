package kotbase

import com.couchbase.lite.DocumentReplication
import kotbase.base.DelegatedClass

public actual class DocumentReplication
internal constructor(
    actual: com.couchbase.lite.DocumentReplication,
    public actual val replicator: Replicator
) : DelegatedClass<DocumentReplication>(actual) {

    public actual val isPush: Boolean
        get() = actual.isPush

    public actual val documents: List<ReplicatedDocument> by lazy {
        actual.documents.map { ReplicatedDocument(it) }
    }
}
