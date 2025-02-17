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

import kotlinx.datetime.Instant
import kotlin.math.roundToLong

/**
 * An ISO 8601 UTC string with milliseconds
 * precision. The default [Instant.toString]
 * can be seconds or nanoseconds precision.
 */
internal fun Instant.toStringMillis(): String {
    return roundToMillis().toString().let {
        if (it.length == 20) {
            it.dropLast(1) + ".000Z"
        } else if (it.length > 24) {
            it.dropLast(it.length - 23) + 'Z'
        } else {
            it
        }
    }
}

internal const val NANOS_PER_MILLI = 1_000_000
internal const val MILLIS_PER_ONE = 1_000

internal fun Instant.roundToMillis(): Instant {
    val millis = nanosecondsOfSecond.toFloat() / NANOS_PER_MILLI
    return Instant.fromEpochMilliseconds(epochSeconds * MILLIS_PER_ONE + millis.roundToLong())
}
