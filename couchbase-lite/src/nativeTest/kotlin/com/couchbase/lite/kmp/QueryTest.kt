package com.couchbase.lite.kmp

import kotlinx.datetime.*

actual fun localToUTC(format: String, dateStr: String): String {
    val date = LocalDateTime.parse(dateStr)
    val instant = date.toInstant(TimeZone.currentSystemDefault())
    return instant.toLocalDateTime(TimeZone.UTC).toString()
}
