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

import cocoapods.CouchbaseLite.*
import kotbase.internal.DelegatedClass
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toNSDate
import platform.Foundation.NSDate
import platform.Foundation.NSNull

internal fun Any.delegateIfNecessary(): Any? = when (this) {
    is NSNull -> null
    is CBLBlob -> asBlob()
    is CBLMutableArray -> asMutableArray()
    is CBLArray -> asArray()
    is CBLMutableDictionary -> asMutableDictionary()
    is CBLDictionary -> asDictionary()
    is NSDate -> toKotlinInstant()
    is List<*> -> delegateIfNecessary()
    is Map<*, *> -> delegateIfNecessary()
    else -> this
}

internal fun List<Any?>.delegateIfNecessary(): List<Any?> =
    map { it?.delegateIfNecessary() }

internal fun <K> Map<K, Any?>.delegateIfNecessary(): Map<K, Any?> =
    mapValues { it.value?.delegateIfNecessary() }

internal fun Any.actualIfDelegated(): Any = when (this) {
    is DelegatedClass<*> -> actual
    is Instant -> toNSDate()
    is List<*> -> actualIfDelegated()
    is Map<*, *> -> actualIfDelegated()
    else -> this
}

internal fun List<Any?>.actualIfDelegated(): List<Any?> =
    map { it?.actualIfDelegated() }

internal fun Map<*, *>.actualIfDelegated(): Map<Any?, *> =
    mapValues { it.value?.actualIfDelegated() }

internal fun checkType(value: Any?) {
    when (value ?: return) {
        is Boolean,
        is Number,
        is String,
        is Instant,
        is Blob,
        is Array,
        is Dictionary,
        is Map<*, *>,
        is List<*> -> return
    }
    // Catch this error to throw IllegalArgumentException rather than eventual ObjC NSInternalInconsistencyException error
    throw IllegalArgumentException(
        "${value::class} is not a valid type. Valid types are simple types and dictionaries and one-dimensional arrays of those types, including MutableDictionary, Dictionary, Map, MutableArray, Array, List, Blob, Instant, String, Number, Boolean and null"
    )
}
