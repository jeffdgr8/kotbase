package com.couchbase.lite.kmm

import com.udobny.kmm.DelegatedClass

public actual class Conflict
internal constructor(actual: com.couchbase.lite.Conflict) :
    DelegatedClass<com.couchbase.lite.Conflict>(actual) {

    public actual val documentId: String
        get() = actual.documentId!!

    public actual val localDocument: Document? by lazy {
        actual.localDocument?.asDocument()
    }

    public actual val remoteDocument: Document? by lazy {
        actual.remoteDocument?.asDocument()
    }
}
