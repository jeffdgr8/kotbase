package kotbase

import kotbase.base.DelegatedClass
import kotbase.ext.toDate
import kotbase.ext.toKotlinInstant
import kotlinx.datetime.Instant
import java.util.*

internal fun Any.delegateIfNecessary(): Any {
    return when (this) {
        is com.couchbase.lite.Blob -> asBlob()
        is com.couchbase.lite.MutableArray -> asMutableArray()
        is com.couchbase.lite.Array -> asArray()
        is com.couchbase.lite.MutableDictionary -> asMutableDictionary()
        is com.couchbase.lite.Dictionary -> asDictionary()
        is Date -> toKotlinInstant()
        is List<*> -> delegateIfNecessary()
        is Map<*, *> -> delegateIfNecessary()
        else -> this
    }
}

internal fun List<Any?>.delegateIfNecessary(): List<Any?> =
    map { it?.delegateIfNecessary() }

internal fun <K> Map<K, Any?>.delegateIfNecessary(): Map<K, Any?> =
    mapValues { it.value?.delegateIfNecessary() }

internal fun Any.actualIfDelegated(): Any {
    return when (this) {
        is DelegatedClass<*> -> actual
        is Instant -> toDate()
        is List<*> -> actualIfDelegated()
        is Map<*, *> -> actualIfDelegated()
        else -> this
    }
}

internal fun List<Any?>.actualIfDelegated(): List<Any?> =
    map { it?.actualIfDelegated() }

internal fun <K> Map<K, Any?>.actualIfDelegated(): Map<K, Any?> =
    mapValues { it.value?.actualIfDelegated() }
