package com.couchbase.lite.kmp

import com.couchbase.lite.kmp.internal.fleece.toKString
import kotlinx.cinterop.*
import libcblite.*
import libcblite.CBLError

internal fun invalidTypeError(value: Any) {
    throw IllegalArgumentException(
        "${value::class} is not a valid type. Valid types are simple types and dictionaries and one-dimensional arrays of those types, including MutableDictionary, Dictionary, Map, MutableArray, Array, List, Blob, Date, String, Number, Boolean and null"
    )
}

internal fun <R> wrapError(
    action: (error: CPointer<CBLError>) -> R
): R = wrapError(CBLError::toExceptionNotNull, action)

internal fun <R, E : Exception> wrapError(
    exceptionFactory: CBLError.() -> E,
    action: (error: CPointer<CBLError>) -> R
): R = memScoped {
    val error = alloc<CBLError>()
    try {
        action(error.ptr).also {
            if (error.code != 0) {
                throw exceptionFactory(error)
            }
        }
    } catch (e: NullPointerException) {
        // if NPE thrown on error, throw error instead
        if (error.code != 0) {
            throw exceptionFactory(error)
        }
        throw e
    }
}

internal fun CBLError.toExceptionNotNull(info: Map<String, Any?>? = null): CouchbaseLiteException =
    toException()!!

internal fun CBLError.toException(info: Map<String, Any?>? = null): CouchbaseLiteException? {
    if (domain == 0.convert() && code == 0) return null
    val domain = when (domain.toUInt()) {
        kCBLDomain -> com.couchbase.lite.kmp.CBLError.Domain.CBLITE
        kCBLPOSIXDomain -> com.couchbase.lite.kmp.CBLError.Domain.POSIX
        kCBLSQLiteDomain -> com.couchbase.lite.kmp.CBLError.Domain.SQLITE
        kCBLFleeceDomain -> com.couchbase.lite.kmp.CBLError.Domain.FLEECE
        kCBLNetworkDomain -> "CouchbaseLite.Network"
        kCBLWebSocketDomain -> "CouchbaseLite.WebSocket"
        else -> "UnknownDomain"
    }
    return CouchbaseLiteException(
        CBLError_Message(readValue()).toKString() ?: "",
        null,
        domain,
        code,
        info
    )
}
