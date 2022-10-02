package com.couchbase.lite.kmp

import com.couchbase.lite.content
import com.couchbase.lite.copy
import com.couchbase.lite.exists
import com.couchbase.lite.kmp.BaseDbTest.DocValidator
import com.couchbase.lite.kmp.internal.utils.StringUtils
import com.couchbase.lite.kmp.internal.utils.TestUtils.assertThrowsCBL
import com.couchbase.lite.saveBlob
import com.udobny.kmp.ext.nowMillis
import com.udobny.kmp.ext.toStringMillis
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import okio.Buffer
import okio.buffer
import okio.use
import kotlin.math.absoluteValue
import kotlin.test.*
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

class DocumentTest : BaseDbTest() {

    @Test
    fun testCreateDoc() {
        val doc1a = MutableDocument()
        assertNotNull(doc1a)
        assertTrue(doc1a.id.isNotEmpty())
        assertEquals(emptyMap(), doc1a.toMap())

        val doc1b = saveDocInBaseTestDb(doc1a)
        assertNotNull(doc1b)
        assertNotSame(doc1a, doc1b)
        assertTrue(doc1b.exists())
        assertEquals(doc1a.id, doc1b.id)
    }

    @Test
    fun testCreateDocWithID() {
        val doc1a = MutableDocument("doc1")
        assertNotNull(doc1a)
        assertEquals("doc1", doc1a.id)
        assertEquals(emptyMap(), doc1a.toMap())

        val doc1b = saveDocInBaseTestDb(doc1a)
        assertNotNull(doc1b)
        assertNotSame(doc1a, doc1b)
        assertTrue(doc1b.exists())
        assertEquals(doc1a.id, doc1b.id)
    }

    @Test
    fun testCreateDocWithEmptyStringID() {
        val doc1a = MutableDocument("")
        assertNotNull(doc1a)
        assertThrowsCBL(CBLError.Domain.CBLITE, CBLError.Code.BAD_DOC_ID) {
            saveDocInBaseTestDb(doc1a)
        }
    }

    @Test
    fun testCreateDocWithNilID() {
        val doc1a = MutableDocument(null as String?)
        assertNotNull(doc1a)
        assertTrue(doc1a.id.isNotEmpty())
        assertEquals(emptyMap(), doc1a.toMap())

        val doc1b = saveDocInBaseTestDb(doc1a)
        assertNotNull(doc1b)
        assertNotSame(doc1a, doc1b)
        assertTrue(doc1b.exists())
        assertEquals(doc1a.id, doc1b.id)
    }

    @Test
    fun testCreateDocWithDict() {
        val dict = mutableMapOf<String, Any?>()
        dict["name"] = "Scott Tiger"
        dict["age"] = 30L

        val address = mapOf(
            "street" to "1 Main street",
            "city" to "Mountain View",
            "state" to "CA"
        )
        dict["address"] = address

        dict["phones"] = listOf("650-123-0001", "650-123-0002")

        val doc1a = MutableDocument(dict)
        assertNotNull(doc1a)
        assertTrue(doc1a.id.isNotEmpty())
        assertEquals(dict, doc1a.toMap())

        val doc1b = saveDocInBaseTestDb(doc1a)
        assertNotNull(doc1b)
        assertNotSame(doc1a, doc1b)
        assertTrue(doc1b.exists())
        assertEquals(doc1a.id, doc1b.id)
        assertEquals(dict, doc1b.toMap())
    }

    @Test
    fun testCreateDocWithIDAndDict() {
        val dict = mutableMapOf<String, Any?>()
        dict["name"] = "Scott Tiger"
        dict["age"] = 30L

        val address = mapOf(
            "street" to "1 Main street",
            "city" to "Mountain View",
            "state" to "CA"
        )
        dict["address"] = address

        dict["phones"] = listOf("650-123-0001", "650-123-0002")

        val doc1a = MutableDocument("doc1", dict)
        assertNotNull(doc1a)
        assertEquals("doc1", doc1a.id)
        assertEquals(dict, doc1a.toMap())

        val doc1b = saveDocInBaseTestDb(doc1a)
        assertNotNull(doc1b)
        assertNotSame(doc1a, doc1b)
        assertTrue(doc1b.exists())
        assertEquals(doc1a.id, doc1b.id)
        assertEquals(dict, doc1b.toMap())
    }

    @Test
    fun testSetDictionaryContent() {
        val dict = mutableMapOf<String, Any?>()
        dict["name"] = "Scott Tiger"
        dict["age"] = 30L

        val address = mapOf(
            "street" to "1 Main street",
            "city" to "Mountain View",
            "state" to "CA"
        )
        dict["address"] = address

        dict["phones"] = listOf("650-123-0001", "650-123-0002")

        var doc = MutableDocument("doc1")
        doc.setData(dict)
        assertEquals(dict, doc.toMap())

        var savedDoc = saveDocInBaseTestDb(doc)
        assertEquals(dict, savedDoc.toMap())

        val nuDict = mutableMapOf<String, Any?>()
        nuDict["name"] = "Danial Tiger"
        nuDict["age"] = 32L

        val nuAddress = mapOf(
            "street" to "2 Main street",
            "city" to "Palo Alto",
            "state" to "CA"
        )
        nuDict["address"] = nuAddress

        nuDict["phones"] = listOf("650-234-0001", "650-234-0002")

        doc = savedDoc.toMutable()
        doc.setData(nuDict)
        assertEquals(nuDict, doc.toMap())

        savedDoc = saveDocInBaseTestDb(doc)
        assertEquals(nuDict, savedDoc.toMap())
    }

    @Test
    fun testMutateEmptyDocument() {
        var doc = MutableDocument("doc")
        baseTestDb.save(doc)

        doc = baseTestDb.getDocument("doc")!!.toMutable()
        doc.setString("foo", "bar")
        baseTestDb.save(doc)
    }

    @Test
    fun testGetValueFromDocument() {
        val doc = MutableDocument("doc1")
        saveDocInBaseTestDb(doc) { d ->
            assertEquals(0, d.getInt("key"))
            assertEquals(0.0f, d.getFloat("key"), 0.0f)
            assertEquals(0.0, d.getDouble("key"), 0.0)
            assertFalse(d.getBoolean("key"))
            assertNull(d.getBlob("key"))
            assertNull(d.getDate("key"))
            assertNull(d.getNumber("key"))
            assertNull(d.getValue("key"))
            assertNull(d.getString("key"))
            assertNull(d.getArray("key"))
            assertNull(d.getDictionary("key"))
            assertEquals(emptyMap(), d.toMap())
        }
    }

    @Test
    fun testSaveThenGetFromAnotherDB() {
        val doc1a = MutableDocument("doc1")
        doc1a.setValue("name", "Scott Tiger")
        saveDocInBaseTestDb(doc1a)

        val anotherDb: Database = baseTestDb.copy()
        val doc1b = anotherDb.getDocument("doc1")
        assertNotSame(doc1a, doc1b)
        assertEquals(doc1a.id, doc1b!!.id)
        assertEquals(doc1a.toMap(), doc1b.toMap())
        anotherDb.close()
    }

    @Test
    fun testNoCacheNoLive() {
        val doc1a = MutableDocument("doc1")
        doc1a.setValue("name", "Scott Tiger")

        saveDocInBaseTestDb(doc1a)

        var doc1b = baseTestDb.getDocument("doc1")!!
        val doc1c = baseTestDb.getDocument("doc1")!!

        val anotherDb = baseTestDb.copy()
        val doc1d = anotherDb.getDocument("doc1")!!

        assertNotSame(doc1a, doc1b)
        assertNotSame(doc1a, doc1c)
        assertNotSame(doc1a, doc1d)
        assertNotSame(doc1b, doc1c)
        assertNotSame(doc1b, doc1d)
        assertNotSame(doc1c, doc1d)

        assertEquals(doc1a.toMap(), doc1b.toMap())
        assertEquals(doc1a.toMap(), doc1c.toMap())
        assertEquals(doc1a.toMap(), doc1d.toMap())

        val mDoc1b = doc1b.toMutable()
        mDoc1b.setValue("name", "Daniel Tiger")
        doc1b = saveDocInBaseTestDb(mDoc1b)

        assertNotEquals(doc1b.toMap(), doc1a.toMap())
        assertNotEquals(doc1b.toMap(), doc1c.toMap())
        assertNotEquals(doc1b.toMap(), doc1d.toMap())

        anotherDb.close()
    }

    @Test
    fun testSetString() {
        val validator4Save = DocValidator { d ->
            assertEquals("", d.getValue("string1"))
            assertEquals("string", d.getValue("string2"))
        }

        val validator4SUpdate = DocValidator { d ->
            assertEquals("string", d.getValue("string1"))
            assertEquals("", d.getValue("string2"))
        }

        // -- setValue
        // save
        var mDoc = MutableDocument("doc1")
        mDoc.setValue("string1", "")
        mDoc.setValue("string2", "string")
        val doc = saveDocInBaseTestDb(mDoc, validator4Save)

        // update
        mDoc = doc.toMutable()
        mDoc.setValue("string1", "string")
        mDoc.setValue("string2", "")
        saveDocInBaseTestDb(mDoc, validator4SUpdate)

        // -- setString
        // save
        var mDoc2 = MutableDocument("doc2")
        mDoc2.setString("string1", "")
        mDoc2.setString("string2", "string")
        val doc2 = saveDocInBaseTestDb(mDoc2, validator4Save)

        // update
        mDoc2 = doc2.toMutable()
        mDoc2.setString("string1", "string")
        mDoc2.setString("string2", "")
        saveDocInBaseTestDb(mDoc2, validator4SUpdate)
    }

    @Test
    fun testGetString() {
        for (i in 1..2) {
            val docID = "doc$i"
            val doc = MutableDocument(docID)
            if (i % 2 == 1) {
                populateData(doc)
            } else {
                populateDataByTypedSetter(doc)
            }
            saveDocInBaseTestDb(doc) { d ->
                assertNull(d.getString("null"))
                assertNull(d.getString("true"))
                assertNull(d.getString("false"))
                assertEquals("string", d.getString("string"))
                assertNull(d.getString("zero"))
                assertNull(d.getString("one"))
                assertNull(d.getString("minus_one"))
                assertNull(d.getString("one_dot_one"))
                assertEquals(TEST_DATE, d.getString("date"))
                assertNull(d.getString("dict"))
                assertNull(d.getString("array"))
                assertNull(d.getString("blob"))
                assertNull(d.getString("non_existing_key"))
            }
        }
    }

    @Test
    fun testSetNumber() {
        val validator4Save = DocValidator { d ->
            assertEquals(1, (d.getValue("number1") as Number).toInt())
            assertEquals(0, (d.getValue("number2") as Number).toInt())
            assertEquals(-1, (d.getValue("number3") as Number).toInt())
            assertEquals(-10, (d.getValue("number4") as Number).toInt())
        }

        val validator4SUpdate = DocValidator { d ->
            assertEquals(0, (d.getValue("number1") as Number).toInt())
            assertEquals(1, (d.getValue("number2") as Number).toInt())
            assertEquals(-10, (d.getValue("number3") as Number).toInt())
            assertEquals(-1, (d.getValue("number4") as Number).toInt())
        }

        // -- setValue
        var mDoc = MutableDocument("doc1")
        mDoc.setValue("number1", 1)
        mDoc.setValue("number2", 0)
        mDoc.setValue("number3", -1)
        mDoc.setValue("number4", -10)
        val doc = saveDocInBaseTestDb(mDoc, validator4Save)

        // Update:
        mDoc = doc.toMutable()
        mDoc.setValue("number1", 0)
        mDoc.setValue("number2", 1)
        mDoc.setValue("number3", -10)
        mDoc.setValue("number4", -1)
        saveDocInBaseTestDb(mDoc, validator4SUpdate)

        // -- setNumber
        // save
        var mDoc2 = MutableDocument("doc2")
        mDoc2.setNumber("number1", 1)
        mDoc2.setNumber("number2", 0)
        mDoc2.setNumber("number3", -1)
        mDoc2.setNumber("number4", -10)
        val doc2 = saveDocInBaseTestDb(mDoc2, validator4Save)

        // Update:
        mDoc2 = doc2.toMutable()
        mDoc2.setNumber("number1", 0)
        mDoc2.setNumber("number2", 1)
        mDoc2.setNumber("number3", -10)
        mDoc2.setNumber("number4", -1)
        saveDocInBaseTestDb(mDoc2, validator4SUpdate)

        // -- setInt
        // save
        var mDoc3 = MutableDocument("doc3")
        mDoc3.setInt("number1", 1)
        mDoc3.setInt("number2", 0)
        mDoc3.setInt("number3", -1)
        mDoc3.setInt("number4", -10)
        val doc3 = saveDocInBaseTestDb(mDoc3, validator4Save)

        // Update:
        mDoc3 = doc3.toMutable()
        mDoc3.setInt("number1", 0)
        mDoc3.setInt("number2", 1)
        mDoc3.setInt("number3", -10)
        mDoc3.setInt("number4", -1)
        saveDocInBaseTestDb(mDoc3, validator4SUpdate)

        // -- setLong
        // save
        var mDoc4 = MutableDocument("doc4")
        mDoc4.setLong("number1", 1)
        mDoc4.setLong("number2", 0)
        mDoc4.setLong("number3", -1)
        mDoc4.setLong("number4", -10)
        val doc4 = saveDocInBaseTestDb(mDoc4, validator4Save)

        // Update:
        mDoc4 = doc4.toMutable()
        mDoc4.setLong("number1", 0)
        mDoc4.setLong("number2", 1)
        mDoc4.setLong("number3", -10)
        mDoc4.setLong("number4", -1)
        saveDocInBaseTestDb(mDoc4, validator4SUpdate)

        // -- setFloat
        // save
        var mDoc5 = MutableDocument("doc5")
        mDoc5.setFloat("number1", 1f)
        mDoc5.setFloat("number2", 0f)
        mDoc5.setFloat("number3", -1f)
        mDoc5.setFloat("number4", -10f)
        val doc5 = saveDocInBaseTestDb(mDoc5, validator4Save)

        // Update:
        mDoc5 = doc5.toMutable()
        mDoc5.setFloat("number1", 0f)
        mDoc5.setFloat("number2", 1f)
        mDoc5.setFloat("number3", -10f)
        mDoc5.setFloat("number4", -1f)
        saveDocInBaseTestDb(mDoc5, validator4SUpdate)

        // -- setDouble
        // save
        var mDoc6 = MutableDocument("doc6")
        mDoc6.setDouble("number1", 1.0)
        mDoc6.setDouble("number2", 0.0)
        mDoc6.setDouble("number3", -1.0)
        mDoc6.setDouble("number4", -10.0)
        val doc6 = saveDocInBaseTestDb(mDoc6, validator4Save)

        // Update:
        mDoc6 = doc6.toMutable()
        mDoc6.setDouble("number1", 0.0)
        mDoc6.setDouble("number2", 1.0)
        mDoc6.setDouble("number3", -10.0)
        mDoc6.setDouble("number4", -1.0)
        saveDocInBaseTestDb(mDoc6, validator4SUpdate)
    }

    @Test
    fun testGetNumber() {
        for (i in 1..2) {
            val docID = "doc$i"
            val doc = MutableDocument(docID)
            if (i % 2 == 1) {
                populateData(doc)
            } else {
                populateDataByTypedSetter(doc)
            }
            saveDocInBaseTestDb(doc) { d ->
                assertNull(d.getNumber("null"))
                assertEquals(1, d.getNumber("true")!!.toInt())
                assertEquals(0, d.getNumber("false")!!.toInt())
                assertNull(d.getNumber("string"))
                assertEquals(0, d.getNumber("zero")!!.toInt())
                assertEquals(1, d.getNumber("one")!!.toInt())
                assertEquals(-1, d.getNumber("minus_one")!!.toInt())
                assertEquals(1.1, d.getNumber("one_dot_one"))
                assertNull(d.getNumber("date"))
                assertNull(d.getNumber("dict"))
                assertNull(d.getNumber("array"))
                assertNull(d.getNumber("blob"))
                assertNull(d.getNumber("non_existing_key"))
            }
        }
    }

    @Test
    fun testGetInteger() {
        for (i in 1..2) {
            val docID = "doc$i"
            val doc = MutableDocument(docID)
            if (i % 2 == 1) {
                populateData(doc)
            } else {
                populateDataByTypedSetter(doc)
            }
            saveDocInBaseTestDb(doc) { d ->
                assertEquals(0, d.getInt("null"))
                assertEquals(1, d.getInt("true"))
                assertEquals(0, d.getInt("false"))
                assertEquals(0, d.getInt("string"))
                assertEquals(0, d.getInt("zero"))
                assertEquals(1, d.getInt("one"))
                assertEquals(-1, d.getInt("minus_one"))
                assertEquals(1, d.getInt("one_dot_one"))
                assertEquals(0, d.getInt("date"))
                assertEquals(0, d.getInt("dict"))
                assertEquals(0, d.getInt("array"))
                assertEquals(0, d.getInt("blob"))
                assertEquals(0, d.getInt("non_existing_key"))
            }
        }
    }

    @Test
    fun testGetLong() {
        for (i in 1..2) {
            val docID = "doc$i"
            val doc = MutableDocument(docID)
            if (i % 2 == 1) {
                populateData(doc)
            } else {
                populateDataByTypedSetter(doc)
            }
            saveDocInBaseTestDb(doc) { d ->
                assertEquals(0, d.getLong("null"))
                assertEquals(1, d.getLong("true"))
                assertEquals(0, d.getLong("false"))
                assertEquals(0, d.getLong("string"))
                assertEquals(0, d.getLong("zero"))
                assertEquals(1, d.getLong("one"))
                assertEquals(-1, d.getLong("minus_one"))
                assertEquals(1, d.getLong("one_dot_one"))
                assertEquals(0, d.getLong("date"))
                assertEquals(0, d.getLong("dict"))
                assertEquals(0, d.getLong("array"))
                assertEquals(0, d.getLong("blob"))
                assertEquals(0, d.getLong("non_existing_key"))
            }
        }
    }

    @Test
    fun testGetFloat() {
        for (i in 1..2) {
            val docID = "doc$i"
            val doc = MutableDocument(docID)
            if (i % 2 == 1) {
                populateData(doc)
            } else {
                populateDataByTypedSetter(doc)
            }
            saveDocInBaseTestDb(doc) { d ->
                assertEquals(0.0f, d.getFloat("null"), 0.0f)
                assertEquals(1.0f, d.getFloat("true"), 0.0f)
                assertEquals(0.0f, d.getFloat("false"), 0.0f)
                assertEquals(0.0f, d.getFloat("string"), 0.0f)
                assertEquals(0.0f, d.getFloat("zero"), 0.0f)
                assertEquals(1.0f, d.getFloat("one"), 0.0f)
                assertEquals(-1.0f, d.getFloat("minus_one"), 0.0f)
                assertEquals(1.1f, d.getFloat("one_dot_one"), 0.0f)
                assertEquals(0.0f, d.getFloat("date"), 0.0f)
                assertEquals(0.0f, d.getFloat("dict"), 0.0f)
                assertEquals(0.0f, d.getFloat("array"), 0.0f)
                assertEquals(0.0f, d.getFloat("blob"), 0.0f)
                assertEquals(0.0f, d.getFloat("non_existing_key"), 0.0f)
            }
        }
    }

    @Test
    fun testGetDouble() {
        for (i in 1..2) {
            val docID = "doc$i"
            val doc = MutableDocument(docID)
            if (i % 2 == 1) {
                populateData(doc)
            } else {
                populateDataByTypedSetter(doc)
            }
            saveDocInBaseTestDb(doc) { d ->
                assertEquals(0.0, d.getDouble("null"), 0.0)
                assertEquals(1.0, d.getDouble("true"), 0.0)
                assertEquals(0.0, d.getDouble("false"), 0.0)
                assertEquals(0.0, d.getDouble("string"), 0.0)
                assertEquals(0.0, d.getDouble("zero"), 0.0)
                assertEquals(1.0, d.getDouble("one"), 0.0)
                assertEquals(-1.0, d.getDouble("minus_one"), 0.0)
                assertEquals(1.1, d.getDouble("one_dot_one"), 0.0)
                assertEquals(0.0, d.getDouble("date"), 0.0)
                assertEquals(0.0, d.getDouble("dict"), 0.0)
                assertEquals(0.0, d.getDouble("array"), 0.0)
                assertEquals(0.0, d.getDouble("blob"), 0.0)
                assertEquals(0.0, d.getDouble("non_existing_key"), 0.0)
            }
        }
    }

    @Test
    fun testSetGetMinMaxNumbers() {
        val validator = DocValidator { doc ->
            assertEquals(Int.MIN_VALUE, doc.getNumber("min_int")!!.toInt())
            assertEquals(Int.MAX_VALUE, doc.getNumber("max_int")!!.toInt())
            assertEquals(Int.MIN_VALUE, (doc.getValue("min_int") as Number).toInt())
            assertEquals(Int.MAX_VALUE, (doc.getValue("max_int") as Number).toInt())
            assertEquals(Int.MIN_VALUE, doc.getInt("min_int"))
            assertEquals(Int.MAX_VALUE, doc.getInt("max_int"))

            assertEquals(Long.MIN_VALUE, doc.getNumber("min_long"))
            assertEquals(Long.MAX_VALUE, doc.getNumber("max_long"))
            assertEquals(Long.MIN_VALUE, doc.getValue("min_long"))
            assertEquals(Long.MAX_VALUE, doc.getValue("max_long"))
            assertEquals(Long.MIN_VALUE, doc.getLong("min_long"))
            assertEquals(Long.MAX_VALUE, doc.getLong("max_long"))

            assertEquals(Float.MIN_VALUE, doc.getNumber("min_float"))
            assertEquals(Float.MAX_VALUE, doc.getNumber("max_float"))
            assertEquals(Float.MIN_VALUE, doc.getValue("min_float"))
            assertEquals(Float.MAX_VALUE, doc.getValue("max_float"))
            assertEquals(Float.MIN_VALUE, doc.getFloat("min_float"), 0.0f)
            assertEquals(Float.MAX_VALUE, doc.getFloat("max_float"), 0.0f)

            assertEquals(Double.MIN_VALUE, doc.getNumber("min_double"))
            assertEquals(Double.MAX_VALUE, doc.getNumber("max_double"))
            assertEquals(Double.MIN_VALUE, doc.getValue("min_double"))
            assertEquals(Double.MAX_VALUE, doc.getValue("max_double"))
            assertEquals(Double.MIN_VALUE, doc.getDouble("min_double"), 0.0)
            assertEquals(Double.MAX_VALUE, doc.getDouble("max_double"), 0.0)
        }

        // -- setValue
        val doc = MutableDocument("doc1")
        doc.setValue("min_int", Int.MIN_VALUE)
        doc.setValue("max_int", Int.MAX_VALUE)
        doc.setValue("min_long", Long.MIN_VALUE)
        doc.setValue("max_long", Long.MAX_VALUE)
        doc.setValue("min_float", Float.MIN_VALUE)
        doc.setValue("max_float", Float.MAX_VALUE)
        doc.setValue("min_double", Double.MIN_VALUE)
        doc.setValue("max_double", Double.MAX_VALUE)
        saveDocInBaseTestDb(doc, validator)

        // -- setInt, setLong, setFloat, setDouble
        val doc2 = MutableDocument("doc2")
        doc2.setInt("min_int", Int.MIN_VALUE)
        doc2.setInt("max_int", Int.MAX_VALUE)
        doc2.setLong("min_long", Long.MIN_VALUE)
        doc2.setLong("max_long", Long.MAX_VALUE)
        doc2.setFloat("min_float", Float.MIN_VALUE)
        doc2.setFloat("max_float", Float.MAX_VALUE)
        doc2.setDouble("min_double", Double.MIN_VALUE)
        doc2.setDouble("max_double", Double.MAX_VALUE)
        saveDocInBaseTestDb(doc2, validator)
    }

    @Test
    fun testSetGetFloatNumbers() {
        val validator = DocValidator { doc ->
            assertEquals(1.00, (doc.getValue("number1") as Number).toDouble(), 0.00001)
            assertEquals(1.00, doc.getNumber("number1")!!.toDouble(), 0.00001)
            assertEquals(1, doc.getInt("number1"))
            assertEquals(1L, doc.getLong("number1"))
            assertEquals(1.00f, doc.getFloat("number1"), 0.00001f)
            assertEquals(1.00, doc.getDouble("number1"), 0.00001)

            assertEquals(1.49, (doc.getValue("number2") as Number).toDouble(), 0.00001)
            assertEquals(1.49, doc.getNumber("number2")!!.toDouble(), 0.00001)
            assertEquals(1, doc.getInt("number2"))
            assertEquals(1L, doc.getLong("number2"))
            assertEquals(1.49f, doc.getFloat("number2"), 0.00001f)
            assertEquals(1.49, doc.getDouble("number2"), 0.00001)

            assertEquals(1.50, (doc.getValue("number3") as Number).toDouble(), 0.00001)
            assertEquals(1.50, doc.getNumber("number3")!!.toDouble(), 0.00001)
            assertEquals(1, doc.getInt("number3"))
            assertEquals(1L, doc.getLong("number3"))
            assertEquals(1.50f, doc.getFloat("number3"), 0.00001f)
            assertEquals(1.50, doc.getDouble("number3"), 0.00001)

            assertEquals(1.51, (doc.getValue("number4") as Number).toDouble(), 0.00001)
            assertEquals(1.51, doc.getNumber("number4")!!.toDouble(), 0.00001)
            assertEquals(1, doc.getInt("number4"))
            assertEquals(1L, doc.getLong("number4"))
            assertEquals(1.51f, doc.getFloat("number4"), 0.00001f)
            assertEquals(1.51, doc.getDouble("number4"), 0.00001)

            assertEquals(1.99, (doc.getValue("number5") as Number).toDouble(), 0.00001) // return 1
            assertEquals(1.99, doc.getNumber("number5")!!.toDouble(), 0.00001) // return 1
            assertEquals(1, doc.getInt("number5"))
            assertEquals(1L, doc.getLong("number5"))
            assertEquals(1.99f, doc.getFloat("number5"), 0.00001f)
            assertEquals(1.99, doc.getDouble("number5"), 0.00001)
        }

        // -- setValue
        val doc = MutableDocument("doc1")
        doc.setValue("number1", 1.00)
        doc.setValue("number2", 1.49)
        doc.setValue("number3", 1.50)
        doc.setValue("number4", 1.51)
        doc.setValue("number5", 1.99)
        saveDocInBaseTestDb(doc, validator)

        // -- setFloat
        val doc2 = MutableDocument("doc2")
        doc2.setFloat("number1", 1.00f)
        doc2.setFloat("number2", 1.49f)
        doc2.setFloat("number3", 1.50f)
        doc2.setFloat("number4", 1.51f)
        doc2.setFloat("number5", 1.99f)
        saveDocInBaseTestDb(doc2, validator)

        // -- setDouble
        val doc3 = MutableDocument("doc3")
        doc3.setDouble("number1", 1.00)
        doc3.setDouble("number2", 1.49)
        doc3.setDouble("number3", 1.50)
        doc3.setDouble("number4", 1.51)
        doc3.setDouble("number5", 1.99)
        saveDocInBaseTestDb(doc3, validator)
    }

    @Test
    fun testSetBoolean() {
        val validator4Save = DocValidator { d ->
            assertEquals(true, d.getValue("boolean1"))
            assertEquals(false, d.getValue("boolean2"))
            assertTrue(d.getBoolean("boolean1"))
            assertFalse(d.getBoolean("boolean2"))
        }
        val validator4Update = DocValidator { d ->
            assertEquals(false, d.getValue("boolean1"))
            assertEquals(true, d.getValue("boolean2"))
            assertFalse(d.getBoolean("boolean1"))
            assertTrue(d.getBoolean("boolean2"))
        }

        // -- setValue
        var mDoc = MutableDocument("doc1")
        mDoc.setValue("boolean1", true)
        mDoc.setValue("boolean2", false)
        val doc = saveDocInBaseTestDb(mDoc, validator4Save)

        // Update:
        mDoc = doc.toMutable()
        mDoc.setValue("boolean1", false)
        mDoc.setValue("boolean2", true)
        saveDocInBaseTestDb(mDoc, validator4Update)

        // -- setBoolean
        var mDoc2 = MutableDocument("doc2")
        mDoc2.setValue("boolean1", true)
        mDoc2.setValue("boolean2", false)
        val doc2 = saveDocInBaseTestDb(mDoc2, validator4Save)

        // Update:
        mDoc2 = doc2.toMutable()
        mDoc2.setValue("boolean1", false)
        mDoc2.setValue("boolean2", true)
        saveDocInBaseTestDb(mDoc2, validator4Update)
    }

    @Test
    fun testGetBoolean() {
        for (i in 1..2) {
            val docID = "doc$i"
            val doc = MutableDocument(docID)
            if (i % 2 == 1) {
                populateData(doc)
            } else {
                populateDataByTypedSetter(doc)
            }
            saveDocInBaseTestDb(doc) { d ->
                assertFalse(d.getBoolean("null"))
                assertTrue(d.getBoolean("true"))
                assertFalse(d.getBoolean("false"))
                assertTrue(d.getBoolean("string"))
                assertFalse(d.getBoolean("zero"))
                assertTrue(d.getBoolean("one"))
                assertTrue(d.getBoolean("minus_one"))
                assertTrue(d.getBoolean("one_dot_one"))
                assertTrue(d.getBoolean("date"))
                assertTrue(d.getBoolean("dict"))
                assertTrue(d.getBoolean("array"))
                assertTrue(d.getBoolean("blob"))
                assertFalse(d.getBoolean("non_existing_key"))
            }
        }
    }

    @Test
    fun testSetDate() {
        var mDoc = MutableDocument("doc1")

        val date = Clock.System.now()
        val dateStr = date.toStringMillis()
        assertTrue(dateStr.isNotEmpty())
        mDoc.setValue("date", date)

        val doc = saveDocInBaseTestDb(mDoc) { d ->
            assertEquals(dateStr, d.getValue("date"))
            assertEquals(dateStr, d.getString("date"))
            assertEquals(dateStr, d.getDate("date")!!.toStringMillis())
        }

        // Update:
        mDoc = doc.toMutable()
        val nuDate = date + 60.seconds
        val nuDateStr = nuDate.toStringMillis()
        mDoc.setValue("date", nuDate)

        saveDocInBaseTestDb(mDoc) { d ->
            assertEquals(nuDateStr, d.getValue("date"))
            assertEquals(nuDateStr, d.getString("date"))
            assertEquals(nuDateStr, d.getDate("date")!!.toStringMillis())
        }
    }

    @Test
    fun testGetDate() {
        for (i in 1..2) {
            val docID = "doc$i"
            val doc = MutableDocument(docID)
            if (i % 2 == 1) {
                populateData(doc)
            } else {
                populateDataByTypedSetter(doc)
            }
            saveDocInBaseTestDb(doc) { d ->
                assertNull(d.getDate("null"))
                assertNull(d.getDate("true"))
                assertNull(d.getDate("false"))
                assertNull(d.getDate("string"))
                assertNull(d.getDate("zero"))
                assertNull(d.getDate("one"))
                assertNull(d.getDate("minus_one"))
                assertNull(d.getDate("one_dot_one"))
                assertEquals(TEST_DATE, d.getDate("date")!!.toStringMillis())
                assertNull(d.getDate("dict"))
                assertNull(d.getDate("array"))
                assertNull(d.getDate("blob"))
                assertNull(d.getDate("non_existing_key"))
            }
        }
    }

    @Test
    fun testSetBlob() {
        val newBlobContent = StringUtils.randomString(100)
        val newBlob = Blob("text/plain", newBlobContent.encodeToByteArray())
        val blob = Blob("text/plain", BLOB_CONTENT.encodeToByteArray())

        val validator4Save = DocValidator { d ->
            assertEquals(
                blob.properties["length"],
                d.getBlob("blob")!!.properties["length"]
            )
            assertEquals(
                blob.properties["content-type"],
                d.getBlob("blob")!!.properties["content-type"]
            )
            assertEquals(
                blob.properties["digest"],
                d.getBlob("blob")!!.properties["digest"]
            )
            assertEquals(
                blob.properties["length"],
                (d.getValue("blob") as Blob).properties["length"]
            )
            assertEquals(
                blob.properties["content-type"],
                (d.getValue("blob") as Blob).properties["content-type"]
            )
            assertEquals(
                blob.properties["digest"],
                (d.getValue("blob") as Blob).properties["digest"]
            )
            assertEquals(BLOB_CONTENT, d.getBlob("blob")!!.content!!.decodeToString())
            assertContentEquals(
                BLOB_CONTENT.encodeToByteArray(),
                d.getBlob("blob")!!.content
            )
        }
        val validator4Update = DocValidator { d ->
            assertEquals(newBlob.properties["length"], d.getBlob("blob")!!.properties["length"])
            assertEquals(
                newBlob.properties["content-type"],
                d.getBlob("blob")!!.properties["content-type"]
            )
            assertEquals(newBlob.properties["digest"], d.getBlob("blob")!!.properties["digest"])
            assertEquals(
                newBlob.properties["length"],
                (d.getValue("blob") as Blob).properties["length"]
            )
            assertEquals(
                newBlob.properties["content-type"],
                (d.getValue("blob") as Blob).properties["content-type"]
            )
            assertEquals(
                newBlob.properties["digest"],
                (d.getValue("blob") as Blob).properties["digest"]
            )
            assertEquals(newBlobContent, d.getBlob("blob")!!.content!!.decodeToString())
            assertContentEquals(
                newBlobContent.encodeToByteArray(),
                d.getBlob("blob")!!.content
            )
        }

        // --setValue
        var mDoc = MutableDocument("doc1")
        mDoc.setValue("blob", blob)
        val doc = saveDocInBaseTestDb(mDoc, validator4Save)

        // Update:
        mDoc = doc.toMutable()
        mDoc.setValue("blob", newBlob)
        saveDocInBaseTestDb(mDoc, validator4Update)

        // --setBlob
        var mDoc2 = MutableDocument("doc2")
        mDoc2.setBlob("blob", blob)
        val doc2 = saveDocInBaseTestDb(mDoc2, validator4Save)

        // Update:
        mDoc2 = doc2.toMutable()
        mDoc2.setBlob("blob", newBlob)
        saveDocInBaseTestDb(mDoc2, validator4Update)
    }

    @Test
    fun testGetBlob() {
        for (i in 1..2) {
            val docID = "doc$i"
            val doc = MutableDocument(docID)
            if (i % 2 == 1) {
                populateData(doc)
            } else {
                populateDataByTypedSetter(doc)
            }
            saveDocInBaseTestDb(doc) { d ->
                assertNull(d.getBlob("null"))
                assertNull(d.getBlob("true"))
                assertNull(d.getBlob("false"))
                assertNull(d.getBlob("string"))
                assertNull(d.getBlob("zero"))
                assertNull(d.getBlob("one"))
                assertNull(d.getBlob("minus_one"))
                assertNull(d.getBlob("one_dot_one"))
                assertNull(d.getBlob("date"))
                assertNull(d.getBlob("dict"))
                assertNull(d.getBlob("array"))
                assertEquals(BLOB_CONTENT, d.getBlob("blob")!!.content!!.decodeToString())
                assertContentEquals(
                    BLOB_CONTENT.encodeToByteArray(),
                    d.getBlob("blob")!!.content
                )
                assertNull(d.getBlob("non_existing_key"))
            }
        }
    }

    @Test
    fun testSetDictionary() {
        for (i in 1..2) {
            val docID = "doc$i"
            // -- setValue
            var mDoc = MutableDocument(docID)
            var mDict = MutableDictionary()
            mDict.setValue("street", "1 Main street")
            if (i % 2 == 1) {
                mDoc.setValue("dict", mDict)
            } else {
                mDoc.setDictionary("dict", mDict)
            }
            assertEquals(mDict, mDoc.getValue("dict"))
            assertEquals(mDict.toMap(), (mDoc.getValue("dict") as MutableDictionary).toMap())

            var doc = saveDocInBaseTestDb(mDoc)

            assertNotSame(mDict, doc.getValue("dict"))
            assertEquals(doc.getValue("dict"), doc.getDictionary("dict"))

            var dict = doc.getValue("dict") as Dictionary
            dict = dict as? MutableDictionary ?: dict.toMutable()
            assertEquals(mDict.toMap(), dict.toMap())

            // Update:
            mDoc = doc.toMutable()
            mDict = mDoc.getDictionary("dict")!!
            mDict.setValue("city", "Mountain View")
            assertEquals(doc.getValue("dict"), doc.getDictionary("dict"))
            val map = mapOf(
                "street" to "1 Main street",
                "city" to "Mountain View"
            )
            assertEquals(map, mDoc.getDictionary("dict")!!.toMap())

            doc = saveDocInBaseTestDb(mDoc)

            assertNotSame(mDict, doc.getValue("dict"))
            assertEquals(doc.getValue("dict"), doc.getDictionary("dict"))
            assertEquals(map, doc.getDictionary("dict")!!.toMap())
        }
    }

    @Test
    fun testGetDictionary() {
        for (i in 1..2) {
            val docID = "doc$i"
            val doc = MutableDocument(docID)
            if (i % 2 == 1) {
                populateData(doc)
            } else {
                populateDataByTypedSetter(doc)
            }
            saveDocInBaseTestDb(doc) { d ->
                assertNull(d.getDictionary("null"))
                assertNull(d.getDictionary("true"))
                assertNull(d.getDictionary("false"))
                assertNull(d.getDictionary("string"))
                assertNull(d.getDictionary("zero"))
                assertNull(d.getDictionary("one"))
                assertNull(d.getDictionary("minus_one"))
                assertNull(d.getDictionary("one_dot_one"))
                assertNull(d.getDictionary("date"))
                assertNotNull(d.getDictionary("dict"))
                val dict = mapOf(
                    "street" to "1 Main street",
                    "city" to "Mountain View",
                    "state" to "CA"
                )
                assertEquals(dict, d.getDictionary("dict")!!.toMap())
                assertNull(d.getDictionary("array"))
                assertNull(d.getDictionary("blob"))
                assertNull(d.getDictionary("non_existing_key"))
            }
        }
    }

    @Test
    fun testSetArray() {
        for (i in 1..2) {
            val docID = "doc$i"
            var mDoc = MutableDocument(docID)
            var array = MutableArray()
            array.addValue("item1")
            array.addValue("item2")
            array.addValue("item3")
            if (i % 2 == 1) {
                mDoc.setValue("array", array)
            } else {
                mDoc.setArray("array", array)
            }
            assertEquals(array, mDoc.getValue("array"))
            assertEquals(
                array.toList(),
                (mDoc.getValue("array") as MutableArray).toList()
            )

            var doc = saveDocInBaseTestDb(mDoc)
            assertNotSame(array, doc.getValue("array"))
            assertEquals(doc.getValue("array"), doc.getArray("array"))

            var mArray = doc.getValue("array") as Array?
            mArray = mArray as? MutableArray ?: mArray!!.toMutable()
            assertEquals(array.toList(), mArray.toList())

            // Update:
            mDoc = doc.toMutable()
            array = mDoc.getArray("array")!!
            array.addValue("item4")
            array.addValue("item5")
            doc = saveDocInBaseTestDb(mDoc)
            assertNotSame(array, doc.getValue("array"))
            assertEquals(doc.getValue("array"), doc.getArray("array"))
            val list = listOf("item1", "item2", "item3", "item4", "item5")
            assertEquals(list, doc.getArray("array")!!.toList())
        }
    }

    @Test
    fun testGetArray() {
        for (i in 1..2) {
            val docID = "doc$i"
            val doc = MutableDocument(docID)
            if (i % 2 == 1) {
                populateData(doc)
            } else {
                populateDataByTypedSetter(doc)
            }
            saveDocInBaseTestDb(doc) { d ->
                assertNull(d.getArray("null"))
                assertNull(d.getArray("true"))
                assertNull(d.getArray("false"))
                assertNull(d.getArray("string"))
                assertNull(d.getArray("zero"))
                assertNull(d.getArray("one"))
                assertNull(d.getArray("minus_one"))
                assertNull(d.getArray("one_dot_one"))
                assertNull(d.getArray("date"))
                assertNull(d.getArray("dict"))
                assertNotNull(d.getArray("array"))
                val list = listOf("650-123-0001", "650-123-0002")
                assertEquals(list, d.getArray("array")!!.toList())
                assertNull(d.getArray("blob"))
                assertNull(d.getArray("non_existing_key"))
            }
        }
    }

    @Test
    fun testSetNull() {
        val mDoc = MutableDocument("doc1")
        mDoc.setValue("obj-null", null)
        mDoc.setString("string-null", null)
        mDoc.setNumber("number-null", null)
        mDoc.setDate("date-null", null)
        mDoc.setArray("array-null", null)
        mDoc.setDictionary("dict-null", null)
        // TODO: NOTE: Current implementation follows iOS way. So set null remove it!!
        saveDocInBaseTestDb(mDoc) { d ->
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
    fun testSetMap() {
        val dict = mapOf(
            "street" to "1 Main street",
            "city" to "Mountain View",
            "state" to "CA"
        )

        val doc = MutableDocument("doc1")
        doc.setValue("address", dict)

        val address = doc.getDictionary("address")
        assertNotNull(address)
        assertEquals(address, doc.getValue("address"))
        assertEquals("1 Main street", address.getString("street"))
        assertEquals("Mountain View", address.getString("city"))
        assertEquals("CA", address.getString("state"))
        assertEquals(dict, address.toMap())

        // Update with a new dictionary:
        val nuDict = mutableMapOf<String, Any?>()
        nuDict["street"] = "1 Second street"
        nuDict["city"] = "Palo Alto"
        nuDict["state"] = "CA"
        doc.setValue("address", nuDict)

        // Check whether the old address dictionary is still accessible:
        assertNotSame(address, doc.getDictionary("address"))
        assertEquals("1 Main street", address.getString("street"))
        assertEquals("Mountain View", address.getString("city"))
        assertEquals("CA", address.getString("state"))
        assertEquals(dict, address.toMap())

        // The old address dictionary should be detached:
        val nuAddress = doc.getDictionary("address")
        assertNotSame(address, nuAddress)

        // Update nuAddress:
        nuAddress!!.setValue("zip", "94302")
        assertEquals("94302", nuAddress.getString("zip"))
        assertNull(address.getString("zip"))

        // Save:
        val savedDoc = saveDocInBaseTestDb(doc)

        nuDict["zip"] = "94302"
        val expected = mapOf(
            "address" to nuDict
        )
        assertEquals(expected, savedDoc.toMap())
    }

    @Test
    fun testSetList() {
        val array = listOf("a", "b", "c")

        val doc = MutableDocument("doc1")
        doc.setValue("members", array)

        val members = doc.getArray("members")
        assertNotNull(members)
        assertEquals(members, doc.getValue("members"))

        assertEquals(3, members.count)
        assertEquals("a", members.getValue(0))
        assertEquals("b", members.getValue(1))
        assertEquals("c", members.getValue(2))
        assertEquals(array, members.toList())

        // Update with a new array:
        val nuArray = listOf("d", "e", "f")
        doc.setValue("members", nuArray)

        // Check whether the old members array is still accessible:
        assertEquals(3, members.count)
        assertEquals("a", members.getValue(0))
        assertEquals("b", members.getValue(1))
        assertEquals("c", members.getValue(2))
        assertEquals(array, members.toList())

        // The old members array should be detached:
        val nuMembers = doc.getArray("members")!!
        assertNotSame(members, nuMembers)

        // Update nuMembers:
        nuMembers.addValue("g")
        assertEquals(4, nuMembers.count)
        assertEquals("g", nuMembers.getValue(3))
        assertEquals(3, members.count)

        // Save
        val savedDoc = saveDocInBaseTestDb(doc)

        val expected = mapOf(
            "members" to listOf("d", "e", "f", "g")
        )
        assertEquals(expected, savedDoc.toMap())
    }

    @Test
    fun testUpdateNestedDictionary() {
        var doc = MutableDocument("doc1")
        val addresses = MutableDictionary()
        doc.setValue("addresses", addresses)

        var shipping = MutableDictionary()
        shipping.setValue("street", "1 Main street")
        shipping.setValue("city", "Mountain View")
        shipping.setValue("state", "CA")
        addresses.setValue("shipping", shipping)

        doc = saveDocInBaseTestDb(doc).toMutable()

        shipping = doc.getDictionary("addresses")!!.getDictionary("shipping")!!
        shipping.setValue("zip", "94042")

        doc = saveDocInBaseTestDb(doc).toMutable()

        val mapShipping = mapOf(
            "street" to "1 Main street",
            "city" to "Mountain View",
            "state" to "CA",
            "zip" to "94042"
        )
        val mapAddresses = mapOf(
            "shipping" to mapShipping
        )
        val expected = mapOf(
            "addresses" to mapAddresses
        )

        assertEquals(expected, doc.toMap())
    }

    @Test
    fun testUpdateDictionaryInArray() {
        var doc = MutableDocument("doc1")
        val addresses = MutableArray()
        doc.setValue("addresses", addresses)

        var address1 = MutableDictionary()
        address1.setValue("street", "1 Main street")
        address1.setValue("city", "Mountain View")
        address1.setValue("state", "CA")
        addresses.addValue(address1)

        var address2 = MutableDictionary()
        address2.setValue("street", "1 Second street")
        address2.setValue("city", "Palo Alto")
        address2.setValue("state", "CA")
        addresses.addValue(address2)

        doc = saveDocInBaseTestDb(doc).toMutable()

        address1 = doc.getArray("addresses")!!.getDictionary(0)!!
        address1.setValue("street", "2 Main street")
        address1.setValue("zip", "94042")
        address2 = doc.getArray("addresses")!!.getDictionary(1)!!
        address2.setValue("street", "2 Second street")
        address2.setValue("zip", "94302")

        doc = saveDocInBaseTestDb(doc).toMutable()

        val mapAddress1 = mapOf(
            "street" to "2 Main street",
            "city" to "Mountain View",
            "state" to "CA",
            "zip" to "94042"
        )

        val mapAddress2 = mapOf(
            "street" to "2 Second street",
            "city" to "Palo Alto",
            "state" to "CA",
            "zip" to "94302"
        )

        val expected = mapOf(
            "addresses" to listOf(mapAddress1, mapAddress2)
        )

        assertEquals(expected, doc.toMap())
    }

    @Test
    fun testUpdateNestedArray() {
        var doc = MutableDocument("doc1")
        val groups = MutableArray()
        doc.setValue("groups", groups)

        var group1 = MutableArray()
        group1.addValue("a")
        group1.addValue("b")
        group1.addValue("c")
        groups.addValue(group1)

        var group2 = MutableArray()
        group2.addValue(1)
        group2.addValue(2)
        group2.addValue(3)
        groups.addValue(group2)

        doc = saveDocInBaseTestDb(doc).toMutable()

        group1 = doc.getArray("groups")!!.getArray(0)!!
        group1.setValue(0, "d")
        group1.setValue(1, "e")
        group1.setValue(2, "f")

        group2 = doc.getArray("groups")!!.getArray(1)!!
        group2.setValue(0, 4)
        group2.setValue(1, 5)
        group2.setValue(2, 6)

        doc = saveDocInBaseTestDb(doc).toMutable()

        val expected = mapOf(
            "groups" to listOf(
                listOf("d", "e", "f"),
                listOf(4L, 5L, 6L)
            )
        )
        assertEquals(expected, doc.toMap())
    }

    @Test
    fun testUpdateArrayInDictionary() {
        var doc = MutableDocument("doc1")

        val group1 = MutableDictionary()
        var member1 = MutableArray()
        member1.addValue("a")
        member1.addValue("b")
        member1.addValue("c")
        group1.setValue("member", member1)
        doc.setValue("group1", group1)

        val group2 = MutableDictionary()
        var member2 = MutableArray()
        member2.addValue(1)
        member2.addValue(2)
        member2.addValue(3)
        group2.setValue("member", member2)
        doc.setValue("group2", group2)

        doc = saveDocInBaseTestDb(doc).toMutable()

        member1 = doc.getDictionary("group1")!!.getArray("member")!!
        member1.setValue(0, "d")
        member1.setValue(1, "e")
        member1.setValue(2, "f")

        member2 = doc.getDictionary("group2")!!.getArray("member")!!
        member2.setValue(0, 4)
        member2.setValue(1, 5)
        member2.setValue(2, 6)

        doc = saveDocInBaseTestDb(doc).toMutable()

        val expected = mutableMapOf<String, Any?>()
        val mapGroup1 = mapOf(
            "member" to listOf("d", "e", "f")
        )
        val mapGroup2 = mapOf(
            "member" to listOf(4L, 5L, 6L)
        )
        expected["group1"] = mapGroup1
        expected["group2"] = mapGroup2
        assertEquals(expected, doc.toMap())
    }

    @Test
    fun testSetDictionaryToMultipleKeys() {
        var doc = MutableDocument("doc1")

        val address = MutableDictionary()
        address.setValue("street", "1 Main street")
        address.setValue("city", "Mountain View")
        address.setValue("state", "CA")
        doc.setValue("shipping", address)
        doc.setValue("billing", address)

        // Update address: both shipping and billing should get the update.
        address.setValue("zip", "94042")
        assertEquals("94042", doc.getDictionary("shipping")!!.getString("zip"))
        assertEquals("94042", doc.getDictionary("billing")!!.getString("zip"))

        doc = saveDocInBaseTestDb(doc).toMutable()

        val shipping = doc.getDictionary("shipping")!!
        val billing = doc.getDictionary("billing")!!

        // After save: both shipping and billing address are now independent to each other
        assertNotSame(shipping, address)
        assertNotSame(billing, address)
        assertNotSame(shipping, billing)

        shipping.setValue("street", "2 Main street")
        billing.setValue("street", "3 Main street")

        // Save update:
        doc = saveDocInBaseTestDb(doc).toMutable()
        assertEquals(
            "2 Main street",
            doc.getDictionary("shipping")!!.getString("street")
        )
        assertEquals(
            "3 Main street",
            doc.getDictionary("billing")!!.getString("street")
        )
    }

    @Test
    fun testSetArrayToMultipleKeys() {
        var doc = MutableDocument("doc1")

        val phones = MutableArray()
        phones.addValue("650-000-0001")
        phones.addValue("650-000-0002")

        doc.setValue("mobile", phones)
        doc.setValue("home", phones)

        assertEquals(phones, doc.getValue("mobile"))
        assertEquals(phones, doc.getValue("home"))

        // Update phones: both mobile and home should get the update
        phones.addValue("650-000-0003")
        assertEquals(
            listOf("650-000-0001", "650-000-0002", "650-000-0003"),
            doc.getArray("mobile")!!.toList()
        )
        assertEquals(
            listOf("650-000-0001", "650-000-0002", "650-000-0003"),
            doc.getArray("home")!!.toList()
        )
        doc = saveDocInBaseTestDb(doc).toMutable()

        // After save: both mobile and home are not independent to each other
        val mobile = doc.getArray("mobile")!!
        val home = doc.getArray("home")!!
        assertNotSame(mobile, phones)
        assertNotSame(home, phones)
        assertNotSame(mobile, home)

        // Update mobile and home:
        mobile.addValue("650-000-1234")
        home.addValue("650-000-5678")

        // Save update:
        doc = saveDocInBaseTestDb(doc).toMutable()

        assertEquals(
            listOf("650-000-0001", "650-000-0002", "650-000-0003", "650-000-1234"),
            doc.getArray("mobile")!!.toList()
        )
        assertEquals(
            listOf("650-000-0001", "650-000-0002", "650-000-0003", "650-000-5678"),
            doc.getArray("home")!!.toList()
        )
    }

    @Test
    fun testToDictionary() {
        val doc1 = MutableDocument("doc1")
        populateData(doc1)

        val expected = mutableMapOf<String, Any?>(
            "true" to true,
            "false" to false,
            "string" to "string",
            "zero" to 0,
            "one" to 1,
            "minus_one" to -1,
            "one_dot_one" to 1.1,
            "date" to TEST_DATE, // expect the stringified date
            "null" to null
        )

        // Dictionary:
        val dict = mapOf(
            "street" to "1 Main street",
            "city" to "Mountain View",
            "state" to "CA"
        )
        expected["dict"] = dict

        // Array:
        val array = listOf(
            "650-123-0001",
            "650-123-0002"
        )
        expected["array"] = array

        // Blob:
        expected["blob"] = makeBlob()

        assertEquals(expected, doc1.toMap())
    }

    @Test
    fun testCount() {
        for (i in 1..2) {
            val docID = "doc$i"
            var doc = MutableDocument(docID)
            if (i % 2 == 1) {
                populateData(doc)
            } else {
                populateDataByTypedSetter(doc)
            }

            assertEquals(12, doc.count)
            assertEquals(12, doc.toMap().size)

            doc = saveDocInBaseTestDb(doc).toMutable()

            assertEquals(12, doc.count)
            assertEquals(12, doc.toMap().size)
        }
    }

    @Test
    fun testRemoveKeys() {
        val doc = MutableDocument("doc1")
        val mapAddress = mapOf(
            "street" to "1 milky way.",
            "city" to "galaxy city",
            "zip" to 12345
        )
        val profile = mapOf(
            "type" to "profile",
            "name" to "Jason",
            "weight" to 130.5,
            "active" to true,
            "age" to 30,
            "address" to mapAddress
        )
        doc.setData(profile)

        saveDocInBaseTestDb(doc)

        doc.remove("name")
        doc.remove("weight")
        doc.remove("age")
        doc.remove("active")
        doc.getDictionary("address")!!.remove("city")

        assertNull(doc.getString("name"))
        assertEquals(0.0f, doc.getFloat("weight"), 0.0f)
        assertEquals(0.0, doc.getDouble("weight"), 0.0)
        assertEquals(0, doc.getInt("age"))
        assertFalse(doc.getBoolean("active"))

        assertNull(doc.getValue("name"))
        assertNull(doc.getValue("weight"))
        assertNull(doc.getValue("age"))
        assertNull(doc.getValue("active"))
        assertNull(doc.getDictionary("address")!!.getValue("city"))

        val address = doc.getDictionary("address")
        val addr = mapOf(
            "street" to "1 milky way.",
            "zip" to 12345
        )
        assertEquals(addr, address!!.toMap())
        val expected = mapOf(
            "type" to "profile",
            "address" to addr
        )
        assertEquals(expected, doc.toMap())

        doc.remove("type")
        doc.remove("address")
        assertNull(doc.getValue("type"))
        assertNull(doc.getValue("address"))
        assertEquals(emptyMap(), doc.toMap())
    }

    @Test
    fun testRemoveKeysBySettingDictionary() {
        val props = mapOf(
            "PropName1" to "Val1",
            "PropName2" to 42
        )

        val mDoc = MutableDocument("docName", props)
        saveDocInBaseTestDb(mDoc)

        val newProps = mapOf(
            "PropName3" to "Val3",
            "PropName4" to 84
        )

        val existingDoc = baseTestDb.getDocument("docName")!!.toMutable()
        existingDoc.setData(newProps)
        saveDocInBaseTestDb(existingDoc)

        assertEquals(newProps, existingDoc.toMap())
    }

    @Test
    fun testContainsKey() {
        val doc = MutableDocument("doc1")
        val mapAddress = mapOf(
            "street" to "1 milky way."
        )
        val profile = mapOf(
            "type" to "profile",
            "name" to "Jason",
            "age" to 30,
            "address" to mapAddress
        )
        doc.setData(profile)

        assertTrue(doc.contains("type"))
        assertTrue(doc.contains("name"))
        assertTrue(doc.contains("age"))
        assertTrue(doc.contains("address"))
        assertFalse(doc.contains("weight"))
    }

    @Test
    fun testDeleteNewDocument() {
        val mDoc = MutableDocument("doc1")
        mDoc.setString("name", "Scott Tiger")

        assertThrowsCBL(CBLError.Domain.CBLITE, CBLError.Code.NOT_FOUND) {
            baseTestDb.delete(mDoc)
        }

        assertEquals("Scott Tiger", mDoc.getString("name"))
    }

    @Test
    fun testDeleteDocument() {
        val docID = "doc1"
        val mDoc = MutableDocument(docID)
        mDoc.setValue("name", "Scott Tiger")

        // Save:
        val doc = saveDocInBaseTestDb(mDoc)

        // Delete:
        baseTestDb.delete(doc)

        assertNull(baseTestDb.getDocument(docID))

        // NOTE: doc is reserved.
        val v = doc.getValue("name")
        assertEquals("Scott Tiger", v)
        val expected = mapOf(
            "name" to "Scott Tiger"
        )
        assertEquals(expected, doc.toMap())
    }

    @Test
    fun testDictionaryAfterDeleteDocument() {
        val addr = mapOf(
            "street" to "1 Main street",
            "city" to "Mountain View",
            "state" to "CA"
        )
        val dict = mapOf(
            "address" to addr
        )

        val mDoc = MutableDocument("doc1", dict)
        val doc = saveDocInBaseTestDb(mDoc)

        val address = doc.getDictionary("address")!!
        assertEquals("1 Main street", address.getValue("street"))
        assertEquals("Mountain View", address.getValue("city"))
        assertEquals("CA", address.getValue("state"))

        baseTestDb.delete(doc)

        // The dictionary still has data but is detached:
        assertEquals("1 Main street", address.getValue("street"))
        assertEquals("Mountain View", address.getValue("city"))
        assertEquals("CA", address.getValue("state"))
    }

    @Test
    fun testArrayAfterDeleteDocument() {
        val dict = mapOf(
            "members" to listOf("a", "b", "c")
        )

        val mDoc = MutableDocument("doc1", dict)
        val doc = saveDocInBaseTestDb(mDoc)

        val members = doc.getArray("members")!!
        assertEquals(3, members.count)
        assertEquals("a", members.getValue(0))
        assertEquals("b", members.getValue(1))
        assertEquals("c", members.getValue(2))

        baseTestDb.delete(doc)

        // The array still has data but is detached:
        assertEquals(3, members.count)
        assertEquals("a", members.getValue(0))
        assertEquals("b", members.getValue(1))
        assertEquals("c", members.getValue(2))
    }

    @Test
    fun testDocumentChangeOnDocumentPurged() = runBlocking {
        baseTestDb.save(MutableDocument("doc1").setValue("theanswer", 18))

        val mutex = Mutex(true)
        val token = baseTestDb.addDocumentChangeListener("doc1") { change ->
            try {
                assertNotNull(change)
                assertEquals("doc1", change.documentID)
            } finally {
                mutex.unlock()
            }
        }
        try {
            baseTestDb.setDocumentExpiration("doc1", Clock.System.now() + 100.milliseconds)
            withTimeout(STD_TIMEOUT_SEC.seconds) {
                mutex.lock()
            }
        } finally {
            // TODO: 3.1 API
            //token.remove()
            baseTestDb.removeChangeListener(token)
        }
    }

    @Test
    fun testPurgeDocument() {
        val docID = "doc1"
        val doc = MutableDocument(docID)
        doc.setValue("type", "profile")
        doc.setValue("name", "Scott")

        // Purge before save:
        assertThrowsCBL(CBLError.Domain.CBLITE, CBLError.Code.NOT_FOUND) {
            baseTestDb.purge(doc)
        }

        assertEquals("profile", doc.getValue("type"))
        assertEquals("Scott", doc.getValue("name"))

        //Save
        val savedDoc = saveDocInBaseTestDb(doc)

        // purge
        baseTestDb.purge(savedDoc)
        assertNull(baseTestDb.getDocument(docID))
    }

    @Test
    fun testPurgeDocumentById() {
        val docID = "doc1"
        val doc = MutableDocument(docID)
        doc.setValue("type", "profile")
        doc.setValue("name", "Scott")

        // Purge before save:
        assertThrowsCBL(CBLError.Domain.CBLITE, CBLError.Code.NOT_FOUND) {
            baseTestDb.purge(docID)
        }

        assertEquals("profile", doc.getValue("type"))
        assertEquals("Scott", doc.getValue("name"))

        //Save
        saveDocInBaseTestDb(doc)

        // purge
        baseTestDb.purge(docID)
        assertNull(baseTestDb.getDocument(docID))
    }

    @Test
    fun testSetAndGetExpirationFromDoc() {
        val dto30 = Clock.System.nowMillis() + 30.seconds

        val doc1a = MutableDocument("doc1")
        val doc1b = MutableDocument("doc2")
        val doc1c = MutableDocument("doc3")
        doc1a.setInt("answer", 12)
        doc1a.setValue("question", "What is six plus six?")
        saveDocInBaseTestDb(doc1a)

        doc1b.setInt("answer", 22)
        doc1b.setValue("question", "What is eleven plus eleven?")
        saveDocInBaseTestDb(doc1b)

        doc1c.setInt("answer", 32)
        doc1c.setValue("question", "What is twenty plus twelve?")
        saveDocInBaseTestDb(doc1c)

        baseTestDb.setDocumentExpiration("doc1", dto30)
        baseTestDb.setDocumentExpiration("doc3", dto30)

        baseTestDb.setDocumentExpiration("doc3", null)
        val exp = baseTestDb.getDocumentExpiration("doc1")
        assertEquals(exp, dto30)
        assertNull(baseTestDb.getDocumentExpiration("doc2"))
        assertNull(baseTestDb.getDocumentExpiration("doc3"))
    }

    @Test
    fun testSetExpirationOnDoc() {
        val now = Clock.System.now()

        val doc1 = MutableDocument("doc1")
        doc1.setInt("answer", 12)
        doc1.setValue("question", "What is six plus six?")
        saveDocInBaseTestDb(doc1)

        val doc2 = MutableDocument("doc2")
        doc2.setInt("answer", 12)
        doc2.setValue("question", "What is six plus six?")
        saveDocInBaseTestDb(doc2)

        baseTestDb.setDocumentExpiration("doc1", now + 100.milliseconds)
        baseTestDb.setDocumentExpiration("doc2", now + LONG_TIMEOUT_SEC.seconds)
        assertEquals(2, baseTestDb.count)

        waitUntil(1000L) { 1L == baseTestDb.count }
    }

    @Test
    fun testSetExpirationOnDeletedDoc() {
        val dto30 = Clock.System.now() + 30.seconds
        val doc1a = MutableDocument("deleted_doc")
        doc1a.setInt("answer", 12)
        doc1a.setValue("question", "What is six plus six?")
        saveDocInBaseTestDb(doc1a)
        baseTestDb.delete(doc1a)
        try {
            baseTestDb.setDocumentExpiration("deleted_doc", dto30)
        } catch (e: CouchbaseLiteException) {
            assertEquals(e.getCode(), CBLError.Code.NOT_FOUND)
        }
    }

    @Test
    fun testGetExpirationFromDeletedDoc() {
        val doc1a = MutableDocument("deleted_doc")
        doc1a.setInt("answer", 12)
        doc1a.setValue("question", "What is six plus six?")
        saveDocInBaseTestDb(doc1a)
        baseTestDb.delete(doc1a)
        try {
            baseTestDb.getDocumentExpiration("deleted_doc")
        } catch (e: CouchbaseLiteException) {
            assertEquals(e.getCode(), CBLError.Code.NOT_FOUND)
        }
    }

    @Test
    fun testSetExpirationOnNoneExistDoc() {
        val dto30 = Clock.System.now() + 30.seconds
        try {
            baseTestDb.setDocumentExpiration("not_exist", dto30)
        } catch (e: CouchbaseLiteException) {
            assertEquals(e.getCode(), CBLError.Code.NOT_FOUND)
        }
    }

    @Test
    fun testGetExpirationFromNoneExistDoc() {
        try {
            baseTestDb.getDocumentExpiration("not_exist")
        } catch (e: CouchbaseLiteException) {
            assertEquals(e.getCode(), CBLError.Code.NOT_FOUND)
        }
    }

    @Test
    fun testLongExpiration() {
        val now = Clock.System.now()
        val d60Days = now + 60.days

        val doc = MutableDocument("doc")
        doc.setInt("answer", 42)
        doc.setValue("question", "What is twenty-one times two?")
        saveDocInBaseTestDb(doc)

        assertNull(baseTestDb.getDocumentExpiration("doc"))
        baseTestDb.setDocumentExpiration("doc", d60Days)

        val exp = baseTestDb.getDocumentExpiration("doc")
        assertNotNull(exp)
        val diff = exp - now
        assertTrue((diff.toDouble(DurationUnit.DAYS) - 60.0).absoluteValue <= 1.0)
    }

    @Test
    fun testReopenDB() {
        val mDoc = MutableDocument("doc1")
        mDoc.setValue("string", "str")
        saveDocInBaseTestDb(mDoc)

        reopenBaseTestDb()

        val doc = baseTestDb.getDocument("doc1")!!
        assertEquals("str", doc.getString("string"))
        val expected = mapOf(
            "string" to "str"
        )
        assertEquals(expected, doc.toMap())
    }

    @Test
    fun testBlob() {
        val content: ByteArray = BLOB_CONTENT.encodeToByteArray()

        // store blob
        var data = Blob("text/plain", content)
        assertNotNull(data)

        var doc = MutableDocument("doc1")
        doc.setValue("name", "Jim")
        doc.setValue("data", data)

        doc = saveDocInBaseTestDb(doc).toMutable()

        assertEquals("Jim", doc.getValue("name"))
        assertTrue(doc.getValue("data") is Blob)
        data = doc.getValue("data") as Blob
        assertEquals(BLOB_CONTENT.length.toLong(), data.length)
        assertContentEquals(content, data.content)
        data.contentStream.use { input ->
            assertNotNull(input)
            val buffer = ByteArray(content.size + 37)
            val bytesRead = input.buffer().read(buffer)
            assertEquals(content.size, bytesRead)
        }
    }

    @Test
    fun testEmptyBlob() {
        val content = "".encodeToByteArray()
        var data = Blob("text/plain", content)
        assertNotNull(data)

        var doc = MutableDocument("doc1")
        doc.setValue("data", data)

        doc = saveDocInBaseTestDb(doc).toMutable()

        assertTrue(doc.getValue("data") is Blob)
        data = doc.getValue("data") as Blob
        assertEquals(0, data.length)
        assertContentEquals(content, data.content)
        data.contentStream.use { input ->
            assertNotNull(input)
            val buffer = ByteArray(37)
            val bytesRead = input.buffer().read(buffer)
            assertEquals(-1, bytesRead)
        }
    }

    @Test
    fun testBlobWithEmptyStream() {
        var doc = MutableDocument("doc1")
        val content = "".encodeToByteArray()
        Buffer().write(content).use { stream ->
            val data = Blob("text/plain", stream)
            assertNotNull(data)
            doc.setValue("data", data)
            doc = saveDocInBaseTestDb(doc).toMutable()
        }

        assertTrue(doc.getValue("data") is Blob)
        val data = doc.getValue("data") as Blob
        assertEquals(0, data.length)
        assertContentEquals(content, data.content)
        data.contentStream.use { input ->
            assertNotNull(input)
            val buffer = ByteArray(37)
            val bytesRead = input.buffer().read(buffer)
            assertEquals(-1, bytesRead)
        }
    }

    @Test
    fun testMultipleBlobRead() {
        val content: ByteArray = BLOB_CONTENT.encodeToByteArray()
        var data = Blob("text/plain", content)
        assertNotNull(data)

        var doc = MutableDocument("doc1")
        doc.setValue("data", data)

        data = doc.getValue("data") as Blob
        for (i in 0 until 5) {
            assertContentEquals(content, data.content)
            data.contentStream.use { input ->
                assertNotNull(input)
                val buffer = ByteArray(content.size + 37)
                val bytesRead = input.buffer().read(buffer)
                assertEquals(content.size, bytesRead)
            }
        }

        doc = saveDocInBaseTestDb(doc).toMutable()

        assertTrue(doc.getValue("data") is Blob)
        data = doc.getValue("data") as Blob
        for (i in 0 until 5) {
            assertContentEquals(content, data.content)
            data.contentStream.use { input ->
                assertNotNull(input)
                val buffer = ByteArray(content.size + 37)
                val bytesRead = input.buffer().read(buffer)
                assertEquals(content.size, bytesRead)
            }
        }
    }

    @Test
    fun testReadExistingBlob() {
        val content: ByteArray = BLOB_CONTENT.encodeToByteArray()
        var data = Blob("text/plain", content)
        assertNotNull(data)

        var doc = MutableDocument("doc1")
        doc.setValue("data", data)
        doc.setValue("name", "Jim")
        doc = saveDocInBaseTestDb(doc).toMutable()

        val obj = doc.getValue("data")
        assertTrue(obj is Blob)
        data = obj
        assertContentEquals(content, data.content)

        reopenBaseTestDb()

        doc = baseTestDb.getDocument("doc1")!!.toMutable()
        doc.setValue("foo", "bar")
        doc = saveDocInBaseTestDb(doc).toMutable()

        assertTrue(doc.getValue("data") is Blob)
        data = doc.getValue("data") as Blob
        assertContentEquals(content, data.content)
    }

    @Test
    fun testEnumeratingKeys() {
        val doc = MutableDocument("doc1")
        for (i in 0 until 20) {
            doc.setLong("key$i", i.toLong())
        }
        val content = doc.toMap()
        val result = mutableMapOf<String, Any?>()
        var count = 0
        for (key in doc) {
            result[key] = doc.getValue(key)
            count++
        }
        assertEquals(content, result)
        assertEquals(content.size, count)

        doc.remove("key2")
        doc.setLong("key20", 20L)
        doc.setLong("key21", 21L)
        val content2 = doc.toMap()
        saveDocInBaseTestDb(doc) { doc1 ->
            val content1 = doc1.toMap()
            val result1 = mutableMapOf<String, Any?>()
            var count1 = 0
            for (key in doc1) {
                result1[key] = doc1.getValue(key)
                count1++
            }
            assertEquals(content1.size, count1)
            assertEquals(content1, result1)
            assertEquals(content1, content2)
        }
    }

    @Test
    fun testToMutable() {
        val content: ByteArray = BLOB_CONTENT.encodeToByteArray()
        val data = Blob("text/plain", content)
        val mDoc1 = MutableDocument("doc1")
        mDoc1.setBlob("data", data)
        mDoc1.setString("name", "Jim")
        mDoc1.setInt("score", 10)

        val mDoc2 = mDoc1.toMutable()

        // https://forums.couchbase.com/t/bug-in-document-tomutable-in-db21/15441
        assertEquals(3, mDoc2.keys.size)
        assertEquals(3, mDoc2.count)

        assertNotSame(mDoc1, mDoc2)
        assertEquals(mDoc2, mDoc1)
        assertEquals(mDoc1, mDoc2)
        assertEquals(mDoc1.getBlob("data"), mDoc2.getBlob("data"))
        assertEquals(mDoc1.getString("name"), mDoc2.getString("name"))
        assertEquals(mDoc1.getInt("score"), mDoc2.getInt("score"))

        val doc1 = saveDocInBaseTestDb(mDoc1)
        val mDoc3 = doc1.toMutable()

        // https://forums.couchbase.com/t/bug-in-document-tomutable-in-db21/15441
        assertEquals(3, mDoc3.keys.size)
        assertEquals(3, mDoc3.count)

        assertEquals(doc1.getBlob("data"), mDoc3.getBlob("data"))
        assertEquals(doc1.getString("name"), mDoc3.getString("name"))
        assertEquals(doc1.getInt("score"), mDoc3.getInt("score"))
    }

    @Test
    fun testEquality() {
        val data1: ByteArray = "data1".encodeToByteArray()
        val data2: ByteArray = "data2".encodeToByteArray()

        val doc1a = MutableDocument("doc1")
        val doc1b = MutableDocument("doc1")
        val doc1c = MutableDocument("doc1")

        doc1a.setInt("answer", 42)
        doc1a.setValue("options", "1,2,3")
        doc1a.setBlob("attachment", Blob("text/plain", data1))

        doc1b.setInt("answer", 42)
        doc1b.setValue("options", "1,2,3")
        doc1b.setBlob("attachment", Blob("text/plain", data1))

        doc1c.setInt("answer", 41)
        doc1c.setValue("options", "1,2")
        doc1c.setBlob("attachment", Blob("text/plain", data2))
        doc1c.setString("comment", "This is a comment")

        assertEquals(doc1a, doc1a)
        assertEquals(doc1a, doc1b)
        assertNotEquals(doc1a, doc1c)

        assertEquals(doc1b, doc1a)
        assertEquals(doc1b, doc1b)
        assertNotEquals(doc1b, doc1c)

        assertNotEquals(doc1c, doc1a)
        assertNotEquals(doc1c, doc1b)
        assertEquals(doc1c, doc1c)

        val savedDoc = saveDocInBaseTestDb(doc1c)
        val mDoc = savedDoc.toMutable()
        assertEquals(savedDoc, mDoc)
        assertEquals(mDoc, savedDoc)
        mDoc.setInt("answer", 50)
        assertNotEquals(savedDoc, mDoc)
        assertNotEquals(mDoc, savedDoc)
    }

    @Test
    fun testEqualityDifferentDocID() {
        val doc1 = MutableDocument("doc1")
        val doc2 = MutableDocument("doc2")
        doc1.setLong("answer", 42L) // TODO: Integer cause inequality with saved doc
        doc2.setLong("answer", 42L) // TODO: Integer cause inequality with saved doc
        val sDoc1 = saveDocInBaseTestDb(doc1)
        val sDoc2 = saveDocInBaseTestDb(doc2)

        assertEquals(doc1, doc1)
        assertEquals(sDoc1, sDoc1)
        assertEquals(doc1, sDoc1)
        assertEquals(sDoc1, doc1)

        assertEquals(doc2, doc2)
        assertEquals(sDoc2, sDoc2)
        assertEquals(doc2, sDoc2)
        assertEquals(sDoc2, doc2)

        assertNotEquals(doc1, doc2)
        assertNotEquals(doc2, doc1)
        assertNotEquals(sDoc1, sDoc2)
        assertNotEquals(sDoc2, sDoc1)
    }

    @Test
    fun testEqualityDifferentDB() {
        var sameDB: Database? = null
        val otherDB = createDb("equ-diff-db")
        try {
            val doc1a = MutableDocument("doc1")
            val doc1b = MutableDocument("doc1")
            doc1a.setLong("answer", 42L)
            doc1b.setLong("answer", 42L)
            assertEquals(doc1a, doc1b)
            assertEquals(doc1b, doc1a)
            var sDoc1a: Document? = saveDocInBaseTestDb(doc1a)
            otherDB.save(doc1b)

            var sDoc1b = otherDB.getDocument(doc1b.id)
            assertEquals(doc1a, sDoc1a)
            assertEquals(sDoc1a, doc1a)
            assertEquals(doc1b, sDoc1b)
            assertEquals(sDoc1b, doc1b)
            assertNotEquals(sDoc1a, sDoc1b)
            assertNotEquals(sDoc1b, sDoc1a)

            sDoc1a = baseTestDb.getDocument("doc1")
            sDoc1b = otherDB.getDocument("doc1")
            assertNotEquals(sDoc1b, sDoc1a)

            sameDB = Database(baseTestDb.name)

            val anotherDoc1a = sameDB.getDocument("doc1")
            assertEquals(anotherDoc1a, sDoc1a)
            assertEquals(sDoc1a, anotherDoc1a)
        } finally {
            closeDb(sameDB)
            deleteDb(otherDB)
        }
    }

    // https://github.com/couchbase/couchbase-lite-android/issues/1449
    @Test
    fun testDeleteDocAndGetDoc() {
        val docID = "doc-1"

        var doc = baseTestDb.getDocument(docID)
        assertNull(doc)

        val mDoc = MutableDocument(docID)
        mDoc.setValue("key", "value")
        doc = saveDocInBaseTestDb(mDoc)
        assertNotNull(doc)
        assertEquals(1, baseTestDb.count)

        doc = baseTestDb.getDocument(docID)
        assertNotNull(doc)
        assertEquals("value", doc.getString("key"))

        baseTestDb.delete(doc)
        assertEquals(0, baseTestDb.count)
        doc = baseTestDb.getDocument(docID)
        assertNull(doc)
    }

    @Test
    fun testEquals() {

        // mDoc1 and mDoc2 have exactly same data
        // mDoc3 is different
        // mDoc4 is different

        val mDoc1 = MutableDocument()
        mDoc1.setValue("key1", 1L)
        mDoc1.setValue("key2", "Hello")
        mDoc1.setValue("key3", null)

        val mDoc2 = MutableDocument()
        mDoc2.setValue("key1", 1L)
        mDoc2.setValue("key2", "Hello")
        mDoc2.setValue("key3", null)

        val mDoc3 = MutableDocument()
        mDoc3.setValue("key1", 100L)
        mDoc3.setValue("key3", true)

        val mDoc4 = MutableDocument()
        mDoc4.setValue("key1", 100L)

        val mDoc5 = MutableDocument()
        mDoc4.setValue("key1", 100L)
        mDoc3.setValue("key3", false)

        val mDoc6 = MutableDocument()
        mDoc6.setValue("key1", 100L)

        val mDoc7 = MutableDocument()
        mDoc7.setValue("key1", 100L)
        mDoc7.setValue("key3", false)

        val mDoc8 = MutableDocument("sameDocID")
        mDoc8.setValue("key1", 100L)

        val mDoc9 = MutableDocument("sameDocID")
        mDoc9.setValue("key1", 100L)
        mDoc9.setValue("key3", false)

        val doc1 = saveDocInBaseTestDb(mDoc1)
        val doc2 = saveDocInBaseTestDb(mDoc2)
        val doc3 = saveDocInBaseTestDb(mDoc3)
        val doc4 = saveDocInBaseTestDb(mDoc4)
        val doc5 = saveDocInBaseTestDb(mDoc5)

        // compare doc1, doc2, mdoc1, and mdoc2
        assertEquals(doc1, doc1)
        assertEquals(doc2, doc2)
        assertNotEquals(doc1, doc2)
        assertNotEquals(doc2, doc1)
        assertEquals(doc1, doc1.toMutable())
        assertNotEquals(doc1, doc2.toMutable())
        assertEquals(doc1.toMutable(), doc1)
        assertNotEquals(doc2.toMutable(), doc1)
        assertEquals(doc1, mDoc1) // mDoc's ID is updated
        assertNotEquals(doc1, mDoc2)
        assertNotEquals(doc2, mDoc1)
        assertEquals(doc2, mDoc2)
        assertEquals(mDoc1, doc1)
        assertNotEquals(mDoc2, doc1)
        assertNotEquals(mDoc1, doc2)
        assertEquals(mDoc2, doc2)
        assertEquals(mDoc1, mDoc1)
        assertEquals(mDoc2, mDoc2)
        assertEquals(mDoc1, mDoc1)
        assertEquals(mDoc2, mDoc2)

        // compare doc1, doc3, mdoc1, and mdoc3
        assertEquals(doc3, doc3)
        assertNotEquals(doc1, doc3)
        assertNotEquals(doc3, doc1)
        assertNotEquals(doc1, doc3.toMutable())
        assertNotEquals(doc3.toMutable(), doc1)
        assertNotEquals(doc1, mDoc3)
        assertNotEquals(doc3, mDoc1)
        assertEquals(doc3, mDoc3)
        assertNotEquals(mDoc3, doc1)
        assertNotEquals(mDoc1, doc3)
        assertEquals(mDoc3, doc3)
        assertEquals(mDoc3, mDoc3)

        // compare doc1, doc4, mdoc1, and mdoc4
        assertEquals(doc4, doc4)
        assertNotEquals(doc1, doc4)
        assertNotEquals(doc4, doc1)
        assertNotEquals(doc1, doc4.toMutable())
        assertNotEquals(doc4.toMutable(), doc1)
        assertNotEquals(doc1, mDoc4)
        assertNotEquals(doc4, mDoc1)
        assertEquals(doc4, mDoc4)
        assertNotEquals(mDoc4, doc1)
        assertNotEquals(mDoc1, doc4)
        assertEquals(mDoc4, doc4)
        assertEquals(mDoc4, mDoc4)

        // compare doc3, doc4, mdoc3, and mdoc4
        assertNotEquals(doc3, doc4)
        assertNotEquals(doc4, doc3)
        assertNotEquals(doc3, doc4.toMutable())
        assertNotEquals(doc4.toMutable(), doc3)
        assertNotEquals(doc3, mDoc4)
        assertNotEquals(doc4, mDoc3)
        assertNotEquals(mDoc4, doc3)
        assertNotEquals(mDoc3, doc4)

        // compare doc3, doc5, mdoc3, and mdoc5
        assertNotEquals(doc3, doc5)
        assertNotEquals(doc5, doc3)
        assertNotEquals(doc3, doc5.toMutable())
        assertNotEquals(doc5.toMutable(), doc3)
        assertNotEquals(doc3, mDoc5)
        assertNotEquals(doc5, mDoc3)
        assertNotEquals(mDoc5, doc3)
        assertNotEquals(mDoc3, doc5)

        // compare doc5, doc4, mDoc5, and mdoc4
        assertNotEquals(doc5, doc4)
        assertNotEquals(doc4, doc5)
        assertNotEquals(doc5, doc4.toMutable())
        assertNotEquals(doc4.toMutable(), doc5)
        assertNotEquals(doc5, mDoc4)
        assertNotEquals(doc4, mDoc5)
        assertNotEquals(mDoc4, doc5)
        assertNotEquals(mDoc5, doc4)

        // compare doc1, mDoc1, and mdoc6
        assertNotEquals(doc1, mDoc6)
        assertNotEquals(mDoc6, doc1)
        assertNotEquals(mDoc6, doc1.toMutable())
        assertNotEquals(mDoc1, mDoc6)
        assertNotEquals(mDoc6, mDoc1)

        // compare doc4, mDoc4, and mdoc6
        assertEquals(mDoc6, mDoc6)
        assertNotEquals(doc4, mDoc6)
        assertNotEquals(mDoc6, doc4)
        assertNotEquals(mDoc6, doc4.toMutable())
        assertNotEquals(mDoc4, mDoc6)
        assertNotEquals(mDoc6, mDoc4)

        // compare doc5, mDoc5, and mdoc7
        assertEquals(mDoc7, mDoc7)
        assertNotEquals(doc5, mDoc7)
        assertNotEquals(mDoc7, doc5)
        assertNotEquals(mDoc7, doc5.toMutable())
        assertNotEquals(mDoc5, mDoc7)
        assertNotEquals(mDoc7, mDoc5)

        // compare mDoc6 and mDoc7
        assertEquals(mDoc6, mDoc6)
        assertNotEquals(mDoc6, mDoc7)
        assertNotEquals(mDoc6, mDoc8)
        assertNotEquals(mDoc6, mDoc9)
        assertNotEquals(mDoc7, mDoc6)
        assertEquals(mDoc7, mDoc7)
        assertNotEquals(mDoc7, mDoc8)
        assertNotEquals(mDoc7, mDoc9)

        // compare mDoc8 and mDoc9
        assertEquals(mDoc8, mDoc8)
        assertNotEquals(mDoc8, mDoc9)
        assertNotEquals(mDoc9, mDoc8)
        assertEquals(mDoc9, mDoc9)

        assertNotNull(doc3)
    }

    @Test
    fun testHashCode() {
        // mDoc1 and mDoc2 have exactly same data
        // mDoc3 is different
        // mDoc4 is different

        val mDoc1 = MutableDocument()
        mDoc1.setValue("key1", 1L)
        mDoc1.setValue("key2", "Hello")
        mDoc1.setValue("key3", null)

        val mDoc2 = MutableDocument()
        mDoc2.setValue("key1", 1L)
        mDoc2.setValue("key2", "Hello")
        mDoc2.setValue("key3", null)

        val mDoc3 = MutableDocument()
        mDoc3.setValue("key1", 100L)
        mDoc3.setValue("key3", true)

        val mDoc4 = MutableDocument()
        mDoc4.setValue("key1", 100L)

        val mDoc5 = MutableDocument()
        mDoc4.setValue("key1", 100L)
        mDoc3.setValue("key3", false)

        val mDoc6 = MutableDocument()
        mDoc6.setValue("key1", 100L)

        val mDoc7 = MutableDocument()
        mDoc7.setValue("key1", 100L)
        mDoc7.setValue("key3", false)

        val doc1 = saveDocInBaseTestDb(mDoc1)
        val doc2 = saveDocInBaseTestDb(mDoc2)
        val doc3 = saveDocInBaseTestDb(mDoc3)
        val doc4 = saveDocInBaseTestDb(mDoc4)
        val doc5 = saveDocInBaseTestDb(mDoc5)

        assertEquals(doc1.hashCode(), doc1.hashCode())
        assertNotEquals(doc1.hashCode(), doc2.hashCode())
        assertNotEquals(doc2.hashCode(), doc1.hashCode())
        assertEquals(doc1.hashCode(), doc1.toMutable().hashCode())
        assertNotEquals(doc1.hashCode(), doc2.toMutable().hashCode())
        assertEquals(doc1.hashCode(), mDoc1.hashCode())
        assertNotEquals(doc1.hashCode(), mDoc2.hashCode())
        assertNotEquals(doc2.hashCode(), mDoc1.hashCode())
        assertEquals(doc2.hashCode(), mDoc2.hashCode())

        assertNotEquals(doc3.hashCode(), doc1.hashCode())
        assertNotEquals(doc3.hashCode(), doc2.hashCode())
        assertNotEquals(doc3.hashCode(), doc1.toMutable().hashCode())
        assertNotEquals(doc3.hashCode(), doc2.toMutable().hashCode())
        assertNotEquals(doc3.hashCode(), mDoc1.hashCode())
        assertNotEquals(doc3.hashCode(), mDoc2.hashCode())
        assertNotEquals(mDoc3.hashCode(), doc1.hashCode())
        assertNotEquals(mDoc3.hashCode(), doc2.hashCode())
        assertNotEquals(mDoc3.hashCode(), doc1.toMutable().hashCode())
        assertNotEquals(mDoc3.hashCode(), doc2.toMutable().hashCode())
        assertNotEquals(mDoc3.hashCode(), mDoc1.hashCode())
        assertNotEquals(mDoc3.hashCode(), mDoc2.hashCode())

        assertNotEquals(doc3.hashCode(), 0)
        assertNotEquals(doc3.hashCode(), Any().hashCode())
        assertNotEquals(doc3.hashCode(), 1.hashCode())
        assertNotEquals(doc3.hashCode(), emptyMap<String, Any?>().hashCode())
        assertNotEquals(doc3.hashCode(), MutableDictionary().hashCode())
        assertNotEquals(doc3.hashCode(), MutableArray().hashCode())
        assertNotEquals(mDoc3.hashCode(), doc1.toMutable().hashCode())
        assertNotEquals(mDoc3.hashCode(), doc2.toMutable().hashCode())
        assertNotEquals(mDoc3.hashCode(), mDoc1.hashCode())
        assertNotEquals(mDoc3.hashCode(), mDoc2.hashCode())

        assertNotEquals(mDoc6.hashCode(), doc1.hashCode())
        assertNotEquals(mDoc6.hashCode(), doc1.toMutable().hashCode())
        assertNotEquals(mDoc6.hashCode(), mDoc1.hashCode())
        assertNotEquals(mDoc6.hashCode(), doc2.hashCode())
        assertNotEquals(mDoc6.hashCode(), doc2.toMutable().hashCode())
        assertNotEquals(mDoc6.hashCode(), mDoc2.hashCode())
        assertNotEquals(mDoc6.hashCode(), doc3.hashCode())
        assertNotEquals(mDoc6.hashCode(), doc3.toMutable().hashCode())
        assertNotEquals(mDoc6.hashCode(), mDoc3.hashCode())
        assertNotEquals(mDoc6.hashCode(), doc4.hashCode())
        assertNotEquals(mDoc6.hashCode(), doc4.toMutable().hashCode())
        assertNotEquals(mDoc6.hashCode(), mDoc4.hashCode())
        assertNotEquals(mDoc6.hashCode(), doc5.hashCode())
        assertNotEquals(mDoc6.hashCode(), doc5.toMutable().hashCode())
        assertNotEquals(mDoc6.hashCode(), mDoc5.hashCode())
        assertEquals(mDoc6.hashCode(), mDoc6.hashCode())
        assertNotEquals(mDoc6.hashCode(), mDoc7.hashCode())

        assertNotEquals(mDoc7.hashCode(), doc1.hashCode())
        assertNotEquals(mDoc7.hashCode(), doc1.toMutable().hashCode())
        assertNotEquals(mDoc7.hashCode(), mDoc1.hashCode())
        assertNotEquals(mDoc7.hashCode(), doc2.hashCode())
        assertNotEquals(mDoc7.hashCode(), doc2.toMutable().hashCode())
        assertNotEquals(mDoc7.hashCode(), mDoc2.hashCode())
        assertNotEquals(mDoc7.hashCode(), doc3.hashCode())
        assertNotEquals(mDoc7.hashCode(), doc3.toMutable().hashCode())
        assertNotEquals(mDoc7.hashCode(), mDoc3.hashCode())
        assertNotEquals(mDoc7.hashCode(), doc4.hashCode())
        assertNotEquals(mDoc7.hashCode(), doc4.toMutable().hashCode())
        assertNotEquals(mDoc7.hashCode(), mDoc4.hashCode())
        assertNotEquals(mDoc7.hashCode(), doc5.hashCode())
        assertNotEquals(mDoc7.hashCode(), doc5.toMutable().hashCode())
        assertNotEquals(mDoc7.hashCode(), mDoc5.hashCode())
        assertNotEquals(mDoc7.hashCode(), mDoc6.hashCode())
        assertEquals(mDoc7.hashCode(), mDoc7.hashCode())

        assertNotEquals(doc3.hashCode(), doc2.hashCode())
        assertNotEquals(doc3.hashCode(), doc1.toMutable().hashCode())
        assertNotEquals(doc3.hashCode(), doc2.toMutable().hashCode())
        assertNotEquals(doc3.hashCode(), mDoc1.hashCode())
        assertNotEquals(doc3.hashCode(), mDoc2.hashCode())
        assertNotEquals(mDoc3.hashCode(), doc1.hashCode())
        assertNotEquals(mDoc3.hashCode(), doc2.hashCode())
        assertNotEquals(mDoc3.hashCode(), doc1.toMutable().hashCode())
        assertNotEquals(mDoc3.hashCode(), doc2.toMutable().hashCode())
        assertNotEquals(mDoc3.hashCode(), mDoc1.hashCode())
        assertNotEquals(mDoc3.hashCode(), mDoc2.hashCode())
    }

    @Test
    fun testRevisionIDNewDoc() {
        val doc = MutableDocument()
        assertNull(doc.revisionID)
        baseTestDb.save(doc)
        assertNotNull(doc.revisionID)
    }

    @Test
    fun testRevisionIDExistingDoc() {
        var mdoc = MutableDocument("doc1")
        baseTestDb.save(mdoc)
        val docRevID = mdoc.revisionID

        val doc = baseTestDb.getDocument("doc1")!!
        assertEquals(docRevID, doc.revisionID)
        assertEquals(docRevID, mdoc.revisionID)

        mdoc = doc.toMutable()
        assertEquals(docRevID, doc.revisionID)
        assertEquals(docRevID, mdoc.revisionID)

        mdoc.setInt("int", 88)
        assertEquals(docRevID, doc.revisionID)
        assertEquals(docRevID, mdoc.revisionID)

        baseTestDb.save(mdoc)
        assertEquals(docRevID, doc.revisionID)
        assertNotEquals(docRevID, mdoc.revisionID)
    }

    ///////////////  JSON tests
    // JSON 3.2
    @Test
    fun testDocToJSON() {
        val mDoc = makeDocument()
        saveDocInBaseTestDb(mDoc)
        verifyDocument(
            Json.parseToJsonElement(baseTestDb.getDocument(mDoc.id)!!.toJSON()!!).jsonObject
        )
    }

    // JSON 3.5.?
    @Test
    fun testMutableDocToJSONBeforeSave() {
        assertFailsWith<IllegalStateException> {
            MutableDocument().toJSON()
        }
    }

    // JSON 3.5.a
    // Java does not have MutableDocument(String json) because it collides with MutableDocument(String id)
    // JSON 3.5.b-c
    @Test
    fun testDocFromJSON() {
        val dbDoc = saveDocInBaseTestDb(
            MutableDocument("fromJSON", readJSONResource("document.json"))
        )
        baseTestDb.saveBlob(makeBlob()) // be sure the blob is there...
        verifyDocument(dbDoc.content)
        verifyDocument(Json.parseToJsonElement(dbDoc.toJSON()!!).jsonObject)
    }

    // JSON 3.5.d.1
    @Test
    fun testDocFromBadJSON1() {
        assertFailsWith<IllegalArgumentException> {
            MutableDocument("fromJSON", "{")
        }
    }

    // JSON 3.5.d.2
    @Test
    fun testDocFromBadJSON2() {
        assertFailsWith<IllegalArgumentException> {
            MutableDocument("fromJSON", "{ab cd: \"xyz\"}")
        }
    }

    // JSON 3.5.d.3
    @Test
    fun testDocFromBadJSON3() {
        assertFailsWith<IllegalArgumentException> {
            MutableDocument("fromJSON", "{ab: \"xyz\" cd: \"xyz\"}")
        }
    }

    // JSON 3.5.e
    @Test
    fun testMutableFromArray() {
        assertFailsWith<IllegalArgumentException> {
            MutableDocument("fromJSON", readJSONResource("array.json"))
        }
    }
}
