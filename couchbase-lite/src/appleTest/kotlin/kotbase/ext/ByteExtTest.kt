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
package kotbase.ext

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.get
import kotlinx.cinterop.reinterpret
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Foundation.data
import kotlin.test.Test
import kotlin.test.assertEquals

class ByteExtTest {

    @Test
    fun test_toNSData() {
        val input = byteArrayOf(0x1, 0x2, 0x3)
        val output = input.toNSData()
        assertEquals(3UL, output.length)
        val bytes = output.bytes!!.reinterpret<ByteVar>()
        assertEquals(0x1, bytes[0])
        assertEquals(0x2, bytes[1])
        assertEquals(0x3, bytes[2])
    }

    @Test
    fun test_zeroLength_toNSData() {
        val input = byteArrayOf()
        val output = input.toNSData()
        assertEquals(0UL, output.length)
    }

    @Test
    fun test_toByteArray() {
        val input = NSData.create(base64Encoding = "AQID")!!
        val output = input.toByteArray()
        assertEquals(3, output.size)
        assertEquals(0x1, output[0])
        assertEquals(0x2, output[1])
        assertEquals(0x3, output[2])
    }

    @Test
    fun test_zeroLength_toByteArray() {
        val input = NSData.data()
        val output = input.toByteArray()
        assertEquals(0, output.size)
    }
}
