package kotbase

import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals

class MiscTest {

    // Verify that round trip String -> Date -> String doesn't alter the string (#1611)
    @Test
    fun testJSONDateRoundTrip() {
        val now = Clock.System.now()
        // internally calls the relevant JSONUtils.toJSONString() and JSONUtils.toDate()
        val array = MutableArray()
        val dateStr = array.addDate(now).getString(0)
        val date = array.getDate(0)!!
        assertEquals(now.toEpochMilliseconds(), date.toEpochMilliseconds())
        array.addDate(date)
        assertEquals(dateStr, array.getString(1))
    }
}
