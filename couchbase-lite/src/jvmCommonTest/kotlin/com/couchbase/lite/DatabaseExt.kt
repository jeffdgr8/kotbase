@file:JvmName("DatabaseExtJvm") // https://youtrack.jetbrains.com/issue/KT-21186

package com.couchbase.lite

import kotbase.Blob
import kotbase.Database
import kotbase.asBlob
import kotbase.base.DelegatedClass

internal actual val Database.dbPath: String?
    get() = actual.dbPath

internal actual fun Database.saveBlob(blob: Blob) =
    actual.saveBlob(blob.actual)

internal actual fun Database.getBlob(props: Map<String, Any?>): Blob? =
    actual.getBlob(props)?.asBlob()

internal actual fun Database.getC4Document(id: String): C4Document =
    C4Document(actual.getC4Document(id))

internal actual class C4Document(actual: com.couchbase.lite.internal.core.C4Document) :
    DelegatedClass<com.couchbase.lite.internal.core.C4Document>(actual) {

    actual fun isRevDeleted(): Boolean =
        actual.deleted()
}
