/*
 * Copyright 2025 Jeff Lockhart
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
package kotbase.ext

import kotlin.time.Clock
import kotlinx.datetime.toNSDate
import kotlin.test.Test
import kotlin.test.assertEquals

class DateExtTest {

    @Test
    fun testToKotlinInstantMillis() {
        val now = Clock.System.nowMillis()
        val nsDate = now.toNSDate()
        val result = nsDate.toKotlinInstantMillis()
        assertEquals(now, result)
    }
}
