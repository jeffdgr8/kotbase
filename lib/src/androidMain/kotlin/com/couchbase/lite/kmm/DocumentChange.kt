package com.couchbase.lite.kmm

import com.udobny.kmm.DelegatedClass

public actual class DocumentChange
internal constructor(actual: com.couchbase.lite.DocumentChange) :
    DelegatedClass<com.couchbase.lite.DocumentChange>(actual) {

    public actual val database: Database by lazy {
        Database(actual.database)
    }

    public actual val documentID: String
        get() = actual.documentID
}
