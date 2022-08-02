package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.CBLDocumentChange
import com.udobny.kmp.DelegatedClass

public actual class DocumentChange
internal constructor(actual: CBLDocumentChange) :
    DelegatedClass<CBLDocumentChange>(actual) {

    public actual val database: Database by lazy {
        Database(actual.database!!)
    }

    public actual val documentID: String
        get() = actual.documentID!!
}
