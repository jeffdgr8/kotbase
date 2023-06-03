@file:JvmName("DateExtJvm") // https://youtrack.jetbrains.com/issue/KT-21186

package kotbase.ext

import kotlinx.datetime.Instant
import java.util.*

public fun Instant.toDate(): Date = Date(toEpochMilliseconds())

public fun Date.toKotlinInstant(): Instant = Instant.fromEpochMilliseconds(time)
