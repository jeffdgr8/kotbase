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
