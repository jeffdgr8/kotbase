package com.couchbase.lite.kmp

import com.couchbase.lite.kmp.internal.utils.TestUtils.assertThrows
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.test.*

class DictionaryTest : BaseDbTest() {

    @Test
    fun testCreateDictionary() {
        val address = MutableDictionary()
        assertEquals(0, address.count)
        assertEquals(emptyMap(), address.toMap())

        val mDoc = MutableDocument("doc1")
        mDoc.setValue("address", address)
        assertEquals(address, mDoc.getDictionary("address"))

        val doc = saveDocInBaseTestDb(mDoc)
        assertEquals(emptyMap(), doc.getDictionary("address")?.toMap())
    }

    @Test
    fun testRecursiveDictionary() {
        assertFailsWith<IllegalArgumentException> {
            val dict = MutableDictionary()
            dict.setDictionary("k1", dict)
        }
    }

    @Test
    fun testCreateDictionaryWithMap() {
        val dict = mutableMapOf<String, Any?>(
            "street" to "1 Main street",
            "city" to "Mountain View",
            "state" to "CA"
        )
        val address = MutableDictionary(dict)
        assertEquals(3, address.count)
        assertEquals("1 Main street", address.getValue("street"))
        assertEquals("Mountain View", address.getValue("city"))
        assertEquals("CA", address.getValue("state"))
        assertEquals(dict, address.toMap())

        val mDoc1 = MutableDocument("doc1")
        mDoc1.setValue("address", address)
        assertEquals(address, mDoc1.getDictionary("address"))

        val doc1 = saveDocInBaseTestDb(mDoc1)
        assertEquals(dict, doc1.getDictionary("address")?.toMap())
    }

    @Test
    fun testGetValueFromNewEmptyDictionary() {
        val mDict = MutableDictionary()

        assertEquals(0, mDict.getInt("key"))
        assertEquals(0.0f, mDict.getFloat("key"), 0.0f)
        assertEquals(0.0, mDict.getDouble("key"), 0.0)
        assertFalse(mDict.getBoolean("key"))
        assertNull(mDict.getBlob("key"))
        assertNull(mDict.getDate("key"))
        assertNull(mDict.getNumber("key"))
        assertNull(mDict.getValue("key"))
        assertNull(mDict.getString("key"))
        assertNull(mDict.getDictionary("key"))
        assertNull(mDict.getArray("key"))
        assertEquals(emptyMap(), mDict.toMap())

        val mDoc = MutableDocument("doc1")
        mDoc.setValue("dict", mDict)

        val doc = saveDocInBaseTestDb(mDoc)

        val dict = doc.getDictionary("dict")!!

        assertEquals(0, dict.getInt("key"))
        assertEquals(0.0f, dict.getFloat("key"), 0.0f)
        assertEquals(0.0, dict.getDouble("key"), 0.0)
        assertFalse(dict.getBoolean("key"))
        assertNull(dict.getBlob("key"))
        assertNull(dict.getDate("key"))
        assertNull(dict.getNumber("key"))
        assertNull(dict.getValue("key"))
        assertNull(dict.getString("key"))
        assertNull(dict.getDictionary("key"))
        assertNull(dict.getArray("key"))
        assertEquals(emptyMap(), dict.toMap())
    }

    @Test
    fun testSetNestedDictionaries() {
        val doc = MutableDocument("doc1")

        val level1 = MutableDictionary()
        level1.setValue("name", "n1")
        doc.setValue("level1", level1)

        val level2 = MutableDictionary()
        level2.setValue("name", "n2")
        doc.setValue("level2", level2)

        val level3 = MutableDictionary()
        level3.setValue("name", "n3")
        doc.setValue("level3", level3)

        assertEquals(level1, doc.getDictionary("level1"))
        assertEquals(level2, doc.getDictionary("level2"))
        assertEquals(level3, doc.getDictionary("level3"))

        val dict = mutableMapOf<String, Any?>()
        val l1 = mapOf<String, Any?>("name" to "n1")
        dict["level1"] = l1
        val l2 = mapOf<String, Any?>("name" to "n2")
        dict["level2"] = l2
        val l3 = mapOf<String, Any?>("name" to "n3")
        dict["level3"] = l3
        assertEquals(dict, doc.toMap())

        val savedDoc = saveDocInBaseTestDb(doc)

        assertNotSame(level1, savedDoc.getDictionary("level1"))
        assertEquals(dict, savedDoc.toMap())
    }

    @Test
    fun testDictionaryArray() {
        val mDoc = MutableDocument("doc1")

        val data = mutableListOf<Any?>()

        val d1 = mapOf<String, Any?>("name" to "1")
        data.add(d1)
        val d2 = mapOf<String, Any?>("name" to "2")
        data.add(d2)
        val d3 = mapOf<String, Any?>("name" to "3")
        data.add(d3)
        val d4 = mapOf<String, Any?>("name" to "4")
        data.add(d4)
        assertEquals(4, data.size)

        mDoc.setValue("array", data)

        val mArray = mDoc.getArray("array")!!
        assertEquals(4, mArray.count)

        val mDict1 = mArray.getDictionary(0)!!
        val mDict2 = mArray.getDictionary(1)!!
        val mDict3 = mArray.getDictionary(2)!!
        val mDict4 = mArray.getDictionary(3)!!

        assertEquals("1", mDict1.getString("name"))
        assertEquals("2", mDict2.getString("name"))
        assertEquals("3", mDict3.getString("name"))
        assertEquals("4", mDict4.getString("name"))

        // after save
        val doc = saveDocInBaseTestDb(mDoc)

        val array = doc.getArray("array")!!
        assertEquals(4, array.count)

        val dict1 = array.getDictionary(0)!!
        val dict2 = array.getDictionary(1)!!
        val dict3 = array.getDictionary(2)!!
        val dict4 = array.getDictionary(3)!!

        assertEquals("1", dict1.getString("name"))
        assertEquals("2", dict2.getString("name"))
        assertEquals("3", dict3.getString("name"))
        assertEquals("4", dict4.getString("name"))
    }

    @Test
    fun testReplaceDictionary() {
        val doc = MutableDocument("doc1")

        val profile1 = MutableDictionary()
        profile1.setValue("name", "Scott Tiger")
        doc.setValue("profile", profile1)
        assertEquals(profile1, doc.getDictionary("profile"))

        val profile2 = MutableDictionary()
        profile2.setValue("name", "Daniel Tiger")
        doc.setValue("profile", profile2)
        assertEquals(profile2, doc.getDictionary("profile"))

        // Profile1 should be now detached:
        profile1.setValue("age", 20)
        assertEquals("Scott Tiger", profile1.getValue("name"))
        assertEquals(20, profile1.getValue("age"))

        // Check profile2:
        assertEquals("Daniel Tiger", profile2.getValue("name"))
        assertNull(profile2.getValue("age"))

        // Save:
        val savedDoc = saveDocInBaseTestDb(doc)

        assertNotSame(profile2, savedDoc.getDictionary("profile"))
        val savedDict = savedDoc.getDictionary("profile")!!
        assertEquals("Daniel Tiger", savedDict.getValue("name"))
    }

    @Test
    fun testReplaceDictionaryDifferentType() {
        val doc = MutableDocument("doc1")

        val profile1 = MutableDictionary()
        profile1.setValue("name", "Scott Tiger")
        doc.setValue("profile", profile1)
        assertEquals(profile1, doc.getDictionary("profile"))

        // Set string value to profile:
        doc.setValue("profile", "Daniel Tiger")
        assertEquals("Daniel Tiger", doc.getValue("profile"))

        // Profile1 should be now detached:
        profile1.setValue("age", 20)
        assertEquals("Scott Tiger", profile1.getValue("name"))
        assertEquals(20, profile1.getValue("age"))

        // Check whether the profile value has no change:
        assertEquals("Daniel Tiger", doc.getValue("profile"))

        // Save
        val savedDoc = saveDocInBaseTestDb(doc)
        assertEquals("Daniel Tiger", savedDoc.getValue("profile"))
    }

    @Test
    fun testRemoveDictionary() {
        var doc = MutableDocument("doc1")
        val profile1 = MutableDictionary()
        profile1.setValue("name", "Scott Tiger")
        doc.setValue("profile", profile1)
        assertEquals(profile1.toMap(), doc.getDictionary("profile")!!.toMap())
        assertTrue(doc.contains("profile"))

        // Remove profile
        doc.remove("profile")
        assertNull(doc.getValue("profile"))
        assertFalse(doc.contains("profile"))

        // Profile1 should be now detached:
        profile1.setValue("age", 20)
        assertEquals("Scott Tiger", profile1.getValue("name"))
        assertEquals(20, profile1.getValue("age"))

        // Check whether the profile value has no change:
        assertNull(doc.getValue("profile"))

        // Save:
        doc = saveDocInBaseTestDb(doc).toMutable()

        assertNull(doc.getValue("profile"))
        assertFalse(doc.contains("profile"))
    }

    @Test
    fun testEnumeratingKeys() {
        val dict = MutableDictionary()
        for (i in 0 until 20) {
            dict.setValue("key$i", i)
        }
        var content = dict.toMap()

        var result = mutableMapOf<String, Any?>()
        var count = 0
        for (key in dict) {
            result[key] = dict.getValue(key)
            count++
        }
        assertEquals(content.size, count)
        assertEquals(content, result)

        // Update:
        dict.remove("key2")
        dict.setValue("key20", 20)
        dict.setValue("key21", 21)
        content = dict.toMap()

        result = mutableMapOf()
        count = 0
        for (key in dict) {
            result[key] = dict.getValue(key)
            count++
        }
        assertEquals(content.size, count)
        assertEquals(content, result)

        val finalContent = content

        val doc = MutableDocument("doc1")
        doc.setValue("dict", dict)
        saveDocInBaseTestDb(doc) { doc1: Document ->
            val result1 = mutableMapOf<String, Any?>()
            var count1 = 0
            val dictObj = doc1.getDictionary("dict")
            for (key in dictObj!!) {
                result1[key] = dict.getValue(key)
                count1++
            }
            assertEquals(finalContent.size, count1)
            assertEquals(finalContent, result1)
        }
    }

    // ??? Surprisingly, no concurrent modification exception.
    @Test
    fun testDictionaryEnumerationWithDataModification1() {
        val dict = MutableDictionary()
        for (i in 0..2) {
            dict.setValue("key-$i", i)
        }

        assertEquals(3, dict.count)

        var n = 0
        val itr = dict.iterator()
        while (itr.hasNext()) {
            if (n++ == 1) {
                dict.setValue("key-3", 3)
            }
            itr.next()
        }

        assertEquals(4, dict.count)
    }

    // ??? Surprisingly, no concurrent modification exception.
    @Test
    fun testDictionaryEnumerationWithDataModification2() {
        var dict = MutableDictionary()
        for (i in 0..2) {
            dict.setValue("key-$i", i)
        }

        assertEquals(3, dict.count)

        val doc = MutableDocument("doc1").setValue("dict", dict)
        dict = saveDocInBaseTestDb(doc).toMutable().getDictionary("dict")!!

        var n = 0
        val itr = dict.iterator()
        while (itr.hasNext()) {
            if (n++ == 1) {
                dict.setValue("key-3", 3)
            }
            itr.next()
        }

        assertEquals(4, dict.count)
    }

    // https://github.com/couchbase/couchbase-lite-core/issues/230
    @Test
    fun testLargeLongValue() {
        var doc = MutableDocument("test")
        val num1 = 1234567L
        val num2 = 12345678L
        val num3 = 123456789L
        doc.setValue("num1", num1)
        doc.setValue("num2", num2)
        doc.setValue("num3", num3)
        doc = saveDocInBaseTestDb(doc).toMutable()
        assertEquals(num1, doc.getLong("num1"))
        assertEquals(num2, doc.getLong("num2"))
        assertEquals(num3, doc.getLong("num3"))
    }

    //https://forums.couchbase.com/t/long-value-on-document-changed-after-saved-to-db/14259/
    @Test
    fun testLargeLongValue2() {
        var doc = MutableDocument("test")
        val num1 = 11989091L
        val num2 = 231548688L
        doc.setValue("num1", num1)
        doc.setValue("num2", num2)
        doc = saveDocInBaseTestDb(doc).toMutable()
        assertEquals(num1, doc.getLong("num1"))
        assertEquals(num2, doc.getLong("num2"))
    }

    @Test
    fun testSetNull() {
        val mDoc = MutableDocument("test")
        val mDict = MutableDictionary()
        mDict.setValue("obj-null", null)
        mDict.setString("string-null", null)
        mDict.setNumber("number-null", null)
        mDict.setDate("date-null", null)
        mDict.setArray("array-null", null)
        mDict.setDictionary("dict-null", null)
        mDoc.setDictionary("dict", mDict)
        saveDocInBaseTestDb(mDoc) { doc ->
            assertEquals(1, doc.count)
            assertTrue(doc.contains("dict"))
            val d = doc.getDictionary("dict")
            assertNotNull(d)
            assertEquals(6, d.count)
            assertTrue(d.contains("obj-null"))
            assertTrue(d.contains("string-null"))
            assertTrue(d.contains("number-null"))
            assertTrue(d.contains("date-null"))
            assertTrue(d.contains("array-null"))
            assertTrue(d.contains("dict-null"))
            assertNull(d.getValue("obj-null"))
            assertNull(d.getValue("string-null"))
            assertNull(d.getValue("number-null"))
            assertNull(d.getValue("date-null"))
            assertNull(d.getValue("array-null"))
            assertNull(d.getValue("dict-null"))
        }
    }

    @Test
    fun testEquals() {

        // mDict1 and mDict2 have exactly same data
        // mDict3 is different
        // mDict4 is different

        val mDict1 = MutableDictionary()
        mDict1.setValue("key1", 1L)
        mDict1.setValue("key2", "Hello")
        mDict1.setValue("key3", null)

        val mDict2 = MutableDictionary()
        mDict2.setValue("key1", 1L)
        mDict2.setValue("key2", "Hello")
        mDict2.setValue("key3", null)

        val mDict3 = MutableDictionary()
        mDict3.setValue("key1", 100L)
        mDict3.setValue("key3", true)

        val mDict4 = MutableDictionary()
        mDict4.setValue("key1", 100L)

        val mDict5 = MutableDictionary()
        mDict4.setValue("key1", 100L)
        mDict3.setValue("key3", false)

        val mDoc = MutableDocument("test")
        mDoc.setDictionary("dict1", mDict1)
        mDoc.setDictionary("dict2", mDict2)
        mDoc.setDictionary("dict3", mDict3)
        mDoc.setDictionary("dict4", mDict4)
        mDoc.setDictionary("dict5", mDict5)

        val doc = saveDocInBaseTestDb(mDoc)
        val dict1 = doc.getDictionary("dict1")
        val dict2 = doc.getDictionary("dict2")
        val dict3 = doc.getDictionary("dict3")
        val dict4 = doc.getDictionary("dict4")
        val dict5 = doc.getDictionary("dict5")

        // compare dict1, dict2, mdict1, and mdict2
        assertEquals(dict1, dict1)
        assertEquals(dict2, dict2)
        assertEquals(dict1, dict2)
        assertEquals(dict2, dict1)
        assertEquals(dict1, dict1!!.toMutable())
        assertEquals(dict1, dict2!!.toMutable())
        assertEquals(dict1.toMutable(), dict1)
        assertEquals(dict2.toMutable(), dict1)
        assertEquals(dict1, mDict1)
        assertEquals(dict1, mDict2)
        assertEquals(dict2, mDict1)
        assertEquals(dict2, mDict2)
        assertEquals(mDict1, dict1)
        assertEquals(mDict2, dict1)
        assertEquals(mDict1, dict2)
        assertEquals(mDict2, dict2)
        assertEquals(mDict1, mDict1)
        assertEquals(mDict2, mDict2)
        assertEquals(mDict1, mDict1)
        assertEquals(mDict2, mDict2)

        // compare dict1, dict3, mdict1, and mdict3
        assertEquals(dict3, dict3)
        assertNotEquals(dict1, dict3)
        assertNotEquals(dict3, dict1)
        assertNotEquals(dict1, dict3!!.toMutable())
        assertNotEquals(dict3.toMutable(), dict1)
        assertNotEquals(dict1, mDict3)
        assertNotEquals(dict3, mDict1)
        assertEquals(dict3, mDict3)
        assertNotEquals(mDict3, dict1)
        assertNotEquals(mDict1, dict3)
        assertEquals(mDict3, dict3)
        assertEquals(mDict3, mDict3)
        assertEquals(mDict3, mDict3)

        // compare dict1, dict4, mdict1, and mdict4
        assertEquals(dict4, dict4)
        assertNotEquals(dict1, dict4)
        assertNotEquals(dict4, dict1)
        assertNotEquals(dict1, dict4!!.toMutable())
        assertNotEquals(dict4.toMutable(), dict1)
        assertNotEquals(dict1, mDict4)
        assertNotEquals(dict4, mDict1)
        assertEquals(dict4, mDict4)
        assertNotEquals(mDict4, dict1)
        assertNotEquals(mDict1, dict4)
        assertEquals(mDict4, dict4)
        assertEquals(mDict4, mDict4)
        assertEquals(mDict4, mDict4)

        // compare dict3, dict4, mdict3, and mdict4
        assertNotEquals(dict3, dict4)
        assertNotEquals(dict4, dict3)
        assertNotEquals(dict3, dict4.toMutable())
        assertNotEquals(dict4.toMutable(), dict3)
        assertNotEquals(dict3, mDict4)
        assertNotEquals(dict4, mDict3)
        assertNotEquals(mDict4, dict3)
        assertNotEquals(mDict3, dict4)

        // compare dict3, dict5, mdict3, and mdict5
        assertNotEquals(dict3, dict5)
        assertNotEquals(dict5, dict3)
        assertNotEquals(dict3, dict5!!.toMutable())
        assertNotEquals(dict5.toMutable(), dict3)
        assertNotEquals(dict3, mDict5)
        assertNotEquals(dict5, mDict3)
        assertNotEquals(mDict5, dict3)
        assertNotEquals(mDict3, dict5)

        // compare dict5, dict4, mDict5, and mdict4
        assertNotEquals(dict5, dict4)
        assertNotEquals(dict4, dict5)
        assertNotEquals(dict5, dict4.toMutable())
        assertNotEquals(dict4.toMutable(), dict5)
        assertNotEquals(dict5, mDict4)
        assertNotEquals(dict4, mDict5)
        assertNotEquals(mDict4, dict5)
        assertNotEquals(mDict5, dict4)

        assertNotNull(dict3)
    }

    @Test
    fun testHashCode() {

        // mDict1 and mDict2 have exactly same data
        // mDict3 is different
        // mDict4 is different

        val mDict1 = MutableDictionary()
        mDict1.setValue("key1", 1L)
        mDict1.setValue("key2", "Hello")
        mDict1.setValue("key3", null)

        val mDict2 = MutableDictionary()
        mDict2.setValue("key1", 1L)
        mDict2.setValue("key2", "Hello")
        mDict2.setValue("key3", null)

        val mDict3 = MutableDictionary()
        mDict3.setValue("key1", 100L)
        mDict3.setValue("key3", true)

        val mDict4 = MutableDictionary()
        mDict4.setValue("key1", 100L)

        val mDict5 = MutableDictionary()
        mDict4.setValue("key1", 100L)
        mDict3.setValue("key3", false)

        val mDoc = MutableDocument("test")
        mDoc.setDictionary("dict1", mDict1)
        mDoc.setDictionary("dict2", mDict2)
        mDoc.setDictionary("dict3", mDict3)
        mDoc.setDictionary("dict4", mDict4)
        mDoc.setDictionary("dict5", mDict5)

        val doc = saveDocInBaseTestDb(mDoc)
        val dict1 = doc.getDictionary("dict1")!!
        val dict2 = doc.getDictionary("dict2")!!
        val dict3 = doc.getDictionary("dict3")!!

        assertEquals(dict1.hashCode(), dict1.hashCode())
        assertEquals(dict1.hashCode(), dict2.hashCode())
        assertEquals(dict2.hashCode(), dict1.hashCode())
        assertEquals(dict1.hashCode(), dict1.toMutable().hashCode())
        assertEquals(dict1.hashCode(), dict2.toMutable().hashCode())
        assertEquals(dict1.hashCode(), mDict1.hashCode())
        assertEquals(dict1.hashCode(), mDict2.hashCode())
        assertEquals(dict2.hashCode(), mDict1.hashCode())
        assertEquals(dict2.hashCode(), mDict2.hashCode())

        assertNotEquals(dict3.hashCode(), dict1.hashCode())
        assertNotEquals(dict3.hashCode(), dict2.hashCode())
        assertNotEquals(dict3.hashCode(), dict1.toMutable().hashCode())
        assertNotEquals(dict3.hashCode(), dict2.toMutable().hashCode())
        assertNotEquals(dict3.hashCode(), mDict1.hashCode())
        assertNotEquals(dict3.hashCode(), mDict2.hashCode())
        assertNotEquals(mDict3.hashCode(), dict1.hashCode())
        assertNotEquals(mDict3.hashCode(), dict2.hashCode())
        assertNotEquals(mDict3.hashCode(), dict1.toMutable().hashCode())
        assertNotEquals(mDict3.hashCode(), dict2.toMutable().hashCode())
        assertNotEquals(mDict3.hashCode(), mDict1.hashCode())
        assertNotEquals(mDict3.hashCode(), mDict2.hashCode())

        assertNotEquals(0, dict3.hashCode())
        assertNotEquals(dict3.hashCode(), Any().hashCode())
        assertNotEquals(dict3.hashCode(), 1.hashCode())
        assertNotEquals(dict3.hashCode(), emptyMap<String, Any?>().hashCode())
        assertNotEquals(dict3.hashCode(), MutableDictionary().hashCode())
        assertNotEquals(dict3.hashCode(), MutableArray().hashCode())
        assertNotEquals(mDict3.hashCode(), doc.hashCode())
        assertNotEquals(mDict3.hashCode(), mDoc.hashCode())
        assertNotEquals(mDict3.hashCode(), dict1.toMutable().hashCode())
        assertNotEquals(mDict3.hashCode(), dict2.toMutable().hashCode())
        assertNotEquals(mDict3.hashCode(), mDict1.hashCode())
        assertNotEquals(mDict3.hashCode(), mDict2.hashCode())
    }

    @Test
    fun testGetDictionary() {
        val mNestedDict = MutableDictionary()
        mNestedDict.setValue("key1", 1L)
        mNestedDict.setValue("key2", "Hello")
        mNestedDict.setValue("key3", null)

        val mDict = MutableDictionary()
        mDict.setValue("key1", 1L)
        mDict.setValue("key2", "Hello")
        mDict.setValue("key3", null)
        mDict.setValue("nestedDict", mNestedDict)

        val mDoc = MutableDocument("test")
        mDoc.setDictionary("dict", mDict)

        val dict = saveDocInBaseTestDb(mDoc).getDictionary("dict")

        assertNotNull(dict)
        assertNull(dict.getDictionary("not-exists"))
        assertNotNull(dict.getDictionary("nestedDict"))

        val nestedDict = dict.getDictionary("nestedDict")
        assertEquals(nestedDict, mNestedDict)
        assertEquals(dict, mDict)
    }

    @Test
    fun testGetArray() {
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

        val array = saveDocInBaseTestDb(mDoc).getArray("array")

        assertNotNull(array)
        assertNull(array.getArray(0))
        assertNull(array.getArray(1))
        assertNull(array.getArray(2))
        assertNotNull(array.getArray(3))

        assertThrows(IndexOutOfBoundsException::class) {
            assertNull(array.getArray(4))
        }

        val nestedArray = array.getArray(3)
        assertEquals(nestedArray, mNestedArray)
        assertEquals(array, mArray)
    }

    // https://github.com/couchbase/couchbase-lite-android/issues/1518
    @Test
    fun testSetValueWithDictionary() {
        val mDict = MutableDictionary()
        mDict.setString("hello", "world")

        var mDoc = MutableDocument("doc1")
        mDoc.setValue("dict", mDict)
        val doc = saveDocInBaseTestDb(mDoc)

        var dict = doc.getDictionary("dict")!!

        mDoc = doc.toMutable()
        mDoc.setValue("dict2", dict)

        dict = saveDocInBaseTestDb(mDoc).getDictionary("dict2")!!
        assertEquals(1, dict.count)
        assertEquals("world", dict.getString("hello"))
    }

    @Test
    fun testSetValueWithArray() {
        val mArray = MutableArray()
        mArray.addString("hello")
        mArray.addString("world")

        var mDoc = MutableDocument("doc1")
        mDoc.setValue("array", mArray)
        val doc = saveDocInBaseTestDb(mDoc)

        var array = doc.getArray("array")!!

        mDoc = doc.toMutable()
        mDoc.setValue("array2", array)

        array = saveDocInBaseTestDb(mDoc).getArray("array2")!!
        assertEquals(2, array.count)
        assertEquals("hello", array.getString(0))
        assertEquals("world", array.getString(1))
    }

    ///////////////  JSON tests
    // JSON 3.3
    @Test
    fun testDictToJSON() {
        val mDoc = MutableDocument().setDictionary("dict", makeDict())
        verifyDict(
            Json.parseToJsonElement(
                saveDocInBaseTestDb(mDoc).getDictionary("dict")!!.toJSON()
            ).jsonObject
        )
    }

    // JSON 3.6.?
    @Test
    fun testDictToJSONBeforeSave() {
        assertFailsWith<IllegalStateException> {
            MutableDictionary().toJSON()
        }
    }

    // JSON 3.5.a-b
    @Test
    fun testDictFromJSON() {
        val mDict = MutableDictionary(readJSONResource("dictionary.json"))
        val mDoc = MutableDocument().setDictionary("dict", mDict)
        val dbDict = saveDocInBaseTestDb(mDoc).getDictionary("dict")
        verifyDict(dbDict)
        verifyDict(Json.parseToJsonElement(dbDict!!.toJSON()).jsonObject)
    }

    // JSON 3.6.c.1
    @Test
    fun testDictFromBadJSON1() {
        assertFailsWith<IllegalArgumentException> {
            MutableDictionary("{")
        }
    }

    // JSON 3.6.c.2
    @Test
    fun testDictFromBadJSON2() {
        assertFailsWith<IllegalArgumentException> {
            MutableDictionary("{ab cd: \"xyz\"}")
        }
    }

    // JSON 3.6.c.3
    @Test
    fun testDictFromBadJSON3() {
        assertFailsWith<IllegalArgumentException> {
            MutableDictionary("{ab: \"xyz\" cd: \"xyz\"}")
        }
    }

    // JSON 3.6.d
    @Test
    fun testDictFromArray() {
        assertFailsWith<IllegalArgumentException> {
            MutableDocument("fromJSON", readJSONResource("array.json"))
        }
    }
}
