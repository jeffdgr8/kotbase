package com.couchbase.lite.kmp

import com.couchbase.lite.kmp.internal.utils.TestUtils.assertThrows
import com.udobny.kmp.ArrayIndexOutOfBoundsException
import com.udobny.kmp.ext.toStringMillis
import com.udobny.kmp.use
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.test.*

class ResultTest : BaseQueryTest() {

    @Test
    fun testGetValueByKey() {
        for (i in 1..2) {
            val docID = prepareData(i)
            val query = generateQuery(baseTestDb, docID)

            // run query
            val rows = verifyQuery(query, false) { _, r ->
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

                assertNull(r.getValue("not_in_query_select"))
            }

            assertEquals(1, rows)
        }
    }

    @Test
    fun testGetValue() {
        for (i in 1..2) {
            val docID = prepareData(i)
            val query = generateQuery(baseTestDb, docID)

            // run query
            val rows = verifyQuery(query, false) { _, r ->
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

                assertThrows(ArrayIndexOutOfBoundsException::class) { r.getValue(-1) }

                assertThrows(ArrayIndexOutOfBoundsException::class) { r.getValue(100) }
            }

            assertEquals(1, rows)
        }
    }

    @Test
    fun testGetStringByKey() {
        for (i in 1..2) {
            val docID = prepareData(i)
            val query = generateQuery(baseTestDb, docID)

            val rows = verifyQuery(query, false) { _, r ->
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

                assertNull(r.getString("not_in_query_select"))
            }

            assertEquals(1, rows)
        }
    }

    @Test
    fun testGetString() {
        for (i in 1..2) {
            val docID = prepareData(i)
            val query = generateQuery(baseTestDb, docID)

            val rows = verifyQuery(query, false) { _, r ->
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

                assertThrows(ArrayIndexOutOfBoundsException::class) { r.getString(-1) }

                assertThrows(ArrayIndexOutOfBoundsException::class) { r.getString(100) }
            }

            assertEquals(1, rows)
        }
    }

    @Test
    fun testGetNumberByKey() {
        for (i in 1..2) {
            val docID = prepareData(i)
            val query = generateQuery(baseTestDb, docID)

            val rows = verifyQuery(query, false) { _, r ->
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

                assertNull(r.getNumber("not_in_query_select"))
            }

            assertEquals(1, rows)
        }
    }

    @Test
    fun testGetNumber() {
        for (i in 1..2) {
            val docID = prepareData(i)
            val query = generateQuery(baseTestDb, docID)

            val rows = verifyQuery(query, false) { _, r ->
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

                assertThrows(ArrayIndexOutOfBoundsException::class) { r.getNumber(-1) }

                assertThrows(ArrayIndexOutOfBoundsException::class) { r.getNumber(100) }
            }

            assertEquals(1, rows)
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testGetIntegerByKey() {
        for (i in 1..2) {
            val docID = prepareData(i)
            val query = generateQuery(baseTestDb, docID)

            val rows = verifyQuery(query, false) { _, r ->
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

                assertEquals(0, r.getInt("not_in_query_select"))
            }

            assertEquals(1, rows)
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testGetInteger() {
        for (i in 1..2) {
            val docID = prepareData(i)
            val query = generateQuery(baseTestDb, docID)

            val rows = verifyQuery(query, false) { _, r ->
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

                assertThrows(ArrayIndexOutOfBoundsException::class) { r.getInt(-1) }

                assertThrows(ArrayIndexOutOfBoundsException::class) { r.getInt(100) }
            }

            assertEquals(1, rows)
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testGetLongByKey() {
        for (i in 1..2) {
            val docID = prepareData(i)
            val query = generateQuery(baseTestDb, docID)

            val rows = verifyQuery(query, false) { _, r ->
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

                assertEquals(0, r.getLong("not_in_query_select"))
            }

            assertEquals(1, rows)
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testGetLong() {
        for (i in 1..2) {
            val docID = prepareData(i)
            val query = generateQuery(baseTestDb, docID)

            val rows = verifyQuery(query, false) { _, r ->
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

                assertThrows(ArrayIndexOutOfBoundsException::class) { r.getLong(-1) }

                assertThrows(ArrayIndexOutOfBoundsException::class) { r.getLong(100) }
            }

            assertEquals(1, rows)
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testGetFloatByKey() {
        for (i in 1..2) {
            val docID = prepareData(i)
            val query = generateQuery(baseTestDb, docID)

            val rows = verifyQuery(query, false) { _, r ->
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

                assertEquals(0.0f, r.getFloat("not_in_query_select"), 0.0f)
            }

            assertEquals(1, rows)
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testGetFloat() {
        for (i in 1..2) {
            val docID = prepareData(i)
            val query = generateQuery(baseTestDb, docID)

            val rows = verifyQuery(query, false) { _, r ->
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

                assertThrows(ArrayIndexOutOfBoundsException::class) { r.getFloat(-1) }

                assertThrows(ArrayIndexOutOfBoundsException::class) { r.getFloat(100) }
            }

            assertEquals(1, rows)
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testGetDoubleByKey() {
        for (i in 1..2) {
            val docID = prepareData(i)
            val query = generateQuery(baseTestDb, docID)

            val rows = verifyQuery(query, false) { _, r ->
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

                assertEquals(0.0, r.getDouble("not_in_query_select"), 0.0)
            }

            assertEquals(1, rows)
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testGetDouble() {
        for (i in 1..2) {
            val docID = prepareData(i)
            val query = generateQuery(baseTestDb, docID)

            val rows = verifyQuery(query, false) { _, r ->
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

                assertThrows(ArrayIndexOutOfBoundsException::class) { r.getDouble(-1) }

                assertThrows(ArrayIndexOutOfBoundsException::class) { r.getDouble(100) }
            }

            assertEquals(1, rows)
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testGetBooleanByKey() {
        for (i in 1..2) {
            val docID = prepareData(i)
            val query = generateQuery(baseTestDb, docID)

            val rows = verifyQuery(query, false) { _, r ->
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

                assertFalse(r.getBoolean("not_in_query_select"))
            }

            assertEquals(1, rows)
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testGetBoolean() {
        for (i in 1..2) {
            val docID = prepareData(i)
            val query = generateQuery(baseTestDb, docID)

            val rows = verifyQuery(query, false) { _, r ->
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

                assertThrows(ArrayIndexOutOfBoundsException::class) { r.getBoolean(-1) }

                assertThrows(ArrayIndexOutOfBoundsException::class) { r.getBoolean(100) }
            }

            assertEquals(1, rows)
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testGetDateByKey() {
        for (i in 1..2) {
            val docID = prepareData(i)
            val query = generateQuery(baseTestDb, docID)

            val rows = verifyQuery(query, false) { _, r ->
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

                assertNull(r.getDate("not_in_query_select"))
            }

            assertEquals(1, rows)
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testGetDate() {
        for (i in 1..2) {
            val docID = prepareData(i)
            val query = generateQuery(baseTestDb, docID)

            val rows = verifyQuery(query, false) { _, r ->
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

                assertThrows(ArrayIndexOutOfBoundsException::class) { r.getDate(-1) }

                assertThrows(ArrayIndexOutOfBoundsException::class) { r.getDate(100) }
            }

            assertEquals(1, rows)
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testGetBlobByKey() {
        for (i in 1..2) {
            val docID = prepareData(i)
            val query = generateQuery(baseTestDb, docID)

            val rows = verifyQuery(query, false) { _, r ->
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
                assertEquals(BLOB_CONTENT, r.getBlob("blob")?.content?.decodeToString())
                assertContentEquals(BLOB_CONTENT.encodeToByteArray(), r.getBlob("blob")?.content)
                assertNull(r.getBlob("non_existing_key"))

                assertNull(r.getBlob("not_in_query_select"))
            }

            assertEquals(1, rows)
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testGetBlob() {
        for (i in 1..2) {
            val docID = prepareData(i)
            val query = generateQuery(baseTestDb, docID)

            val rows = verifyQuery(query, false) { _, r ->
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
                assertEquals(BLOB_CONTENT, r.getBlob(11)?.content?.decodeToString())
                assertContentEquals(BLOB_CONTENT.encodeToByteArray(), r.getBlob(11)?.content)
                assertNull(r.getBlob(12))

                assertThrows(ArrayIndexOutOfBoundsException::class) { r.getBlob(-1) }

                assertThrows(ArrayIndexOutOfBoundsException::class) { r.getBlob(100) }
            }

            assertEquals(1, rows)
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testGetDictionaryByKey() {
        for (i in 1..2) {
            val docID = prepareData(i)
            val query = generateQuery(baseTestDb, docID)

            val rows = verifyQuery(query, false) { _, r ->
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

                assertNull(r.getDictionary("not_in_query_select"))
            }

            assertEquals(1, rows)
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testGetDictionary() {
        for (i in 1..2) {
            val docID = prepareData(i)
            val query = generateQuery(baseTestDb, docID)

            val rows = verifyQuery(query, false) { _, r ->
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

                assertThrows(ArrayIndexOutOfBoundsException::class) { r.getDictionary(-1) }

                assertThrows(ArrayIndexOutOfBoundsException::class) { r.getDictionary(100) }
            }

            assertEquals(1, rows)
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testGetArrayByKey() {
        for (i in 1..2) {
            val docID = prepareData(i)
            val query = generateQuery(baseTestDb, docID)

            val rows = verifyQuery(query, false) { _, r ->
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

                assertNull(r.getArray("not_in_query_select"))
            }

            assertEquals(1, rows)
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testGetArray() {
        for (i in 1..2) {
            val docID = prepareData(i)
            val query = generateQuery(baseTestDb, docID)

            val rows = verifyQuery(query, false) { _, r ->
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

                assertThrows(ArrayIndexOutOfBoundsException::class) { r.getArray(-1) }

                assertThrows(ArrayIndexOutOfBoundsException::class) { r.getArray(100) }
            }

            assertEquals(1, rows)
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testGetKeys() {
        for (i in 1..2) {
            val docID = prepareData(i)
            val query = generateQuery(baseTestDb, docID)

            val rows = verifyQuery(query, false) { _, r ->
                val keys = r.keys.toMutableList()
                assertNotNull(keys)
                assertEquals(13, keys.size)
                keys.sort()
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
                ).sorted()
                assertEquals(expected, keys)

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
    @Throws(CouchbaseLiteException::class)
    fun testContains() {
        for (i in 1..2) {
            val docID = prepareData(i)
            val query = generateQuery(baseTestDb, docID)

            val rows = verifyQuery(query, false) { _, r ->
                // exists -> true
                val expected = listOf(
                    "null", "true", "false", "string", "zero", "one",
                    "minus_one", "one_dot_one", "date", "dict", "array",
                    "blob"
                )
                for (key in expected) {
                    assertTrue(r.contains(key))
                }
                // not exists -> false
                assertFalse(r.contains("non_existing_key"))

                assertFalse(r.contains("not_in_query_select"))
            }

            assertEquals(1, rows)
        }
    }

    // Contributed by Bryan Welter:
    // https://github.com/couchbase/couchbase-lite-android-ce/issues/27
    @Test
    @Throws(CouchbaseLiteException::class)
    fun testEmptyDict() {
        val doc1 = "doc1"
        val key1 = "emptyDict"

        val mDoc = MutableDocument(doc1)
        mDoc.setDictionary(key1, MutableDictionary())
        saveDocInBaseTestDb(mDoc)

        val query = QueryBuilder.select(SelectResult.property(key1))
            .from(DataSource.database(baseTestDb))
            .where(Meta.id.equalTo(Expression.string(doc1)))

        query.execute().use { results ->
            assertNotNull(results)
            for (result in results.allResults()) {
                assertNotNull(result)
                assertEquals(1, result.toMap().size)
                val emptyDict = result.getDictionary(key1)
                assertNotNull(emptyDict)
                assertTrue(emptyDict.count == 0)
            }
        }
    }

    ///////////////  JSON tests

    // JSON 3.8
    @Test
    @Throws(CouchbaseLiteException::class)
    fun testResultToJSON() {
        for (i in 0 until 5) {
            val mDoc = makeDocument()
            mDoc.setString("id", "jsonQuery-$i")
            saveDocInBaseTestDb(mDoc)
        }

        val projection = arrayOfNulls<SelectResult>(29)
        // `makeDocument` creates a document with 29 properties named doc-1 through doc-29
        for (i in 1..29) {
            projection[i - 1] = SelectResult.property("doc-$i")
        }

        @Suppress("UNCHECKED_CAST")
        QueryBuilder.select(*projection as kotlin.Array<SelectResult>)
            .from(DataSource.database(baseTestDb))
            .where(Expression.property("id").equalTo(Expression.string("jsonQuery-4")))
            .execute().use { results ->

                val result = results.next()
                assertNotNull(result)
                assertNull(results.next())

                verifyDocument(result)
                verifyDocument(Json.parseToJsonElement(result.toJSON()).jsonObject)
            }
    }

    ///////////////  Tooling
    private fun generateQuery(db: Database, docID: String): Query {
        val exDocID = Expression.string(docID)
        return QueryBuilder.select(
            SR_NULL,
            SR_TRUE,
            SR_FALSE,
            SR_STRING,
            SR_ZERO,
            SR_ONE,
            SR_MINUS_ONE,
            SR_ONE_DOT_ONE,
            SR_DATE,
            SR_DICT,
            SR_ARRAY,
            SR_BLOB,
            SR_NO_KEY
        )
            .from(DataSource.database(db))
            .where(Meta.id.equalTo(exDocID))
    }

    @Throws(CouchbaseLiteException::class)
    private fun prepareData(i: Int): String {
        val mDoc = MutableDocument("doc$i")
        if (i % 2 == 1) {
            populateData(mDoc)
        } else {
            populateDataByTypedSetter(mDoc)
        }
        saveDocInBaseTestDb(mDoc)
        return mDoc.id
    }

    companion object {

        private val SR_NULL = SelectResult.property("null")
        private val SR_TRUE = SelectResult.property("true")
        private val SR_FALSE = SelectResult.property("false")
        private val SR_STRING = SelectResult.property("string")
        private val SR_ZERO = SelectResult.property("zero")
        private val SR_ONE = SelectResult.property("one")
        private val SR_MINUS_ONE = SelectResult.property("minus_one")
        private val SR_ONE_DOT_ONE = SelectResult.property("one_dot_one")
        private val SR_DATE = SelectResult.property("date")
        private val SR_DICT = SelectResult.property("dict")
        private val SR_ARRAY = SelectResult.property("array")
        private val SR_BLOB = SelectResult.property("blob")
        private val SR_NO_KEY = SelectResult.property("non_existing_key")
    }
}
