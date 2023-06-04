package kotbase

import kotbase.base.DelegatedClass
import com.couchbase.lite.ReplicatedDocument as CBLReplicatedDocument

public actual class ReplicatedDocument
internal constructor(actual: CBLReplicatedDocument) : DelegatedClass<CBLReplicatedDocument>(actual) {

    public actual val id: String
        get() = actual.id

    public actual val flags: Set<DocumentFlag>
        get() = actual.flags

    public actual val error: CouchbaseLiteException?
        get() = actual.error
}
