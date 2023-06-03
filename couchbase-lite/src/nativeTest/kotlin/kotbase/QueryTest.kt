package kotbase

import kotlinx.datetime.*

actual fun localToUTC(format: String, dateStr: String): String {
    val instant = if (format.length == 10) {
        val date = LocalDate.parse(dateStr)
        date.atTime(0, 0).toInstant(TimeZone.currentSystemDefault())
    } else {
        val isoStr = dateStr.replace(' ', 'T')
        val date = LocalDateTime.parse(isoStr)
        date.toInstant(TimeZone.currentSystemDefault())
    }
    return instant.toString()
}
