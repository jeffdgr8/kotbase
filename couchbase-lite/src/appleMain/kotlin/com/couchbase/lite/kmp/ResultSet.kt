package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.CBLQueryResult
import cocoapods.CouchbaseLite.CBLQueryResultSet
import com.udobny.kmp.DelegatedClass

public actual class ResultSet
internal constructor(actual: CBLQueryResultSet) :
    DelegatedClass<CBLQueryResultSet>(actual), Iterable<Result>, AutoCloseable {

    public actual operator fun next(): Result? =
        (actual.nextObject() as CBLQueryResult?)?.asResult()

    @Suppress("UNCHECKED_CAST")
    public actual fun allResults(): List<Result> =
        (actual.allResults() as List<CBLQueryResult>).map { Result(it) }

    actual override fun iterator(): Iterator<Result> =
        allResults().iterator()

    override fun close() {
        // no close() in Objective-C SDK
        // https://github.com/couchbase/couchbase-lite-ios/blob/b1eca5996b06564e65ae1c0a1a8bb55db28f37f5/Objective-C/CBLQueryResultSet.mm#L47
    }
}

internal fun CBLQueryResultSet.asResultSet() = ResultSet(this)
