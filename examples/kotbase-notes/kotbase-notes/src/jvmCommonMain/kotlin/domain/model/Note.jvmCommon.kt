package domain.model

import kotlin.time.Instant
import java.time.LocalDateTime
import java.time.Month
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.time.toJavaInstant

actual fun Instant.toLocalizedString(): String {
    val javaInstant = toJavaInstant()
    val zoneId = ZoneOffset.systemDefault()
    val now = LocalDateTime.now()

    val startOfDay = now.toLocalDate()
        .atStartOfDay(zoneId)
        .toInstant()

    val startOfYear = YearMonth.of(now.year, Month.JANUARY)
        .atDay(1)
        .atStartOfDay(zoneId)
        .toInstant()

    val localDateTime = LocalDateTime.ofInstant(javaInstant, zoneId)

    return when {
        javaInstant.isAfter(startOfDay) -> localDateTime.formatAsTime()
        javaInstant.isAfter(startOfYear) -> localDateTime.formatAsDay()
        else -> localDateTime.formatAsFullDate()
    }
}

private fun LocalDateTime.formatAsTime(): String =
    format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))

private fun LocalDateTime.formatAsDay(): String =
    format(DateTimeFormatter.ofPattern("MMM d"))

private fun LocalDateTime.formatAsFullDate(): String =
    format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
