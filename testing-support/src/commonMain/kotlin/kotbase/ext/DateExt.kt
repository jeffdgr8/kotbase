@file:JvmName("DateExtTesting") // https://youtrack.jetbrains.com/issue/KT-21186

package kotbase.ext

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.jvm.JvmName

/**
 * Returns the [Instant] corresponding to the current time,
 * according to this clock, with at most a millisecond precision.
 * The default [Clock.now] may be nanoseconds precision.
 */
fun Clock.nowMillis(): Instant =
    Instant.fromEpochMilliseconds(now().toEpochMilliseconds())
