package com.couchbase.lite

import cocoapods.CouchbaseLite.*
import com.couchbase.lite.kmm.BaseTest.Companion.DB_EXTENSION
import com.couchbase.lite.kmm.Blob
import com.couchbase.lite.kmm.Database
import com.couchbase.lite.kmm.asBlob
import com.couchbase.lite.kmm.ext.toCouchbaseLiteException
import com.udobny.kmm.DelegatedClass
import com.udobny.kmm.ext.wrapError
import kotlinx.cinterop.*
import platform.Foundation.NSError
import platform.objc.objc_sync_enter
import platform.objc.objc_sync_exit

internal actual val Database.isOpen: Boolean
    get() = isOpen

internal actual fun <R> Database.withLock(action: () -> R): R {
    objc_sync_enter(actual)
    val result = action()
    objc_sync_exit(actual)
    return result
}

internal actual val Database.dbPath: String?
    get() {
        // CBLDatabase.databasePath(name, dir)
        val name = name.replace('/', ':') + DB_EXTENSION
        val dir = config.getDirectory().dropLastWhile { it == '/' }
        return "$dir/$name"
    }

internal actual fun Database.saveBlob(blob: Blob) {
    wrapError(NSError::toCouchbaseLiteException) { error ->
        actual.saveBlob(blob.actual, error)
    }
}

@Suppress("UNCHECKED_CAST")
internal actual fun Database.getBlob(props: Map<String, Any?>): Blob? {
    if (!Blob.isBlob(props)) {
        throw IllegalArgumentException("getBlob arg does not specify a blob")
    }
    return actual.getBlob(props as Map<Any?, *>)?.asBlob()
}

internal actual fun Database.getC4Document(id: String): C4Document {
    val doc = getCBLDocument(actual, id)!!
    return C4Document(doc.c4Doc!!)
}

internal actual class C4Document(actual: CBLC4Document) : DelegatedClass<CBLC4Document>(actual) {

    actual fun isRevDeleted(): Boolean =
        (actual.revFlags and kRevDeleted.toUByte()) != 0.toUByte()
}
