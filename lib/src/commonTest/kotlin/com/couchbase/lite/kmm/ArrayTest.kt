package com.couchbase.lite.kmm

import com.udobny.kmm.test.AndroidInstrumented
import kotlinx.datetime.Instant
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import okio.IOException
import kotlin.test.*

@AndroidInstrumented
class ArrayTest : BaseDbTest() {

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testCreate() {
        val array = MutableArray()
        assertEquals(0, array.count)
        assertEquals(emptyList(), array.toList())
        val doc = MutableDocument("doc1")
        doc.setValue("array", array)
        assertEquals(array, doc.getArray("array"))
        val updatedDoc = saveDocInBaseTestDb(doc)
        assertEquals(emptyList(), updatedDoc.getArray("array")?.toList())
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testCreateWithList() {
        val data = mutableListOf<Any?>()
        data.add("1")
        data.add("2")
        data.add("3")
        val array = MutableArray(data)
        assertEquals(3, array.count)
        assertEquals(data, array.toList())
        val doc = MutableDocument("doc1")
        doc.setValue("array", array)
        assertEquals(array, doc.getArray("array"))
        val savedDoc = saveDocInBaseTestDb(doc)
        assertEquals(data, savedDoc.getArray("array")?.toList())
    }

    @Test
    fun testRecursiveArray() {
        assertFailsWith<IllegalArgumentException> {
            val array = MutableArray()
            array.addArray(array)
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testSetList() {
        var data = mutableListOf<Any?>()
        data.add("1")
        data.add("2")
        data.add("3")
        var array = MutableArray()
        array.setData(data)
        assertEquals(3, array.count)
        assertEquals(data, array.toList())
        val doc = MutableDocument("doc1")
        doc.setValue("array", array)
        assertEquals(array, doc.getArray("array"))

        // save
        val savedDoc = saveDocInBaseTestDb(doc)
        assertEquals(data, savedDoc.getArray("array")?.toList())

        // update
        array = savedDoc.getArray("array")!!.toMutable()
        data = mutableListOf()
        data.add("4")
        data.add("5")
        data.add("6")
        array.setData(data)
        assertEquals(data.size, array.count)
        assertEquals(data, array.toList())
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testAddNull() {
        val array = MutableArray()
        array.addValue(null)
        val doc = MutableDocument("doc1")
        save(doc, "array", array) { a ->
            assertEquals(1, a.count)
            assertNull(a.getValue(0))
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testAddObjects() {
        for (i in 0..1) {
            val array = MutableArray()

            // Add objects of all types:
            if (i % 2 == 0) {
                populateData(array)
            } else {
                populateDataByType(array)
            }
            val doc = MutableDocument("doc1")
            save(doc, "array", array) { a ->
                assertEquals(12, a.count)
                assertEquals(true, a.getValue(0))
                assertEquals(false, a.getValue(1))
                assertEquals("string", a.getValue(2))
                assertEquals(0, (a.getValue(3) as Number).toInt())
                assertEquals(1, (a.getValue(4) as Number).toInt())
                assertEquals(-1, (a.getValue(5) as Number).toInt())
                assertEquals(1.1, a.getValue(6))
                assertEquals(TEST_DATE, a.getValue(7))
                assertNull(a.getValue(8))

                // dictionary
                val dict = a.getValue(9) as Dictionary
                val subdict = if (dict is MutableDictionary) dict else dict.toMutable()
                val expectedMap = mutableMapOf<String, Any?>()
                expectedMap["name"] = "Scott Tiger"
                assertEquals(expectedMap, subdict.toMap())

                // array
                val array1 = a.getValue(10) as Array
                val subarray = if (array1 is MutableArray) array1 else array1.toMutable()
                val expected = mutableListOf<Any?>()
                expected.add("a")
                expected.add("b")
                expected.add("c")
                assertEquals(expected, subarray.toList())

                // blob
                verifyBlob(a.getValue(11))
            }
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testAddObjectsToExistingArray() {
        for (i in 0..1) {
            var array: MutableArray? = MutableArray()
            if (i % 2 == 0) {
                populateData(array!!)
            } else {
                populateDataByType(array!!)
            }

            // Save
            val docID = "doc$i"
            var doc = MutableDocument(docID)
            doc.setValue("array", array)
            doc = saveDocInBaseTestDb(doc).toMutable()

            // Get an existing array:
            array = doc.getArray("array")
            assertNotNull(array)
            assertEquals(12, array.count)

            // Update:
            if (i % 2 == 0) {
                populateData(array)
            } else {
                populateDataByType(array)
            }
            assertEquals(24, array.count)
            save(doc, "array", array) { a ->
                assertEquals(24, a.count)
                assertEquals(true, a.getValue(12))
                assertEquals(false, a.getValue(12 + 1))
                assertEquals("string", a.getValue(12 + 2))
                assertEquals(0, (a.getValue(12 + 3) as Number).toInt())
                assertEquals(1, (a.getValue(12 + 4) as Number).toInt())
                assertEquals(-1, (a.getValue(12 + 5) as Number).toInt())
                assertEquals(1.1, a.getValue(12 + 6))
                assertEquals(TEST_DATE, a.getValue(12 + 7))
                assertNull(a.getValue(12 + 8))

                // dictionary
                val dict = a.getValue(12 + 9) as Dictionary
                val subdict = if (dict is MutableDictionary) dict else dict.toMutable()
                val expectedMap = mutableMapOf<String, Any?>()
                expectedMap["name"] = "Scott Tiger"
                assertEquals(expectedMap, subdict.toMap())

                // array
                val array1 = a.getValue(12 + 10) as Array
                val subarray = if (array1 is MutableArray) array1 else array1.toMutable()
                val expected = mutableListOf<Any?>()
                expected.add("a")
                expected.add("b")
                expected.add("c")
                assertEquals(expected, subarray.toList())

                // blob
                verifyBlob(a.getValue(12 + 11))
            }
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testSetObject() {
        val data = arrayOfAllTypes()

        // Prepare CBLArray with NSNull placeholders:
        val array = MutableArray()
        for (i in data.indices) {
            array.addValue(null)
        }

        // Set object at index:
        for (i in data.indices) {
            array.setValue(i, data[i])
        }
        val doc = MutableDocument("doc1")
        save(doc, "array", array) { a ->
            assertEquals(12, a.count)
            assertEquals(true, a.getValue(0))
            assertEquals(false, a.getValue(1))
            assertEquals("string", a.getValue(2))
            assertEquals(0, (a.getValue(3) as Number).toInt())
            assertEquals(1, (a.getValue(4) as Number).toInt())
            assertEquals(-1, (a.getValue(5) as Number).toInt())
            assertEquals(1.1, a.getValue(6))
            assertEquals(TEST_DATE, a.getValue(7))
            assertNull(a.getValue(8))

            // dictionary
            val dict = a.getValue(9) as Dictionary
            val subdict = if (dict is MutableDictionary) dict else dict.toMutable()
            val expectedMap = mutableMapOf<String, Any?>()
            expectedMap["name"] = "Scott Tiger"
            assertEquals(expectedMap, subdict.toMap())

            // array
            val array1 = a.getValue(10) as Array
            val subarray = if (array1 is MutableArray) array1 else array1.toMutable()
            val expected = mutableListOf<Any?>()
            expected.add("a")
            expected.add("b")
            expected.add("c")
            assertEquals(expected, subarray.toList())

            // blob
            verifyBlob(a.getValue(11))
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testSetObjectToExistingArray() {
        for (i in 0..1) {
            val array = MutableArray()
            if (i % 2 == 0) {
                populateData(array)
            } else {
                populateDataByType(array)
            }

            // Save
            val docID = "doc$i"
            var doc = MutableDocument(docID)
            doc.setArray("array", array)
            doc = saveDocInBaseTestDb(doc).toMutable()
            val gotArray = doc.getArray("array")!!
            val data = arrayOfAllTypes()
            assertEquals(data.size, gotArray.count)

            // reverse the array
            for (j in data.indices) {
                gotArray.setValue(j, data[data.size - j - 1])
            }
            save(doc, "array", gotArray) { a ->
                assertEquals(12, a.count)
                assertEquals(true, a.getValue(11))
                assertEquals(false, a.getValue(10))
                assertEquals("string", a.getValue(9))
                assertEquals(0, (a.getValue(8) as Number).toInt())
                assertEquals(1, (a.getValue(7) as Number).toInt())
                assertEquals(-1, (a.getValue(6) as Number).toInt())
                assertEquals(1.1, a.getValue(5))
                assertEquals(TEST_DATE, a.getValue(4))
                assertNull(a.getValue(3))

                // dictionary
                val dict = a.getValue(2) as Dictionary
                val subdict = if (dict is MutableDictionary) dict else dict.toMutable()
                val expectedMap = mutableMapOf<String, Any?>()
                expectedMap["name"] = "Scott Tiger"
                assertEquals(expectedMap, subdict.toMap())

                // array
                val array1 = a.getValue(1) as Array
                val subarray = if (array1 is MutableArray) array1 else array1.toMutable()
                val expected = mutableListOf<Any?>()
                expected.add("a")
                expected.add("b")
                expected.add("c")
                assertEquals(expected, subarray.toList())

                // blob
                verifyBlob(a.getValue(0))
            }
        }
    }

    @Test
    fun testSetObjectOutOfBound() {
        val array = MutableArray()
        array.addValue("a")
        assertFailsWith<IndexOutOfBoundsException> { array.setValue(-1, "b") }
        assertFailsWith<IndexOutOfBoundsException> { array.setValue(1, "b") }
    }

    @Test
    fun testInsertObject() {
        val array = MutableArray()
        array.insertValue(0, "a")
        assertEquals(1, array.count)
        assertEquals("a", array.getValue(0))
        array.insertValue(0, "c")
        assertEquals(2, array.count)
        assertEquals("c", array.getValue(0))
        assertEquals("a", array.getValue(1))
        array.insertValue(1, "d")
        assertEquals(3, array.count)
        assertEquals("c", array.getValue(0))
        assertEquals("d", array.getValue(1))
        assertEquals("a", array.getValue(2))
        array.insertValue(2, "e")
        assertEquals(4, array.count)
        assertEquals("c", array.getValue(0))
        assertEquals("d", array.getValue(1))
        assertEquals("e", array.getValue(2))
        assertEquals("a", array.getValue(3))
        array.insertValue(4, "f")
        assertEquals(5, array.count)
        assertEquals("c", array.getValue(0))
        assertEquals("d", array.getValue(1))
        assertEquals("e", array.getValue(2))
        assertEquals("a", array.getValue(3))
        assertEquals("f", array.getValue(4))
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testInsertObjectToExistingArray() {
        var mDoc = MutableDocument("doc1")
        mDoc.setValue("array", MutableArray())
        var doc = saveDocInBaseTestDb(mDoc)
        mDoc = doc.toMutable()
        var mArray = mDoc.getArray("array")
        assertNotNull(mArray)
        mArray.insertValue(0, "a")
        doc = save(mDoc, "array", mArray) { a ->
            assertEquals(1, a.count)
            assertEquals("a", a.getValue(0))
        }
        mDoc = doc.toMutable()
        mArray = mDoc.getArray("array")
        assertNotNull(mArray)
        mArray.insertValue(0, "c")
        doc = save(mDoc, "array", mArray) { a ->
            assertEquals(2, a.count)
            assertEquals("c", a.getValue(0))
            assertEquals("a", a.getValue(1))
        }
        mDoc = doc.toMutable()
        mArray = mDoc.getArray("array")
        assertNotNull(mArray)
        mArray.insertValue(1, "d")
        doc = save(mDoc, "array", mArray) { a ->
            assertEquals(3, a.count)
            assertEquals("c", a.getValue(0))
            assertEquals("d", a.getValue(1))
            assertEquals("a", a.getValue(2))
        }
        mDoc = doc.toMutable()
        mArray = mDoc.getArray("array")
        assertNotNull(mArray)
        mArray.insertValue(2, "e")
        doc = save(mDoc, "array", mArray) { a ->
            assertEquals(4, a.count)
            assertEquals("c", a.getValue(0))
            assertEquals("d", a.getValue(1))
            assertEquals("e", a.getValue(2))
            assertEquals("a", a.getValue(3))
        }
        mDoc = doc.toMutable()
        mArray = mDoc.getArray("array")
        assertNotNull(mArray)
        mArray.insertValue(4, "f")
        save(mDoc, "array", mArray) { a ->
            assertEquals(5, a.count)
            assertEquals("c", a.getValue(0))
            assertEquals("d", a.getValue(1))
            assertEquals("e", a.getValue(2))
            assertEquals("a", a.getValue(3))
            assertEquals("f", a.getValue(4))
        }
    }

    @Test
    fun testInsertObjectOutOfBound() {
        val array = MutableArray()
        array.addValue("a")
        assertFailsWith<IndexOutOfBoundsException> { array.insertValue(-1, "b") }
        assertFailsWith<IndexOutOfBoundsException> { array.insertValue(2, "b") }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testRemove() {
        for (i in 0..1) {
            val array = MutableArray()
            if (i % 2 == 0) {
                populateData(array)
            } else {
                populateDataByType(array)
            }
            for (j in array.count - 1 downTo 0) {
                array.remove(j)
            }
            val docID = "doc$i"
            val doc = MutableDocument(docID)
            save(doc, "array", array) { a ->
                assertEquals(0, a.count)
                assertEquals(emptyList(), a.toList())
            }
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testRemoveExistingArray() {
        for (i in 0..1) {
            var array = MutableArray()
            if (i % 2 == 0) {
                populateData(array)
            } else {
                populateDataByType(array)
            }
            val docID = "doc$i"
            var doc = MutableDocument(docID)
            doc.setValue("array", array)
            doc = saveDocInBaseTestDb(doc).toMutable()
            array = doc.getArray("array")!!
            for (j in array.count - 1 downTo 0) {
                array.remove(j)
            }
            save(doc, "array", array) { a ->
                assertEquals(0, a.count)
                assertEquals(emptyList(), a.toList())
            }
        }
    }

    @Test
    fun testRemoveOutOfBound() {
        val array = MutableArray()
        array.addValue("a")
        assertFailsWith<IndexOutOfBoundsException> { array.remove(-1) }
        assertFailsWith<IndexOutOfBoundsException> { array.remove(1) }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testCount() {
        for (i in 0..1) {
            val array = MutableArray()
            if (i % 2 == 0) {
                populateData(array)
            } else {
                populateDataByType(array)
            }
            val docID = "doc$i"
            val doc = MutableDocument(docID)
            save(doc, "array", array) { a ->
                assertEquals(12, a.count)
            }
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testGetString() {
        for (i in 0..1) {
            val array = MutableArray()
            if (i % 2 == 0) {
                populateData(array)
            } else {
                populateDataByType(array)
            }
            assertEquals(12, array.count)
            val docID = "doc$i"
            val doc = MutableDocument(docID)
            save(doc, "array", array) { a ->
                assertNull(a.getString(0))
                assertNull(a.getString(1))
                assertEquals("string", a.getString(2))
                assertNull(a.getString(3))
                assertNull(a.getString(4))
                assertNull(a.getString(5))
                assertNull(a.getString(6))
                assertEquals(TEST_DATE, a.getString(7))
                assertNull(a.getString(8))
                assertNull(a.getString(9))
                assertNull(a.getString(10))
                assertNull(a.getString(11))
            }
        }
    }

    // !!! Fails on Nexus 4
    @Test
    @Throws(CouchbaseLiteException::class)
    fun testGetNumber() {
        for (i in 0..1) {
            val array = MutableArray()
            if (i % 2 == 0) {
                populateData(array)
            } else {
                populateDataByType(array)
            }
            assertEquals(12, array.count)
            val docID = "doc$i"
            val doc = MutableDocument(docID)
            save(doc, "array", array) { a ->
                assertEquals(1, a.getNumber(0)?.toInt())
                assertEquals(0, a.getNumber(1)?.toInt())
                assertNull(a.getNumber(2))
                assertEquals(0, a.getNumber(3)?.toInt())
                assertEquals(1, a.getNumber(4)?.toInt())
                assertEquals(-1, a.getNumber(5)?.toInt())
                assertEquals(1.1, a.getNumber(6))
                assertNull(a.getNumber(7))
                assertNull(a.getNumber(8))
                assertNull(a.getNumber(9))
                assertNull(a.getNumber(10))
                assertNull(a.getNumber(11))
            }
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testGetInteger() {
        for (i in 0..1) {
            val array = MutableArray()
            if (i % 2 == 0) {
                populateData(array)
            } else {
                populateDataByType(array)
            }
            assertEquals(12, array.count)
            val docID = "doc$i"
            val doc = MutableDocument(docID)
            save(doc, "array", array) { a ->
                assertEquals(1, a.getInt(0))
                assertEquals(0, a.getInt(1))
                assertEquals(0, a.getInt(2))
                assertEquals(0, a.getInt(3))
                assertEquals(1, a.getInt(4))
                assertEquals(-1, a.getInt(5))
                assertEquals(1, a.getInt(6))
                assertEquals(0, a.getInt(7))
                assertEquals(0, a.getInt(8))
                assertEquals(0, a.getInt(9))
                assertEquals(0, a.getInt(10))
                assertEquals(0, a.getInt(11))
            }
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testGetLong() {
        for (i in 0..1) {
            val array = MutableArray()
            if (i % 2 == 0) {
                populateData(array)
            } else {
                populateDataByType(array)
            }
            assertEquals(12, array.count)
            val docID = "doc$i"
            val doc = MutableDocument(docID)
            save(doc, "array", array) { a ->
                assertEquals(1, a.getLong(0))
                assertEquals(0, a.getLong(1))
                assertEquals(0, a.getLong(2))
                assertEquals(0, a.getLong(3))
                assertEquals(1, a.getLong(4))
                assertEquals(-1, a.getLong(5))
                assertEquals(1, a.getLong(6))
                assertEquals(0, a.getLong(7))
                assertEquals(0, a.getLong(8))
                assertEquals(0, a.getLong(9))
                assertEquals(0, a.getLong(10))
                assertEquals(0, a.getLong(11))
            }
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testGetFloat() {
        for (i in 0..1) {
            val array = MutableArray()
            if (i % 2 == 0) {
                populateData(array)
            } else {
                populateDataByType(array)
            }
            assertEquals(12, array.count)
            val docID = "doc$i"
            val doc = MutableDocument(docID)
            save(doc, "array", array) { a ->
                assertEquals(1.0f, a.getFloat(0), 0.0f)
                assertEquals(0.0f, a.getFloat(1), 0.0f)
                assertEquals(0.0f, a.getFloat(2), 0.0f)
                assertEquals(0.0f, a.getFloat(3), 0.0f)
                assertEquals(1.0f, a.getFloat(4), 0.0f)
                assertEquals(-1.0f, a.getFloat(5), 0.0f)
                assertEquals(1.1f, a.getFloat(6), 0.0f)
                assertEquals(0.0f, a.getFloat(7), 0.0f)
                assertEquals(0.0f, a.getFloat(8), 0.0f)
                assertEquals(0.0f, a.getFloat(9), 0.0f)
                assertEquals(0.0f, a.getFloat(10), 0.0f)
                assertEquals(0.0f, a.getFloat(11), 0.0f)
            }
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testGetDouble() {
        for (i in 0..1) {
            val array = MutableArray()
            if (i % 2 == 0) {
                populateData(array)
            } else {
                populateDataByType(array)
            }
            assertEquals(12, array.count)
            val docID = "doc$i"
            val doc = MutableDocument(docID)
            save(doc, "array", array) { a ->
                assertEquals(1.0, a.getDouble(0), 0.0)
                assertEquals(0.0, a.getDouble(1), 0.0)
                assertEquals(0.0, a.getDouble(2), 0.0)
                assertEquals(0.0, a.getDouble(3), 0.0)
                assertEquals(1.0, a.getDouble(4), 0.0)
                assertEquals(-1.0, a.getDouble(5), 0.0)
                assertEquals(1.1, a.getDouble(6), 0.0)
                assertEquals(0.0, a.getDouble(7), 0.0)
                assertEquals(0.0, a.getDouble(8), 0.0)
                assertEquals(0.0, a.getDouble(9), 0.0)
                assertEquals(0.0, a.getDouble(10), 0.0)
                assertEquals(0.0, a.getDouble(11), 0.0)
            }
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testSetGetMinMaxNumbers() {
        val array = MutableArray()
        array.addValue(Int.MIN_VALUE)
        array.addValue(Int.MAX_VALUE)
        array.addValue(Long.MIN_VALUE)
        array.addValue(Long.MAX_VALUE)
        array.addValue(Float.MIN_VALUE)
        array.addValue(Float.MAX_VALUE)
        array.addValue(Double.MIN_VALUE)
        array.addValue(Double.MAX_VALUE)

        val doc = MutableDocument("doc1")
        save(doc, "array", array) { a ->
            assertEquals(Int.MIN_VALUE, a.getNumber(0)?.toInt())
            assertEquals(Int.MAX_VALUE, a.getNumber(1)?.toInt())
            assertEquals(Int.MIN_VALUE, (a.getValue(0) as Number).toInt())
            assertEquals(Int.MAX_VALUE, (a.getValue(1) as Number).toInt())
            assertEquals(Int.MIN_VALUE, a.getInt(0))
            assertEquals(Int.MAX_VALUE, a.getInt(1))

            assertEquals(Long.MIN_VALUE, a.getNumber(2))
            assertEquals(Long.MAX_VALUE, a.getNumber(3))
            assertEquals(Long.MIN_VALUE, a.getValue(2))
            assertEquals(Long.MAX_VALUE, a.getValue(3))
            assertEquals(Long.MIN_VALUE, a.getLong(2))
            assertEquals(Long.MAX_VALUE, a.getLong(3))
            assertEquals(Float.MIN_VALUE, a.getNumber(4))
            assertEquals(Float.MAX_VALUE, a.getNumber(5))
            assertEquals(Float.MIN_VALUE, a.getValue(4))
            assertEquals(Float.MAX_VALUE, a.getValue(5))
            assertEquals(Float.MIN_VALUE, a.getFloat(4), 0.0f)
            assertEquals(Float.MAX_VALUE, a.getFloat(5), 0.0f)
            assertEquals(Double.MIN_VALUE, a.getNumber(6))
            assertEquals(Double.MAX_VALUE, a.getNumber(7))
            assertEquals(Double.MIN_VALUE, a.getValue(6))
            assertEquals(Double.MAX_VALUE, a.getValue(7))
            assertEquals(Double.MIN_VALUE, a.getDouble(6), 0.0)
            assertEquals(Double.MAX_VALUE, a.getDouble(7), 0.0)
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testSetGetFloatNumbers() {
        val array = MutableArray()
        array.addValue(1.00)
        array.addValue(1.49)
        array.addValue(1.50)
        array.addValue(1.51)
        array.addValue(1.99)

        val doc = MutableDocument("doc1")
        save(doc, "array", array) { a ->
            // NOTE: Number which has no floating part is stored as Integer.
            //       This causes type difference between before and after storing data
            //       into the database.
            assertEquals(1.00, (a.getValue(0) as Number).toDouble(), 0.0)
            assertEquals(1.00, a.getNumber(0)!!.toDouble(), 0.0)
            assertEquals(1, a.getInt(0))
            assertEquals(1L, a.getLong(0))
            assertEquals(1.00f, a.getFloat(0), 0.0f)
            assertEquals(1.00, a.getDouble(0), 0.0)

            assertEquals(1.49, a.getValue(1))
            assertEquals(1.49, a.getNumber(1))
            assertEquals(1, a.getInt(1))
            assertEquals(1L, a.getLong(1))
            assertEquals(1.49f, a.getFloat(1), 0.0f)
            assertEquals(1.49, a.getDouble(1), 0.0)

            assertEquals(1.50, (a.getValue(2) as Number).toDouble(), 0.0)
            assertEquals(1.50, a.getNumber(2)!!.toDouble(), 0.0)
            assertEquals(1, a.getInt(2))
            assertEquals(1L, a.getLong(2))
            assertEquals(1.50f, a.getFloat(2), 0.0f)
            assertEquals(1.50, a.getDouble(2), 0.0)

            assertEquals(1.51, a.getValue(3))
            assertEquals(1.51, a.getNumber(3))
            assertEquals(1, a.getInt(3))
            assertEquals(1L, a.getLong(3))
            assertEquals(1.51f, a.getFloat(3), 0.0f)
            assertEquals(1.51, a.getDouble(3), 0.0)

            assertEquals(1.99, a.getValue(4))
            assertEquals(1.99, a.getNumber(4))
            assertEquals(1, a.getInt(4))
            assertEquals(1L, a.getLong(4))
            assertEquals(1.99f, a.getFloat(4), 0.0f)
            assertEquals(1.99, a.getDouble(4), 0.0)
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testGetBoolean() {
        for (i in 0..1) {
            val array = MutableArray()
            if (i % 2 == 0) {
                populateData(array)
            } else {
                populateDataByType(array)
            }
            assertEquals(12, array.count)
            val docID = "doc$i"
            val doc = MutableDocument(docID)
            save(doc, "array", array) { a ->
                assertTrue(a.getBoolean(0))
                assertFalse(a.getBoolean(1))
                assertTrue(a.getBoolean(2))
                assertFalse(a.getBoolean(3))
                assertTrue(a.getBoolean(4))
                assertTrue(a.getBoolean(5))
                assertTrue(a.getBoolean(6))
                assertTrue(a.getBoolean(7))
                assertFalse(a.getBoolean(8))
                assertTrue(a.getBoolean(9))
                assertTrue(a.getBoolean(10))
                assertTrue(a.getBoolean(11))
            }
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testGetDate() {
        for (i in 0..1) {
            val array = MutableArray()
            if (i % 2 == 0) {
                populateData(array)
            } else {
                populateDataByType(array)
            }
            assertEquals(12, array.count)
            val docID = "doc$i"
            val doc = MutableDocument(docID)
            save(doc, "array", array) { a ->
                assertNull(a.getDate(0))
                assertNull(a.getDate(1))
                assertNull(a.getDate(2))
                assertNull(a.getDate(3))
                assertNull(a.getDate(4))
                assertNull(a.getDate(5))
                assertNull(a.getDate(6))
                assertEquals(TEST_DATE, a.getDate(7).toString())
                assertNull(a.getDate(8))
                assertNull(a.getDate(9))
                assertNull(a.getDate(10))
                assertNull(a.getDate(11))
            }
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testGetMap() {
        for (i in 0..1) {
            val array = MutableArray()
            if (i % 2 == 0) {
                populateData(array)
            } else {
                populateDataByType(array)
            }
            assertEquals(12, array.count)
            val docID = "doc$i"
            val doc = MutableDocument(docID)
            save(doc, "array", array) { a ->
                assertNull(a.getDictionary(0))
                assertNull(a.getDictionary(1))
                assertNull(a.getDictionary(2))
                assertNull(a.getDictionary(3))
                assertNull(a.getDictionary(4))
                assertNull(a.getDictionary(5))
                assertNull(a.getDictionary(6))
                assertNull(a.getDictionary(7))
                assertNull(a.getDictionary(8))
                val map = mutableMapOf<String, Any?>()
                map["name"] = "Scott Tiger"
                assertEquals(map, a.getDictionary(9)?.toMap())
                assertNull(a.getDictionary(10))
                assertNull(a.getDictionary(11))
            }
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testGetArray() {
        for (i in 0..1) {
            val array = MutableArray()
            if (i % 2 == 0) {
                populateData(array)
            } else {
                populateDataByType(array)
            }
            assertEquals(12, array.count)
            val docID = "doc$i"
            val doc = MutableDocument(docID)
            save(doc, "array", array) { a ->
                assertNull(a.getArray(0))
                assertNull(a.getArray(1))
                assertNull(a.getArray(2))
                assertNull(a.getArray(3))
                assertNull(a.getArray(4))
                assertNull(a.getArray(5))
                assertNull(a.getArray(6))
                assertNull(a.getArray(7))
                assertNull(a.getArray(9))
                assertEquals(listOf("a", "b", "c"), a.getArray(10)?.toList())
                assertNull(a.getDictionary(11))
            }
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testSetNestedArray() {
        val array1 = MutableArray()
        val array2 = MutableArray()
        val array3 = MutableArray()
        array1.addValue(array2)
        array2.addValue(array3)
        array3.addValue("a")
        array3.addValue("b")
        array3.addValue("c")
        val doc = MutableDocument("doc1")
        save(doc, "array", array1) { a1 ->
            assertEquals(1, a1.count)
            val a2 = a1.getArray(0)!!
            assertEquals(1, a2.count)
            val a3 = a2.getArray(0)!!
            assertEquals(3, a3.count)
            assertEquals("a", a3.getValue(0))
            assertEquals("b", a3.getValue(1))
            assertEquals("c", a3.getValue(2))
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testReplaceArray() {
        var doc = MutableDocument("doc1")
        val array1 = MutableArray()
        array1.addValue("a")
        array1.addValue("b")
        array1.addValue("c")
        assertEquals(3, array1.count)
        assertEquals(listOf("a", "b", "c"), array1.toList())
        doc.setValue("array", array1)
        var array2 = MutableArray()
        array2.addValue("x")
        array2.addValue("y")
        array2.addValue("z")
        assertEquals(3, array2.count)
        assertEquals(listOf("x", "y", "z"), array2.toList())

        // Replace:
        doc.setValue("array", array2)

        // array1 should be now detached:
        array1.addValue("d")
        assertEquals(4, array1.count)
        assertEquals(listOf("a", "b", "c", "d"), array1.toList())

        // Check array2:
        assertEquals(3, array2.count)
        assertEquals(listOf("x", "y", "z"), array2.toList())

        // Save:
        doc = saveDocInBaseTestDb(doc).toMutable()

        // Check current array:
        assertNotSame(doc.getArray("array"), array2)
        array2 = doc.getArray("array")!!
        assertEquals(3, array2.count)
        assertEquals(listOf("x", "y", "z"), array2.toList())
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testReplaceArrayDifferentType() {
        var doc = MutableDocument("doc1")
        val array1 = MutableArray()
        array1.addValue("a")
        array1.addValue("b")
        array1.addValue("c")
        assertEquals(3, array1.count)
        assertEquals(listOf("a", "b", "c"), array1.toList())
        doc.setValue("array", array1)

        // Replace:
        doc.setValue("array", "Daniel Tiger")

        // array1 should be now detached:
        array1.addValue("d")
        assertEquals(4, array1.count)
        assertEquals(listOf("a", "b", "c", "d"), array1.toList())

        // Save:
        doc = saveDocInBaseTestDb(doc).toMutable()
        assertEquals("Daniel Tiger", doc.getString("array"))
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testEnumeratingArray() {
        val array = MutableArray()
        for (i in 0..19) {
            array.addValue(i)
        }
        var content = array.toList()
        var result = mutableListOf<Any?>()
        var counter = 0
        for (item in array) {
            assertNotNull(item)
            result.add(item)
            counter++
        }
        assertEquals(content, result)
        assertEquals(array.count, counter)

        // Update:
        array.remove(1)
        array.addValue(20)
        array.addValue(21)
        content = array.toList()
        result = mutableListOf()
        for (item in array) {
            assertNotNull(item)
            result.add(item)
        }
        assertEquals(content, result)
        val doc = MutableDocument("doc1")
        doc.setValue("array", array)
        val c = content
        save(doc, "array", array) { array1 ->
            val r = mutableListOf<Any?>()
            for (item in array1) {
                assertNotNull(item)
                r.add(item)
            }
            assertEquals(c.toString(), r.toString())
        }
    }

    // ??? Surprisingly, no conncurrent modification exception.
    @Test
    fun testArrayEnumerationWithDataModification1() {
        val array = MutableArray()
        for (i in 0..2) {
            array.addValue(i)
        }
        assertEquals(3, array.count)
        assertContentEquals(arrayOf(0, 1, 2), array.toList().toTypedArray())
        var n = 0
        val itr = array.iterator()
        while (itr.hasNext()) {
            if (n++ == 1) {
                array.addValue(3)
            }
            itr.next()
        }
        assertEquals(4, array.count)
        assertContentEquals(arrayOf(0, 1, 2, 3), array.toList().toTypedArray())
    }

    // ??? Surprisingly, no conncurrent modification exception.
    @Test
    @Throws(CouchbaseLiteException::class)
    fun testArrayEnumerationWithDataModification2() {
        var array: MutableArray? = MutableArray()
        for (i in 0..2) {
            array!!.addValue(i)
        }
        assertEquals(3, array!!.count)
        assertContentEquals(arrayOf(0, 1, 2), array.toList().toTypedArray())
        val doc = MutableDocument("doc1").setValue("array", array)
        array = saveDocInBaseTestDb(doc).toMutable().getArray("array")
        assertNotNull(array)
        var n = 0
        val itr = array.iterator()
        while (itr.hasNext()) {
            if (n++ == 1) {
                array.addValue(3)
            }
            itr.next()
        }
        assertEquals(4, array.count)
        // this is friggin' bizarre:
        // after a roundtrip through the db those integers turn into longs
        assertContentEquals(arrayOf(0L, 1L, 2L, 3), array.toList().toTypedArray())
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testSetNull() {
        val mDoc = MutableDocument("test")
        val mArray = MutableArray()
        mArray.addValue(null)
        mArray.addString(null)
        mArray.addNumber(null)
        mArray.addDate(null)
        mArray.addArray(null)
        mArray.addDictionary(null)
        mDoc.setArray("array", mArray)
        saveDocInBaseTestDb(mDoc) { doc ->
            assertEquals(1, doc.count)
            assertTrue(doc.contains("array"))
            val array = doc.getArray("array")
            assertNotNull(array)
            assertEquals(6, array.count)
            assertNull(array.getValue(0))
            assertNull(array.getValue(1))
            assertNull(array.getValue(2))
            assertNull(array.getValue(3))
            assertNull(array.getValue(4))
            assertNull(array.getValue(5))
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testEquals() {

        // mArray1 and mArray2 have exactly same data
        // mArray3 is different
        // mArray4 is different
        // mArray5 is different
        val mArray1 = MutableArray()
        mArray1.addValue(1L)
        mArray1.addValue("Hello")
        mArray1.addValue(null)
        val mArray2 = MutableArray()
        mArray2.addValue(1L)
        mArray2.addValue("Hello")
        mArray2.addValue(null)
        val mArray3 = MutableArray()
        mArray3.addValue(100L)
        mArray3.addValue(true)
        val mArray4 = MutableArray()
        mArray4.addValue(100L)
        val mArray5 = MutableArray()
        mArray4.addValue(100L)
        mArray3.addValue(false)
        val mDoc = MutableDocument("test")
        mDoc.setArray("array1", mArray1)
        mDoc.setArray("array2", mArray2)
        mDoc.setArray("array3", mArray3)
        mDoc.setArray("array4", mArray4)
        mDoc.setArray("array5", mArray5)
        val doc: Document = saveDocInBaseTestDb(mDoc)
        val array1 = doc.getArray("array1")!!
        val array2 = doc.getArray("array2")!!
        val array3 = doc.getArray("array3")!!
        val array4 = doc.getArray("array4")!!
        val array5 = doc.getArray("array5")!!

        // compare array1, array2, marray1, and marray2
        assertEquals(array1, array1)
        assertEquals(array2, array2)
        assertEquals(array1, array2)
        assertEquals(array2, array1)
        assertEquals(array1, array1.toMutable())
        assertEquals(array1, array2.toMutable())
        assertEquals(array1.toMutable(), array1)
        assertEquals(array2.toMutable(), array1)
        assertEquals(array1, mArray1)
        assertEquals(array1, mArray2)
        assertEquals(array2, mArray1)
        assertEquals(array2, mArray2)
        assertEquals(mArray1, array1)
        assertEquals(mArray2, array1)
        assertEquals(mArray1, array2)
        assertEquals(mArray2, array2)
        assertEquals(mArray1, mArray1)
        assertEquals(mArray2, mArray2)
        assertEquals(mArray1, mArray1)
        assertEquals(mArray2, mArray2)

        // compare array1, array3, marray1, and marray3
        assertEquals(array3, array3)
        assertNotEquals(array1, array3)
        assertNotEquals(array3, array1)
        assertNotEquals(array1, array3.toMutable())
        assertNotEquals(array3.toMutable(), array1)
        assertNotEquals(array1, mArray3)
        assertNotEquals(array3, mArray1)
        assertEquals(array3, mArray3)
        assertNotEquals(mArray3, array1)
        assertNotEquals(mArray1, array3)
        assertEquals(mArray3, array3)
        assertEquals(mArray3, mArray3)
        assertEquals(mArray3, mArray3)

        // compare array1, array4, marray1, and marray4
        assertEquals(array4, array4)
        assertNotEquals(array1, array4)
        assertNotEquals(array4, array1)
        assertNotEquals(array1, array4.toMutable())
        assertNotEquals(array4.toMutable(), array1)
        assertNotEquals(array1, mArray4)
        assertNotEquals(array4, mArray1)
        assertEquals(array4, mArray4)
        assertNotEquals(mArray4, array1)
        assertNotEquals(mArray1, array4)
        assertEquals(mArray4, array4)
        assertEquals(mArray4, mArray4)
        assertEquals(mArray4, mArray4)

        // compare array3, array4, marray3, and marray4
        assertNotEquals(array3, array4)
        assertNotEquals(array4, array3)
        assertNotEquals(array3, array4.toMutable())
        assertNotEquals(array4.toMutable(), array3)
        assertNotEquals(array3, mArray4)
        assertNotEquals(array4, mArray3)
        assertNotEquals(mArray4, array3)
        assertNotEquals(mArray3, array4)

        // compare array3, array5, marray3, and marray5
        assertNotEquals(array3, array5)
        assertNotEquals(array5, array3)
        assertNotEquals(array3, array5.toMutable())
        assertNotEquals(array5.toMutable(), array3)
        assertNotEquals(array3, mArray5)
        assertNotEquals(array5, mArray3)
        assertNotEquals(mArray5, array3)
        assertNotEquals(mArray3, array5)

        // compare array5, array4, mArray5, and marray4
        assertNotEquals(array5, array4)
        assertNotEquals(array4, array5)
        assertNotEquals(array5, array4.toMutable())
        assertNotEquals(array4.toMutable(), array5)
        assertNotEquals(array5, mArray4)
        assertNotEquals(array4, mArray5)
        assertNotEquals(mArray4, array5)
        assertNotEquals(mArray5, array4)

        // against other type
        assertNotEquals<Any?>(null, array3)
        assertNotEquals(array3, Any())
        assertNotEquals<Any?>(1, array3)
        assertNotEquals<Any?>(array3, emptyMap<Any, Any>())
        assertNotEquals<Any?>(array3, MutableDictionary())
        assertNotEquals(array3, MutableArray())
        assertNotEquals<Any?>(array3, doc)
        assertNotEquals<Any?>(array3, mDoc)
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testHashCode() {
        // mArray1 and mArray2 have exactly same data
        // mArray3 is different
        // mArray4 is different
        // mArray5 is different

        val mArray1 = MutableArray()
        mArray1.addValue(1L)
        mArray1.addValue("Hello")
        mArray1.addValue(null)

        val mArray2 = MutableArray()
        mArray2.addValue(1L)
        mArray2.addValue("Hello")
        mArray2.addValue(null)

        val mArray3 = MutableArray()
        mArray3.addValue(100L)
        mArray3.addValue(true)

        val mArray4 = MutableArray()
        mArray4.addValue(100L)

        val mArray5 = MutableArray()
        mArray4.addValue(100L)
        mArray3.addValue(false)

        val mDoc = MutableDocument("test")
        mDoc.setArray("array1", mArray1)
        mDoc.setArray("array2", mArray2)
        mDoc.setArray("array3", mArray3)
        mDoc.setArray("array4", mArray4)
        mDoc.setArray("array5", mArray5)

        val doc: Document = saveDocInBaseTestDb(mDoc)
        val array1 = doc.getArray("array1")!!
        val array2 = doc.getArray("array2")!!
        val array3 = doc.getArray("array3")
        val array4 = doc.getArray("array4")
        val array5 = doc.getArray("array5")

        assertEquals(array1.hashCode(), array1.hashCode())
        assertEquals(array1.hashCode(), array2.hashCode())
        assertEquals(array2.hashCode(), array1.hashCode())
        assertEquals(array1.hashCode(), array1.toMutable().hashCode())
        assertEquals(array1.hashCode(), array2.toMutable().hashCode())
        assertEquals(array1.hashCode(), mArray1.hashCode())
        assertEquals(array1.hashCode(), mArray2.hashCode())
        assertEquals(array2.hashCode(), mArray1.hashCode())
        assertEquals(array2.hashCode(), mArray2.hashCode())

        assertNotEquals(array3.hashCode(), array1.hashCode())
        assertNotEquals(array3.hashCode(), array2.hashCode())
        assertNotEquals(array3.hashCode(), array1.toMutable().hashCode())
        assertNotEquals(array3.hashCode(), array2.toMutable().hashCode())
        assertNotEquals(array3.hashCode(), mArray1.hashCode())
        assertNotEquals(array3.hashCode(), mArray2.hashCode())
        assertNotEquals(mArray3.hashCode(), array1.hashCode())
        assertNotEquals(mArray3.hashCode(), array2.hashCode())
        assertNotEquals(mArray3.hashCode(), array1.toMutable().hashCode())
        assertNotEquals(mArray3.hashCode(), array2.toMutable().hashCode())
        assertNotEquals(mArray3.hashCode(), mArray1.hashCode())
        assertNotEquals(mArray3.hashCode(), mArray2.hashCode())

        assertNotEquals(array1.hashCode(), array4.hashCode())
        assertNotEquals(array1.hashCode(), array5.hashCode())
        assertNotEquals(array2.hashCode(), array4.hashCode())
        assertNotEquals(array2.hashCode(), array5.hashCode())
        assertNotEquals(array3.hashCode(), array4.hashCode())
        assertNotEquals(array3.hashCode(), array5.hashCode())

        assertNotEquals(0, array3.hashCode())
        assertNotEquals(array3.hashCode(), Any().hashCode())
        assertNotEquals(array3.hashCode(), 1.hashCode())
        assertNotEquals(array3.hashCode(), emptyMap<Any, Any>().hashCode())
        assertNotEquals(array3.hashCode(), MutableDictionary().hashCode())
        assertNotEquals(array3.hashCode(), MutableArray().hashCode())
        assertNotEquals(mArray3.hashCode(), doc.hashCode())
        assertNotEquals(mArray3.hashCode(), mDoc.hashCode())
        assertNotEquals(mArray3.hashCode(), array1.toMutable().hashCode())
        assertNotEquals(mArray3.hashCode(), array2.toMutable().hashCode())
        assertNotEquals(mArray3.hashCode(), mArray1.hashCode())
        assertNotEquals(mArray3.hashCode(), mArray2.hashCode())
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testGetDictionary() {
        val mNestedDict = MutableDictionary()
        mNestedDict.setValue("key1", 1L)
        mNestedDict.setValue("key2", "Hello")
        mNestedDict.setValue("key3", null)
        val mArray = MutableArray()
        mArray.addValue(1L)
        mArray.addValue("Hello")
        mArray.addValue(null)
        mArray.addValue(mNestedDict)
        val mDoc = MutableDocument("test")
        mDoc.setArray("array", mArray)
        val doc: Document = saveDocInBaseTestDb(mDoc)
        val array = doc.getArray("array")
        assertNotNull(array)
        assertNull(array.getDictionary(0))
        assertNull(array.getDictionary(1))
        assertNull(array.getDictionary(2))
        assertNotNull(array.getDictionary(3))
        assertFailsWith<IndexOutOfBoundsException> {
            assertNull(array.getDictionary(4))
        }
        val nestedDict = array.getDictionary(3)
        assertEquals(nestedDict, mNestedDict)
        assertEquals(array, mArray)
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testGetArray2() {
        val mNestedArray = MutableArray()
        mNestedArray.addValue(1L)
        mNestedArray.addValue("Hello")
        mNestedArray.addValue(null)
        val mArray = MutableArray()
        mArray.addValue(1L)
        mArray.addValue("Hello")
        mArray.addValue(null)
        mArray.addValue(mNestedArray)
        val mDoc = MutableDocument("test")
        mDoc.setValue("array", mArray)
        val doc: Document = saveDocInBaseTestDb(mDoc)
        val array = doc.getArray("array")
        assertNotNull(array)
        assertNull(array.getArray(0))
        assertNull(array.getArray(1))
        assertNull(array.getArray(2))
        assertNotNull(array.getArray(3))
        assertFailsWith<IndexOutOfBoundsException> {
            assertNull(array.getArray(4))
        }
        val nestedArray = array.getArray(3)
        assertEquals(nestedArray, mNestedArray)
        assertEquals(array, mArray)
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testAddInt() {
        val mDoc = MutableDocument("test")
        val mArray = MutableArray()
        mArray.addInt(0)
        mArray.addInt(Int.MAX_VALUE)
        mArray.addInt(Int.MIN_VALUE)
        mDoc.setArray("array", mArray)
        saveDocInBaseTestDb(mDoc) { doc ->
            assertEquals(1, doc.count)
            assertTrue(doc.contains("array"))
            val array = doc.getArray("array")
            assertNotNull(array)
            assertEquals(3, array.count)
            assertEquals(0, array.getInt(0))
            assertEquals(Int.MAX_VALUE, array.getInt(1))
            assertEquals(Int.MIN_VALUE, array.getInt(2))
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testSetInt() {
        val mDoc = MutableDocument("test")
        val mArray = MutableArray()
        mArray.addInt(0)
        mArray.addInt(Int.MAX_VALUE)
        mArray.addInt(Int.MIN_VALUE)
        mArray.setInt(0, Int.MAX_VALUE)
        mArray.setInt(1, Int.MIN_VALUE)
        mArray.setInt(2, 0)
        mDoc.setArray("array", mArray)
        saveDocInBaseTestDb(mDoc) { doc ->
            assertEquals(1, doc.count)
            assertTrue(doc.contains("array"))
            val array = doc.getArray("array")
            assertNotNull(array)
            assertEquals(3, array.count)
            assertEquals(0, array.getInt(2))
            assertEquals(Int.MAX_VALUE, array.getInt(0))
            assertEquals(Int.MIN_VALUE, array.getInt(1))
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testInsertInt() {
        val mDoc = MutableDocument("test")
        val mArray = MutableArray()
        mArray.addInt(10) // will be pushed 3 times.
        mArray.insertInt(0, 0)
        mArray.insertInt(1, Int.MAX_VALUE)
        mArray.insertInt(2, Int.MIN_VALUE)
        mDoc.setArray("array", mArray)
        saveDocInBaseTestDb(mDoc) { doc ->
            assertEquals(1, doc.count)
            assertTrue(doc.contains("array"))
            val array = doc.getArray("array")
            assertNotNull(array)
            assertEquals(4, array.count)
            assertEquals(0, array.getInt(0))
            assertEquals(Int.MAX_VALUE, array.getInt(1))
            assertEquals(Int.MIN_VALUE, array.getInt(2))
            assertEquals(10, array.getInt(3))
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testAddLong() {
        val mDoc = MutableDocument("test")
        val mArray = MutableArray()
        mArray.addLong(0)
        mArray.addLong(Long.MAX_VALUE)
        mArray.addLong(Long.MIN_VALUE)
        mDoc.setArray("array", mArray)
        saveDocInBaseTestDb(mDoc) { doc ->
            assertEquals(1, doc.count)
            assertTrue(doc.contains("array"))
            val array = doc.getArray("array")
            assertNotNull(array)
            assertEquals(3, array.count)
            assertEquals(0, array.getLong(0))
            assertEquals(Long.MAX_VALUE, array.getLong(1))
            assertEquals(Long.MIN_VALUE, array.getLong(2))
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testSetLong() {
        val mDoc = MutableDocument("test")
        val mArray = MutableArray()
        mArray.addLong(0)
        mArray.addLong(Long.MAX_VALUE)
        mArray.addLong(Long.MIN_VALUE)
        mArray.setLong(0, Long.MAX_VALUE)
        mArray.setLong(1, Long.MIN_VALUE)
        mArray.setLong(2, 0)
        mDoc.setArray("array", mArray)
        saveDocInBaseTestDb(mDoc) { doc ->
            assertEquals(1, doc.count)
            assertTrue(doc.contains("array"))
            val array = doc.getArray("array")
            assertNotNull(array)
            assertEquals(3, array.count)
            assertEquals(0, array.getLong(2))
            assertEquals(Long.MAX_VALUE, array.getLong(0))
            assertEquals(Long.MIN_VALUE, array.getLong(1))
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testInsertLong() {
        val mDoc = MutableDocument("test")
        val mArray = MutableArray()
        mArray.addLong(10) // will be pushed 3 times.
        mArray.insertLong(0, 0)
        mArray.insertLong(1, Long.MAX_VALUE)
        mArray.insertLong(2, Long.MIN_VALUE)
        mDoc.setArray("array", mArray)
        saveDocInBaseTestDb(mDoc) { doc ->
            assertEquals(1, doc.count)
            assertTrue(doc.contains("array"))
            val array = doc.getArray("array")
            assertNotNull(array)
            assertEquals(4, array.count)
            assertEquals(0, array.getLong(0))
            assertEquals(Long.MAX_VALUE, array.getLong(1))
            assertEquals(Long.MIN_VALUE, array.getLong(2))
            assertEquals(10, array.getLong(3))
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testAddFloat() {
        val mDoc = MutableDocument("test")
        val mArray = MutableArray()
        mArray.addFloat(0.0f)
        mArray.addFloat(Float.MAX_VALUE)
        mArray.addFloat(Float.MIN_VALUE)
        mDoc.setArray("array", mArray)
        saveDocInBaseTestDb(mDoc) { doc ->
            assertEquals(1, doc.count)
            assertTrue(doc.contains("array"))
            val array = doc.getArray("array")
            assertNotNull(array)
            assertEquals(3, array.count)
            assertEquals(0.0f, array.getFloat(0), 0.0f)
            assertEquals(Float.MAX_VALUE, array.getFloat(1), 0.0f)
            assertEquals(Float.MIN_VALUE, array.getFloat(2), 0.0f)
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testSetFloat() {
        val mDoc = MutableDocument("test")
        val mArray = MutableArray()
        mArray.addFloat(0f)
        mArray.addFloat(Float.MAX_VALUE)
        mArray.addFloat(Float.MIN_VALUE)
        mArray.setFloat(0, Float.MAX_VALUE)
        mArray.setFloat(1, Float.MIN_VALUE)
        mArray.setFloat(2, 0f)
        mDoc.setArray("array", mArray)
        saveDocInBaseTestDb(mDoc) { doc ->
            assertEquals(1, doc.count)
            assertTrue(doc.contains("array"))
            val array = doc.getArray("array")
            assertNotNull(array)
            assertEquals(3, array.count)
            assertEquals(0.0f, array.getLong(2).toFloat(), 0.0f)
            assertEquals(Float.MAX_VALUE, array.getFloat(0), 0.0f)
            assertEquals(Float.MIN_VALUE, array.getFloat(1), 0.0f)
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testInsertFloat() {
        val mDoc = MutableDocument("test")
        val mArray = MutableArray()
        mArray.addFloat(10f) // will be pushed 3 times.
        mArray.insertFloat(0, 0f)
        mArray.insertFloat(1, Float.MAX_VALUE)
        mArray.insertFloat(2, Float.MIN_VALUE)
        mDoc.setArray("array", mArray)
        saveDocInBaseTestDb(mDoc) { doc ->
            assertEquals(1, doc.count)
            assertTrue(doc.contains("array"))
            val array = doc.getArray("array")
            assertNotNull(array)
            assertEquals(4, array.count)
            assertEquals(0f, array.getFloat(0), 0f)
            assertEquals(Float.MAX_VALUE, array.getFloat(1), 0f)
            assertEquals(Float.MIN_VALUE, array.getFloat(2), 0f)
            assertEquals(10f, array.getFloat(3), 0f)
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testAddDouble() {
        val mDoc = MutableDocument("test")
        val mArray = MutableArray()
        mArray.addDouble(0.0)
        mArray.addDouble(Double.MAX_VALUE)
        mArray.addDouble(Double.MIN_VALUE)
        mDoc.setArray("array", mArray)
        saveDocInBaseTestDb(mDoc) { doc ->
            assertEquals(1, doc.count)
            assertTrue(doc.contains("array"))
            val array = doc.getArray("array")
            assertNotNull(array)
            assertEquals(3, array.count)
            assertEquals(0.0, array.getDouble(0), 0.0)
            assertEquals(Double.MAX_VALUE, array.getDouble(1), 0.0)
            assertEquals(Double.MIN_VALUE, array.getDouble(2), 0.0)
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testSetDouble() {
        val mDoc = MutableDocument("test")
        val mArray = MutableArray()
        mArray.addDouble(0.0)
        mArray.addDouble(Double.MAX_VALUE)
        mArray.addDouble(Double.MIN_VALUE)
        mArray.setDouble(0, Double.MAX_VALUE)
        mArray.setDouble(1, Double.MIN_VALUE)
        mArray.setDouble(2, 0.0)
        mDoc.setArray("array", mArray)
        saveDocInBaseTestDb(mDoc) { doc ->
            assertEquals(1, doc.count)
            assertTrue(doc.contains("array"))
            val array = doc.getArray("array")
            assertNotNull(array)
            assertEquals(3, array.count)
            assertEquals(0.0, array.getDouble(2), 0.0)
            assertEquals(Double.MAX_VALUE, array.getDouble(0), 0.0)
            assertEquals(Double.MIN_VALUE, array.getDouble(1), 0.0)
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testInsertDouble() {
        val mDoc = MutableDocument("test")
        val mArray = MutableArray()
        mArray.addDouble(10.0) // will be pushed 3 times.
        mArray.insertDouble(0, 0.0)
        mArray.insertDouble(1, Double.MAX_VALUE)
        mArray.insertDouble(2, Double.MIN_VALUE)
        mDoc.setArray("array", mArray)
        saveDocInBaseTestDb(mDoc) { doc ->
            assertEquals(1, doc.count)
            assertTrue(doc.contains("array"))
            val array = doc.getArray("array")
            assertNotNull(array)
            assertEquals(4, array.count)
            assertEquals(0.0, array.getDouble(0), 0.0)
            assertEquals(Double.MAX_VALUE, array.getDouble(1), 0.0)
            assertEquals(Double.MIN_VALUE, array.getDouble(2), 0.0)
            assertEquals(10.0, array.getDouble(3), 0.0)
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testAddNumber() {
        val mDoc = MutableDocument("test")
        val mArray = MutableArray()
        mArray.addNumber(Int.MAX_VALUE)
        mArray.addNumber(Long.MAX_VALUE)
        mArray.addNumber(Double.MAX_VALUE)
        mDoc.setArray("array", mArray)
        saveDocInBaseTestDb(mDoc) { doc ->
            assertEquals(1, doc.count)
            assertTrue(doc.contains("array"))
            val array = doc.getArray("array")
            assertNotNull(array)
            assertEquals(3, array.count)

            assertEquals(Int.MAX_VALUE, array.getNumber(0)?.toInt())
            assertEquals(Long.MAX_VALUE, array.getNumber(1)?.toLong())
            assertEquals(Double.MAX_VALUE, array.getNumber(2)!!.toDouble(), 0.0)
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testSetNumber() {
        val mDoc = MutableDocument("test")
        val mArray = MutableArray()
        mArray.addNumber(Int.MAX_VALUE)
        mArray.addNumber(Long.MAX_VALUE)
        mArray.addNumber(Double.MAX_VALUE)
        mArray.setNumber(0, Long.MAX_VALUE)
        mArray.setNumber(1, Double.MAX_VALUE)
        mArray.setNumber(2, Int.MAX_VALUE)
        mDoc.setArray("array", mArray)
        saveDocInBaseTestDb(mDoc) { doc ->
            assertEquals(1, doc.count)
            assertTrue(doc.contains("array"))
            val array = doc.getArray("array")
            assertNotNull(array)
            assertEquals(3, array.count)
            assertEquals(Int.MAX_VALUE, array.getNumber(2)?.toInt())
            assertEquals(Long.MAX_VALUE, array.getNumber(0)?.toLong())
            assertEquals(Double.MAX_VALUE, array.getNumber(1)!!.toDouble(), 0.0)
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testInsertNumber() {
        val mDoc = MutableDocument("test")
        val mArray = MutableArray()
        mArray.addNumber(10L) // will be pushed 3 times.
        mArray.insertNumber(0, Int.MAX_VALUE)
        mArray.insertNumber(1, Long.MAX_VALUE)
        mArray.insertNumber(2, Double.MAX_VALUE)
        mDoc.setArray("array", mArray)
        saveDocInBaseTestDb(mDoc) { doc ->
            assertEquals(1, doc.count)
            assertTrue(doc.contains("array"))
            val array = doc.getArray("array")
            assertNotNull(array)
            assertEquals(4, array.count)
            assertEquals(Int.MAX_VALUE, array.getInt(0))
            assertEquals(Long.MAX_VALUE, array.getLong(1))
            assertEquals(Double.MAX_VALUE, array.getDouble(2), 0.0)
            assertEquals(10L, array.getNumber(3)?.toLong())
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testAddString() {
        val mDoc = MutableDocument("test")
        val mArray = MutableArray()
        mArray.addString("")
        mArray.addString("Hello")
        mArray.addString("World")
        mDoc.setArray("array", mArray)
        saveDocInBaseTestDb(mDoc) { doc ->
            assertEquals(1, doc.count)
            assertTrue(doc.contains("array"))
            val array = doc.getArray("array")
            assertNotNull(array)
            assertEquals(3, array.count)
            assertEquals("", array.getString(0))
            assertEquals("Hello", array.getString(1))
            assertEquals("World", array.getString(2))
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testSetString() {
        val mDoc = MutableDocument("test")
        val mArray = MutableArray()
        mArray.addString("")
        mArray.addString("Hello")
        mArray.addString("World")
        mArray.setString(0, "Hello")
        mArray.setString(1, "World")
        mArray.setString(2, "")
        mDoc.setArray("array", mArray)
        saveDocInBaseTestDb(mDoc) { doc ->
            assertEquals(1, doc.count)
            assertTrue(doc.contains("array"))
            val array = doc.getArray("array")
            assertNotNull(array)
            assertEquals(3, array.count)
            assertEquals("", array.getString(2))
            assertEquals("Hello", array.getString(0))
            assertEquals("World", array.getString(1))
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testInsertString() {
        val mDoc = MutableDocument("test")
        val mArray = MutableArray()
        mArray.addString("") // will be pushed 3 times.
        mArray.insertString(0, "Hello")
        mArray.insertString(1, "World")
        mArray.insertString(2, "!")
        mDoc.setArray("array", mArray)
        saveDocInBaseTestDb(mDoc) { doc ->
            assertEquals(1, doc.count)
            assertTrue(doc.contains("array"))
            val array = doc.getArray("array")
            assertNotNull(array)
            assertEquals(4, array.count)
            assertEquals("Hello", array.getString(0))
            assertEquals("World", array.getString(1))
            assertEquals("!", array.getString(2))
            assertEquals("", array.getString(3))
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testAddBoolean() {
        val mDoc = MutableDocument("test")
        val mArray = MutableArray()
        mArray.addBoolean(true)
        mArray.addBoolean(false)
        mDoc.setArray("array", mArray)
        saveDocInBaseTestDb(mDoc) { doc ->
            assertEquals(1, doc.count)
            assertTrue(doc.contains("array"))
            val array = doc.getArray("array")
            assertNotNull(array)
            assertEquals(2, array.count)
            assertTrue(array.getBoolean(0))
            assertFalse(array.getBoolean(1))
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testSetBoolean() {
        val mDoc = MutableDocument("test")
        val mArray = MutableArray()
        mArray.addBoolean(true)
        mArray.addBoolean(false)
        mArray.setBoolean(0, false)
        mArray.setBoolean(1, true)
        mDoc.setArray("array", mArray)
        saveDocInBaseTestDb(mDoc) { doc ->
            assertEquals(1, doc.count)
            assertTrue(doc.contains("array"))
            val array = doc.getArray("array")
            assertNotNull(array)
            assertEquals(2, array.count)
            assertTrue(array.getBoolean(1))
            assertFalse(array.getBoolean(0))
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testInsertBoolean() {
        val mDoc = MutableDocument("test")
        val mArray = MutableArray()
        mArray.addBoolean(false) // will be pushed 2 times
        mArray.addBoolean(true) // will be pushed 2 times.
        mArray.insertBoolean(0, true)
        mArray.insertBoolean(1, false)
        mDoc.setArray("array", mArray)
        saveDocInBaseTestDb(mDoc) { doc ->
            assertEquals(1, doc.count)
            assertTrue(doc.contains("array"))
            val array = doc.getArray("array")
            assertNotNull(array)
            assertEquals(4, array.count)
            assertTrue(array.getBoolean(0))
            assertFalse(array.getBoolean(1))
            assertFalse(array.getBoolean(2))
            assertTrue(array.getBoolean(3))
        }
    }

    ///////////////  JSON tests
    // JSON 3.4
    @Test
    @Throws(
        CouchbaseLiteException::class,
        SerializationException::class,
        IllegalArgumentException::class,
        IllegalStateException::class,
        NumberFormatException::class
    )
    fun testArrayToJSON() {
        val mDoc = MutableDocument().setArray("array", makeArray())
        verifyArray(
            Json.parseToJsonElement(
                saveDocInBaseTestDb(mDoc).getArray("array")!!.toJSON()
            ).jsonArray
        )
    }

    // JSON 3.7.?
    @Test
    fun testArrayToJSONBeforeSave() {
        assertFailsWith<IllegalStateException> {
            MutableArray().toJSON()
        }
    }

    // JSON 3.7.a-b
    @Test
    @Throws(
        SerializationException::class,
        IOException::class,
        CouchbaseLiteException::class,
        IllegalArgumentException::class,
        IllegalStateException::class,
        NumberFormatException::class
    )
    fun testArrayFromJSON() {
        val mArray = MutableArray(readJSONResource("array.json"))
        val mDoc = MutableDocument().setArray("array", mArray)
        val dbArray = saveDocInBaseTestDb(mDoc).getArray("array")
        verifyArray(dbArray)
        verifyArray(Json.parseToJsonElement(dbArray!!.toJSON()).jsonArray)
    }

    // JSON 3.7.c.1
    @Test
    fun testArrayFromBadJSON1() {
        assertFailsWith<IllegalArgumentException> {
            MutableArray("[")
        }
    }

    // JSON 3.7.c.2
    @Test
    fun testArrayFromBadJSON2() {
        assertFailsWith<IllegalArgumentException> {
            MutableArray("[ab cd]")
        }
    }

    // JSON 3.7.d
    @Test
    @Throws(IOException::class)
    fun testDictFromArray() {
        assertFailsWith<IllegalArgumentException> {
            MutableArray(readJSONResource("dictionary.json"))
        }
    }

    ///////////////  Tooling
    private fun arrayOfAllTypes(): List<Any?> {
        val list = mutableListOf<Any?>(
            true,
            false,
            "string",
            0,
            1,
            -1,
            1.1,
            Instant.parse(TEST_DATE),
            null
        )

        // Dictionary
        val subdict = MutableDictionary()
        subdict.setValue("name", "Scott Tiger")
        list.add(subdict)

        // Array
        val subarray = MutableArray()
        subarray.addValue("a")
        subarray.addValue("b")
        subarray.addValue("c")
        list.add(subarray)

        // Blob
        list.add(Blob("text/plain", BLOB_CONTENT.encodeToByteArray()))
        return list
    }

    private fun populateData(array: MutableArray) {
        val data = arrayOfAllTypes()
        for (o in data) {
            array.addValue(o)
        }
    }

    private fun populateDataByType(array: MutableArray) {
        val data = arrayOfAllTypes()
        for (o in data) {
            when (o) {
                is Int -> array.addInt(o)
                is Long -> array.addLong(o)
                is Float -> array.addFloat(o)
                is Double -> array.addDouble(o)
                is Number -> array.addNumber(o)
                is String -> array.addString(o)
                is Boolean -> array.addBoolean(o)
                is Instant -> array.addDate(o)
                is Blob -> array.addBlob(o)
                is MutableDictionary -> array.addDictionary(o)
                is MutableArray -> array.addArray(o)
                else -> array.addValue(o)
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    private fun save(
        mDoc: MutableDocument,
        key: String,
        mArray: MutableArray,
        validator: (Array) -> Unit
    ): Document {
        validator(mArray)
        mDoc.setValue(key, mArray)
        val doc: Document = saveDocInBaseTestDb(mDoc)
        val array = doc.getArray(key)
        validator(array!!)
        return doc
    }

    private fun verifyBlob(obj: Any?) {
        assertTrue(obj is Blob)
        val blob: Blob = obj
        assertNotNull(blob)
        val contents = blob.getContent()
        assertNotNull(contents)
        assertContentEquals(BLOB_CONTENT.encodeToByteArray(), contents)
        assertEquals(BLOB_CONTENT, contents.decodeToString())
    }
}
