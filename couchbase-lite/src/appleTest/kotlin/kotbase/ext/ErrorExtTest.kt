package kotbase.ext

import kotlinx.cinterop.get
import kotlinx.cinterop.value
import platform.Foundation.NSError
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.fail

class ErrorExtTest {

    @Test
    fun test_wrapError() {
        assertFailsWith<NSErrorException> {
            wrapError(NSError::toException) {
                it[0].value = NSError.errorWithDomain("Kotlin", 0, null)
            }
            fail("exception should be thrown before here")
        }
    }
}
