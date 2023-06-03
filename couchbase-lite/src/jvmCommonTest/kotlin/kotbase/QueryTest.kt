package kotbase

import java.text.SimpleDateFormat
import java.util.*

actual fun localToUTC(format: String, dateStr: String): String {
    val tz = TimeZone.getDefault()
    var df = SimpleDateFormat(format)
    df.timeZone = tz
    val date = df.parse(dateStr)!!
    df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    df.timeZone = TimeZone.getTimeZone("UTC")
    return df.format(date).replace(".000", "")
}
