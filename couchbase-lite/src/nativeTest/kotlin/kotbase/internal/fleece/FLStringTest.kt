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
package kotbase.internal.fleece

import kotlinx.cinterop.*
import platform.posix.free
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class FLStringTest {

    @Test
    fun test_toFLStringScoped() {
        val kString = "doc1"
        val kBytes = kString.encodeToByteArray()
        assertEquals(4, kBytes.size)
        memScoped {
            val flString = kString.toFLString(this)
            flString.useContents {
                assertEquals(4U, size)
                val flBytes = buf?.reinterpret<ByteVar>()
                assertNotNull(flBytes)
                assertEquals(kBytes[0], flBytes[0])
                assertEquals(kBytes[1], flBytes[1])
                assertEquals(kBytes[2], flBytes[2])
                assertEquals(kBytes[3], flBytes[3])
                assertEquals(0, flBytes[4])
            }
        }

        val nullString: String? = null
        memScoped {
            val flString = nullString.toFLString(this)
            flString.useContents {
                assertEquals(0U, size)
                assertNull(buf)
            }
        }
    }

    @Test
    fun test_toFLStringHeap() {
        val kString = "doc1"
        val kBytes = kString.encodeToByteArray()
        assertEquals(4, kBytes.size)
        val flString = kString.toFLString()
        flString.useContents {
            assertEquals(4U, size)
            val flBytes = buf?.reinterpret<ByteVar>()
            assertNotNull(flBytes)
            assertEquals(kBytes[0], flBytes[0])
            assertEquals(kBytes[1], flBytes[1])
            assertEquals(kBytes[2], flBytes[2])
            assertEquals(kBytes[3], flBytes[3])
            assertEquals(0, flBytes[4])
            free(buf)
        }

        val nullString: String? = null
        val nullFlString = nullString.toFLString()
        nullFlString.useContents {
            assertEquals(0U, size)
            assertNull(buf)
        }
    }

    @Test
    fun test_toKString() {
        val kString = "doc1"
        memScoped {
            val flString = kString.toFLString(this)
            assertEquals(kString, flString.toKString())
        }
    }
}
