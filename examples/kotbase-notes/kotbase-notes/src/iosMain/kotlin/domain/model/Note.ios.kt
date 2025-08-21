package domain.model

import kotlin.time.Instant
import kotlinx.datetime.toNSDate
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterMediumStyle
import platform.Foundation.NSDateFormatterNoStyle
import platform.Foundation.NSDateFormatterShortStyle
import platform.Foundation.NSOrderedDescending
import platform.Foundation.compare

actual fun Instant.toLocalizedString(): String {
    val nsDate = toNSDate()
    val calendar = NSCalendar.currentCalendar
    val components = calendar.components(
        NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay,
        fromDate = nsDate
    )
    components.hour = 0
    components.minute = 0
    components.second = 0

    val startOfDay = calendar.dateFromComponents(components)!!

    components.month = 1
    components.day = 1

    val startOfYear = calendar.dateFromComponents(components)!!

    return when {
        nsDate.compare(startOfDay) == NSOrderedDescending -> nsDate.formatAsTime()
        nsDate.compare(startOfYear) == NSOrderedDescending -> nsDate.formatAsDay()
        else -> nsDate.formatAsFullDate()
    }
}

private fun NSDate.formatAsTime(): String {
    val formatter = NSDateFormatter().apply {
        dateStyle = NSDateFormatterNoStyle
        timeStyle = NSDateFormatterShortStyle
    }
    return formatter.stringFromDate(this)
}

private fun NSDate.formatAsDay(): String {
    val formatter = NSDateFormatter().apply {
        setLocalizedDateFormatFromTemplate("MMM d")
    }
    return formatter.stringFromDate(this)
}

private fun NSDate.formatAsFullDate(): String {
    val formatter = NSDateFormatter().apply {
        dateStyle = NSDateFormatterMediumStyle
        timeStyle = NSDateFormatterNoStyle
    }
    return formatter.stringFromDate(this)
}
