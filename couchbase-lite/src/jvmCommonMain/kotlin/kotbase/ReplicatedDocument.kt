package kotbase

import com.couchbase.lite.ReplicatedDocument
import kotbase.base.DelegatedClass

public actual class ReplicatedDocument
internal constructor(actual: com.couchbase.lite.ReplicatedDocument) :
    DelegatedClass<ReplicatedDocument>(actual) {

    public actual val id: String
        get() = actual.id

    public actual val flags: Set<DocumentFlag>
        get() = actual.flags

    public actual val error: CouchbaseLiteException?
        get() = actual.error
}
