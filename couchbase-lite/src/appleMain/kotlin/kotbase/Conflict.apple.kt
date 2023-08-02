package kotbase

import cocoapods.CouchbaseLite.CBLConflict
import kotbase.base.DelegatedClass

public actual class Conflict
internal constructor(actual: CBLConflict) :
    DelegatedClass<CBLConflict>(actual) {

    public actual val documentId: String
        get() = actual.documentID

    public actual val localDocument: Document? by lazy {
        actual.localDocument?.asDocument()
    }

    public actual val remoteDocument: Document? by lazy {
        actual.remoteDocument?.asDocument()
    }
}
