package com.udobny.kmm.ext

import kotlinx.datetime.Instant
import java.util.*

public fun Instant.toDate(): Date = Date(toEpochMilliseconds())

public fun Date.toKotlinInstant(): Instant = Instant.fromEpochMilliseconds(time)
