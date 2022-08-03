package com.udobny.kmp.ext

import kotlinx.datetime.Instant
import kotlinx.datetime.toNSDate

public actual fun Instant.toNativeDate(): Any = toNSDate()
