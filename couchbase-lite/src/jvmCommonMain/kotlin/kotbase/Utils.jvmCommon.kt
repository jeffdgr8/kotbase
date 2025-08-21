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
import kotbase.ext.toDate
import kotbase.ext.toKotlinInstant
import kotlin.time.Instant
import java.util.*
import com.couchbase.lite.Array as CBLArray
import com.couchbase.lite.Blob as CBLBlob
import com.couchbase.lite.Dictionary as CBLDictionary
import com.couchbase.lite.MutableArray as CBLMutableArray
import com.couchbase.lite.MutableDictionary as CBLMutableDictionary

internal fun Any.delegateIfNecessary(): Any = when (this) {
    is CBLBlob -> asBlob()
    is CBLMutableArray -> asMutableArray()
    is CBLArray -> asArray()
    is CBLMutableDictionary -> asMutableDictionary()
    is CBLDictionary -> asDictionary()
    is Date -> toKotlinInstant()
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
    is Instant -> toDate()
    is List<*> -> actualIfDelegated()
    is Map<*, *> -> actualIfDelegated()
    else -> this
}

internal fun List<Any?>.actualIfDelegated(): List<Any?> =
    map { it?.actualIfDelegated() }

internal fun <K> Map<K, Any?>.actualIfDelegated(): Map<K, Any?> =
    mapValues { it.value?.actualIfDelegated() }
