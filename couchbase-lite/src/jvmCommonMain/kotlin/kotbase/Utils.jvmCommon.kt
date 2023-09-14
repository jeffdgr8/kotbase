package kotbase

import kotbase.base.DelegatedClass
import kotbase.ext.toDate
import kotbase.ext.toKotlinInstant
import kotlinx.datetime.Instant
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
    is Array -> actual
    is Dictionary -> actual
    is Expression -> actual
    is Instant -> toDate()
    is List<*> -> actualIfDelegated()
    is Map<*, *> -> actualIfDelegated()
    else -> this
}

internal fun List<Any?>.actualIfDelegated(): List<Any?> =
    map { it?.actualIfDelegated() }

internal fun <K> Map<K, Any?>.actualIfDelegated(): Map<K, Any?> =
    mapValues { it.value?.actualIfDelegated() }
