/*
 * Copyright 2022-2023 Jeff Lockhart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kotbase

import kotbase.internal.DelegatedClass
import com.couchbase.lite.Result as CBLResult
import com.couchbase.lite.ResultSet as CBLResultSet

public actual class ResultSet
internal constructor(actual: CBLResultSet) : DelegatedClass<CBLResultSet>(actual), Iterable<Result>, AutoCloseable {

    public actual operator fun next(): Result? =
        actual.next()?.asResult()

    public actual fun allResults(): List<Result> =
        actual.allResults().map { Result(it) }

    actual override fun iterator(): Iterator<Result> = object : Iterator<Result> {

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
