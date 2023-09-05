@file:Suppress("ACTUAL_WITHOUT_EXPECT")

package kotbase

import kotbase.base.DelegatedClass
import com.couchbase.lite.Result as CBLResult
import com.couchbase.lite.ResultSet as CBLResultSet

public actual class ResultSet
internal constructor(actual: CBLResultSet) : DelegatedClass<CBLResultSet>(actual), Iterable<Result>, AutoCloseable {

    public actual operator fun next(): Result? =
        actual.next()?.asResult()

    public actual fun allResults(): List<Result> =
        actual.allResults().map { Result(it) }

    actual override operator fun iterator(): Iterator<Result> = object : Iterator<Result> {

        private val iter: Iterator<CBLResult> =
            actual.iterator()

        override fun hasNext(): Boolean =
            iter.hasNext()

        override fun next(): Result =
            Result(iter.next())
    }

    actual override fun close() {
        actual.close()
    }
}

internal fun CBLResultSet.asResultSet() = ResultSet(this)
