package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.CBLConflict
import com.udobny.kmp.DelegatedClass

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
