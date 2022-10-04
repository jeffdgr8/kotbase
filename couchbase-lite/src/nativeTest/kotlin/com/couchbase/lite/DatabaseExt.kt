package com.couchbase.lite

import com.couchbase.lite.kmp.*
import com.couchbase.lite.kmp.internal.wrapCBLError
import libcblite.CBLDatabase_GetBlob
import libcblite.CBLDatabase_SaveBlob

internal actual val Database.isOpen: Boolean
    get() = !isClosed

internal actual fun <R> Database.withLock(action: () -> R): R =
    withLock(action)

internal actual val Database.dbPath: String?
    get() {
        // CBLDatabase.databasePath(name, dir)
        val name = name.replace('/', ':') + BaseTest.DB_EXTENSION
        val dir = config.directory.dropLastWhile { it == '/' }
        return "$dir/$name"
    }

internal actual fun Database.saveBlob(blob: Blob) {
    wrapCBLError { error ->
        CBLDatabase_SaveBlob(actual, blob.actual, error)
    }
}

@Suppress("UNCHECKED_CAST")
internal actual fun Database.getBlob(props: Map<String, Any?>): Blob? {
    if (!Blob.isBlob(props)) {
        throw IllegalArgumentException("getBlob arg does not specify a blob")
    }
    return try {
        wrapCBLError { error ->
            val dict = MutableDictionary(props)
            CBLDatabase_GetBlob(actual, dict.actual, error)?.asBlob()
        }
    } catch (e: CouchbaseLiteException) {
        println(e)
        null
    }
}

// TODO: implement native C4Document

internal actual fun Database.getC4Document(id: String): C4Document {
    return C4Document()
}

internal actual class C4Document {

    actual fun isRevDeleted(): Boolean =
        false
}
