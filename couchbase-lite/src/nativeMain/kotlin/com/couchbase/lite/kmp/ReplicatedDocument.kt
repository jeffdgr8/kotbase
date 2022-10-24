package com.couchbase.lite.kmp

import com.couchbase.lite.kmp.internal.fleece.toKString
import com.couchbase.lite.kmp.internal.toException
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
}
