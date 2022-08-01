package com.couchbase.lite.kmm

import com.udobny.kmm.DelegatedClass

public actual class ResultSet
internal constructor(actual: com.couchbase.lite.ResultSet) :
    DelegatedClass<com.couchbase.lite.ResultSet>(actual), Iterable<Result>, AutoCloseable {

    public actual operator fun next(): Result? =
        actual.next()?.asResult()

    public actual fun allResults(): List<Result> =
        actual.allResults().map { Result(it) }

    actual override operator fun iterator(): Iterator<Result> {
        return object : Iterator<Result> {

            private val iter: Iterator<com.couchbase.lite.Result> =
                actual.iterator()

            override fun hasNext(): Boolean =
                iter.hasNext()

            override fun next(): Result =
                Result(iter.next())
        }
    }

    override fun close() {
        actual.close()
    }
}

internal fun com.couchbase.lite.ResultSet.asResultSet() = ResultSet(this)
