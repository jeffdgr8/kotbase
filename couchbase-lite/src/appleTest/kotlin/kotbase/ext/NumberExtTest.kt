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
