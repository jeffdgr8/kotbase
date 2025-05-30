/*
 * Copyright 2023 Jeff Lockhart
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
package kotbase.ktx

import kotbase.Query
import kotbase.QueryChange
import kotbase.ResultSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic

/**
 * Returns a [Flow] that maps the Query [ResultSet] to instances of a class
 * that can be created using the given [factory] lambda.
 *
 * Example of usage with kotlinx-serialization:
 *
 * ```
 * @Serializable
 * class User(
 *   val name: String,
 *   val surname: String,
 *   val age: Int
 * )
 *
 * val users: Flow<List<User>> = query.asObjectsFlow { json: String ->
 *     Json.decodeFromString<User>(json)
 * }
 * ```
 *
 * @param coroutineContext optional CoroutineContext on which to run the change listener:
 * default is the flow collector's CoroutineContext
 * @param factory the lambda used for creating object instances.
 */
public fun <T : Any> Query.asObjectsFlow(
    coroutineContext: CoroutineContext?,
    factory: (String) -> T?
): Flow<List<T>> = asQueryFlow(coroutineContext).mapToObjects(factory)

@Deprecated("For binary compatibility", level = DeprecationLevel.HIDDEN)
@JvmSynthetic
@JvmName("asObjectsFlow\$default")
public fun <T : Any> Query.asObjectsFlow(
    coroutineContext: CoroutineContext?,
    factory: (String) -> T?,
    flags: Int,
    obj: Any?
): Flow<List<T>> = asObjectsFlow(if (flags and 1 != 0) null else coroutineContext, factory)

/**
 * Returns a [Flow] that maps the Query [ResultSet] to instances of a class
 * that can be created using the given [factory] lambda.
 *
 * Example of usage with kotlinx-serialization:
 *
 * ```
 * @Serializable
 * class User(
 *   val name: String,
 *   val surname: String,
 *   val age: Int
 * )
 *
 * val users: Flow<List<User>> = query.asObjectsFlow { json: String ->
 *     Json.decodeFromString<User>(json)
 * }
 * ```
 *
 * @param factory the lambda used for creating object instances.
 */
public fun <T : Any> Query.asObjectsFlow(
    factory: (String) -> T?
): Flow<List<T>> = asQueryFlow(null).mapToObjects(factory)

public fun <T : Any> Flow<QueryChange>.mapToObjects(
    factory: (String) -> T?
): Flow<List<T>> = mapNotNull { queryChange -> queryChange.results?.toObjects(factory) }
