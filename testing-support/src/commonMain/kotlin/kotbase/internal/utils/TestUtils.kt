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
package kotbase.internal.utils

import kotbase.CouchbaseLiteException
import kotbase.code
import kotbase.domain
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
            assertEquals(code, e.code)
            assertEquals(domain, e.domain)
        }
    }
}
