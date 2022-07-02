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
}
