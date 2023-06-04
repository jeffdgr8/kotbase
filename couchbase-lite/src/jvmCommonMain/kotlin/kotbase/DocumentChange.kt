package kotbase

import kotbase.base.DelegatedClass
import com.couchbase.lite.DocumentChange as CBLDocumentChange

public actual class DocumentChange
internal constructor(actual: CBLDocumentChange) : DelegatedClass<CBLDocumentChange>(actual) {

    public actual val database: Database by lazy {
        Database(actual.database)
    }

    public actual val documentID: String
        get() = actual.documentID
}
