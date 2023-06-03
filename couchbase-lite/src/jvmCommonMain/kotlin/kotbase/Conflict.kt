package kotbase

import com.couchbase.lite.Conflict
import kotbase.base.DelegatedClass

public actual class Conflict
internal constructor(actual: com.couchbase.lite.Conflict) :
    DelegatedClass<Conflict>(actual) {

    public actual val documentId: String
        get() = actual.documentId!!

    public actual val localDocument: Document? by lazy {
        actual.localDocument?.asDocument()
    }

    public actual val remoteDocument: Document? by lazy {
        actual.remoteDocument?.asDocument()
    }
}
