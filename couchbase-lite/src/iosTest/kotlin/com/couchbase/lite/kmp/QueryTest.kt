package com.couchbase.lite.kmp

import platform.Foundation.NSDateFormatter
import platform.Foundation.NSTimeZone
import platform.Foundation.defaultTimeZone
import platform.Foundation.timeZoneWithName

actual fun localToUTC(format: String, dateStr: String): String {
    val df = NSDateFormatter()
    df.dateFormat = format
    df.timeZone = NSTimeZone.defaultTimeZone
    val date = df.dateFromString(dateStr)!!
    df.dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    df.timeZone = NSTimeZone.timeZoneWithName("UTC")!!
    return df.stringFromDate(date).replace(".000", "")
}
