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
package kotbase.internal

import kotbase.CBLError.Code
import kotbase.CBLError.Domain
import kotbase.CouchbaseLiteException
import kotbase.internal.fleece.toKString
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
        debug.CBLError_Message(readValue()).toKString() ?: "",
        null,
        domain,
        code,
        info
    )
}
