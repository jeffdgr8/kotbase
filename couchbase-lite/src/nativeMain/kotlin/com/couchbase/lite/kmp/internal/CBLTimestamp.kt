package com.couchbase.lite.kmp.internal

import kotlinx.datetime.Instant
import libcblite.CBLTimestamp

internal fun CBLTimestamp.toKotlinInstant(): Instant =
    Instant.fromEpochMilliseconds(this)