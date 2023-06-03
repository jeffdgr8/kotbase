package kotbase.internal

import kotlinx.datetime.Instant
import libcblite.CBLTimestamp

internal fun CBLTimestamp.toKotlinInstant(): Instant? {
    return if (this != 0L) {
        Instant.fromEpochMilliseconds(this)
    } else null
}
