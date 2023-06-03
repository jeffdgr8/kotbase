package kotbase.ext

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * An ISO 8601 UTC string with milliseconds
 * precision. The default [Instant.toString]
 * can be seconds or nanoseconds precision.
 */
public fun Instant.toStringMillis(): String {
    return toString().let {
        if (it.length == 20) {
            it.dropLast(1) + ".000Z"
        } else if (it.length > 24) {
            it.dropLast(it.length - 23) + 'Z'
        } else {
            it
        }
    }
}

/**
 * Returns the [Instant] corresponding to the current time,
 * according to this clock, with at most a millisecond precision.
 * The default [Clock.now] may be nanoseconds precision.
 */
public fun Clock.nowMillis(): Instant =
    Instant.fromEpochMilliseconds(now().toEpochMilliseconds())
