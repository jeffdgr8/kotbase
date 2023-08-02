package kotbase

import kotbase.internal.fleece.toKString
import kotbase.internal.toException
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.pointed
import libcblite.CBLReplicatedDocument

public actual class ReplicatedDocument
internal constructor(actual: CPointer<CBLReplicatedDocument>) {

    public actual val id: String =
        actual.pointed.ID.toKString()!!

    public actual val flags: Set<DocumentFlag> =
        actual.pointed.flags.toDocumentFlags()

    public actual val error: CouchbaseLiteException? =
        actual.pointed.error.toException()

    override fun toString(): String = "ReplicatedDocument{@$id, $error}"
}
