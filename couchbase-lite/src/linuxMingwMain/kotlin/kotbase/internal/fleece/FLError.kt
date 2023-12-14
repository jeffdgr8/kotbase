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
package kotbase.internal.fleece

import kotbase.CBLError
import kotbase.CouchbaseLiteException
import kotlinx.cinterop.*
import libcblite.*

internal fun <R> wrapFLError(
    action: (error: CPointer<FLErrorVar>) -> R
): R = wrapFLError(FLError::toExceptionNotNull, action)

internal fun <R, E : Exception> wrapFLError(
    exceptionFactory: FLError.() -> E,
    action: (error: CPointer<FLErrorVar>) -> R
): R = memScoped {
    val error = alloc<FLErrorVar>()
    try {
        action(error.ptr).also {
            if (error.value != 0U) {
                throw exceptionFactory(error.value)
            }
        }
    } catch (e: NullPointerException) {
        // if NPE thrown on error, throw error instead
        if (error.value != 0U) {
            throw exceptionFactory(error.value)
        }
        throw e
    }
}

internal fun FLError.toExceptionNotNull(): CouchbaseLiteException =
    toException()!!

internal fun FLError.toException(): CouchbaseLiteException? {
    if (this == 0U) return null
    val domain = when (this) {
        kFLPOSIXError -> CBLError.Domain.POSIX
        else -> CBLError.Domain.FLEECE
    }
    val message = when (this) {
        kFLMemoryError -> "Memory error"
        kFLOutOfRange -> "Out of range"
        kFLInvalidData -> "Invalid data"
        kFLEncodeError -> "Encode error"
        kFLJSONError -> "JSON error"
        kFLUnknownValue -> "Unknown value"
        kFLInternalError -> "Internal error"
        kFLNotFound -> "Not found"
        kFLSharedKeysStateError -> "Shared keys state error"
        kFLPOSIXError -> "POSIX error"
        kFLUnsupported -> "Unsupported"
        else -> "Unknown"
    }
    return CouchbaseLiteException(
        message,
        domain,
        this.toInt()
    )
}
