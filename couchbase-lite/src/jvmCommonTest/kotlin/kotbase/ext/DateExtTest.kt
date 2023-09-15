package kotbase.ext

import kotlinx.datetime.Instant
import kotlin.test.Test
import java.util.*
import kotlin.test.assertEquals

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
