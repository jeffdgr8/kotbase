package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.CBLQueryChange
import com.couchbase.lite.kmp.ext.toCouchbaseLiteException
import com.udobny.kmp.DelegatedClass

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
