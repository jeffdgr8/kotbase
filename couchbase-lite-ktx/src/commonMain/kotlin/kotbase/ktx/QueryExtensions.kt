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
 * From https://github.com/MOLO17/couchbase-lite-kotlin/blob/master/library/src/main/java/com/molo17/couchbase/lite/QueryExtensions.kt
 * Modified by Jeff Lockhart
 * - Use kotbase package
 * - Adapt to use queryChangeFlow()
 * - Resolve explicitApiWarning() requirements
 * - Add optional CoroutineContext parameter
 */

@file:Suppress("NOTHING_TO_INLINE", "KotlinRedundantDiagnosticSuppress")

package kotbase.ktx

import kotbase.Query
import kotbase.QueryChange
import kotbase.ResultSet
import kotbase.queryChangeFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlin.coroutines.CoroutineContext

/**
 * Returns a [Flow] that emits the Query [ResultSet] every time the underlying
 * data set changes.
 *
 * If the query fails, the [Flow] throws an error.
 */
public fun Query.asFlow(coroutineContext: CoroutineContext? = null): Flow<ResultSet> =
    asQueryFlow(coroutineContext).mapNotNull { it.results }

/**
 * Returns a [Flow] that maps the Query [ResultSet] to instances of a class
 * that can be created using the given [factory] lambda.
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
 * val users: Flow<List<User>> = query.asObjectsFlow(::User)
 * ```
 *
 * Using Kotlin Map delegation for creating such instances is a great shorthand.
 *
 * @param factory the lambda used for creating object instances.
 */
public fun <T : Any> Query.asObjectsFlow(
    coroutineContext: CoroutineContext? = null,
    factory: (Map<String, Any?>) -> T?
): Flow<List<T>> = asQueryFlow(coroutineContext).mapToObjects(factory)

public fun <T : Any> Flow<QueryChange>.mapToObjects(
    factory: (Map<String, Any?>) -> T?
): Flow<List<T>> = mapNotNull { queryChange -> queryChange.results?.toObjects(factory) }

///////////////////////////////////////////////////////////////////////////
// Private functions
///////////////////////////////////////////////////////////////////////////

internal fun Query.asQueryFlow(coroutineContext: CoroutineContext? = null): Flow<QueryChange> =
    queryChangeFlow(coroutineContext).onEach { change ->
        change.error?.let { throw it }
    }
