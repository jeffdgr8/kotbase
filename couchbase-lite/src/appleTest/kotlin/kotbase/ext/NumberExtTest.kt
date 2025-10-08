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

import kotlinx.cinterop.toKString
import platform.Foundation.NSMutableDictionary
import platform.Foundation.NSNumber
import platform.Foundation.NSString
import platform.Foundation.valueForKey
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

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

    /**
     * Demonstrate behavior of Boolean <-> NSNumber
     */
    @Ignore
    @Test
    @Suppress("CAST_NEVER_SUCCEEDS")
    fun testBooleanNSNumber() {
        val yes = true as NSNumber
        val no = false as NSNumber
        val one = 1 as NSNumber
        val zero = 0 as NSNumber
        val long = 1L as NSNumber
        val double = 0.5 as NSNumber
        val float = 0.5F as NSNumber
        assertTrue(yes.isBoolean)
        assertTrue(no.isBoolean)
        assertFalse(one.isBoolean)
        assertFalse(zero.isBoolean)
        assertFalse(long.isBoolean)
        assertFalse(double.isBoolean)
        assertFalse(float.isBoolean)
        assertEquals(true, yes.boolValue)
        assertEquals(false, no.boolValue)
        assertEquals(true, yes as Boolean)
        assertEquals(false, no as Boolean)
        assertEquals(true, yes)
        assertEquals(false, no)

        val dict = NSMutableDictionary()
        dict.setObject(true, "yes" as NSString)
        assertEquals(true, dict.valueForKey("yes"))
        dict.setObject(1, "one" as NSString)
        assertNotEquals(true, dict.valueForKey("one") as? Boolean)
        dict.setObject(1L, "long" as NSString)
        assertNotEquals(true, dict.valueForKey("long") as? Boolean)

        println("yes (${yes::class}) objCType = ${yes.objCType!!.toKString()}")
        println("no (${no::class}) objCType = ${no.objCType!!.toKString()}")
        println("one (${one::class}) objCType = ${one.objCType!!.toKString()}")
        println("zero (${zero::class}) objCType = ${zero.objCType!!.toKString()}")
        println("long (${long::class}) objCType = ${long.objCType!!.toKString()}")
        println("double (${double::class}) objCType = ${double.objCType!!.toKString()}")
        println("float (${float::class}) objCType = ${float.objCType!!.toKString()}")
    }
}

// objCType
// Boolean = c
// Int = i
// Long = q
// Double = d
// Float = f
internal val NSNumber.isBoolean: Boolean
    get() = objCType!!.toKString() == "c"
