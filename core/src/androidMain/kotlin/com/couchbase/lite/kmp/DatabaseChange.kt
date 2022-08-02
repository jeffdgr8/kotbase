package com.couchbase.lite.kmp

import com.udobny.kmp.DelegatedClass

public actual class DatabaseChange
internal constructor(actual: com.couchbase.lite.DatabaseChange) :
    DelegatedClass<com.couchbase.lite.DatabaseChange>(actual) {

    public actual val database: Database by lazy {
        Database(actual.database)
    }

    public actual val documentIDs: List<String>
        get() = actual.documentIDs
}
