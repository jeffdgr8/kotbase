package com.couchbase.lite.kmp.internal

import com.couchbase.lite.kmp.CBLError.Code
import com.couchbase.lite.kmp.CBLError.Domain
import com.couchbase.lite.kmp.CouchbaseLiteException
import com.couchbase.lite.kmp.internal.fleece.toKString
import kotlinx.cinterop.*
import libcblite.*

internal fun <R> wrapCBLError(
    action: (error: CPointer<CBLError>) -> R
): R = wrapCBLError(CBLError::toExceptionNotNull, action)

internal fun <R, E : Exception> wrapCBLError(
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
    toException(info)!!

internal fun CBLError.toException(info: Map<String, Any?>? = null): CouchbaseLiteException? {
    if (domain == 0.toUByte() && code == 0) return null
    val code = when (domain.toUInt()) {
        kCBLNetworkDomain -> code + Code.NETWORK_OFFSET
        kCBLWebSocketDomain -> code + Code.HTTP_BASE
        else -> code
    }
    val domain = when (domain.toUInt()) {
        kCBLDomain, kCBLNetworkDomain, kCBLWebSocketDomain -> Domain.CBLITE
        kCBLPOSIXDomain -> Domain.POSIX
        kCBLSQLiteDomain -> Domain.SQLITE
        kCBLFleeceDomain -> Domain.FLEECE
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
