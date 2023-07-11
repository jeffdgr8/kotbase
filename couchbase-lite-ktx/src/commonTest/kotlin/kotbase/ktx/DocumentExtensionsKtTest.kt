/*
 * Copyright (c) 2020 MOLO17
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * From https://github.com/MOLO17/couchbase-lite-kotlin/blob/master/library/src/test/java/com/molo17/couchbase/lite/DocumentExtensionsKtTest.kt
 * Modified by Jeff Lockhart
 * - Use kotbase package
 */

package kotbase.ktx

import kotbase.ext.nowMillis
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Created by Damiano Giusti on 19/03/2020.
 */
class DocumentExtensionsKtTest {

    @Test
    fun MutableDocument_adds_properties_correctly() {
        val date = Clock.System.nowMillis()
        val document = MutableDocument {
            "string" to "test-string"
            "int" to 1
            "long" to 1L
            "float" to 1F
            "double" to 1.0
            "date" to date
        }
        assertEquals(expected = "test-string", actual = document.getString("string"))
        assertEquals(expected = 1, actual = document.getInt("int"))
        assertEquals(expected = 1L, actual = document.getLong("long"))
        assertEquals(expected = 1F, actual = document.getFloat("float"))
        assertEquals(expected = 1.0, actual = document.getDouble("double"))
        assertEquals(expected = date, actual = document.getDate("date"))
    }
}