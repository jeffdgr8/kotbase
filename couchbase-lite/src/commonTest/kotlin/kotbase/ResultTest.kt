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
package kotbase

import kotbase.ext.toStringMillis
import kotbase.internal.utils.JsonObject
import kotbase.internal.utils.paddedString
import kotlinx.datetime.Instant
import kotlin.test.*

class ResultTest : BaseQueryTest() {

    @Test
    fun testGetValueByKey() {
        runTest { query ->
            // run query
            val rows = verifyQueryWithEnumerator(
                query
            ) { _, r ->
                assertEquals(13, r.count)

                assertNull(r.getValue("null"))
                assertEquals(true, r.getValue("true"))
                assertEquals(false, r.getValue("false"))
                assertEquals("string", r.getValue("string"))
                assertEquals(0L, r.getValue("zero"))
                assertEquals(1L, r.getValue("one"))
                assertEquals(-1L, r.getValue("minus_one"))
                assertEquals(1.1, r.getValue("one_dot_one"))
                assertEquals(TEST_DATE, r.getValue("date"))
                assertTrue(r.getValue("dict") is Dictionary)
                assertTrue(r.getValue("array") is Array)
                assertTrue(r.getValue("blob") is Blob)
                assertNull(r.getValue("non_existing_key"))

                //assertFailsWith<IllegalArgumentException> { r.getValue(null) }

                assertNull(r.getValue("not_in_query_select"))
            }
            assertEquals(1, rows)
        }
    }

    private fun runTest(test: (Query) -> Unit) {
        for (i in 1..2) {
            val docID = prepareData(i)
            val query = generateQuery(docID)
            test(query)
        }
    }

    @Test
    fun testGetValue() {
        runTest { query ->
            // run query
            val rows = verifyQueryWithEnumerator(query) { _, r ->
                assertEquals(13, r.count)

                assertNull(r.getValue(0))
                assertEquals(true, r.getValue(1))
                assertEquals(false, r.getValue(2))
                assertEquals("string", r.getValue(3))
                assertEquals(0L, r.getValue(4))
                assertEquals(1L, r.getValue(5))
                assertEquals(-1L, r.getValue(6))
                assertEquals(1.1, r.getValue(7))
                assertEquals(TEST_DATE, r.getValue(8))
                assertTrue(r.getValue(9) is Dictionary)
                assertTrue(r.getValue(10) is Array)
                assertTrue(r.getValue(11) is Blob)
                assertNull(r.getValue(12))

                assertFailsWith<IndexOutOfBoundsException> { r.getValue(-1) }

                assertFailsWith<IndexOutOfBoundsException> { r.getValue(100) }
            }

            assertEquals(1, rows)
        }
    }

    @Test
    fun testGetStringByKey() {
        runTest { query ->
            val rows = verifyQueryWithEnumerator(query) { _, r ->
                assertEquals(13, r.count)

                assertNull(r.getString("null"))
                assertNull(r.getString("true"))
                assertNull(r.getString("false"))
                assertEquals("string", r.getString("string"))
                assertNull(r.getString("zero"))
                assertNull(r.getString("one"))
                assertNull(r.getString("minus_one"))
                assertNull(r.getString("one_dot_one"))
                assertEquals(TEST_DATE, r.getString("date"))
                assertNull(r.getString("dict"))
                assertNull(r.getString("array"))
                assertNull(r.getString("blob"))
                assertNull(r.getString("non_existing_key"))

                //assertFailsWith<IllegalArgumentException> { r.getString(null) }

                assertNull(r.getString("not_in_query_select"))
            }

            assertEquals(1, rows)
        }
    }

    @Test
    fun testGetString() {
        runTest { query ->
            val rows = verifyQueryWithEnumerator(query) { _, r ->
                assertEquals(13, r.count)

                assertNull(r.getString(0))
                assertNull(r.getString(1))
                assertNull(r.getString(2))
                assertEquals("string", r.getString(3))
                assertNull(r.getString(4))
                assertNull(r.getString(5))
                assertNull(r.getString(6))
                assertNull(r.getString(7))
                assertEquals(TEST_DATE, r.getString(8))
                assertNull(r.getString(9))
                assertNull(r.getString(10))
                assertNull(r.getString(11))
                assertNull(r.getString(12))

                assertFailsWith<IndexOutOfBoundsException> { r.getString(-1) }

                assertFailsWith<IndexOutOfBoundsException> { r.getString(100) }
            }

            assertEquals(1, rows)
        }
    }


    @Test
    fun testGetNumberByKey() {
        runTest { query ->
            val rows = verifyQueryWithEnumerator(query) { _, r ->
                assertEquals(13, r.count)

                assertNull(r.getNumber("null"))
                assertEquals(1, r.getNumber("true")!!.toInt())
                assertEquals(0, r.getNumber("false")!!.toInt())
                assertNull(r.getNumber("string"))
                assertEquals(0, r.getNumber("zero")!!.toInt())
                assertEquals(1, r.getNumber("one")!!.toInt())
                assertEquals(-1, r.getNumber("minus_one")!!.toInt())
                assertEquals(1.1, r.getNumber("one_dot_one"))
                assertNull(r.getNumber("date"))
                assertNull(r.getNumber("dict"))
                assertNull(r.getNumber("array"))
                assertNull(r.getNumber("blob"))
                assertNull(r.getNumber("non_existing_key"))

                //assertFailsWith<IllegalArgumentException> { r.getNumber(null) }

                assertNull(r.getNumber("not_in_query_select"))
            }

            assertEquals(1, rows)
        }
    }

    @Test
    fun testGetNumber() {
        runTest { query ->
            val rows = verifyQueryWithEnumerator(query) { _, r ->
                assertEquals(13, r.count)

                assertNull(r.getNumber(0)) // null
                assertEquals(1, r.getNumber(1)!!.toInt()) // true
                assertEquals(0, r.getNumber(2)!!.toInt()) // false
                assertNull(r.getNumber(3)) // string
                assertEquals(0, r.getNumber(4)!!.toInt())
                assertEquals(1, r.getNumber(5)!!.toInt())
                assertEquals(-1, r.getNumber(6)!!.toInt())
                assertEquals(1.1, r.getNumber(7))
                assertNull(r.getNumber(8))
                assertNull(r.getNumber(9))
                assertNull(r.getNumber(10))
                assertNull(r.getNumber(11))
                assertNull(r.getNumber(12))

                assertFailsWith<IndexOutOfBoundsException> { r.getNumber(-1) }

                assertFailsWith<IndexOutOfBoundsException> { r.getNumber(100) }
            }

            assertEquals(1, rows)
        }
    }

    @Test
    fun testGetIntegerByKey() {
        runTest { query ->
            val rows = verifyQueryWithEnumerator(query) { _, r ->
                assertEquals(13, r.count)

                assertEquals(0, r.getInt("null"))
                assertEquals(1, r.getInt("true"))
                assertEquals(0, r.getInt("false"))
                assertEquals(0, r.getInt("string"))
                assertEquals(0, r.getInt("zero"))
                assertEquals(1, r.getInt("one"))
                assertEquals(-1, r.getInt("minus_one"))
                assertEquals(1, r.getInt("one_dot_one"))
                assertEquals(0, r.getInt("date"))
                assertEquals(0, r.getInt("dict"))
                assertEquals(0, r.getInt("array"))
                assertEquals(0, r.getInt("blob"))
                assertEquals(0, r.getInt("non_existing_key"))

                //assertFailsWith<IllegalArgumentException> { r.getInt(null) }

                assertEquals(0, r.getInt("not_in_query_select"))
            }

            assertEquals(1, rows)
        }
    }

    @Test
    fun testGetInteger() {
        runTest { query ->
            val rows = verifyQueryWithEnumerator(query) { _, r ->
                assertEquals(13, r.count)

                assertEquals(0, r.getInt(0))
                assertEquals(1, r.getInt(1))
                assertEquals(0, r.getInt(2))
                assertEquals(0, r.getInt(3))
                assertEquals(0, r.getInt(4))
                assertEquals(1, r.getInt(5))
                assertEquals(-1, r.getInt(6))
                assertEquals(1, r.getInt(7))
                assertEquals(0, r.getInt(8))
                assertEquals(0, r.getInt(9))
                assertEquals(0, r.getInt(10))
                assertEquals(0, r.getInt(11))
                assertEquals(0, r.getInt(12))

                assertFailsWith<IndexOutOfBoundsException> { r.getInt(-1) }

                assertFailsWith<IndexOutOfBoundsException> { r.getInt(100) }
            }

            assertEquals(1, rows)
        }
    }

    @Test
    fun testGetLongByKey() {
        runTest { query ->
            val rows = verifyQueryWithEnumerator(query) { _, r ->
                assertEquals(13, r.count)

                assertEquals(0, r.getLong("null"))
                assertEquals(1, r.getLong("true"))
                assertEquals(0, r.getLong("false"))
                assertEquals(0, r.getLong("string"))
                assertEquals(0, r.getLong("zero"))
                assertEquals(1, r.getLong("one"))
                assertEquals(-1, r.getLong("minus_one"))
                assertEquals(1, r.getLong("one_dot_one"))
                assertEquals(0, r.getLong("date"))
                assertEquals(0, r.getLong("dict"))
                assertEquals(0, r.getLong("array"))
                assertEquals(0, r.getLong("blob"))
                assertEquals(0, r.getLong("non_existing_key"))

                //assertFailsWith<IllegalArgumentException> { r.getLong(null) }

                assertEquals(0, r.getLong("not_in_query_select"))
            }

            assertEquals(1, rows)
        }
    }

    @Test
    fun testGetLong() {
        runTest { query ->
            val rows = verifyQueryWithEnumerator(query) { _, r ->
                assertEquals(13, r.count)

                assertEquals(0, r.getLong(0))
                assertEquals(1, r.getLong(1))
                assertEquals(0, r.getLong(2))
                assertEquals(0, r.getLong(3))
                assertEquals(0, r.getLong(4))
                assertEquals(1, r.getLong(5))
                assertEquals(-1, r.getLong(6))
                assertEquals(1, r.getLong(7))
                assertEquals(0, r.getLong(8))
                assertEquals(0, r.getLong(9))
                assertEquals(0, r.getLong(10))
                assertEquals(0, r.getLong(11))
                assertEquals(0, r.getLong(12))

                assertFailsWith<IndexOutOfBoundsException> { r.getLong(-1) }

                assertFailsWith<IndexOutOfBoundsException> { r.getLong(100) }
            }

            assertEquals(1, rows)
        }
    }

    @Test
    fun testGetFloatByKey() {
        runTest { query ->
            val rows = verifyQueryWithEnumerator(query) { _, r ->
                assertEquals(13, r.count)

                assertEquals(0.0f, r.getFloat("null"), 0.0f)
                assertEquals(1.0f, r.getFloat("true"), 0.0f)
                assertEquals(0.0f, r.getFloat("false"), 0.0f)
                assertEquals(0.0f, r.getFloat("string"), 0.0f)
                assertEquals(0.0f, r.getFloat("zero"), 0.0f)
                assertEquals(1.0f, r.getFloat("one"), 0.0f)
                assertEquals(-1.0f, r.getFloat("minus_one"), 0.0f)
                assertEquals(1.1f, r.getFloat("one_dot_one"), 0.0f)
                assertEquals(0.0f, r.getFloat("date"), 0.0f)
                assertEquals(0.0f, r.getFloat("dict"), 0.0f)
                assertEquals(0.0f, r.getFloat("array"), 0.0f)
                assertEquals(0.0f, r.getFloat("blob"), 0.0f)
                assertEquals(0.0f, r.getFloat("non_existing_key"), 0.0f)

                //assertFailsWith<IllegalArgumentException> { r.getFloat(null) }

                assertEquals(0.0f, r.getFloat("not_in_query_select"), 0.0f)
            }
            assertEquals(1, rows)
        }
    }

    @Test
    fun testGetFloat() {
        runTest { query ->
            val rows = verifyQueryWithEnumerator(query) { _, r ->
                assertEquals(13, r.count)

                assertEquals(0.0f, r.getFloat(0), 0.0f)
                assertEquals(1.0f, r.getFloat(1), 0.0f)
                assertEquals(0.0f, r.getFloat(2), 0.0f)
                assertEquals(0.0f, r.getFloat(3), 0.0f)
                assertEquals(0.0f, r.getFloat(4), 0.0f)
                assertEquals(1.0f, r.getFloat(5), 0.0f)
                assertEquals(-1.0f, r.getFloat(6), 0.0f)
                assertEquals(1.1f, r.getFloat(7), 0.0f)
                assertEquals(0.0f, r.getFloat(8), 0.0f)
                assertEquals(0.0f, r.getFloat(9), 0.0f)
                assertEquals(0.0f, r.getFloat(10), 0.0f)
                assertEquals(0.0f, r.getFloat(11), 0.0f)
                assertEquals(0.0f, r.getFloat(12), 0.0f)

                assertFailsWith<IndexOutOfBoundsException> { r.getFloat(-1) }

                assertFailsWith<IndexOutOfBoundsException> { r.getFloat(100) }
            }

            assertEquals(1, rows)
        }
    }

    @Test
    fun testGetDoubleByKey() {
        runTest { query ->
            val rows = verifyQueryWithEnumerator(query) { _, r ->
                assertEquals(13, r.count)

                assertEquals(0.0, r.getDouble("null"), 0.0)
                assertEquals(1.0, r.getDouble("true"), 0.0)
                assertEquals(0.0, r.getDouble("false"), 0.0)
                assertEquals(0.0, r.getDouble("string"), 0.0)
                assertEquals(0.0, r.getDouble("zero"), 0.0)
                assertEquals(1.0, r.getDouble("one"), 0.0)
                assertEquals(-1.0, r.getDouble("minus_one"), 0.0)
                assertEquals(1.1, r.getDouble("one_dot_one"), 0.0)
                assertEquals(0.0, r.getDouble("date"), 0.0)
                assertEquals(0.0, r.getDouble("dict"), 0.0)
                assertEquals(0.0, r.getDouble("array"), 0.0)
                assertEquals(0.0, r.getDouble("blob"), 0.0)
                assertEquals(0.0, r.getDouble("non_existing_key"), 0.0)

                //assertFailsWith<IllegalArgumentException> { r.getDouble(null) }

                assertEquals(0.0, r.getDouble("not_in_query_select"), 0.0)
            }

            assertEquals(1, rows)
        }
    }

    @Test
    fun testGetDouble() {
        runTest { query ->
            val rows = verifyQueryWithEnumerator(query) { _, r ->
                assertEquals(13, r.count)

                assertEquals(0.0, r.getDouble(0), 0.0)
                assertEquals(1.0, r.getDouble(1), 0.0)
                assertEquals(0.0, r.getDouble(2), 0.0)
                assertEquals(0.0, r.getDouble(3), 0.0)
                assertEquals(0.0, r.getDouble(4), 0.0)
                assertEquals(1.0, r.getDouble(5), 0.0)
                assertEquals(-1.0, r.getDouble(6), 0.0)
                assertEquals(1.1, r.getDouble(7), 0.0)
                assertEquals(0.0, r.getDouble(8), 0.0)
                assertEquals(0.0, r.getDouble(9), 0.0)
                assertEquals(0.0, r.getDouble(10), 0.0)
                assertEquals(0.0, r.getDouble(11), 0.0)
                assertEquals(0.0, r.getDouble(12), 0.0)

                assertFailsWith<IndexOutOfBoundsException> { r.getDouble(-1) }

                assertFailsWith<IndexOutOfBoundsException> { r.getDouble(100) }
            }

            assertEquals(1, rows)
        }
    }

    @Test
    fun testGetBooleanByKey() {
        runTest { query ->
            val rows = verifyQueryWithEnumerator(query) { _, r ->
                assertEquals(13, r.count)

                assertFalse(r.getBoolean("null"))
                assertTrue(r.getBoolean("true"))
                assertFalse(r.getBoolean("false"))
                assertTrue(r.getBoolean("string"))
                assertFalse(r.getBoolean("zero"))
                assertTrue(r.getBoolean("one"))
                assertTrue(r.getBoolean("minus_one"))
                assertTrue(r.getBoolean("one_dot_one"))
                assertTrue(r.getBoolean("date"))
                assertTrue(r.getBoolean("dict"))
                assertTrue(r.getBoolean("array"))
                assertTrue(r.getBoolean("blob"))
                assertFalse(r.getBoolean("non_existing_key"))

                //assertFailsWith<IllegalArgumentException> { r.getBoolean(null) }

                assertFalse(r.getBoolean("not_in_query_select"))
            }

            assertEquals(1, rows)
        }
    }

    @Test
    fun testGetBoolean() {
        runTest { query ->
            val rows = verifyQueryWithEnumerator(query) { _, r ->
                assertEquals(13, r.count)

                assertFalse(r.getBoolean(0))
                assertTrue(r.getBoolean(1))
                assertFalse(r.getBoolean(2))
                assertTrue(r.getBoolean(3))
                assertFalse(r.getBoolean(4))
                assertTrue(r.getBoolean(5))
                assertTrue(r.getBoolean(6))
                assertTrue(r.getBoolean(7))
                assertTrue(r.getBoolean(8))
                assertTrue(r.getBoolean(9))
                assertTrue(r.getBoolean(10))
                assertTrue(r.getBoolean(11))
                assertFalse(r.getBoolean(12))

                assertFailsWith<IndexOutOfBoundsException> { r.getBoolean(-1) }

                assertFailsWith<IndexOutOfBoundsException> { r.getBoolean(100) }
            }

            assertEquals(1, rows)
        }
    }

    @Test
    fun testGetDateByKey() {
        runTest { query ->
            val rows = verifyQueryWithEnumerator(query) { _, r ->
                assertEquals(13, r.count)

                assertNull(r.getDate("null"))
                assertNull(r.getDate("true"))
                assertNull(r.getDate("false"))
                assertNull(r.getDate("string"))
                assertNull(r.getDate("zero"))
                assertNull(r.getDate("one"))
                assertNull(r.getDate("minus_one"))
                assertNull(r.getDate("one_dot_one"))
                assertEquals(TEST_DATE, r.getDate("date")!!.toStringMillis())
                assertNull(r.getDate("dict"))
                assertNull(r.getDate("array"))
                assertNull(r.getDate("blob"))
                assertNull(r.getDate("non_existing_key"))

                //assertFailsWith<IllegalArgumentException> { r.getDate(null) }

                assertNull(r.getDate("not_in_query_select"))
            }

            assertEquals(1, rows)
        }
    }

    @Test
    fun testGetDate() {
        runTest { query ->
            val rows = verifyQueryWithEnumerator(query) { _, r ->
                assertEquals(13, r.count)

                assertNull(r.getDate(0))
                assertNull(r.getDate(1))
                assertNull(r.getDate(2))
                assertNull(r.getDate(3))
                assertNull(r.getDate(4))
                assertNull(r.getDate(5))
                assertNull(r.getDate(6))
                assertNull(r.getDate(7))
                assertEquals(TEST_DATE, r.getDate(8)!!.toStringMillis())
                assertNull(r.getDate(9))
                assertNull(r.getDate(10))
                assertNull(r.getDate(11))
                assertNull(r.getDate(12))

                assertFailsWith<IndexOutOfBoundsException> { r.getDate(-1) }

                assertFailsWith<IndexOutOfBoundsException> { r.getDate(100) }
            }

            assertEquals(1, rows)
        }
    }

    @Test
    fun testGetBlobByKey() {
        runTest { query ->
            val rows = verifyQueryWithEnumerator(query) { _, r ->
                assertEquals(13, r.count)

                assertNull(r.getBlob("null"))
                assertNull(r.getBlob("true"))
                assertNull(r.getBlob("false"))
                assertNull(r.getBlob("string"))
                assertNull(r.getBlob("zero"))
                assertNull(r.getBlob("one"))
                assertNull(r.getBlob("minus_one"))
                assertNull(r.getBlob("one_dot_one"))
                assertNull(r.getBlob("date"))
                assertNull(r.getBlob("dict"))
                assertNull(r.getBlob("array"))
                assertEquals(BLOB_CONTENT, r.getBlob("blob")!!.content!!.decodeToString())
                assertContentEquals(
                    BLOB_CONTENT.encodeToByteArray(),
                    r.getBlob("blob")!!.content
                )
                assertNull(r.getBlob("non_existing_key"))

                //assertFailsWith<IllegalArgumentException> { r.getBlob(null) }

                assertNull(r.getBlob("not_in_query_select"))
            }

            assertEquals(1, rows)
        }
    }

    @Test
    fun testGetBlob() {
        runTest { query ->
            val rows = verifyQueryWithEnumerator(query) { _, r ->
                assertEquals(13, r.count)

                assertNull(r.getBlob(0))
                assertNull(r.getBlob(1))
                assertNull(r.getBlob(2))
                assertNull(r.getBlob(3))
                assertNull(r.getBlob(4))
                assertNull(r.getBlob(5))
                assertNull(r.getBlob(6))
                assertNull(r.getBlob(7))
                assertNull(r.getBlob(8))
                assertNull(r.getBlob(9))
                assertNull(r.getBlob(10))
                assertEquals(BLOB_CONTENT, r.getBlob(11)!!.content!!.decodeToString())
                assertContentEquals(
                    BLOB_CONTENT.encodeToByteArray(),
                    r.getBlob(11)!!.content
                )
                assertNull(r.getBlob(12))

                assertFailsWith<IndexOutOfBoundsException> { r.getBlob(-1) }

                assertFailsWith<IndexOutOfBoundsException> { r.getBlob(100) }
            }

            assertEquals(1, rows)
        }
    }

    @Test
    fun testGetDictionaryByKey() {
        runTest { query ->
            val rows = verifyQueryWithEnumerator(query) { _, r ->
                assertEquals(13, r.count)

                assertNull(r.getDictionary("null"))
                assertNull(r.getDictionary("true"))
                assertNull(r.getDictionary("false"))
                assertNull(r.getDictionary("string"))
                assertNull(r.getDictionary("zero"))
                assertNull(r.getDictionary("one"))
                assertNull(r.getDictionary("minus_one"))
                assertNull(r.getDictionary("one_dot_one"))
                assertNull(r.getDictionary("date"))
                assertNotNull(r.getDictionary("dict"))
                val dict = mapOf(
                    "street" to "1 Main street",
                    "city" to "Mountain View",
                    "state" to "CA"
                )
                assertEquals(dict, r.getDictionary("dict")!!.toMap())
                assertNull(r.getDictionary("array"))
                assertNull(r.getDictionary("blob"))
                assertNull(r.getDictionary("non_existing_key"))

                //assertFailsWith<IllegalArgumentException> { r.getDictionary(null) }

                assertNull(r.getDictionary("not_in_query_select"))
            }

            assertEquals(1, rows)
        }
    }

    @Test
    fun testGetDictionary() {
        runTest { query ->
            val rows = verifyQueryWithEnumerator(query) { _, r ->
                assertEquals(13, r.count)

                assertNull(r.getDictionary(0))
                assertNull(r.getDictionary(1))
                assertNull(r.getDictionary(2))
                assertNull(r.getDictionary(3))
                assertNull(r.getDictionary(4))
                assertNull(r.getDictionary(5))
                assertNull(r.getDictionary(6))
                assertNull(r.getDictionary(7))
                assertNull(r.getDictionary(8))
                assertNotNull(r.getDictionary(9))
                val dict = mapOf(
                    "street" to "1 Main street",
                    "city" to "Mountain View",
                    "state" to "CA"
                )
                assertEquals(dict, r.getDictionary(9)!!.toMap())
                assertNull(r.getDictionary(10))
                assertNull(r.getDictionary(11))
                assertNull(r.getDictionary(12))

                assertFailsWith<IndexOutOfBoundsException> { r.getDictionary(-1) }

                assertFailsWith<IndexOutOfBoundsException> { r.getDictionary(100) }
            }

            assertEquals(1, rows)
        }
    }

    @Test
    fun testGetArrayByKey() {
        runTest { query ->
            val rows = verifyQueryWithEnumerator(query) { _, r ->
                assertEquals(13, r.count)

                assertNull(r.getArray("null"))
                assertNull(r.getArray("true"))
                assertNull(r.getArray("false"))
                assertNull(r.getArray("string"))
                assertNull(r.getArray("zero"))
                assertNull(r.getArray("one"))
                assertNull(r.getArray("minus_one"))
                assertNull(r.getArray("one_dot_one"))
                assertNull(r.getArray("date"))
                assertNull(r.getArray("dict"))
                assertNotNull(r.getArray("array"))
                val list = listOf("650-123-0001", "650-123-0002")
                assertEquals(list, r.getArray("array")!!.toList())
                assertNull(r.getArray("blob"))
                assertNull(r.getArray("non_existing_key"))

                //assertFailsWith<IllegalArgumentException> { r.getArray(null) }

                assertNull(r.getArray("not_in_query_select"))
            }
            assertEquals(1, rows)
        }
    }

    @Test
    fun testGetArray() {
        runTest { query ->
            val rows = verifyQueryWithEnumerator(query) { _, r ->
                assertEquals(13, r.count)

                assertNull(r.getArray(0))
                assertNull(r.getArray(1))
                assertNull(r.getArray(2))
                assertNull(r.getArray(3))
                assertNull(r.getArray(4))
                assertNull(r.getArray(5))
                assertNull(r.getArray(6))
                assertNull(r.getArray(7))
                assertNull(r.getArray(8))
                assertNull(r.getArray(9))
                assertNotNull(r.getArray(10))
                val list = listOf("650-123-0001", "650-123-0002")
                assertEquals(list, r.getArray(10)!!.toList())
                assertNull(r.getArray(11))
                assertNull(r.getArray(12))

                assertFailsWith<IndexOutOfBoundsException> { r.getArray(-1) }

                assertFailsWith<IndexOutOfBoundsException> { r.getArray(100) }
            }
            assertEquals(1, rows)
        }
    }

    @Test
    fun testGetKeys() {
        runTest { query ->
            val rows = verifyQueryWithEnumerator(query) { _, r ->
                val keys = r.keys
                assertNotNull(keys)
                assertEquals(13, keys.size)
                val expected = listOf(
                    "null",
                    "true",
                    "false",
                    "string",
                    "zero",
                    "one",
                    "minus_one",
                    "one_dot_one",
                    "date",
                    "dict",
                    "array",
                    "blob",
                    "non_existing_key"
                )
                assertEquals(expected.sorted(), keys.sorted())

                // Result.iterator() test
                val itr = r.iterator()
                var i1 = 0
                while (itr.hasNext()) {
                    assertTrue(expected.contains(itr.next()))
                    i1++
                }

                assertEquals(expected.size, i1)
            }
            assertEquals(1, rows)
        }
    }

    @Test
    fun testContains() {
        runTest { query ->
            val rows = verifyQueryWithEnumerator(query) { _, r ->
                // exists -> true
                val expected = listOf(
                    "null", "true", "false", "string", "zero", "one",
                    "minus_one", "one_dot_one", "date", "dict", "array",
                    "blob"
                )
                for (key in expected) { assertTrue(r.contains(key)) }
                // not exists -> false
                assertFalse(r.contains("non_existing_key"))

                //assertFailsWith<IllegalArgumentException> { r.contains(null) }

                assertFalse(r.contains("not_in_query_select"))
            }

            assertEquals(1, rows)
        }
    }

    // Contributed by Bryan Welter:
    // https://github.com/couchbase/couchbase-lite-android-ce/issues/27
    @Test
    fun testEmptyDict() {
        val docId = docId()
        val key = getUniqueName("emptyDict")

        val mDoc = MutableDocument(docId)
        mDoc.setDictionary(key, MutableDictionary())
        saveDocInCollection(mDoc)

        val query = QueryBuilder.select(SelectResult.property(key))
            .from(DataSource.collection(testCollection))
            .where(Meta.id.equalTo(Expression.string(docId)))

        query.execute().use { results ->
            assertNotNull(results)
            for (result in results.allResults()) {
                assertNotNull(result)
                assertEquals(1, result.toMap().size)
                val emptyDict = result.getDictionary(key)
                assertNotNull(emptyDict)
                assertTrue(emptyDict.count == 0)
            }
        }
    }

    @Test
    fun testResultRefAfterClose() {
        val mDoc = makeDocument()
        saveDocInCollection(mDoc)

        val testCollection = testCollection
        val result: Result?
        val dict: Dictionary?
        val array: Array?
        val results: ResultSet = QueryBuilder
            .createQuery("SELECT * FROM " + testCollection.fullName, testCollection.database)
            .execute()

        result = results.next()
        assertNotNull(result)

        dict = result.getDictionary(0)
        assertNotNull(dict)

        array = dict.getArray("doc-25")
        assertNotNull(array)

        val `val` = array.getString(20)
        assertNotNull(`val`)

        results.close()

        assertNull(results.next())
        // behavior differs between platforms because of caching in Kotbase and native platforms not implementing ResultSet.close()
        //assertFailsWith<CouchbaseLiteError> { result.getDictionary(0) }
        //assertFailsWith<CouchbaseLiteError> { dict.getArray("doc-25") }
        //assertFailsWith<CouchbaseLiteError> { array.getString(20) }
    }


    ///////////////  JSON tests

    // JSON 3.8
    @Test
    fun testResultToJSON() {
        for (i in 0..4) {
            val mDoc = makeDocument()
            mDoc.setString("id", "jsonQuery-$i")
            saveDocInCollection(mDoc)
        }

        val projection = Array(29) { i ->
            // `makeDocument` creates a document with 29 properties named doc-1 through doc-29
            SelectResult.property("doc-${i + 1}")
        }

        QueryBuilder.select(*projection)
            .from(DataSource.collection(testCollection))
            .where(Expression.property("id").equalTo(Expression.string("jsonQuery-4")))
            .execute().use { results ->
                val result = results.next()
                assertNotNull(result)
                assertNull(results.next())

                verifyDocument(result)
                verifyDocument(JsonObject(result.toJSON()))
            }
    }


    ///////////////  Tooling
    // !!! Should be using the standard data tools
    private fun docId() = getUniqueName("doc")

    private fun docId(i: Int) = "doc-${i.paddedString(3)}"

    private fun prepareData(i: Int): String {
        val mDoc = MutableDocument(docId(i))
        if (i % 2 == 1) { populateData(mDoc) }
        else { populateDataByTypedSetter(mDoc) }
        saveDocInCollection(mDoc)
        return mDoc.id
    }

    private fun populateData(doc: MutableDocument) {
        doc.setValue("true", true)
        doc.setValue("false", false)
        doc.setValue("string", "string")
        doc.setValue("zero", 0)
        doc.setValue("one", 1)
        doc.setValue("minus_one", -1)
        doc.setValue("one_dot_one", 1.1)
        doc.setValue("date", Instant.parse(TEST_DATE))
        doc.setValue("null", null)

        // Dictionary:
        val dict = MutableDictionary()
        dict.setValue("street", "1 Main street")
        dict.setValue("city", "Mountain View")
        dict.setValue("state", "CA")
        doc.setValue("dict", dict)

        // Array:
        val array = MutableArray()
        array.addValue("650-123-0001")
        array.addValue("650-123-0002")
        doc.setValue("array", array)

        // Blob:
        doc.setValue("blob", Blob("text/plain", BLOB_CONTENT.encodeToByteArray()))
    }

    private fun populateDataByTypedSetter(doc: MutableDocument) {
        doc.setBoolean("true", true)
        doc.setBoolean("false", false)
        doc.setString("string", "string")
        doc.setNumber("zero", 0)
        doc.setInt("one", 1)
        doc.setLong("minus_one", -1)
        doc.setDouble("one_dot_one", 1.1)
        doc.setDate("date", Instant.parse(TEST_DATE))
        doc.setString("null", null)

        // Dictionary:
        val dict = MutableDictionary()
        dict.setString("street", "1 Main street")
        dict.setString("city", "Mountain View")
        dict.setString("state", "CA")
        doc.setDictionary("dict", dict)

        // Array:
        val array = MutableArray()
        array.addString("650-123-0001")
        array.addString("650-123-0002")
        doc.setArray("array", array)

        // Blob:
        doc.setValue("blob", Blob("text/plain", BLOB_CONTENT.encodeToByteArray()))
    }

    private fun generateQuery(docID: String) = generateQuery(docID, testCollection)

    private fun generateQuery(docID: String, collection: Collection): Query {
        return QueryBuilder.select(
                SelectResult.property("null"),
                SelectResult.property("true"),
                SelectResult.property("false"),
                SelectResult.property("string"),
                SelectResult.property("zero"),
                SelectResult.property("one"),
                SelectResult.property("minus_one"),
                SelectResult.property("one_dot_one"),
                SelectResult.property("date"),
                SelectResult.property("dict"),
                SelectResult.property("array"),
                SelectResult.property("blob"),
                SelectResult.property("non_existing_key")
            )
            .from(DataSource.collection(collection))
            .where(Meta.id.equalTo(Expression.string(docID)))
    }
}
