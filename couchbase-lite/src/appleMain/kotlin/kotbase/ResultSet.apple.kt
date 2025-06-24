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

import cocoapods.CouchbaseLite.CBLQueryResult
import cocoapods.CouchbaseLite.CBLQueryResultSet
import kotbase.internal.DelegatedClass

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

    actual override fun close() {
        // no close() in Objective-C SDK
        // https://github.com/couchbase/couchbase-lite-ios/blob/b1eca5996b06564e65ae1c0a1a8bb55db28f37f5/Objective-C/CBLQueryResultSet.mm#L47
    }
}

internal fun CBLQueryResultSet.asResultSet() = ResultSet(this)
