package kotbase

import com.couchbase.lite.DocumentChange
import kotbase.base.DelegatedClass

public actual class DocumentChange
internal constructor(actual: com.couchbase.lite.DocumentChange) :
    DelegatedClass<DocumentChange>(actual) {

    public actual val database: Database by lazy {
        Database(actual.database)
    }

    public actual val documentID: String
        get() = actual.documentID
}
