package com.couchbase.lite

import cocoapods.CouchbaseLite.*
import com.couchbase.lite.kmp.BaseTest.Companion.DB_EXTENSION
import com.couchbase.lite.kmp.Blob
import com.couchbase.lite.kmp.Database
import com.couchbase.lite.kmp.asBlob
import com.couchbase.lite.kmp.ext.toCouchbaseLiteException
import com.udobny.kmp.DelegatedClass
import com.udobny.kmp.ext.wrapError
import platform.Foundation.NSError

internal actual val Database.isOpen: Boolean
    get() = !actual.isClosed()

internal actual fun <R> Database.withLock(action: () -> R): R =
    withLock(action)

internal actual val Database.dbPath: String?
    get() {
        // CBLDatabase.databasePath(name, dir)
        val name = name.replace('/', ':') + DB_EXTENSION
        val dir = config.directory.dropLastWhile { it == '/' }
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
    val doc = wrapError(NSError::toCouchbaseLiteException) { error ->
        CBLDocument.create(actual, id, true, error)
    }
    return C4Document(doc?.c4Doc!!)
}

internal actual class C4Document(actual: CBLC4Document) : DelegatedClass<CBLC4Document>(actual) {

    actual fun isRevDeleted(): Boolean =
        (actual.revFlags and kRevDeleted.toUByte()) != 0.toUByte()
}