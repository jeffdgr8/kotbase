package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.CBLQueryResult
import cocoapods.CouchbaseLite.CBLQueryResultSet
import com.udobny.kmm.DelegatedClass

public actual class ResultSet
internal constructor(actual: CBLQueryResultSet) :
    DelegatedClass<CBLQueryResultSet>(actual), Iterable<Result> {

    public actual operator fun next(): Result? =
        (actual.nextObject() as CBLQueryResult?)?.asResult()

    @Suppress("UNCHECKED_CAST")
    public actual fun allResults(): List<Result> =
        (actual.allResults() as List<CBLQueryResult>).map { Result(it) }

    actual override fun iterator(): Iterator<Result> =
        allResults().iterator()
}

internal fun CBLQueryResultSet.asResultSet() = ResultSet(this)
