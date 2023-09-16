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
package kotbase.ext

import platform.Foundation.NSNumber
import kotlin.test.Test
import kotlin.test.assertEquals

class NumberExtTest {

    @Test
    @Suppress("CAST_NEVER_SUCCEEDS")
    fun test_asNumber() {
        val zero = 0 as NSNumber
        assertEquals(0, zero.asNumber())
        val number = 42 as NSNumber
        assertEquals(42, number.asNumber())
        val negative = -100 as NSNumber
        assertEquals(-100, negative.asNumber())

        val yes = true as NSNumber
        assertEquals(1, yes.asNumber())
        val no = false as NSNumber
        assertEquals(0, no.asNumber())
    }
}
