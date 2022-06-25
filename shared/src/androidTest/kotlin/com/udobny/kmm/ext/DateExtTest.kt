package com.udobny.kmm.ext

import kotlinx.datetime.Instant
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DateExtTest {

    @Test
    fun test_toDate() {
        val millis = 1234567890123L
        val input = Instant.fromEpochMilliseconds(millis)
        val output = input.toDate()
        assertEquals(millis, output.time)
    }

    @Test
    fun test_toKotlinInstant() {
        val millis = 1234567890123L
        val input = Date(millis)
        val output = input.toKotlinInstant()
        assertEquals(millis, output.toEpochMilliseconds())
    }

    @Test
    fun test_toNativeDateDeep() {
        val input1 = "not a date"
        val output1 = input1.toNativeDateDeep()
        assertEquals(input1, output1)

        val millis = 1234567890123L
        val input2 = Instant.fromEpochMilliseconds(millis)
        val output2 = input2.toNativeDateDeep()
        assertTrue(output2 is Date)
        assertEquals(millis, output2.time)

        val input3 = listOf(input2)
        val output3 = input3.toNativeDateDeep()
        assertTrue(output3 is List<*>)
        assertEquals(output2, output3.first())

        val input4 = mapOf(0 to input2)
        val output4 = input4.toNativeDateDeep()
        assertTrue(output4 is Map<*, *>)
        assertEquals(output2, output4[0])
    }

    @Test
    fun test_toNativeDatesDeep() {
        val millis = 1234567890123L
        val input = listOf(
            0,
            "1",
            Instant.fromEpochMilliseconds(millis),
            null,
            listOf(Instant.fromEpochMilliseconds(millis)),
            mapOf("0" to Instant.fromEpochMilliseconds(millis))
        )
        val output = input.toNativeDatesDeep()
        assertEquals(6, output.size)
        assertEquals(0, output[0])
        assertEquals("1", output[1])
        assertNull(output[3])
        assertTrue(output[2] is Date)
        assertTrue(output[4] is List<*>)
        assertTrue(output[5] is Map<*, *>)
        assertEquals(millis, (output[2] as Date).time)
        assertEquals(output[2], (output[4] as List<*>)[0])
        assertEquals(output[2], (output[5] as Map<*, *>)["0"])
    }

    @Test
    fun test_toNativeDateValuesDeep() {
        val millis = 1234567890123L
        val input = mapOf(
            "0" to 0,
            "1" to "1",
            "2" to Instant.fromEpochMilliseconds(millis),
            "3" to null,
            "4" to listOf(Instant.fromEpochMilliseconds(millis)),
            "5" to mapOf("0" to Instant.fromEpochMilliseconds(millis))
        )
        val output = input.toNativeDateValuesDeep()
        assertEquals(6, output.size)
        assertEquals(0, output["0"])
        assertEquals("1", output["1"])
        assertNull(output["3"])
        assertTrue(output["2"] is Date)
        assertTrue(output["4"] is List<*>)
        assertTrue(output["5"] is Map<*, *>)
        assertEquals(millis, (output["2"] as Date).time)
        assertEquals(output["2"], (output["4"] as List<*>)[0])
        assertEquals(output["2"], (output["5"] as Map<*, *>)["0"])
    }
}
