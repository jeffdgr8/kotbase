package com.couchbase.lite.kmp.internal.utils

import com.couchbase.lite.kmp.CouchbaseLiteException
import kotlin.reflect.KClass
import kotlin.reflect.cast
import kotlin.test.assertEquals
import kotlin.test.fail

object TestUtils {

    inline fun <reified T : Exception> assertThrows(
        noinline test: () -> Unit
    ) {
        assertThrows(T::class, test)
    }

    fun <T : Exception> assertThrows(
        ex: KClass<T>,
        test: () -> Unit
    ) {
        try {
            test()
            fail("Expecting exception: $ex")
        } catch (e: Throwable) {
            try {
                ex.cast(e)
            } catch (e1: ClassCastException) {
                fail("Expecting exception: $ex but got $e")
            }
        }
    }

    fun assertThrowsCBL(
        domain: String?,
        code: Int,
        task: () -> Unit
    ) {
        try {
            task()
            fail("Expected a CouchbaseLiteException")
        } catch (e: CouchbaseLiteException) {
            assertEquals(code, e.getCode())
            assertEquals(domain, e.getDomain())
        }
    }
}
