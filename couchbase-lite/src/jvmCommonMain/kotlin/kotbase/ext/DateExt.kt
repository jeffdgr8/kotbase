@file:JvmName("DateExtJvm") // https://youtrack.jetbrains.com/issue/KT-21186

package kotbase.ext

import kotlinx.datetime.Instant
import java.util.*

internal fun Instant.toDate(): Date = Date(toEpochMilliseconds())

internal fun Date.toKotlinInstant(): Instant = Instant.fromEpochMilliseconds(time)
