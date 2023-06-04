package kotbase

import kotbase.base.DelegatedClass
import com.couchbase.lite.Conflict as CBLConflict

public actual class Conflict
internal constructor(actual: CBLConflict) : DelegatedClass<CBLConflict>(actual) {

    public actual val documentId: String
        get() = actual.documentId!!

    public actual val localDocument: Document? by lazy {
        actual.localDocument?.asDocument()
    }

    public actual val remoteDocument: Document? by lazy {
        actual.remoteDocument?.asDocument()
    }
}
