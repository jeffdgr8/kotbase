package kotbase.ext

import kotlinx.cinterop.*
import platform.Foundation.NSError
import platform.Foundation.NSLocalizedDescriptionKey
import platform.Foundation.NSUnderlyingErrorKey

public fun <R, E : Exception> wrapError(
    exceptionFactory: NSError.() -> E,
    action: (error: CPointer<ObjCObjectVar<NSError?>>) -> R
): R = memScoped {
    val error = alloc<ObjCObjectVar<NSError?>>()
    try {
        action(error.ptr).also {
            error.value?.let {
                throw exceptionFactory(it)
            }
        }
    } catch (e: NullPointerException) {
        // ObjC optional init constructors may throw NPE on error, throw error instead
        error.value?.let {
            throw exceptionFactory(it)
        }
        throw e
    }
}

public fun <R> wrapError(action: (error: CPointer<ObjCObjectVar<NSError?>>) -> R): R =
    wrapError(NSError::toException, action)

public fun Exception.toNSError(): NSError = when (this) {
    is NSErrorException -> nsError
    else -> NSError(
        "Kotlin",
        0,
        mapOf(
            NSLocalizedDescriptionKey to message,
            NSUnderlyingErrorKey to this
        )
    )
}

public fun NSError.toException(): Exception = when (val cause = userInfo[NSUnderlyingErrorKey]) {
    is Exception -> cause
    else -> NSErrorException(this)
}

public class NSErrorException(public val nsError: NSError) : Exception(nsError.localizedDescription)