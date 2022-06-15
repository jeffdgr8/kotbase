package com.couchbase.lite.kmm

import com.udobny.kmm.DelegatedClass

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
