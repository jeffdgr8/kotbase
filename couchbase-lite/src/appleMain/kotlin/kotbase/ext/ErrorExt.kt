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
