/*
 * Copyright 2025 Jeff Lockhart
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
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import kotlin.math.roundToLong

internal fun NSDate.toKotlinInstantMillis(): Instant {
    val secs = timeIntervalSince1970()
    val fullSeconds = secs.toLong()
    val millis = (secs - fullSeconds) * MILLIS_PER_ONE
    return Instant.fromEpochMilliseconds(fullSeconds * MILLIS_PER_ONE + millis.roundToLong())
}
