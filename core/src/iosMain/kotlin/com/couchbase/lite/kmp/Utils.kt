package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.*
import com.udobny.kmp.DelegatedClass
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toNSDate
import platform.Foundation.NSDate
import platform.Foundation.NSNull

internal fun Any.delegateIfNecessary(): Any? {
    return when (this) {
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
}

internal fun List<Any?>.delegateIfNecessary(): List<Any?> =
    map { it?.delegateIfNecessary() }

internal fun <K> Map<K, Any?>.delegateIfNecessary(): Map<K, Any?> =
    mapValues { it.value?.delegateIfNecessary() }

internal fun Any.actualIfDelegated(): Any {
    return when (this) {
        is DelegatedClass<*> -> actual
        is Instant -> toNSDate()
        is List<*> -> actualIfDelegated()
        is Map<*, *> -> actualIfDelegated()
        else -> this
    }
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
        "${value::class} is not a valid type. You may only pass MutableDictionary, Dictionary, MutableArray, Array, Instant, String, Number, Boolean, Blob, null, or a List or a Map whose members are one of the preceding types."
    )
}
