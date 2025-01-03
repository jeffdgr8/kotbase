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

import kotbase.ext.nowMillis
import kotbase.ext.toStringMillis
import kotbase.test.assertIntEquals
import kotlinx.datetime.Clock
import kotlin.test.*

class FragmentTest : BaseDbTest() {

    @Test
    fun testBasicGetFragmentValues() {
        val dict = mapOf(
            "name" to "Jason",
            "address" to mapOf(
                "street" to "1 Main Street",
                "phones" to mapOf("mobile" to "650-123-4567")
            ),
            "references" to listOf(
                mapOf("name" to "Scott"),
                mapOf("name" to "Sam")
            )
        )
        val doc = MutableDocument("doc1")
        doc.setData(dict)

        assertEquals("Jason", doc["name"].string)
        assertEquals("1 Main Street", doc["address"]["street"].string)
        assertEquals("650-123-4567", doc["address"]["phones"]["mobile"].string)
        assertEquals("Scott", doc["references"][0]["name"].string)
        assertEquals("Sam", doc["references"][1]["name"].string)

        assertNull(doc["references"][2]["name"].value)
        assertNull(doc["dummy"]["dummy"]["dummy"].value)
        assertNull(doc["dummy"]["dummy"][0]["dummy"].value)
    }

    @Test
    fun testBasicSetFragmentValues() {
        val doc = MutableDocument("doc1")
        doc["name"].value = "Jason"

        doc["address"].value = MutableDictionary()
        doc["address"]["street"].value = "1 Main Street"
        doc["address"]["phones"].value = MutableDictionary()
        doc["address"]["phones"]["mobile"].value = "650-123-4567"

        assertEquals("Jason", doc["name"].string)
        assertEquals("1 Main Street", doc["address"]["street"].string)
        assertEquals("650-123-4567", doc["address"]["phones"]["mobile"].string)
    }

    @Test
    fun testGetDocFragmentWithID() {
        val dict = mapOf(
            "address" to mapOf(
                "street" to "1 Main street",
                "city" to "Mountain View",
                "state" to "CA"
            )
        )
        saveDocInCollection(MutableDocument("doc1", data = dict))

        val doc = testCollection["doc1"]
        assertNotNull(doc)
        assertTrue(doc.exists)
        assertNotNull(doc.document)
        assertEquals("1 Main street", doc["address"]["street"].string)
        assertEquals("Mountain View", doc["address"]["city"].string)
        assertEquals("CA", doc["address"]["state"].string)
    }

    @Test
    fun testGetDocFragmentWithNonExistingID() {
        val doc = testCollection["doc1"]
        assertNotNull(doc)
        assertFalse(doc.exists)
        assertNull(doc["address"]["street"].string)
        assertNull(doc["address"]["city"].string)
        assertNull(doc["address"]["state"].string)
    }

    @Test
    fun testGetDocFragmentWithIDFromCollection() {
        val col = testDatabase.createCollection("colA", scopeName = "scopeA")

        // Not exist
        var doc = col["doc1"]
        assertNotNull(doc)
        assertFalse(doc.exists)

        val dict = mapOf(
            "address" to mapOf(
                "street" to "1 Main street",
                "city" to "Mountain View",
                "state" to "CA"
            )
        )
        saveDocInCollection(MutableDocument("doc1", data = dict), collection = col)

        // Exist
        doc = col["doc1"]
        assertNotNull(doc)
        assertTrue(doc.exists)
        assertNotNull(doc.document)
        assertEquals("1 Main street", doc["address"]["street"].string)
        assertEquals("Mountain View", doc["address"]["city"].string)
        assertEquals("CA", doc["address"]["state"].string)
    }

    @Test
    fun testGetFragmentFromDictionaryValue() {
        val dict = mapOf<String, Any?>(
            "address" to mapOf(
                "street" to "1 Main street",
                "city" to "Mountain View",
                "state" to "CA"
            )
        )
        val doc = MutableDocument("doc1", data = dict)
        val d = saveDocInCollection(doc)
        val fragment = d["address"]
        assertNotNull(fragment)
        assertTrue(fragment.exists)
        assertNull(fragment.string)
        assertNull(fragment.date)
        assertEquals(0, fragment.int)
        assertEquals(0.0F, fragment.float)
        assertEquals(0.0, fragment.double)
        assertTrue(fragment.boolean)
        assertNull(fragment.array)
        assertNull(fragment.array)
        assertNotNull(fragment.value)
        assertNotNull(fragment.dictionary)
        assertSame(fragment.dictionary, fragment.value)
        assertEquals(dict["address"], fragment.dictionary!!.toMap())
    }

    @Test
    fun testGetFragmentFromArrayValue() {
        val dict = mapOf<String, Any?>(
            "references" to listOf(
                mapOf("name" to "Scott"),
                mapOf("name" to "Sam")
            )
        )
        val doc = MutableDocument("doc1", dict)
        val d = saveDocInCollection(doc)
        val fragment = d["references"]
        assertNotNull(fragment)
        assertTrue(fragment.exists)
        assertNull(fragment.string)
        assertNull(fragment.date)
        assertEquals(0, fragment.int)
        assertEquals(0.0F, fragment.float)
        assertEquals(0.0, fragment.double)
        assertTrue(fragment.boolean)
        assertNotNull(fragment.array)
        assertNotNull(fragment.value)
        assertSame(fragment.array, fragment.value)
        assertEquals(dict["references"], fragment.array!!.toList())
        assertNull(fragment.dictionary)
    }

    @Test
    fun testGetFragmentFromInteger() {
        val doc = MutableDocument("doc1")
        doc.setValue("integer", 10)
        val d = saveDocInCollection(doc)
        val fragment = d["integer"]
        assertNotNull(fragment)
        assertTrue(fragment.exists)
        assertNull(fragment.string)
        assertNull(fragment.date)
        assertEquals(10, fragment.int)
        assertEquals(10.0F, fragment.float)
        assertEquals(10.0, fragment.double)
        assertTrue(fragment.boolean)
        assertNull(fragment.array)
        assertIntEquals(10, fragment.value)
        assertNull(fragment.dictionary)
    }

    @Test
    fun testGetFragmentFromFloat() {
        val doc = MutableDocument("doc1")
        doc.setValue("float", 100.10F)
        val d = saveDocInCollection(doc)
        val fragment = d["float"]
        assertNotNull(fragment)
        assertTrue(fragment.exists)
        assertNull(fragment.string)
        assertNull(fragment.date)
        assertEquals(100, fragment.int)
        assertEquals(100.10F, fragment.float)
        assertEquals(100.10, fragment.double, 0.1)
        assertTrue(fragment.boolean)
        assertNull(fragment.array)
        assertEquals(100.10F, fragment.value)
        assertNull(fragment.dictionary)
    }

    @Test
    fun testGetFragmentFromDouble() {
        val doc = MutableDocument("doc1")
        doc.setValue("double", 99.99)
        val d = saveDocInCollection(doc)
        val fragment = d["double"]
        assertNotNull(fragment)
        assertTrue(fragment.exists)
        assertNull(fragment.string)
        assertNull(fragment.date)
        assertEquals(99, fragment.int)
        assertEquals(99.99F, fragment.float)
        assertEquals(99.99, fragment.double)
        assertTrue(fragment.boolean)
        assertNull(fragment.array)
        assertEquals(99.99, fragment.value)
        assertNull(fragment.dictionary)
    }

    @Test
    fun testGetFragmentFromBoolean() {
        val doc = MutableDocument("doc1")
        doc.setValue("boolean", true)
        val d = saveDocInCollection(doc)
        val fragment = d["boolean"]
        assertNotNull(fragment)
        assertTrue(fragment.exists)
        assertNull(fragment.string)
        assertNull(fragment.date)
        assertEquals(1, fragment.int)
        assertEquals(1.0F, fragment.float)
        assertEquals(1.0, fragment.double)
        assertTrue(fragment.boolean)
        assertNull(fragment.array)
        assertEquals(true, fragment.value)
        assertNull(fragment.dictionary)
    }

    @Test
    fun testGetFragmentFromDate() {
        val date = Clock.System.nowMillis()
        val dateStr = date.toStringMillis()
        val doc = MutableDocument("doc1")
        doc.setValue("date", date)
        val d = saveDocInCollection(doc)
        val fragment = d["date"]
        assertNotNull(fragment)
        assertTrue(fragment.exists)
        assertEquals(dateStr, fragment.string)
        assertEquals(dateStr, fragment.date!!.toStringMillis())
        assertEquals(0, fragment.int)
        assertEquals(0.0F, fragment.float)
        assertEquals(0.0, fragment.double)
        assertTrue(fragment.boolean)
        assertNull(fragment.array)
        assertEquals(dateStr, fragment.value)
        assertNull(fragment.dictionary)
    }

    @Test
    fun testGetFragmentFromString() {
        val doc = MutableDocument("doc1")
        doc.setValue("string", "hello world")
        val d = saveDocInCollection(doc)
        val fragment = d["string"]
        assertNotNull(fragment)
        assertTrue(fragment.exists)
        assertEquals("hello world", fragment.string)
        assertNull(fragment.date)
        assertEquals(0, fragment.int)
        assertEquals(0.0F, fragment.float)
        assertEquals(0.0, fragment.double)
        assertTrue(fragment.boolean)
        assertNull(fragment.array)
        assertEquals("hello world", fragment.value)
        assertNull(fragment.dictionary)
    }

    @Test
    fun testGetNestedDictionaryFragment() {
        val dict = mapOf(
            "address" to mapOf(
                "street" to "1 Main street",
                "phones" to mapOf("mobile" to "650-123-4567")
            )
        )
        val doc = MutableDocument("doc1", dict)
        val d = saveDocInCollection(doc)
        val fragment = d["address"]["phones"]
        assertNotNull(fragment)
        assertTrue(fragment.exists)
        assertNull(fragment.string)
        assertNull(fragment.date)
        assertEquals(0, fragment.int)
        assertEquals(0.0F, fragment.float)
        assertEquals(0.0, fragment.double)
        assertTrue(fragment.boolean)
        assertNull(fragment.array)
        assertNull(fragment.array)
        assertNotNull(fragment.value)
        assertNotNull(fragment.dictionary)
        assertSame(fragment.dictionary, fragment.value)
        assertEquals(dict["address"]!!["phones"], fragment.dictionary!!.toMap())
    }

    @Test
    fun testGetNestedNonExistingDictionaryFragment() {
        val dict = mapOf(
            "address" to mapOf(
                "street" to "1 Main street",
                "phones" to mapOf("mobile" to "650-123-4567")
            )
        )
        val doc = MutableDocument("doc1", dict)
        val d = saveDocInCollection(doc)
        val fragment = d["address"]["country"]
        assertNotNull(fragment)
        assertFalse(fragment.exists)
        assertNull(fragment.string)
        assertNull(fragment.date)
        assertEquals(0, fragment.int)
        assertEquals(0.0F, fragment.float)
        assertEquals(0.0, fragment.double)
        assertFalse(fragment.boolean)
        assertNull(fragment.array)
        assertNull(fragment.array)
        assertNull(fragment.value)
        assertNull(fragment.dictionary)
    }

    @Test
    fun testGetNestedArrayFragments() {
        val dict = mapOf<String, Any?>("nested-array" to listOf(listOf(1, 2, 3), listOf(4, 5, 6)))
        val doc = MutableDocument("doc1", dict)
        val d = saveDocInCollection(doc)
        val fragment = d["nested-array"]
        assertNotNull(fragment)
        assertTrue(fragment.exists)
        assertNull(fragment.string)
        assertNull(fragment.date)
        assertEquals(0, fragment.int)
        assertEquals(0.0F, fragment.float)
        assertEquals(0.0, fragment.double)
        assertTrue(fragment.boolean)
        assertNotNull(fragment.array)
        assertNotNull(fragment.value)
        assertSame(fragment.array, fragment.value)
        assertIntEquals(dict["nested-array"], fragment.array!!.toList())
        assertNull(fragment.dictionary)
    }

    @Test
    fun testGetNestedNonExistingArrayFragments() {
        val dict = mapOf("nested-array" to listOf(listOf(1, 2, 3), listOf(4, 5, 6)))
        val doc = MutableDocument("doc1", dict)
        val d = saveDocInCollection(doc)
        val fragment = d["nested-array"][2]
        assertNotNull(fragment)
        assertFalse(fragment.exists)
        assertNull(fragment.string)
        assertNull(fragment.date)
        assertEquals(0, fragment.int)
        assertEquals(0.0F, fragment.float)
        assertEquals(0.0, fragment.double)
        assertFalse(fragment.boolean)
        assertNull(fragment.array)
        assertNull(fragment.array)
        assertNull(fragment.value)
        assertNull(fragment.dictionary)
    }

    @Test
    fun testDictionaryFragmentSet() {
        val date = Clock.System.nowMillis()
        val doc = MutableDocument("doc1")
        doc["string"].value = "value"
        doc["bool"].value = true
        doc["int"].value = 7
        doc["float"].value = 2.2F
        doc["double"].value = 2.2
        doc["date"].value = date

        val d = saveDocInCollection(doc)
        assertEquals("value", d["string"].string)
        assertEquals(true, d["bool"].boolean)
        assertEquals(7, d["int"].int)
        assertEquals(2.2F, d["float"].float)
        assertEquals(2.2, d["double"].double)
        assertEquals(date.toStringMillis(), d["date"].date!!.toStringMillis())
    }

    @Test
    fun testDictionaryFragmentsetData() {
        val data = mapOf(
            "name" to "Jason",
            "address" to mapOf(
                "street" to "1 Main street",
                "phones" to mapOf("mobile" to "650-123-4567")
            )
        )
        val doc = MutableDocument("doc1")
        val dict = MutableDictionary(data)
        doc["dict"].value = dict

        val d = saveDocInCollection(doc)
        assertEquals("Jason", d["dict"]["name"].string)
        assertEquals("1 Main street", d["dict"]["address"]["street"].string)
        assertEquals("650-123-4567", d["dict"]["address"]["phones"]["mobile"].string)
    }

    @Test
    fun testDictionaryFragmentSetArray() {
        val doc = MutableDocument("doc1")
        val array = MutableArray(listOf(0, 1, 2))
        doc["array"].value = array

        val d = saveDocInCollection(doc)
        assertEquals(0, d["array"][0].int)
        assertEquals(1, d["array"][1].int)
        assertEquals(2, d["array"][2].int)
        assertNull(d["array"][3].value)
        assertFalse(d["array"][3].exists)
        assertEquals(0, d["array"][3].int)
    }

    @Test
    fun testDictionaryFragmentSetSwiftDict() {
        val data = mapOf(
            "name" to "Jason",
            "address" to mapOf(
                "street" to "1 Main street",
                "phones" to mapOf("mobile" to "650-123-4567")
            )
        )
        val doc = MutableDocument("doc1")
        doc["dict"].value = data

        val d = saveDocInCollection(doc)
        assertEquals("Jason", d["dict"]["name"].string)
        assertEquals("1 Main street", d["dict"]["address"]["street"].string)
        assertEquals("650-123-4567", d["dict"]["address"]["phones"]["mobile"].string)
    }

    @Test
    fun testDictionaryFragmentSetSwiftArray() {
        val doc = MutableDocument("doc1")
        doc["dict"].value = emptyMap<String, Any?>()
        doc["dict"]["array"].value = listOf(0, 1, 2)

        val d = saveDocInCollection(doc)
        assertEquals(0, d["dict"]["array"][0].int)
        assertEquals(1, d["dict"]["array"][1].int)
        assertEquals(2, d["dict"]["array"][2].int)
        assertNull(d["dict"]["array"][3].value)
        assertFalse(d["dict"]["array"][3].exists)
        assertEquals(0, d["dict"]["array"][3].int)
    }

    @Test
    fun testNonDictionaryFragmentsetValue() {
        val doc = MutableDocument("doc1")
        doc.setValue("string1", "value1")
        doc.setValue("string2", "value2")

        val d = saveDocInCollection(doc)
        val mDoc = d.toMutable()
        mDoc["string1"].value = 10
        assertIntEquals(10, mDoc["string1"].value)
        assertEquals("value2", mDoc["string2"].value)
    }

    @Test
    fun testArrayFragmentsetData() {
        val data = mapOf(
            "name" to "Jason",
            "address" to mapOf(
                "street" to "1 Main street",
                "phones" to mapOf("mobile" to "650-123-4567")
            )
        )

        val doc = MutableDocument("doc1")
        doc["array"].value = emptyList<Any?>()

        val dict = MutableDictionary(data)
        doc["array"].array!!.addValue(dict)

        val d = saveDocInCollection(doc)
        assertNotNull(d["array"][0])
        assertTrue(d["array"][0].exists)
        assertNotNull(d["array"][1])
        assertFalse(d["array"][1].exists)

        assertEquals("Jason", d["array"][0]["name"].string)
        assertEquals("1 Main street", d["array"][0]["address"]["street"].string)
        assertEquals("650-123-4567", d["array"][0]["address"]["phones"]["mobile"].string)
    }

    @Test
    fun testArrayFragmentSetSwiftDictionary() {
        val data = mapOf(
            "name" to "Jason",
            "address" to mapOf(
                "street" to "1 Main street",
                "phones" to mapOf("mobile" to "650-123-4567")
            )
        )

        val doc = MutableDocument("doc1")
        doc["array"].value = emptyList<Any?>()
        doc["array"].array!!.addValue(data)

        val d = saveDocInCollection(doc)
        assertNotNull(d["array"][0])
        assertTrue(d["array"][0].exists)
        assertNotNull(d["array"][1])
        assertFalse(d["array"][1].exists)

        assertEquals("Jason", d["array"][0]["name"].string)
        assertEquals("1 Main street", d["array"][0]["address"]["street"].string)
        assertEquals("650-123-4567", d["array"][0]["address"]["phones"]["mobile"].string)
    }

    @Test
    fun testArrayFragmentSetArrayObject() {
        val doc = MutableDocument("doc1")
        doc["array"].value = emptyList<Any?>()
        val array = MutableArray()
        array.addValue("Jason")
        array.addValue(5.5)
        array.addValue(true)
        doc["array"].array!!.addValue(array)

        val d = saveDocInCollection(doc)
        assertEquals("Jason", d["array"][0][0].string)
        assertEquals(5.5F, d["array"][0][1].float)
        assertEquals(true, d["array"][0][2].boolean)
    }

    @Test
    fun testArrayFragmentSetArray() {
        val doc = MutableDocument("doc1")
        doc["array"].value = emptyList<Any?>()
        doc["array"].array!!.addValue(listOf("Jason", 5.5, true))

        val d = saveDocInCollection(doc)
        assertEquals("Jason", d["array"][0][0].string)
        assertEquals(5.5F, d["array"][0][1].float)
        assertEquals(true, d["array"][0][2].boolean)
    }

    @Test
    fun testNonExistingArrayFragmentsetValue() {
        val doc = MutableDocument("doc1")
        doc["array"][0][0].value = 1
        doc["array"][0][1].value = false
        doc["array"][0][2].value = "hello"

        val d = saveDocInCollection(doc)
        assertNull(d["array"][0][0].value)
        assertNull(d["array"][0][1].value)
        assertNull(d["array"][0][2].value)
        assertEquals(emptyMap(), d.toMap())
    }

    @Test
    fun testOutOfRangeArrayFragmentsetValue() {
        val doc = MutableDocument("doc1")
        doc["array"].value = emptyList<Any?>()
        doc["array"].array?.addValue(listOf("Jason", 5.5, true))
        doc["array"][0][3].value = 1

        val d = saveDocInCollection(doc)
        assertNotNull(d["array"][0][3])
        assertFalse(d["array"][0][3].exists)
    }
}
