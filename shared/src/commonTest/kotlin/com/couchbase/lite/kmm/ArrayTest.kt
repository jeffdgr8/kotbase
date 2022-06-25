//package com.couchbase.lite.kmm
//
//class ArrayTest {
//}
//
////
//// Copyright (c) 2020 Couchbase, Inc.
////
//// Licensed under the Apache License, Version 2.0 (the "License");
//// you may not use this file except in compliance with the License.
//// You may obtain a copy of the License at
////
//// http://www.apache.org/licenses/LICENSE-2.0
////
//// Unless required by applicable law or agreed to in writing, software
//// distributed under the License is distributed on an "AS IS" BASIS,
//// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//// See the License for the specific language governing permissions and
//// limitations under the License.
////
//import com.couchbase.lite.internal.utils.TestUtils.assertThrows
//
//
//class ArrayTest : BaseDbTest() {
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testCreate() {
//        val array = MutableArray()
//        assertEquals(0, array.count())
//        assertEquals(java.util.ArrayList<E>(), array.toList())
//        val doc = MutableDocument("doc1")
//        doc.setValue("array", array)
//        org.junit.Assert.assertEquals(array, doc.getArray("array"))
//        val updatedDoc: Document = saveDocInBaseTestDb(doc)
//        assertEquals(java.util.ArrayList<E>(), updatedDoc.getArray("array")!!.toList())
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testCreateWithList() {
//        val data: MutableList<Any?> = java.util.ArrayList<Any>()
//        data.add("1")
//        data.add("2")
//        data.add("3")
//        val array = MutableArray(data)
//        assertEquals(3, array.count())
//        assertEquals(data, array.toList())
//        val doc = MutableDocument("doc1")
//        doc.setValue("array", array)
//        org.junit.Assert.assertEquals(array, doc.getArray("array"))
//        val savedDoc: Document = saveDocInBaseTestDb(doc)
//        assertEquals(data, savedDoc.getArray("array")!!.toList())
//    }
//
//    @org.junit.Test(expected = java.lang.IllegalArgumentException::class)
//    fun testRecursiveArray() {
//        val array = MutableArray()
//        array.addArray(array)
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testSetList() {
//        var data: MutableList<Any?> = java.util.ArrayList<Any>()
//        data.add("1")
//        data.add("2")
//        data.add("3")
//        var array = MutableArray()
//        array.setData(data)
//        assertEquals(3, array.count())
//        assertEquals(data, array.toList())
//        val doc = MutableDocument("doc1")
//        doc.setValue("array", array)
//        org.junit.Assert.assertEquals(array, doc.getArray("array"))
//
//        // save
//        val savedDoc: Document = saveDocInBaseTestDb(doc)
//        assertEquals(data, savedDoc.getArray("array")!!.toList())
//
//        // update
//        array = savedDoc.getArray("array")!!.toMutable()
//        data = java.util.ArrayList<Any>()
//        data.add("4")
//        data.add("5")
//        data.add("6")
//        array.setData(data)
//        assertEquals(data.size, array.count())
//        assertEquals(data, array.toList())
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testAddNull() {
//        val array = MutableArray()
//        array.addValue(null)
//        val doc = MutableDocument("doc1")
//        save(doc, "array", array, com.couchbase.lite.internal.utils.Fn.Consumer<Array> { a: Array ->
//            assertEquals(1, a.count())
//            org.junit.Assert.assertNull(a.getValue(0))
//        })
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testAddObjects() {
//        for (i in 0..1) {
//            val array = MutableArray()
//
//            // Add objects of all types:
//            if (i % 2 == 0) {
//                populateData(array)
//            } else {
//                populateDataByType(array)
//            }
//            val doc = MutableDocument("doc1")
//            save(
//                doc,
//                "array",
//                array,
//                com.couchbase.lite.internal.utils.Fn.Consumer<Array> { a: Array ->
//                    assertEquals(12, a.count())
//                    assertEquals(true, a.getValue(0))
//                    assertEquals(false, a.getValue(1))
//                    assertEquals("string", a.getValue(2))
//                    org.junit.Assert.assertEquals(
//                        0,
//                        (a.getValue(3) as Number?)!!.toInt().toLong()
//                    )
//                    org.junit.Assert.assertEquals(
//                        1,
//                        (a.getValue(4) as Number?)!!.toInt().toLong()
//                    )
//                    org.junit.Assert.assertEquals(
//                        -1,
//                        (a.getValue(5) as Number?)!!.toInt().toLong()
//                    )
//                    assertEquals(1.1, a.getValue(6))
//                    assertEquals(TEST_DATE, a.getValue(7))
//                    org.junit.Assert.assertNull(a.getValue(8))
//
//                    // dictionary
//                    val dict =
//                        a.getValue(9) as Dictionary?
//                    val subdict =
//                        if (dict is MutableDictionary) dict else dict!!.toMutable()
//                    val expectedMap: MutableMap<String, Any> =
//                        java.util.HashMap<String, Any>()
//                    expectedMap["name"] = "Scott Tiger"
//                    assertEquals(expectedMap, subdict.toMap())
//
//                    // array
//                    val array1 =
//                        a.getValue(10) as Array?
//                    val subarray =
//                        if (array1 is MutableArray) array1 else array1!!.toMutable()
//                    val expected: MutableList<Any> = java.util.ArrayList<Any>()
//                    expected.add("a")
//                    expected.add("b")
//                    expected.add("c")
//                    assertEquals(expected, subarray.toList())
//
//                    // blob
//                    verifyBlob(a.getValue(11))
//                })
//        }
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testAddObjectsToExistingArray() {
//        for (i in 0..1) {
//            var array = MutableArray()
//            if (i % 2 == 0) {
//                populateData(array)
//            } else {
//                populateDataByType(array)
//            }
//
//            // Save
//            val docID: String = String.format(java.util.Locale.ENGLISH, "doc%d", i)
//            var doc = MutableDocument(docID)
//            doc.setValue("array", array)
//            doc = saveDocInBaseTestDb(doc).toMutable()
//
//            // Get an existing array:
//            array = doc.getArray("array")
//            org.junit.Assert.assertNotNull(array)
//            assertEquals(12, array.count())
//
//            // Update:
//            if (i % 2 == 0) {
//                populateData(array)
//            } else {
//                populateDataByType(array)
//            }
//            assertEquals(24, array.count())
//            save(
//                doc,
//                "array",
//                array,
//                com.couchbase.lite.internal.utils.Fn.Consumer<Array> { a: Array ->
//                    assertEquals(24, a.count())
//                    assertEquals(true, a.getValue(12))
//                    assertEquals(false, a.getValue(12 + 1))
//                    assertEquals("string", a.getValue(12 + 2))
//                    org.junit.Assert.assertEquals(
//                        0,
//                        (a.getValue(12 + 3) as Number?)!!.toInt().toLong()
//                    )
//                    org.junit.Assert.assertEquals(
//                        1,
//                        (a.getValue(12 + 4) as Number?)!!.toInt().toLong()
//                    )
//                    org.junit.Assert.assertEquals(
//                        -1,
//                        (a.getValue(12 + 5) as Number?)!!.toInt().toLong()
//                    )
//                    assertEquals(1.1, a.getValue(12 + 6))
//                    assertEquals(TEST_DATE, a.getValue(12 + 7))
//                    org.junit.Assert.assertNull(a.getValue(12 + 8))
//
//                    // dictionary
//                    val dict =
//                        a.getValue(12 + 9) as Dictionary?
//                    val subdict =
//                        if (dict is MutableDictionary) dict else dict!!.toMutable()
//                    val expectedMap: MutableMap<String, Any> =
//                        java.util.HashMap<String, Any>()
//                    expectedMap["name"] = "Scott Tiger"
//                    assertEquals(expectedMap, subdict.toMap())
//
//                    // array
//                    val array1 =
//                        a.getValue(12 + 10) as Array?
//                    val subarray =
//                        if (array1 is MutableArray) array1 else array1!!.toMutable()
//                    val expected: MutableList<Any> = java.util.ArrayList<Any>()
//                    expected.add("a")
//                    expected.add("b")
//                    expected.add("c")
//                    assertEquals(expected, subarray.toList())
//
//                    // blob
//                    verifyBlob(a.getValue(12 + 11))
//                })
//        }
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testSetObject() {
//        val data = arrayOfAllTypes()
//
//        // Prepare CBLArray with NSNull placeholders:
//        val array = MutableArray()
//        for (i in data.indices) {
//            array.addValue(null)
//        }
//
//        // Set object at index:
//        for (i in data.indices) {
//            array.setValue(i, data[i])
//        }
//        val doc = MutableDocument("doc1")
//        save(doc, "array", array, com.couchbase.lite.internal.utils.Fn.Consumer<Array> { a: Array ->
//            assertEquals(12, a.count())
//            assertEquals(true, a.getValue(0))
//            assertEquals(false, a.getValue(1))
//            assertEquals("string", a.getValue(2))
//            org.junit.Assert.assertEquals(0, (a.getValue(3) as Number?)!!.toInt().toLong())
//            org.junit.Assert.assertEquals(1, (a.getValue(4) as Number?)!!.toInt().toLong())
//            org.junit.Assert.assertEquals(-1, (a.getValue(5) as Number?)!!.toInt().toLong())
//            assertEquals(1.1, a.getValue(6))
//            assertEquals(TEST_DATE, a.getValue(7))
//            org.junit.Assert.assertNull(a.getValue(8))
//
//            // dictionary
//            val dict =
//                a.getValue(9) as Dictionary?
//            val subdict =
//                if (dict is MutableDictionary) dict else dict!!.toMutable()
//            val expectedMap: MutableMap<String, Any> =
//                java.util.HashMap<String, Any>()
//            expectedMap["name"] = "Scott Tiger"
//            assertEquals(expectedMap, subdict.toMap())
//
//            // array
//            val array1 =
//                a.getValue(10) as Array?
//            val subarray =
//                if (array1 is MutableArray) array1 else array1!!.toMutable()
//            val expected: MutableList<Any> = java.util.ArrayList<Any>()
//            expected.add("a")
//            expected.add("b")
//            expected.add("c")
//            assertEquals(expected, subarray.toList())
//
//            // blob
//            verifyBlob(a.getValue(11))
//        })
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testSetObjectToExistingArray() {
//        for (i in 0..1) {
//            val array = MutableArray()
//            if (i % 2 == 0) {
//                populateData(array)
//            } else {
//                populateDataByType(array)
//            }
//
//            // Save
//            val docID: String = String.format(java.util.Locale.ENGLISH, "doc%d", i)
//            var doc = MutableDocument(docID)
//            doc.setArray("array", array)
//            doc = saveDocInBaseTestDb(doc).toMutable()
//            val gotArray = doc.getArray("array")
//            val data = arrayOfAllTypes()
//            assertEquals(data.size, gotArray!!.count())
//
//            // reverse the array
//            for (j in data.indices) {
//                gotArray!!.setValue(j, data[data.size - j - 1])
//            }
//            save(
//                doc,
//                "array",
//                gotArray,
//                com.couchbase.lite.internal.utils.Fn.Consumer<Array> { a: Array ->
//                    assertEquals(12, a.count())
//                    assertEquals(true, a.getValue(11))
//                    assertEquals(false, a.getValue(10))
//                    assertEquals("string", a.getValue(9))
//                    org.junit.Assert.assertEquals(
//                        0,
//                        (a.getValue(8) as Number?)!!.toInt().toLong()
//                    )
//                    org.junit.Assert.assertEquals(
//                        1,
//                        (a.getValue(7) as Number?)!!.toInt().toLong()
//                    )
//                    org.junit.Assert.assertEquals(
//                        -1,
//                        (a.getValue(6) as Number?)!!.toInt().toLong()
//                    )
//                    assertEquals(1.1, a.getValue(5))
//                    assertEquals(TEST_DATE, a.getValue(4))
//                    org.junit.Assert.assertNull(a.getValue(3))
//
//                    // dictionary
//                    val dict =
//                        a.getValue(2) as Dictionary?
//                    val subdict =
//                        if (dict is MutableDictionary) dict else dict!!.toMutable()
//                    val expectedMap: MutableMap<String, Any> =
//                        java.util.HashMap<String, Any>()
//                    expectedMap["name"] = "Scott Tiger"
//                    assertEquals(expectedMap, subdict.toMap())
//
//                    // array
//                    val array1 =
//                        a.getValue(1) as Array?
//                    val subarray =
//                        if (array1 is MutableArray) array1 else array1!!.toMutable()
//                    val expected: MutableList<Any> = java.util.ArrayList<Any>()
//                    expected.add("a")
//                    expected.add("b")
//                    expected.add("c")
//                    assertEquals(expected, subarray.toList())
//
//                    // blob
//                    verifyBlob(a.getValue(0))
//                })
//        }
//    }
//
//    @org.junit.Test
//    fun testSetObjectOutOfBound() {
//        val array = MutableArray()
//        array.addValue("a")
//        assertThrows(java.lang.IndexOutOfBoundsException::class.java) { array.setValue(-1, "b") }
//        assertThrows(java.lang.IndexOutOfBoundsException::class.java) { array.setValue(1, "b") }
//    }
//
//    @org.junit.Test
//    fun testInsertObject() {
//        val array = MutableArray()
//        array.insertValue(0, "a")
//        assertEquals(1, array.count())
//        assertEquals("a", array.getValue(0))
//        array.insertValue(0, "c")
//        assertEquals(2, array.count())
//        assertEquals("c", array.getValue(0))
//        assertEquals("a", array.getValue(1))
//        array.insertValue(1, "d")
//        assertEquals(3, array.count())
//        assertEquals("c", array.getValue(0))
//        assertEquals("d", array.getValue(1))
//        assertEquals("a", array.getValue(2))
//        array.insertValue(2, "e")
//        assertEquals(4, array.count())
//        assertEquals("c", array.getValue(0))
//        assertEquals("d", array.getValue(1))
//        assertEquals("e", array.getValue(2))
//        assertEquals("a", array.getValue(3))
//        array.insertValue(4, "f")
//        assertEquals(5, array.count())
//        assertEquals("c", array.getValue(0))
//        assertEquals("d", array.getValue(1))
//        assertEquals("e", array.getValue(2))
//        assertEquals("a", array.getValue(3))
//        assertEquals("f", array.getValue(4))
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testInsertObjectToExistingArray() {
//        var mDoc = MutableDocument("doc1")
//        mDoc.setValue("array", MutableArray())
//        var doc: Document = saveDocInBaseTestDb(mDoc)
//        mDoc = doc.toMutable()
//        var mArray = mDoc.getArray("array")
//        org.junit.Assert.assertNotNull(mArray)
//        mArray!!.insertValue(0, "a")
//        doc = save(
//            mDoc,
//            "array",
//            mArray,
//            com.couchbase.lite.internal.utils.Fn.Consumer<Array> { a: Array ->
//                assertEquals(1, a.count())
//                assertEquals("a", a.getValue(0))
//            })
//        mDoc = doc.toMutable()
//        mArray = mDoc.getArray("array")
//        org.junit.Assert.assertNotNull(mArray)
//        mArray!!.insertValue(0, "c")
//        doc = save(
//            mDoc,
//            "array",
//            mArray,
//            com.couchbase.lite.internal.utils.Fn.Consumer<Array> { a: Array ->
//                assertEquals(2, a.count())
//                assertEquals("c", a.getValue(0))
//                assertEquals("a", a.getValue(1))
//            })
//        mDoc = doc.toMutable()
//        mArray = mDoc.getArray("array")
//        org.junit.Assert.assertNotNull(mArray)
//        mArray!!.insertValue(1, "d")
//        doc = save(
//            mDoc,
//            "array",
//            mArray,
//            com.couchbase.lite.internal.utils.Fn.Consumer<Array> { a: Array ->
//                assertEquals(3, a.count())
//                assertEquals("c", a.getValue(0))
//                assertEquals("d", a.getValue(1))
//                assertEquals("a", a.getValue(2))
//            })
//        mDoc = doc.toMutable()
//        mArray = mDoc.getArray("array")
//        org.junit.Assert.assertNotNull(mArray)
//        mArray!!.insertValue(2, "e")
//        doc = save(
//            mDoc,
//            "array",
//            mArray,
//            com.couchbase.lite.internal.utils.Fn.Consumer<Array> { a: Array ->
//                assertEquals(4, a.count())
//                assertEquals("c", a.getValue(0))
//                assertEquals("d", a.getValue(1))
//                assertEquals("e", a.getValue(2))
//                assertEquals("a", a.getValue(3))
//            })
//        mDoc = doc.toMutable()
//        mArray = mDoc.getArray("array")
//        org.junit.Assert.assertNotNull(mArray)
//        mArray!!.insertValue(4, "f")
//        save(
//            mDoc,
//            "array",
//            mArray,
//            com.couchbase.lite.internal.utils.Fn.Consumer<Array> { a: Array ->
//                assertEquals(5, a.count())
//                assertEquals("c", a.getValue(0))
//                assertEquals("d", a.getValue(1))
//                assertEquals("e", a.getValue(2))
//                assertEquals("a", a.getValue(3))
//                assertEquals("f", a.getValue(4))
//            })
//    }
//
//    @org.junit.Test
//    fun testInsertObjectOutOfBound() {
//        val array = MutableArray()
//        array.addValue("a")
//        assertThrows(java.lang.IndexOutOfBoundsException::class.java) { array.insertValue(-1, "b") }
//        assertThrows(java.lang.IndexOutOfBoundsException::class.java) { array.insertValue(2, "b") }
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testRemove() {
//        for (i in 0..1) {
//            val array = MutableArray()
//            if (i % 2 == 0) {
//                populateData(array)
//            } else {
//                populateDataByType(array)
//            }
//            for (j in array.count() - 1 downTo 0) {
//                array.remove(j)
//            }
//            val docID: String = String.format(java.util.Locale.ENGLISH, "doc%d", i)
//            val doc = MutableDocument(docID)
//            save(
//                doc,
//                "array",
//                array,
//                com.couchbase.lite.internal.utils.Fn.Consumer<Array> { a: Array ->
//                    assertEquals(0, a.count())
//                    assertEquals(java.util.ArrayList<E>(), a.toList())
//                })
//        }
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testRemoveExistingArray() {
//        for (i in 0..1) {
//            var array = MutableArray()
//            if (i % 2 == 0) {
//                populateData(array)
//            } else {
//                populateDataByType(array)
//            }
//            val docID: String = String.format(java.util.Locale.ENGLISH, "doc%d", i)
//            var doc = MutableDocument(docID)
//            doc.setValue("array", array)
//            doc = saveDocInBaseTestDb(doc).toMutable()
//            array = doc.getArray("array")
//            for (j in array.count() - 1 downTo 0) {
//                array.remove(j)
//            }
//            save(
//                doc,
//                "array",
//                array,
//                com.couchbase.lite.internal.utils.Fn.Consumer<Array> { a: Array ->
//                    assertEquals(0, a.count())
//                    assertEquals(java.util.ArrayList<E>(), a.toList())
//                })
//        }
//    }
//
//    @org.junit.Test
//    fun testRemoveOutOfBound() {
//        val array = MutableArray()
//        array.addValue("a")
//        assertThrows(java.lang.IndexOutOfBoundsException::class.java) { array.remove(-1) }
//        assertThrows(java.lang.IndexOutOfBoundsException::class.java) { array.remove(1) }
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testCount() {
//        for (i in 0..1) {
//            val array = MutableArray()
//            if (i % 2 == 0) {
//                populateData(array)
//            } else {
//                populateDataByType(array)
//            }
//            val docID: String = String.format(java.util.Locale.ENGLISH, "doc%d", i)
//            val doc = MutableDocument(docID)
//            save(doc, "array", array,
//                com.couchbase.lite.internal.utils.Fn.Consumer<Array> { a: Array ->
//                    assertEquals(
//                        12,
//                        a.count()
//                    )
//                })
//        }
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testGetString() {
//        for (i in 0..1) {
//            val array = MutableArray()
//            if (i % 2 == 0) {
//                populateData(array)
//            } else {
//                populateDataByType(array)
//            }
//            assertEquals(12, array.count())
//            val docID: String = String.format(java.util.Locale.ENGLISH, "doc%d", i)
//            val doc = MutableDocument(docID)
//            save(
//                doc,
//                "array",
//                array,
//                com.couchbase.lite.internal.utils.Fn.Consumer<Array> { a: Array ->
//                    org.junit.Assert.assertNull(a.getString(0))
//                    org.junit.Assert.assertNull(a.getString(1))
//                    assertEquals("string", a.getString(2))
//                    org.junit.Assert.assertNull(a.getString(3))
//                    org.junit.Assert.assertNull(a.getString(4))
//                    org.junit.Assert.assertNull(a.getString(5))
//                    org.junit.Assert.assertNull(a.getString(6))
//                    assertEquals(TEST_DATE, a.getString(7))
//                    org.junit.Assert.assertNull(a.getString(8))
//                    org.junit.Assert.assertNull(a.getString(9))
//                    org.junit.Assert.assertNull(a.getString(10))
//                    org.junit.Assert.assertNull(a.getString(11))
//                })
//        }
//    }
//
//    // !!! Fails on Nexus 4
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testGetNumber() {
//        for (i in 0..1) {
//            val array = MutableArray()
//            if (i % 2 == 0) {
//                populateData(array)
//            } else {
//                populateDataByType(array)
//            }
//            assertEquals(12, array.count())
//            val docID: String = String.format(java.util.Locale.ENGLISH, "doc%d", i)
//            val doc = MutableDocument(docID)
//            save(
//                doc,
//                "array",
//                array,
//                com.couchbase.lite.internal.utils.Fn.Consumer<Array> { a: Array ->
//                    assertEquals(1, a.getNumber(0).intValue())
//                    assertEquals(0, a.getNumber(1).intValue())
//                    org.junit.Assert.assertNull(a.getNumber(2))
//                    assertEquals(0, a.getNumber(3).intValue())
//                    assertEquals(1, a.getNumber(4).intValue())
//                    assertEquals(-1, a.getNumber(5).intValue())
//                    assertEquals(1.1, a.getNumber(6))
//                    org.junit.Assert.assertNull(a.getNumber(7))
//                    org.junit.Assert.assertNull(a.getNumber(8))
//                    org.junit.Assert.assertNull(a.getNumber(9))
//                    org.junit.Assert.assertNull(a.getNumber(10))
//                    org.junit.Assert.assertNull(a.getNumber(11))
//                })
//        }
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testGetInteger() {
//        for (i in 0..1) {
//            val array = MutableArray()
//            if (i % 2 == 0) {
//                populateData(array)
//            } else {
//                populateDataByType(array)
//            }
//            assertEquals(12, array.count())
//            val docID: String = String.format(java.util.Locale.ENGLISH, "doc%d", i)
//            val doc = MutableDocument(docID)
//            save(
//                doc,
//                "array",
//                array,
//                com.couchbase.lite.internal.utils.Fn.Consumer<Array> { a: Array ->
//                    org.junit.Assert.assertEquals(1, a.getInt(0).toLong())
//                    org.junit.Assert.assertEquals(0, a.getInt(1).toLong())
//                    org.junit.Assert.assertEquals(0, a.getInt(2).toLong())
//                    org.junit.Assert.assertEquals(0, a.getInt(3).toLong())
//                    org.junit.Assert.assertEquals(1, a.getInt(4).toLong())
//                    org.junit.Assert.assertEquals(-1, a.getInt(5).toLong())
//                    org.junit.Assert.assertEquals(1, a.getInt(6).toLong())
//                    org.junit.Assert.assertEquals(0, a.getInt(7).toLong())
//                    org.junit.Assert.assertEquals(0, a.getInt(8).toLong())
//                    org.junit.Assert.assertEquals(0, a.getInt(9).toLong())
//                    org.junit.Assert.assertEquals(0, a.getInt(10).toLong())
//                    org.junit.Assert.assertEquals(0, a.getInt(11).toLong())
//                })
//        }
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testGetLong() {
//        for (i in 0..1) {
//            val array = MutableArray()
//            if (i % 2 == 0) {
//                populateData(array)
//            } else {
//                populateDataByType(array)
//            }
//            assertEquals(12, array.count())
//            val docID: String = String.format(java.util.Locale.ENGLISH, "doc%d", i)
//            val doc = MutableDocument(docID)
//            save(
//                doc,
//                "array",
//                array,
//                com.couchbase.lite.internal.utils.Fn.Consumer<Array> { a: Array ->
//                    org.junit.Assert.assertEquals(1, a.getLong(0))
//                    org.junit.Assert.assertEquals(0, a.getLong(1))
//                    org.junit.Assert.assertEquals(0, a.getLong(2))
//                    org.junit.Assert.assertEquals(0, a.getLong(3))
//                    org.junit.Assert.assertEquals(1, a.getLong(4))
//                    org.junit.Assert.assertEquals(-1, a.getLong(5))
//                    org.junit.Assert.assertEquals(1, a.getLong(6))
//                    org.junit.Assert.assertEquals(0, a.getLong(7))
//                    org.junit.Assert.assertEquals(0, a.getLong(8))
//                    org.junit.Assert.assertEquals(0, a.getLong(9))
//                    org.junit.Assert.assertEquals(0, a.getLong(10))
//                    org.junit.Assert.assertEquals(0, a.getLong(11))
//                })
//        }
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testGetFloat() {
//        for (i in 0..1) {
//            val array = MutableArray()
//            if (i % 2 == 0) {
//                populateData(array)
//            } else {
//                populateDataByType(array)
//            }
//            assertEquals(12, array.count())
//            val docID: String = String.format(java.util.Locale.ENGLISH, "doc%d", i)
//            val doc = MutableDocument(docID)
//            save(
//                doc,
//                "array",
//                array,
//                com.couchbase.lite.internal.utils.Fn.Consumer<Array> { a: Array ->
//                    org.junit.Assert.assertEquals(1.0f, a.getFloat(0), 0.0f)
//                    org.junit.Assert.assertEquals(0.0f, a.getFloat(1), 0.0f)
//                    org.junit.Assert.assertEquals(0.0f, a.getFloat(2), 0.0f)
//                    org.junit.Assert.assertEquals(0.0f, a.getFloat(3), 0.0f)
//                    org.junit.Assert.assertEquals(1.0f, a.getFloat(4), 0.0f)
//                    org.junit.Assert.assertEquals(-1.0f, a.getFloat(5), 0.0f)
//                    org.junit.Assert.assertEquals(1.1f, a.getFloat(6), 0.0f)
//                    org.junit.Assert.assertEquals(0.0f, a.getFloat(7), 0.0f)
//                    org.junit.Assert.assertEquals(0.0f, a.getFloat(8), 0.0f)
//                    org.junit.Assert.assertEquals(0.0f, a.getFloat(9), 0.0f)
//                    org.junit.Assert.assertEquals(0.0f, a.getFloat(10), 0.0f)
//                    org.junit.Assert.assertEquals(0.0f, a.getFloat(11), 0.0f)
//                })
//        }
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testGetDouble() {
//        for (i in 0..1) {
//            val array = MutableArray()
//            if (i % 2 == 0) {
//                populateData(array)
//            } else {
//                populateDataByType(array)
//            }
//            assertEquals(12, array.count())
//            val docID: String = String.format(java.util.Locale.ENGLISH, "doc%d", i)
//            val doc = MutableDocument(docID)
//            save(
//                doc,
//                "array",
//                array,
//                com.couchbase.lite.internal.utils.Fn.Consumer<Array> { a: Array ->
//                    org.junit.Assert.assertEquals(1.0, a.getDouble(0), 0.0)
//                    org.junit.Assert.assertEquals(0.0, a.getDouble(1), 0.0)
//                    org.junit.Assert.assertEquals(0.0, a.getDouble(2), 0.0)
//                    org.junit.Assert.assertEquals(0.0, a.getDouble(3), 0.0)
//                    org.junit.Assert.assertEquals(1.0, a.getDouble(4), 0.0)
//                    org.junit.Assert.assertEquals(-1.0, a.getDouble(5), 0.0)
//                    org.junit.Assert.assertEquals(1.1, a.getDouble(6), 0.0)
//                    org.junit.Assert.assertEquals(0.0, a.getDouble(7), 0.0)
//                    org.junit.Assert.assertEquals(0.0, a.getDouble(8), 0.0)
//                    org.junit.Assert.assertEquals(0.0, a.getDouble(9), 0.0)
//                    org.junit.Assert.assertEquals(0.0, a.getDouble(10), 0.0)
//                    org.junit.Assert.assertEquals(0.0, a.getDouble(11), 0.0)
//                })
//        }
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testSetGetMinMaxNumbers() {
//        val array = MutableArray()
//        array.addValue(Int.MIN_VALUE)
//        array.addValue(Int.MAX_VALUE)
//        array.addValue(Long.MIN_VALUE)
//        array.addValue(Long.MAX_VALUE)
//        array.addValue(Float.MIN_VALUE)
//        array.addValue(Float.MAX_VALUE)
//        array.addValue(Double.MIN_VALUE)
//        array.addValue(Double.MAX_VALUE)
//        val doc = MutableDocument("doc1")
//        save(doc, "array", array, com.couchbase.lite.internal.utils.Fn.Consumer<Array> { a: Array ->
//            assertEquals(Int.MIN_VALUE, a.getNumber(0).intValue())
//            assertEquals(Int.MAX_VALUE, a.getNumber(1).intValue())
//            org.junit.Assert.assertEquals(
//                Int.MIN_VALUE.toLong(),
//                (a.getValue(0) as Number?)!!.toInt().toLong()
//            )
//            org.junit.Assert.assertEquals(
//                Int.MAX_VALUE.toLong(),
//                (a.getValue(1) as Number?)!!.toInt().toLong()
//            )
//            org.junit.Assert.assertEquals(Int.MIN_VALUE.toLong(), a.getInt(0).toLong())
//            org.junit.Assert.assertEquals(Int.MAX_VALUE.toLong(), a.getInt(1).toLong())
//            assertEquals(Long.MIN_VALUE, a.getNumber(2))
//            assertEquals(Long.MAX_VALUE, a.getNumber(3))
//            assertEquals(Long.MIN_VALUE, a.getValue(2))
//            assertEquals(Long.MAX_VALUE, a.getValue(3))
//            org.junit.Assert.assertEquals(Long.MIN_VALUE, a.getLong(2))
//            org.junit.Assert.assertEquals(Long.MAX_VALUE, a.getLong(3))
//            assertEquals(Float.MIN_VALUE, a.getNumber(4))
//            assertEquals(Float.MAX_VALUE, a.getNumber(5))
//            assertEquals(Float.MIN_VALUE, a.getValue(4))
//            assertEquals(Float.MAX_VALUE, a.getValue(5))
//            org.junit.Assert.assertEquals(Float.MIN_VALUE, a.getFloat(4), 0.0f)
//            org.junit.Assert.assertEquals(Float.MAX_VALUE, a.getFloat(5), 0.0f)
//            assertEquals(Double.MIN_VALUE, a.getNumber(6))
//            assertEquals(Double.MAX_VALUE, a.getNumber(7))
//            assertEquals(Double.MIN_VALUE, a.getValue(6))
//            assertEquals(Double.MAX_VALUE, a.getValue(7))
//            org.junit.Assert.assertEquals(Double.MIN_VALUE, a.getDouble(6), 0.0)
//            org.junit.Assert.assertEquals(Double.MAX_VALUE, a.getDouble(7), 0.0)
//        })
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testSetGetFloatNumbers() {
//        val array = MutableArray()
//        array.addValue(1.00)
//        array.addValue(1.49)
//        array.addValue(1.50)
//        array.addValue(1.51)
//        array.addValue(1.99)
//        val doc = MutableDocument("doc1")
//        save(doc, "array", array, com.couchbase.lite.internal.utils.Fn.Consumer<Array> { a: Array ->
//            // NOTE: Number which has no floating part is stored as Integer.
//            //       This causes type difference between before and after storing data
//            //       into the database.
//            org.junit.Assert.assertEquals(1.00, (a.getValue(0) as Number?)!!.toDouble(), 0.0)
//            assertEquals(1.00, a.getNumber(0).doubleValue(), 0.0)
//            org.junit.Assert.assertEquals(1, a.getInt(0).toLong())
//            org.junit.Assert.assertEquals(1L, a.getLong(0))
//            org.junit.Assert.assertEquals(1.00f, a.getFloat(0), 0.0f)
//            org.junit.Assert.assertEquals(1.00, a.getDouble(0), 0.0)
//            assertEquals(1.49, a.getValue(1))
//            assertEquals(1.49, a.getNumber(1))
//            org.junit.Assert.assertEquals(1, a.getInt(1).toLong())
//            org.junit.Assert.assertEquals(1L, a.getLong(1))
//            org.junit.Assert.assertEquals(1.49f, a.getFloat(1), 0.0f)
//            org.junit.Assert.assertEquals(1.49, a.getDouble(1), 0.0)
//            org.junit.Assert.assertEquals(1.50, (a.getValue(2) as Number?)!!.toDouble(), 0.0)
//            assertEquals(1.50, a.getNumber(2).doubleValue(), 0.0)
//            org.junit.Assert.assertEquals(1, a.getInt(2).toLong())
//            org.junit.Assert.assertEquals(1L, a.getLong(2))
//            org.junit.Assert.assertEquals(1.50f, a.getFloat(2), 0.0f)
//            org.junit.Assert.assertEquals(1.50, a.getDouble(2), 0.0)
//            assertEquals(1.51, a.getValue(3))
//            assertEquals(1.51, a.getNumber(3))
//            org.junit.Assert.assertEquals(1, a.getInt(3).toLong())
//            org.junit.Assert.assertEquals(1L, a.getLong(3))
//            org.junit.Assert.assertEquals(1.51f, a.getFloat(3), 0.0f)
//            org.junit.Assert.assertEquals(1.51, a.getDouble(3), 0.0)
//            assertEquals(1.99, a.getValue(4))
//            assertEquals(1.99, a.getNumber(4))
//            org.junit.Assert.assertEquals(1, a.getInt(4).toLong())
//            org.junit.Assert.assertEquals(1L, a.getLong(4))
//            org.junit.Assert.assertEquals(1.99f, a.getFloat(4), 0.0f)
//            org.junit.Assert.assertEquals(1.99, a.getDouble(4), 0.0)
//        })
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testGetBoolean() {
//        for (i in 0..1) {
//            val array = MutableArray()
//            if (i % 2 == 0) {
//                populateData(array)
//            } else {
//                populateDataByType(array)
//            }
//            assertEquals(12, array.count())
//            val docID: String = String.format(java.util.Locale.ENGLISH, "doc%d", i)
//            val doc = MutableDocument(docID)
//            save(
//                doc,
//                "array",
//                array,
//                com.couchbase.lite.internal.utils.Fn.Consumer<Array> { a: Array ->
//                    org.junit.Assert.assertTrue(a.getBoolean(0))
//                    org.junit.Assert.assertFalse(a.getBoolean(1))
//                    org.junit.Assert.assertTrue(a.getBoolean(2))
//                    org.junit.Assert.assertFalse(a.getBoolean(3))
//                    org.junit.Assert.assertTrue(a.getBoolean(4))
//                    org.junit.Assert.assertTrue(a.getBoolean(5))
//                    org.junit.Assert.assertTrue(a.getBoolean(6))
//                    org.junit.Assert.assertTrue(a.getBoolean(7))
//                    org.junit.Assert.assertFalse(a.getBoolean(8))
//                    org.junit.Assert.assertTrue(a.getBoolean(9))
//                    org.junit.Assert.assertTrue(a.getBoolean(10))
//                    org.junit.Assert.assertTrue(a.getBoolean(11))
//                })
//        }
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testGetDate() {
//        for (i in 0..1) {
//            val array = MutableArray()
//            if (i % 2 == 0) {
//                populateData(array)
//            } else {
//                populateDataByType(array)
//            }
//            assertEquals(12, array.count())
//            val docID: String = String.format(java.util.Locale.ENGLISH, "doc%d", i)
//            val doc = MutableDocument(docID)
//            save(
//                doc,
//                "array",
//                array,
//                com.couchbase.lite.internal.utils.Fn.Consumer<Array> { a: Array ->
//                    org.junit.Assert.assertNull(a.getDate(0))
//                    org.junit.Assert.assertNull(a.getDate(1))
//                    org.junit.Assert.assertNull(a.getDate(2))
//                    org.junit.Assert.assertNull(a.getDate(3))
//                    org.junit.Assert.assertNull(a.getDate(4))
//                    org.junit.Assert.assertNull(a.getDate(5))
//                    org.junit.Assert.assertNull(a.getDate(6))
//                    assertEquals(TEST_DATE, JSONUtils.toJSONString(a.getDate(7)))
//                    org.junit.Assert.assertNull(a.getDate(8))
//                    org.junit.Assert.assertNull(a.getDate(9))
//                    org.junit.Assert.assertNull(a.getDate(10))
//                    org.junit.Assert.assertNull(a.getDate(11))
//                })
//        }
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testGetMap() {
//        for (i in 0..1) {
//            val array = MutableArray()
//            if (i % 2 == 0) {
//                populateData(array)
//            } else {
//                populateDataByType(array)
//            }
//            assertEquals(12, array.count())
//            val docID: String = String.format(java.util.Locale.ENGLISH, "doc%d", i)
//            val doc = MutableDocument(docID)
//            save(
//                doc,
//                "array",
//                array,
//                com.couchbase.lite.internal.utils.Fn.Consumer<Array> { a: Array ->
//                    org.junit.Assert.assertNull(a.getDictionary(0))
//                    org.junit.Assert.assertNull(a.getDictionary(1))
//                    org.junit.Assert.assertNull(a.getDictionary(2))
//                    org.junit.Assert.assertNull(a.getDictionary(3))
//                    org.junit.Assert.assertNull(a.getDictionary(4))
//                    org.junit.Assert.assertNull(a.getDictionary(5))
//                    org.junit.Assert.assertNull(a.getDictionary(6))
//                    org.junit.Assert.assertNull(a.getDictionary(7))
//                    org.junit.Assert.assertNull(a.getDictionary(8))
//                    val map: MutableMap<String, Any> =
//                        java.util.HashMap<String, Any>()
//                    map["name"] = "Scott Tiger"
//                    assertEquals(map, a.getDictionary(9)!!.toMap())
//                    org.junit.Assert.assertNull(a.getDictionary(10))
//                    org.junit.Assert.assertNull(a.getDictionary(11))
//                })
//        }
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testGetArray() {
//        for (i in 0..1) {
//            val array = MutableArray()
//            if (i % 2 == 0) {
//                populateData(array)
//            } else {
//                populateDataByType(array)
//            }
//            assertEquals(12, array.count())
//            val docID: String = String.format(java.util.Locale.ENGLISH, "doc%d", i)
//            val doc = MutableDocument(docID)
//            save(
//                doc,
//                "array",
//                array,
//                com.couchbase.lite.internal.utils.Fn.Consumer<Array> { a: Array ->
//                    org.junit.Assert.assertNull(a.getArray(0))
//                    org.junit.Assert.assertNull(a.getArray(1))
//                    org.junit.Assert.assertNull(a.getArray(2))
//                    org.junit.Assert.assertNull(a.getArray(3))
//                    org.junit.Assert.assertNull(a.getArray(4))
//                    org.junit.Assert.assertNull(a.getArray(5))
//                    org.junit.Assert.assertNull(a.getArray(6))
//                    org.junit.Assert.assertNull(a.getArray(7))
//                    org.junit.Assert.assertNull(a.getArray(9))
//                    assertEquals(java.util.Arrays.asList("a", "b", "c"), a.getArray(10)!!.toList())
//                    org.junit.Assert.assertNull(a.getDictionary(11))
//                })
//        }
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testSetNestedArray() {
//        val array1 = MutableArray()
//        val array2 = MutableArray()
//        val array3 = MutableArray()
//        array1.addValue(array2)
//        array2.addValue(array3)
//        array3.addValue("a")
//        array3.addValue("b")
//        array3.addValue("c")
//        val doc = MutableDocument("doc1")
//        save(
//            doc,
//            "array",
//            array1,
//            com.couchbase.lite.internal.utils.Fn.Consumer<Array> { a1: Array ->
//                assertEquals(1, a1.count())
//                val a2 = a1.getArray(0)
//                assertEquals(1, a2!!.count())
//                val a3 = a2!!.getArray(0)
//                assertEquals(3, a3!!.count())
//                assertEquals("a", a3!!.getValue(0))
//                assertEquals("b", a3!!.getValue(1))
//                assertEquals("c", a3!!.getValue(2))
//            })
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testReplaceArray() {
//        var doc = MutableDocument("doc1")
//        val array1 = MutableArray()
//        array1.addValue("a")
//        array1.addValue("b")
//        array1.addValue("c")
//        assertEquals(3, array1.count())
//        assertEquals(java.util.Arrays.asList("a", "b", "c"), array1.toList())
//        doc.setValue("array", array1)
//        var array2: MutableArray? = MutableArray()
//        array2!!.addValue("x")
//        array2.addValue("y")
//        array2.addValue("z")
//        assertEquals(3, array2.count())
//        assertEquals(java.util.Arrays.asList("x", "y", "z"), array2.toList())
//
//        // Replace:
//        doc.setValue("array", array2)
//
//        // array1 should be now detached:
//        array1.addValue("d")
//        assertEquals(4, array1.count())
//        assertEquals(java.util.Arrays.asList("a", "b", "c", "d"), array1.toList())
//
//        // Check array2:
//        assertEquals(3, array2.count())
//        assertEquals(java.util.Arrays.asList("x", "y", "z"), array2.toList())
//
//        // Save:
//        doc = saveDocInBaseTestDb(doc).toMutable()
//
//        // Check current array:
//        org.junit.Assert.assertNotSame(doc.getArray("array"), array2)
//        array2 = doc.getArray("array")
//        assertEquals(3, array2!!.count())
//        assertEquals(java.util.Arrays.asList("x", "y", "z"), array2!!.toList())
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testReplaceArrayDifferentType() {
//        var doc = MutableDocument("doc1")
//        val array1 = MutableArray()
//        array1.addValue("a")
//        array1.addValue("b")
//        array1.addValue("c")
//        assertEquals(3, array1.count())
//        assertEquals(java.util.Arrays.asList("a", "b", "c"), array1.toList())
//        doc.setValue("array", array1)
//
//        // Replace:
//        doc.setValue("array", "Daniel Tiger")
//
//        // array1 should be now detached:
//        array1.addValue("d")
//        assertEquals(4, array1.count())
//        assertEquals(java.util.Arrays.asList("a", "b", "c", "d"), array1.toList())
//
//        // Save:
//        doc = saveDocInBaseTestDb(doc).toMutable()
//        assertEquals("Daniel Tiger", doc.getString("array"))
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testEnumeratingArray() {
//        val array = MutableArray()
//        for (i in 0..19) {
//            array.addValue(i)
//        }
//        var content = array.toList()
//        var result: MutableList<Any?> = java.util.ArrayList<Any>()
//        var counter = 0
//        for (item in array) {
//            org.junit.Assert.assertNotNull(item)
//            result.add(item)
//            counter++
//        }
//        org.junit.Assert.assertEquals(content, result)
//        assertEquals(array.count(), counter)
//
//        // Update:
//        array.remove(1)
//        array.addValue(20)
//        array.addValue(21)
//        content = array.toList()
//        result = java.util.ArrayList<Any>()
//        for (item in array) {
//            org.junit.Assert.assertNotNull(item)
//            result.add(item)
//        }
//        org.junit.Assert.assertEquals(content, result)
//        val doc = MutableDocument("doc1")
//        doc.setValue("array", array)
//        val c = content
//        save(
//            doc,
//            "array",
//            array,
//            com.couchbase.lite.internal.utils.Fn.Consumer<Array> { array1: Array ->
//                val r: MutableList<Any> = java.util.ArrayList<Any>()
//                for (item in array1) {
//                    org.junit.Assert.assertNotNull(item)
//                    r.add(item)
//                }
//                org.junit.Assert.assertEquals(c.toString(), r.toString())
//            })
//    }
//
//    // ??? Surprisingly, no conncurrent modification exception.
//    @org.junit.Test
//    fun testArrayEnumerationWithDataModification1() {
//        val array = MutableArray()
//        for (i in 0..2) {
//            array.addValue(i)
//        }
//        assertEquals(3, array.count())
//        assertArrayEquals(arrayOf<Any>(0, 1, 2), array.toList().toArray())
//        var n = 0
//        val itr = array.iterator()
//        while (itr.hasNext()) {
//            if (n++ == 1) {
//                array.addValue(3)
//            }
//            itr.next()
//        }
//        assertEquals(4, array.count())
//        assertArrayEquals(arrayOf<Any>(0, 1, 2, 3), array.toList().toArray())
//    }
//
//    // ??? Surprisingly, no conncurrent modification exception.
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testArrayEnumerationWithDataModification2() {
//        var array = MutableArray()
//        for (i in 0..2) {
//            array.addValue(i)
//        }
//        assertEquals(3, array.count())
//        assertArrayEquals(arrayOf<Any>(0, 1, 2), array.toList().toArray())
//        val doc = MutableDocument("doc1").setValue("array", array)
//        array = saveDocInBaseTestDb(doc).toMutable().getArray("array")
//        org.junit.Assert.assertNotNull(array)
//        var n = 0
//        val itr = array.iterator()
//        while (itr.hasNext()) {
//            if (n++ == 1) {
//                array.addValue(3)
//            }
//            itr.next()
//        }
//        assertEquals(4, array.count())
//        // this is friggin' bizarre:
//        // after a roundtrip through the db those integers turn into longs
//        assertArrayEquals(arrayOf<Any>(0L, 1L, 2L, 3), array.toList().toArray())
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testSetNull() {
//        val mDoc = MutableDocument("test")
//        val mArray = MutableArray()
//        mArray.addValue(null)
//        mArray.addString(null)
//        mArray.addNumber(null)
//        mArray.addDate(null)
//        mArray.addArray(null)
//        mArray.addDictionary(null)
//        mDoc.setArray("array", mArray)
//        saveDocInBaseTestDb(mDoc) { doc ->
//            assertEquals(1, doc.count())
//            org.junit.Assert.assertTrue(doc.contains("array"))
//            val array: Array = doc.getArray("array")
//            org.junit.Assert.assertNotNull(array)
//            assertEquals(6, array.count())
//            org.junit.Assert.assertNull(array.getValue(0))
//            org.junit.Assert.assertNull(array.getValue(1))
//            org.junit.Assert.assertNull(array.getValue(2))
//            org.junit.Assert.assertNull(array.getValue(3))
//            org.junit.Assert.assertNull(array.getValue(4))
//            org.junit.Assert.assertNull(array.getValue(5))
//        }
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testEquals() {
//
//        // mArray1 and mArray2 have exactly same data
//        // mArray3 is different
//        // mArray4 is different
//        // mArray5 is different
//        val mArray1 = MutableArray()
//        mArray1.addValue(1L)
//        mArray1.addValue("Hello")
//        mArray1.addValue(null)
//        val mArray2 = MutableArray()
//        mArray2.addValue(1L)
//        mArray2.addValue("Hello")
//        mArray2.addValue(null)
//        val mArray3 = MutableArray()
//        mArray3.addValue(100L)
//        mArray3.addValue(true)
//        val mArray4 = MutableArray()
//        mArray4.addValue(100L)
//        val mArray5 = MutableArray()
//        mArray4.addValue(100L)
//        mArray3.addValue(false)
//        val mDoc = MutableDocument("test")
//        mDoc.setArray("array1", mArray1)
//        mDoc.setArray("array2", mArray2)
//        mDoc.setArray("array3", mArray3)
//        mDoc.setArray("array4", mArray4)
//        mDoc.setArray("array5", mArray5)
//        val doc: Document = saveDocInBaseTestDb(mDoc)
//        val array1 = doc.getArray("array1")
//        val array2 = doc.getArray("array2")
//        val array3 = doc.getArray("array3")
//        val array4 = doc.getArray("array4")
//        val array5 = doc.getArray("array5")
//
//        // compare array1, array2, marray1, and marray2
//        org.junit.Assert.assertEquals(array1, array1)
//        org.junit.Assert.assertEquals(array2, array2)
//        org.junit.Assert.assertEquals(array1, array2)
//        org.junit.Assert.assertEquals(array2, array1)
//        org.junit.Assert.assertEquals(array1, array1!!.toMutable())
//        org.junit.Assert.assertEquals(array1, array2!!.toMutable())
//        org.junit.Assert.assertEquals(array1.toMutable(), array1)
//        org.junit.Assert.assertEquals(array2.toMutable(), array1)
//        org.junit.Assert.assertEquals(array1, mArray1)
//        org.junit.Assert.assertEquals(array1, mArray2)
//        org.junit.Assert.assertEquals(array2, mArray1)
//        org.junit.Assert.assertEquals(array2, mArray2)
//        org.junit.Assert.assertEquals(mArray1, array1)
//        org.junit.Assert.assertEquals(mArray2, array1)
//        org.junit.Assert.assertEquals(mArray1, array2)
//        org.junit.Assert.assertEquals(mArray2, array2)
//        org.junit.Assert.assertEquals(mArray1, mArray1)
//        org.junit.Assert.assertEquals(mArray2, mArray2)
//        org.junit.Assert.assertEquals(mArray1, mArray1)
//        org.junit.Assert.assertEquals(mArray2, mArray2)
//
//        // compare array1, array3, marray1, and marray3
//        org.junit.Assert.assertEquals(array3, array3)
//        org.junit.Assert.assertNotEquals(array1, array3)
//        org.junit.Assert.assertNotEquals(array3, array1)
//        org.junit.Assert.assertNotEquals(array1, array3!!.toMutable())
//        org.junit.Assert.assertNotEquals(array3.toMutable(), array1)
//        org.junit.Assert.assertNotEquals(array1, mArray3)
//        org.junit.Assert.assertNotEquals(array3, mArray1)
//        org.junit.Assert.assertEquals(array3, mArray3)
//        org.junit.Assert.assertNotEquals(mArray3, array1)
//        org.junit.Assert.assertNotEquals(mArray1, array3)
//        org.junit.Assert.assertEquals(mArray3, array3)
//        org.junit.Assert.assertEquals(mArray3, mArray3)
//        org.junit.Assert.assertEquals(mArray3, mArray3)
//
//        // compare array1, array4, marray1, and marray4
//        org.junit.Assert.assertEquals(array4, array4)
//        org.junit.Assert.assertNotEquals(array1, array4)
//        org.junit.Assert.assertNotEquals(array4, array1)
//        org.junit.Assert.assertNotEquals(array1, array4!!.toMutable())
//        org.junit.Assert.assertNotEquals(array4.toMutable(), array1)
//        org.junit.Assert.assertNotEquals(array1, mArray4)
//        org.junit.Assert.assertNotEquals(array4, mArray1)
//        org.junit.Assert.assertEquals(array4, mArray4)
//        org.junit.Assert.assertNotEquals(mArray4, array1)
//        org.junit.Assert.assertNotEquals(mArray1, array4)
//        org.junit.Assert.assertEquals(mArray4, array4)
//        org.junit.Assert.assertEquals(mArray4, mArray4)
//        org.junit.Assert.assertEquals(mArray4, mArray4)
//
//        // compare array3, array4, marray3, and marray4
//        org.junit.Assert.assertNotEquals(array3, array4)
//        org.junit.Assert.assertNotEquals(array4, array3)
//        org.junit.Assert.assertNotEquals(array3, array4.toMutable())
//        org.junit.Assert.assertNotEquals(array4.toMutable(), array3)
//        org.junit.Assert.assertNotEquals(array3, mArray4)
//        org.junit.Assert.assertNotEquals(array4, mArray3)
//        org.junit.Assert.assertNotEquals(mArray4, array3)
//        org.junit.Assert.assertNotEquals(mArray3, array4)
//
//        // compare array3, array5, marray3, and marray5
//        org.junit.Assert.assertNotEquals(array3, array5)
//        org.junit.Assert.assertNotEquals(array5, array3)
//        org.junit.Assert.assertNotEquals(array3, array5!!.toMutable())
//        org.junit.Assert.assertNotEquals(array5.toMutable(), array3)
//        org.junit.Assert.assertNotEquals(array3, mArray5)
//        org.junit.Assert.assertNotEquals(array5, mArray3)
//        org.junit.Assert.assertNotEquals(mArray5, array3)
//        org.junit.Assert.assertNotEquals(mArray3, array5)
//
//        // compare array5, array4, mArray5, and marray4
//        org.junit.Assert.assertNotEquals(array5, array4)
//        org.junit.Assert.assertNotEquals(array4, array5)
//        org.junit.Assert.assertNotEquals(array5, array4.toMutable())
//        org.junit.Assert.assertNotEquals(array4.toMutable(), array5)
//        org.junit.Assert.assertNotEquals(array5, mArray4)
//        org.junit.Assert.assertNotEquals(array4, mArray5)
//        org.junit.Assert.assertNotEquals(mArray4, array5)
//        org.junit.Assert.assertNotEquals(mArray5, array4)
//
//        // against other type
//        org.junit.Assert.assertNotEquals(null, array3)
//        org.junit.Assert.assertNotEquals(array3, Any())
//        org.junit.Assert.assertNotEquals(1, array3)
//        org.junit.Assert.assertNotEquals(array3, java.util.HashMap<Any, Any>())
//        org.junit.Assert.assertNotEquals(array3, MutableDictionary())
//        org.junit.Assert.assertNotEquals(array3, MutableArray())
//        org.junit.Assert.assertNotEquals(array3, doc)
//        org.junit.Assert.assertNotEquals(array3, mDoc)
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testHashCode() {
//        // mArray1 and mArray2 have exactly same data
//        // mArray3 is different
//        // mArray4 is different
//        // mArray5 is different
//        val mArray1 = MutableArray()
//        mArray1.addValue(1L)
//        mArray1.addValue("Hello")
//        mArray1.addValue(null)
//        val mArray2 = MutableArray()
//        mArray2.addValue(1L)
//        mArray2.addValue("Hello")
//        mArray2.addValue(null)
//        val mArray3 = MutableArray()
//        mArray3.addValue(100L)
//        mArray3.addValue(true)
//        val mArray4 = MutableArray()
//        mArray4.addValue(100L)
//        val mArray5 = MutableArray()
//        mArray4.addValue(100L)
//        mArray3.addValue(false)
//        val mDoc = MutableDocument("test")
//        mDoc.setArray("array1", mArray1)
//        mDoc.setArray("array2", mArray2)
//        mDoc.setArray("array3", mArray3)
//        mDoc.setArray("array4", mArray4)
//        mDoc.setArray("array5", mArray5)
//        val doc: Document = saveDocInBaseTestDb(mDoc)
//        val array1 = doc.getArray("array1")
//        val array2 = doc.getArray("array2")
//        val array3 = doc.getArray("array3")
//        val array4 = doc.getArray("array4")
//        val array5 = doc.getArray("array5")
//        org.junit.Assert.assertEquals(array1.hashCode().toLong(), array1.hashCode().toLong())
//        org.junit.Assert.assertEquals(array1.hashCode().toLong(), array2.hashCode().toLong())
//        org.junit.Assert.assertEquals(array2.hashCode().toLong(), array1.hashCode().toLong())
//        org.junit.Assert.assertEquals(
//            array1.hashCode().toLong(),
//            array1!!.toMutable().hashCode().toLong()
//        )
//        org.junit.Assert.assertEquals(
//            array1.hashCode().toLong(),
//            array2!!.toMutable().hashCode().toLong()
//        )
//        org.junit.Assert.assertEquals(array1.hashCode().toLong(), mArray1.hashCode().toLong())
//        org.junit.Assert.assertEquals(array1.hashCode().toLong(), mArray2.hashCode().toLong())
//        org.junit.Assert.assertEquals(array2.hashCode().toLong(), mArray1.hashCode().toLong())
//        org.junit.Assert.assertEquals(array2.hashCode().toLong(), mArray2.hashCode().toLong())
//        org.junit.Assert.assertNotEquals(array3.hashCode().toLong(), array1.hashCode().toLong())
//        org.junit.Assert.assertNotEquals(array3.hashCode().toLong(), array2.hashCode().toLong())
//        org.junit.Assert.assertNotEquals(
//            array3.hashCode().toLong(),
//            array1.toMutable().hashCode().toLong()
//        )
//        org.junit.Assert.assertNotEquals(
//            array3.hashCode().toLong(),
//            array2.toMutable().hashCode().toLong()
//        )
//        org.junit.Assert.assertNotEquals(array3.hashCode().toLong(), mArray1.hashCode().toLong())
//        org.junit.Assert.assertNotEquals(array3.hashCode().toLong(), mArray2.hashCode().toLong())
//        org.junit.Assert.assertNotEquals(mArray3.hashCode().toLong(), array1.hashCode().toLong())
//        org.junit.Assert.assertNotEquals(mArray3.hashCode().toLong(), array2.hashCode().toLong())
//        org.junit.Assert.assertNotEquals(
//            mArray3.hashCode().toLong(),
//            array1.toMutable().hashCode().toLong()
//        )
//        org.junit.Assert.assertNotEquals(
//            mArray3.hashCode().toLong(),
//            array2.toMutable().hashCode().toLong()
//        )
//        org.junit.Assert.assertNotEquals(mArray3.hashCode().toLong(), mArray1.hashCode().toLong())
//        org.junit.Assert.assertNotEquals(mArray3.hashCode().toLong(), mArray2.hashCode().toLong())
//        org.junit.Assert.assertNotEquals(array1.hashCode().toLong(), array4.hashCode().toLong())
//        org.junit.Assert.assertNotEquals(array1.hashCode().toLong(), array5.hashCode().toLong())
//        org.junit.Assert.assertNotEquals(array2.hashCode().toLong(), array4.hashCode().toLong())
//        org.junit.Assert.assertNotEquals(array2.hashCode().toLong(), array5.hashCode().toLong())
//        org.junit.Assert.assertNotEquals(array3.hashCode().toLong(), array4.hashCode().toLong())
//        org.junit.Assert.assertNotEquals(array3.hashCode().toLong(), array5.hashCode().toLong())
//        org.junit.Assert.assertNotEquals(0, array3.hashCode().toLong())
//        org.junit.Assert.assertNotEquals(array3.hashCode().toLong(), Any().hashCode().toLong())
//        org.junit.Assert.assertNotEquals(
//            array3.hashCode().toLong(),
//            java.lang.Integer.valueOf(1).hashCode().toLong()
//        )
//        org.junit.Assert.assertNotEquals(
//            array3.hashCode().toLong(),
//            java.util.HashMap<Any, Any>().hashCode().toLong()
//        )
//        org.junit.Assert.assertNotEquals(
//            array3.hashCode().toLong(),
//            MutableDictionary().hashCode().toLong()
//        )
//        org.junit.Assert.assertNotEquals(
//            array3.hashCode().toLong(),
//            MutableArray().hashCode().toLong()
//        )
//        org.junit.Assert.assertNotEquals(mArray3.hashCode().toLong(), doc.hashCode().toLong())
//        org.junit.Assert.assertNotEquals(mArray3.hashCode().toLong(), mDoc.hashCode().toLong())
//        org.junit.Assert.assertNotEquals(
//            mArray3.hashCode().toLong(),
//            array1.toMutable().hashCode().toLong()
//        )
//        org.junit.Assert.assertNotEquals(
//            mArray3.hashCode().toLong(),
//            array2.toMutable().hashCode().toLong()
//        )
//        org.junit.Assert.assertNotEquals(mArray3.hashCode().toLong(), mArray1.hashCode().toLong())
//        org.junit.Assert.assertNotEquals(mArray3.hashCode().toLong(), mArray2.hashCode().toLong())
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testGetDictionary() {
//        val mNestedDict = MutableDictionary()
//        mNestedDict.setValue("key1", 1L)
//        mNestedDict.setValue("key2", "Hello")
//        mNestedDict.setValue("key3", null)
//        val mArray = MutableArray()
//        mArray.addValue(1L)
//        mArray.addValue("Hello")
//        mArray.addValue(null)
//        mArray.addValue(mNestedDict)
//        val mDoc = MutableDocument("test")
//        mDoc.setArray("array", mArray)
//        val doc: Document = saveDocInBaseTestDb(mDoc)
//        val array = doc.getArray("array")
//        org.junit.Assert.assertNotNull(array)
//        org.junit.Assert.assertNull(array!!.getDictionary(0))
//        org.junit.Assert.assertNull(array.getDictionary(1))
//        org.junit.Assert.assertNull(array.getDictionary(2))
//        org.junit.Assert.assertNotNull(array.getDictionary(3))
//        assertThrows(java.lang.IndexOutOfBoundsException::class.java) {
//            org.junit.Assert.assertNull(
//                array.getDictionary(4)
//            )
//        }
//        val nestedDict = array.getDictionary(3)
//        org.junit.Assert.assertEquals(nestedDict, mNestedDict)
//        org.junit.Assert.assertEquals(array, mArray)
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testGetArray2() {
//        val mNestedArray = MutableArray()
//        mNestedArray.addValue(1L)
//        mNestedArray.addValue("Hello")
//        mNestedArray.addValue(null)
//        val mArray = MutableArray()
//        mArray.addValue(1L)
//        mArray.addValue("Hello")
//        mArray.addValue(null)
//        mArray.addValue(mNestedArray)
//        val mDoc = MutableDocument("test")
//        mDoc.setValue("array", mArray)
//        val doc: Document = saveDocInBaseTestDb(mDoc)
//        val array = doc.getArray("array")
//        org.junit.Assert.assertNotNull(array)
//        org.junit.Assert.assertNull(array!!.getArray(0))
//        org.junit.Assert.assertNull(array.getArray(1))
//        org.junit.Assert.assertNull(array.getArray(2))
//        org.junit.Assert.assertNotNull(array.getArray(3))
//        assertThrows(java.lang.IndexOutOfBoundsException::class.java) {
//            org.junit.Assert.assertNull(
//                array.getArray(4)
//            )
//        }
//        val nestedArray = array.getArray(3)
//        org.junit.Assert.assertEquals(nestedArray, mNestedArray)
//        org.junit.Assert.assertEquals(array, mArray)
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testAddInt() {
//        val mDoc = MutableDocument("test")
//        val mArray = MutableArray()
//        mArray.addInt(0)
//        mArray.addInt(Int.MAX_VALUE)
//        mArray.addInt(Int.MIN_VALUE)
//        mDoc.setArray("array", mArray)
//        saveDocInBaseTestDb(mDoc) { doc ->
//            assertEquals(1, doc.count())
//            org.junit.Assert.assertTrue(doc.contains("array"))
//            val array: Array = doc.getArray("array")
//            org.junit.Assert.assertNotNull(array)
//            assertEquals(3, array.count())
//            org.junit.Assert.assertEquals(0, array.getInt(0).toLong())
//            org.junit.Assert.assertEquals(
//                Int.MAX_VALUE.toLong(),
//                array.getInt(1).toLong()
//            )
//            org.junit.Assert.assertEquals(
//                Int.MIN_VALUE.toLong(),
//                array.getInt(2).toLong()
//            )
//        }
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testSetInt() {
//        val mDoc = MutableDocument("test")
//        val mArray = MutableArray()
//        mArray.addInt(0)
//        mArray.addInt(Int.MAX_VALUE)
//        mArray.addInt(Int.MIN_VALUE)
//        mArray.setInt(0, Int.MAX_VALUE)
//        mArray.setInt(1, Int.MIN_VALUE)
//        mArray.setInt(2, 0)
//        mDoc.setArray("array", mArray)
//        saveDocInBaseTestDb(mDoc) { doc ->
//            assertEquals(1, doc.count())
//            org.junit.Assert.assertTrue(doc.contains("array"))
//            val array: Array = doc.getArray("array")
//            org.junit.Assert.assertNotNull(array)
//            assertEquals(3, array.count())
//            org.junit.Assert.assertEquals(0, array.getInt(2).toLong())
//            org.junit.Assert.assertEquals(
//                Int.MAX_VALUE.toLong(),
//                array.getInt(0).toLong()
//            )
//            org.junit.Assert.assertEquals(
//                Int.MIN_VALUE.toLong(),
//                array.getInt(1).toLong()
//            )
//        }
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testInsertInt() {
//        val mDoc = MutableDocument("test")
//        val mArray = MutableArray()
//        mArray.addInt(10) // will be pushed 3 times.
//        mArray.insertInt(0, 0)
//        mArray.insertInt(1, Int.MAX_VALUE)
//        mArray.insertInt(2, Int.MIN_VALUE)
//        mDoc.setArray("array", mArray)
//        saveDocInBaseTestDb(mDoc) { doc ->
//            assertEquals(1, doc.count())
//            org.junit.Assert.assertTrue(doc.contains("array"))
//            val array: Array = doc.getArray("array")
//            org.junit.Assert.assertNotNull(array)
//            assertEquals(4, array.count())
//            org.junit.Assert.assertEquals(0, array.getInt(0).toLong())
//            org.junit.Assert.assertEquals(
//                Int.MAX_VALUE.toLong(),
//                array.getInt(1).toLong()
//            )
//            org.junit.Assert.assertEquals(
//                Int.MIN_VALUE.toLong(),
//                array.getInt(2).toLong()
//            )
//            org.junit.Assert.assertEquals(10, array.getInt(3).toLong())
//        }
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testAddLong() {
//        val mDoc = MutableDocument("test")
//        val mArray = MutableArray()
//        mArray.addLong(0)
//        mArray.addLong(Long.MAX_VALUE)
//        mArray.addLong(Long.MIN_VALUE)
//        mDoc.setArray("array", mArray)
//        saveDocInBaseTestDb(mDoc) { doc ->
//            assertEquals(1, doc.count())
//            org.junit.Assert.assertTrue(doc.contains("array"))
//            val array: Array = doc.getArray("array")
//            org.junit.Assert.assertNotNull(array)
//            assertEquals(3, array.count())
//            org.junit.Assert.assertEquals(0, array.getLong(0))
//            org.junit.Assert.assertEquals(Long.MAX_VALUE, array.getLong(1))
//            org.junit.Assert.assertEquals(Long.MIN_VALUE, array.getLong(2))
//        }
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testSetLong() {
//        val mDoc = MutableDocument("test")
//        val mArray = MutableArray()
//        mArray.addLong(0)
//        mArray.addLong(Long.MAX_VALUE)
//        mArray.addLong(Long.MIN_VALUE)
//        mArray.setLong(0, Long.MAX_VALUE)
//        mArray.setLong(1, Long.MIN_VALUE)
//        mArray.setLong(2, 0)
//        mDoc.setArray("array", mArray)
//        saveDocInBaseTestDb(mDoc) { doc ->
//            assertEquals(1, doc.count())
//            org.junit.Assert.assertTrue(doc.contains("array"))
//            val array: Array = doc.getArray("array")
//            org.junit.Assert.assertNotNull(array)
//            assertEquals(3, array.count())
//            org.junit.Assert.assertEquals(0, array.getLong(2))
//            org.junit.Assert.assertEquals(Long.MAX_VALUE, array.getLong(0))
//            org.junit.Assert.assertEquals(Long.MIN_VALUE, array.getLong(1))
//        }
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testInsertLong() {
//        val mDoc = MutableDocument("test")
//        val mArray = MutableArray()
//        mArray.addLong(10) // will be pushed 3 times.
//        mArray.insertLong(0, 0)
//        mArray.insertLong(1, Long.MAX_VALUE)
//        mArray.insertLong(2, Long.MIN_VALUE)
//        mDoc.setArray("array", mArray)
//        saveDocInBaseTestDb(mDoc) { doc ->
//            assertEquals(1, doc.count())
//            org.junit.Assert.assertTrue(doc.contains("array"))
//            val array: Array = doc.getArray("array")
//            org.junit.Assert.assertNotNull(array)
//            assertEquals(4, array.count())
//            org.junit.Assert.assertEquals(0, array.getLong(0))
//            org.junit.Assert.assertEquals(Long.MAX_VALUE, array.getLong(1))
//            org.junit.Assert.assertEquals(Long.MIN_VALUE, array.getLong(2))
//            org.junit.Assert.assertEquals(10, array.getLong(3))
//        }
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testAddFloat() {
//        val mDoc = MutableDocument("test")
//        val mArray = MutableArray()
//        mArray.addFloat(0.0f)
//        mArray.addFloat(Float.MAX_VALUE)
//        mArray.addFloat(Float.MIN_VALUE)
//        mDoc.setArray("array", mArray)
//        saveDocInBaseTestDb(mDoc) { doc ->
//            assertEquals(1, doc.count())
//            org.junit.Assert.assertTrue(doc.contains("array"))
//            val array: Array = doc.getArray("array")
//            org.junit.Assert.assertNotNull(array)
//            assertEquals(3, array.count())
//            org.junit.Assert.assertEquals(0.0f, array.getFloat(0), 0.0f)
//            org.junit.Assert.assertEquals(Float.MAX_VALUE, array.getFloat(1), 0.0f)
//            org.junit.Assert.assertEquals(Float.MIN_VALUE, array.getFloat(2), 0.0f)
//        }
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testSetFloat() {
//        val mDoc = MutableDocument("test")
//        val mArray = MutableArray()
//        mArray.addFloat(0f)
//        mArray.addFloat(Float.MAX_VALUE)
//        mArray.addFloat(Float.MIN_VALUE)
//        mArray.setFloat(0, Float.MAX_VALUE)
//        mArray.setFloat(1, Float.MIN_VALUE)
//        mArray.setFloat(2, 0f)
//        mDoc.setArray("array", mArray)
//        saveDocInBaseTestDb(mDoc) { doc ->
//            assertEquals(1, doc.count())
//            org.junit.Assert.assertTrue(doc.contains("array"))
//            val array: Array = doc.getArray("array")
//            org.junit.Assert.assertNotNull(array)
//            assertEquals(3, array.count())
//            org.junit.Assert.assertEquals(0.0f, array.getLong(2).toFloat(), 0.0f)
//            org.junit.Assert.assertEquals(Float.MAX_VALUE, array.getFloat(0), 0.0f)
//            org.junit.Assert.assertEquals(Float.MIN_VALUE, array.getFloat(1), 0.0f)
//        }
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testInsertFloat() {
//        val mDoc = MutableDocument("test")
//        val mArray = MutableArray()
//        mArray.addFloat(10f) // will be pushed 3 times.
//        mArray.insertFloat(0, 0f)
//        mArray.insertFloat(1, Float.MAX_VALUE)
//        mArray.insertFloat(2, Float.MIN_VALUE)
//        mDoc.setArray("array", mArray)
//        saveDocInBaseTestDb(mDoc) { doc ->
//            assertEquals(1, doc.count())
//            org.junit.Assert.assertTrue(doc.contains("array"))
//            val array: Array = doc.getArray("array")
//            org.junit.Assert.assertNotNull(array)
//            assertEquals(4, array.count())
//            org.junit.Assert.assertEquals(0f, array.getFloat(0), 0f)
//            org.junit.Assert.assertEquals(Float.MAX_VALUE, array.getFloat(1), 0f)
//            org.junit.Assert.assertEquals(Float.MIN_VALUE, array.getFloat(2), 0f)
//            org.junit.Assert.assertEquals(10f, array.getFloat(3), 0f)
//        }
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testAddDouble() {
//        val mDoc = MutableDocument("test")
//        val mArray = MutableArray()
//        mArray.addDouble(0.0)
//        mArray.addDouble(Double.MAX_VALUE)
//        mArray.addDouble(Double.MIN_VALUE)
//        mDoc.setArray("array", mArray)
//        saveDocInBaseTestDb(mDoc) { doc ->
//            assertEquals(1, doc.count())
//            org.junit.Assert.assertTrue(doc.contains("array"))
//            val array: Array = doc.getArray("array")
//            org.junit.Assert.assertNotNull(array)
//            assertEquals(3, array.count())
//            org.junit.Assert.assertEquals(0.0, array.getDouble(0), 0.0)
//            org.junit.Assert.assertEquals(
//                Double.MAX_VALUE,
//                array.getDouble(1),
//                0.0
//            )
//            org.junit.Assert.assertEquals(
//                Double.MIN_VALUE,
//                array.getDouble(2),
//                0.0
//            )
//        }
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testSetDouble() {
//        val mDoc = MutableDocument("test")
//        val mArray = MutableArray()
//        mArray.addDouble(0.0)
//        mArray.addDouble(Double.MAX_VALUE)
//        mArray.addDouble(Double.MIN_VALUE)
//        mArray.setDouble(0, Double.MAX_VALUE)
//        mArray.setDouble(1, Double.MIN_VALUE)
//        mArray.setDouble(2, 0.0)
//        mDoc.setArray("array", mArray)
//        saveDocInBaseTestDb(mDoc) { doc ->
//            assertEquals(1, doc.count())
//            org.junit.Assert.assertTrue(doc.contains("array"))
//            val array: Array = doc.getArray("array")
//            org.junit.Assert.assertNotNull(array)
//            assertEquals(3, array.count())
//            org.junit.Assert.assertEquals(0.0, array.getDouble(2), 0.0)
//            org.junit.Assert.assertEquals(
//                Double.MAX_VALUE,
//                array.getDouble(0),
//                0.0
//            )
//            org.junit.Assert.assertEquals(
//                Double.MIN_VALUE,
//                array.getDouble(1),
//                0.0
//            )
//        }
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testInsertDouble() {
//        val mDoc = MutableDocument("test")
//        val mArray = MutableArray()
//        mArray.addDouble(10.0) // will be pushed 3 times.
//        mArray.insertDouble(0, 0.0)
//        mArray.insertDouble(1, Double.MAX_VALUE)
//        mArray.insertDouble(2, Double.MIN_VALUE)
//        mDoc.setArray("array", mArray)
//        saveDocInBaseTestDb(mDoc) { doc ->
//            assertEquals(1, doc.count())
//            org.junit.Assert.assertTrue(doc.contains("array"))
//            val array: Array = doc.getArray("array")
//            org.junit.Assert.assertNotNull(array)
//            assertEquals(4, array.count())
//            org.junit.Assert.assertEquals(0.0, array.getDouble(0), 0.0)
//            org.junit.Assert.assertEquals(
//                Double.MAX_VALUE,
//                array.getDouble(1),
//                0.0
//            )
//            org.junit.Assert.assertEquals(
//                Double.MIN_VALUE,
//                array.getDouble(2),
//                0.0
//            )
//            org.junit.Assert.assertEquals(10.0, array.getDouble(3), 0.0)
//        }
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testAddNumber() {
//        val mDoc = MutableDocument("test")
//        val mArray = MutableArray()
//        mArray.addNumber(Int.MAX_VALUE)
//        mArray.addNumber(Long.MAX_VALUE)
//        mArray.addNumber(Double.MAX_VALUE)
//        mDoc.setArray("array", mArray)
//        saveDocInBaseTestDb(mDoc) { doc ->
//            assertEquals(1, doc.count())
//            org.junit.Assert.assertTrue(doc.contains("array"))
//            val array: Array = doc.getArray("array")
//            org.junit.Assert.assertNotNull(array)
//            assertEquals(3, array.count())
//            assertEquals(Int.MAX_VALUE, array.getNumber(0).intValue())
//            assertEquals(Long.MAX_VALUE, array.getNumber(1).longValue())
//            assertEquals(Double.MAX_VALUE, array.getNumber(2).doubleValue(), 0.0)
//        }
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testSetNumber() {
//        val mDoc = MutableDocument("test")
//        val mArray = MutableArray()
//        mArray.addNumber(Int.MAX_VALUE)
//        mArray.addNumber(Long.MAX_VALUE)
//        mArray.addNumber(Double.MAX_VALUE)
//        mArray.setNumber(0, Long.MAX_VALUE)
//        mArray.setNumber(1, Double.MAX_VALUE)
//        mArray.setNumber(2, Int.MAX_VALUE)
//        mDoc.setArray("array", mArray)
//        saveDocInBaseTestDb(mDoc) { doc ->
//            assertEquals(1, doc.count())
//            org.junit.Assert.assertTrue(doc.contains("array"))
//            val array: Array = doc.getArray("array")
//            org.junit.Assert.assertNotNull(array)
//            assertEquals(3, array.count())
//            assertEquals(Int.MAX_VALUE, array.getNumber(2).intValue())
//            assertEquals(Long.MAX_VALUE, array.getNumber(0).longValue())
//            assertEquals(Double.MAX_VALUE, array.getNumber(1).doubleValue(), 0.0)
//        }
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testInsertNumber() {
//        val mDoc = MutableDocument("test")
//        val mArray = MutableArray()
//        mArray.addNumber(10L) // will be pushed 3 times.
//        mArray.insertNumber(0, Int.MAX_VALUE)
//        mArray.insertNumber(1, Long.MAX_VALUE)
//        mArray.insertNumber(2, Double.MAX_VALUE)
//        mDoc.setArray("array", mArray)
//        saveDocInBaseTestDb(mDoc) { doc ->
//            assertEquals(1, doc.count())
//            org.junit.Assert.assertTrue(doc.contains("array"))
//            val array: Array = doc.getArray("array")
//            org.junit.Assert.assertNotNull(array)
//            assertEquals(4, array.count())
//            org.junit.Assert.assertEquals(
//                Int.MAX_VALUE.toLong(),
//                array.getInt(0).toLong()
//            )
//            org.junit.Assert.assertEquals(Long.MAX_VALUE, array.getLong(1))
//            org.junit.Assert.assertEquals(
//                Double.MAX_VALUE,
//                array.getDouble(2),
//                0.0
//            )
//            assertEquals(10L, array.getNumber(3).longValue())
//        }
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testAddString() {
//        val mDoc = MutableDocument("test")
//        val mArray = MutableArray()
//        mArray.addString("")
//        mArray.addString("Hello")
//        mArray.addString("World")
//        mDoc.setArray("array", mArray)
//        saveDocInBaseTestDb(mDoc) { doc ->
//            assertEquals(1, doc.count())
//            org.junit.Assert.assertTrue(doc.contains("array"))
//            val array: Array = doc.getArray("array")
//            org.junit.Assert.assertNotNull(array)
//            assertEquals(3, array.count())
//            assertEquals("", array.getString(0))
//            assertEquals("Hello", array.getString(1))
//            assertEquals("World", array.getString(2))
//        }
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testSetString() {
//        val mDoc = MutableDocument("test")
//        val mArray = MutableArray()
//        mArray.addString("")
//        mArray.addString("Hello")
//        mArray.addString("World")
//        mArray.setString(0, "Hello")
//        mArray.setString(1, "World")
//        mArray.setString(2, "")
//        mDoc.setArray("array", mArray)
//        saveDocInBaseTestDb(mDoc) { doc ->
//            assertEquals(1, doc.count())
//            org.junit.Assert.assertTrue(doc.contains("array"))
//            val array: Array = doc.getArray("array")
//            org.junit.Assert.assertNotNull(array)
//            assertEquals(3, array.count())
//            assertEquals("", array.getString(2))
//            assertEquals("Hello", array.getString(0))
//            assertEquals("World", array.getString(1))
//        }
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testInsertString() {
//        val mDoc = MutableDocument("test")
//        val mArray = MutableArray()
//        mArray.addString("") // will be pushed 3 times.
//        mArray.insertString(0, "Hello")
//        mArray.insertString(1, "World")
//        mArray.insertString(2, "!")
//        mDoc.setArray("array", mArray)
//        saveDocInBaseTestDb(mDoc) { doc ->
//            assertEquals(1, doc.count())
//            org.junit.Assert.assertTrue(doc.contains("array"))
//            val array: Array = doc.getArray("array")
//            org.junit.Assert.assertNotNull(array)
//            assertEquals(4, array.count())
//            assertEquals("Hello", array.getString(0))
//            assertEquals("World", array.getString(1))
//            assertEquals("!", array.getString(2))
//            assertEquals("", array.getString(3))
//        }
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testAddBoolean() {
//        val mDoc = MutableDocument("test")
//        val mArray = MutableArray()
//        mArray.addBoolean(true)
//        mArray.addBoolean(false)
//        mDoc.setArray("array", mArray)
//        saveDocInBaseTestDb(mDoc) { doc ->
//            assertEquals(1, doc.count())
//            org.junit.Assert.assertTrue(doc.contains("array"))
//            val array: Array = doc.getArray("array")
//            org.junit.Assert.assertNotNull(array)
//            assertEquals(2, array.count())
//            org.junit.Assert.assertTrue(array.getBoolean(0))
//            org.junit.Assert.assertFalse(array.getBoolean(1))
//        }
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testSetBoolean() {
//        val mDoc = MutableDocument("test")
//        val mArray = MutableArray()
//        mArray.addBoolean(true)
//        mArray.addBoolean(false)
//        mArray.setBoolean(0, false)
//        mArray.setBoolean(1, true)
//        mDoc.setArray("array", mArray)
//        saveDocInBaseTestDb(mDoc) { doc ->
//            assertEquals(1, doc.count())
//            org.junit.Assert.assertTrue(doc.contains("array"))
//            val array: Array = doc.getArray("array")
//            org.junit.Assert.assertNotNull(array)
//            assertEquals(2, array.count())
//            org.junit.Assert.assertTrue(array.getBoolean(1))
//            org.junit.Assert.assertFalse(array.getBoolean(0))
//        }
//    }
//
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class)
//    fun testInsertBoolean() {
//        val mDoc = MutableDocument("test")
//        val mArray = MutableArray()
//        mArray.addBoolean(false) // will be pushed 2 times
//        mArray.addBoolean(true) // will be pushed 2 times.
//        mArray.insertBoolean(0, true)
//        mArray.insertBoolean(1, false)
//        mDoc.setArray("array", mArray)
//        saveDocInBaseTestDb(mDoc) { doc ->
//            assertEquals(1, doc.count())
//            org.junit.Assert.assertTrue(doc.contains("array"))
//            val array: Array = doc.getArray("array")
//            org.junit.Assert.assertNotNull(array)
//            assertEquals(4, array.count())
//            org.junit.Assert.assertTrue(array.getBoolean(0))
//            org.junit.Assert.assertFalse(array.getBoolean(1))
//            org.junit.Assert.assertFalse(array.getBoolean(2))
//            org.junit.Assert.assertTrue(array.getBoolean(3))
//        }
//    }
//
//    ///////////////  JSON tests
//    // JSON 3.4
//    @org.junit.Test
//    @Throws(CouchbaseLiteException::class, org.json.JSONException::class)
//    fun testArrayToJSON() {
//        val mDoc = MutableDocument().setArray("array", makeArray())
//        verifyArray(JSONArray(saveDocInBaseTestDb(mDoc).getArray("array").toJSON()))
//    }
//
//    // JSON 3.7.?
//    @org.junit.Test(expected = java.lang.IllegalStateException::class)
//    fun testArrayToJSONBeforeSave() {
//        MutableArray().toJSON()
//    }
//
//    // JSON 3.7.a-b
//    @org.junit.Test
//    @Throws(
//        org.json.JSONException::class,
//        java.io.IOException::class,
//        CouchbaseLiteException::class
//    )
//    fun testArrayFromJSON() {
//        val mArray: MutableArray = MutableArray(readJSONResource("array.json"))
//        val mDoc = MutableDocument().setArray("array", mArray)
//        val dbArray: Array = saveDocInBaseTestDb(mDoc).getArray("array")
//        verifyArray(dbArray)
//        verifyArray(JSONArray(dbArray.toJSON()))
//    }
//
//    // JSON 3.7.c.1
//    @org.junit.Test(expected = java.lang.IllegalArgumentException::class)
//    fun testArrayFromBadJSON1() {
//        MutableArray("[")
//    }
//
//    // JSON 3.7.c.2
//    @org.junit.Test(expected = java.lang.IllegalArgumentException::class)
//    fun testArrayFromBadJSON2() {
//        MutableArray("[ab cd]")
//    }
//
//    // JSON 3.7.d
//    @org.junit.Test(expected = java.lang.IllegalArgumentException::class)
//    @Throws(java.io.IOException::class)
//    fun testDictFromArray() {
//        MutableArray(readJSONResource("dictionary.json"))
//    }
//
//    ///////////////  Tooling
//    private fun arrayOfAllTypes(): List<Any?> {
//        val list: MutableList<Any?> = java.util.ArrayList<Any>()
//        list.add(true)
//        list.add(false)
//        list.add("string")
//        list.add(0)
//        list.add(1)
//        list.add(-1)
//        list.add(1.1)
//        list.add(JSONUtils.toDate(TEST_DATE))
//        list.add(null)
//
//        // Dictionary
//        val subdict = MutableDictionary()
//        subdict.setValue("name", "Scott Tiger")
//        list.add(subdict)
//
//        // Array
//        val subarray = MutableArray()
//        subarray.addValue("a")
//        subarray.addValue("b")
//        subarray.addValue("c")
//        list.add(subarray)
//
//        // Blob
//        list.add(Blob("text/plain", BLOB_CONTENT.getBytes(java.nio.charset.StandardCharsets.UTF_8)))
//        return list
//    }
//
//    private fun populateData(array: MutableArray?) {
//        val data = arrayOfAllTypes()
//        for (o in data) {
//            array!!.addValue(o)
//        }
//    }
//
//    private fun populateDataByType(array: MutableArray?) {
//        val data = arrayOfAllTypes()
//        for (o in data) {
//            if (o is Int) {
//                array!!.addInt(o.toInt())
//            } else if (o is Long) {
//                array!!.addLong(o.toLong())
//            } else if (o is Float) {
//                array!!.addFloat(o.toFloat())
//            } else if (o is Double) {
//                array!!.addDouble(o.toDouble())
//            } else if (o is Number) {
//                array!!.addNumber(o as Number?)
//            } else if (o is String) {
//                array!!.addString(o as String?)
//            } else if (o is Boolean) {
//                array!!.addBoolean(o.toBoolean())
//            } else if (o is java.util.Date) {
//                array!!.addDate(o as java.util.Date?)
//            } else if (o is Blob) {
//                array!!.addBlob(o as Blob?)
//            } else if (o is MutableDictionary) {
//                array!!.addDictionary(o as MutableDictionary?)
//            } else if (o is MutableArray) {
//                array!!.addArray(o as MutableArray?)
//            } else {
//                array!!.addValue(o)
//            }
//        }
//    }
//
//    @Throws(CouchbaseLiteException::class)
//    private fun save(
//        mDoc: MutableDocument,
//        key: String,
//        mArray: MutableArray?,
//        validator: com.couchbase.lite.internal.utils.Fn.Consumer<Array>
//    ): Document {
//        validator.accept(mArray)
//        mDoc.setValue(key, mArray)
//        val doc: Document = saveDocInBaseTestDb(mDoc)
//        val array = doc.getArray(key)
//        validator.accept(array)
//        return doc
//    }
//
//    private fun verifyBlob(obj: Any?) {
//        org.junit.Assert.assertTrue(obj is Blob)
//        val blob = obj as Blob?
//        org.junit.Assert.assertNotNull(blob)
//        val contents = blob!!.getContent()
//        org.junit.Assert.assertNotNull(contents)
//        assertArrayEquals(BLOB_CONTENT.getBytes(java.nio.charset.StandardCharsets.UTF_8), contents)
//        assertEquals(BLOB_CONTENT, String(contents))
//    }
//}