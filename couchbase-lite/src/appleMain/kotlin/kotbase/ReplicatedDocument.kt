package kotbase

import cocoapods.CouchbaseLite.CBLReplicatedDocument
import kotbase.base.DelegatedClass
import kotbase.ext.toCouchbaseLiteException

public actual class ReplicatedDocument
internal constructor(actual: CBLReplicatedDocument) :
    DelegatedClass<CBLReplicatedDocument>(actual) {

    public actual val id: String
        get() = actual.id

    public actual val flags: Set<DocumentFlag> by lazy {
        actual.flags.toDocumentFlags()
    }

    public actual val error: CouchbaseLiteException? by lazy {
        actual.error?.toCouchbaseLiteException()
    }

    override fun toString(): String = "ReplicatedDocument{@$id, $error}"
}
