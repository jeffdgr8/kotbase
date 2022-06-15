package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.CBLDocumentChange
import com.udobny.kmm.DelegatedClass

public actual class DocumentChange
internal constructor(actual: CBLDocumentChange) :
    DelegatedClass<CBLDocumentChange>(actual) {

    public actual val database: Database by lazy {
        Database(actual.database!!)
    }

    public actual val documentID: String
        get() = actual.documentID!!
}
