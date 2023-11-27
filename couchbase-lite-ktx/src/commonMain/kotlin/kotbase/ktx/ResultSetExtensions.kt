/*
 * Copyright (c) 2020 MOLO17
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * From https://github.com/MOLO17/couchbase-lite-kotlin/blob/master/library/src/main/java/com/molo17/couchbase/lite/ResultSetExtensions.kt
 * Modified by Jeff Lockhart
 * - Use kotbase package
 * - Resolve explicitApiWarning() requirements
 * - Handle first projected property is dictionary edge case
 */

package kotbase.ktx

import kotbase.ResultSet

/**
 * Maps the [ResultSet] to a [List] of objects that are created
 * using the given [factory] lambda.
 *
 * Example of usage:
 *
 * ```
 * class User(map: Map<String, Any?>) {
 *   val name: String by map
 *   val surname: String by map
 *   val age: Int by map
 * }
 *
 * val users: List<User> = query.execute().toObjects(::User)
 * ```
 *
 * Using Kotlin Map delegation for creating such instances is a great shorthand.
 *
 * @param factory lambda used for creating an object instance
 * @return a [List] of objects of type [T]
 */
public inline fun <T : Any> ResultSet.toObjects(factory: (Map<String, Any?>) -> T?): List<T> =
    mapNotNull { result ->
        result.run {

            // If the query was written using the `SelectResult.all()` expression,
            // then the `Result` contains N dictionary entries. Each entry is
            // identified by a key which is the database name from which the data
            // has been read.
            //
            // --> `getDictionary(0)?.toMap()`
            // For the scope of this utility method, we provide access to only the first
            // dictionary since it will cover most of the cases.
            //
            // --> `?: toMap()`
            // If the first dictionary is `null`, then the query was written using the
            // projections for each Document key. We can then use the `Result` object as a Map.

            when (count) {
                0 -> null
                1 -> getDictionary(0)?.toMap() ?: toMap()
                else -> toMap()
            }
        }?.let(factory)
    }