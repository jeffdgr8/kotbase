package com.couchbase.lite

import kotbase.*
import kotbase.internal.DbContext
import kotbase.internal.wrapCBLError
import libcblite.CBLDatabase_GetBlob
import libcblite.CBLDatabase_SaveBlob

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
    blob.checkSetDb(DbContext(this))
}

internal actual fun Database.getBlob(props: Map<String, Any?>): Blob? {
    if (!Blob.isBlob(props)) {
        throw IllegalArgumentException("getBlob arg does not specify a blob")
    }
    return try {
        wrapCBLError { error ->
            val dict = MutableDictionary(props)
            CBLDatabase_GetBlob(actual, dict.actual, error)
                ?.asBlob(DbContext(this))
        }
    } catch (e: CouchbaseLiteException) {
        println(e)
        null
    }
}

// TODO: implement native C getC4Document()

internal actual fun Database.getC4Document(id: String): C4Document =
    C4Document(getDocument(id))

internal actual class C4Document(private val doc: Document?) {

    actual fun isRevDeleted(): Boolean =
        doc == null
}