package com.couchbase.lite.kmp

import com.udobny.kmp.DelegatedClass

public actual class QueryChange
internal constructor(actual: com.couchbase.lite.QueryChange) :
    DelegatedClass<com.couchbase.lite.QueryChange>(actual) {

    public actual val query: Query by lazy {
        actual.query.asQuery()
    }

    public actual val results: ResultSet? by lazy {
        actual.results?.asResultSet()
    }

    public actual val error: Throwable?
        get() = actual.error
}
