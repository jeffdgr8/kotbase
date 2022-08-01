package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.CBLQueryChange
import com.couchbase.lite.kmm.ext.toCouchbaseLiteException
import com.udobny.kmm.DelegatedClass

public actual class QueryChange
internal constructor(actual: CBLQueryChange) :
    DelegatedClass<CBLQueryChange>(actual) {

    public actual val query: Query by lazy {
        DelegatedQuery(actual.query)
    }

    public actual val results: ResultSet? by lazy {
        actual.results?.asResultSet()
    }

    public actual val error: Throwable? by lazy {
        actual.error?.toCouchbaseLiteException()
    }
}
