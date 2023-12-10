/*
 * Copyright 2022-2023 Jeff Lockhart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kotbase

import platform.Foundation.NSDateFormatter
import platform.Foundation.NSTimeZone
import platform.Foundation.defaultTimeZone
import platform.Foundation.timeZoneWithName

internal actual fun localToUTC(format: String, dateStr: String): String {
    val df = NSDateFormatter()
    df.dateFormat = format
    df.timeZone = NSTimeZone.defaultTimeZone
    val date = df.dateFromString(dateStr)!!
    df.dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    df.timeZone = NSTimeZone.timeZoneWithName("UTC")!!
    return df.stringFromDate(date).replace(".000", "")
}
