@file:Suppress("MemberVisibilityCanBePrivate")

package kotbase

import com.couchbase.lite.isOpen
import com.couchbase.lite.withDbLock
import kotbase.internal.utils.*
import kotlinx.datetime.Instant
import kotlinx.serialization.json.*
import okio.IOException
import okio.buffer
import okio.use
import kotlin.test.*

abstract class BaseDbTest : BaseTest() {

    fun interface DocValidator {
        fun accept(document: Document)
    }

    protected open fun <T : Comparable<T>?> assertContents(l1: List<T>, vararg contents: T) {
        val l2 = contents.toList()
        assertTrue(l1.containsAll(l2) && l2.containsAll(l1))
    }

    protected lateinit var baseTestDb: Database

    @BeforeTest
    fun setUpBaseDbTest() {
        baseTestDb = createDb("base_db")
        Report.log("Created base test DB: $baseTestDb")
        assertNotNull(baseTestDb)
        baseTestDb.withDbLock { assertTrue(baseTestDb.isOpen) }
    }

    @AfterTest
    fun tearDownBaseDbTest() {
        if (::baseTestDb.isInitialized) {
            deleteDb(baseTestDb)
            Report.log("Deleted baseTestDb: $baseTestDb")
        } else {
            Report.log("baseTestDb never initialized")
        }
    }

    protected fun createSingleDocInBaseTestDb(docID: String?): Document {
        val n = baseTestDb.count
        val doc = MutableDocument(docID)
        doc.setValue("key", 1)
        val savedDoc = saveDocInBaseTestDb(doc)
        assertEquals(n + 1, baseTestDb.count)
        assertEquals(1, savedDoc.sequence)
        return savedDoc
    }

    protected fun createDocsInDb(first: Int, count: Int, db: Database) {
        db.inBatch {
            for (i in first until first + count) {
                val doc = MutableDocument("doc-$i")
                doc.setNumber("count", i)
                doc.setString("inverse", "minus-$i")
                db.save(doc)
            }
        }
    }

    protected fun saveDocInBaseTestDb(doc: MutableDocument): Document {
        baseTestDb.save(doc)
        val savedDoc = baseTestDb.getDocument(doc.id)
        assertNotNull(savedDoc)
        assertEquals(doc.id, savedDoc.id)
        return savedDoc
    }

    protected fun saveDocInBaseTestDb(doc: MutableDocument, validator: DocValidator): Document {
        validator.accept(doc)
        val savedDoc = saveDocInBaseTestDb(doc)
        validator.accept(doc)
        validator.accept(savedDoc)
        return savedDoc
    }

    // used from other package's tests
    protected fun populateData(doc: MutableDocument) {
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

    // used from other package's tests
    protected fun populateDataByTypedSetter(doc: MutableDocument) {
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

    // file is one JSON object per line
    protected fun loadJSONResource(name: String) {
        PlatformUtils.getAsset(name)?.use { fileSource ->
            fileSource.buffer().use { bufferedFileSource ->
                var n = 1
                while (true) {
                    val line = bufferedFileSource.readUtf8Line() ?: break
                    if (line.isBlank()) continue

                    val doc = MutableDocument("doc-${n++.paddedString(3)}")
                    doc.setData(JSONUtils.fromJSON(Json.parseToJsonElement(line).jsonObject))

                    saveDocInBaseTestDb(doc)
                }
            }
        }
    }

    protected fun readJSONResource(name: String): String {
        val buf = StringBuilder()
        PlatformUtils.getAsset(name)?.use { fileSource ->
            fileSource.buffer().use { bufferedFileSource ->
                while (true) {
                    val line = bufferedFileSource.readUtf8Line() ?: break
                    if (line.isNotBlank()) {
                        buf.append(line)
                    }
                }
            }
        }
        return buf.toString()
    }

    protected fun makeArray(): MutableArray {
        // A small array
        val simpleArray = MutableArray()
        simpleArray.addInt(54)
        simpleArray.addString("Joplin")

        // A small dictionary
        val simpleDict = MutableDictionary()
        simpleDict.setInt("sdict-1", 58)
        simpleDict.setString("sdict-2", "Winehouse")

        val array = MutableArray()
        array.addValue(null)

        array.addBoolean(true)
        array.addBoolean(false)

        array.addInt(0)
        array.addInt(Int.MIN_VALUE)
        array.addInt(Int.MAX_VALUE)

        array.addLong(0L)
        array.addLong(Long.MIN_VALUE)
        array.addLong(Long.MAX_VALUE)

        array.addFloat(0.0f)
        array.addFloat(Float.MIN_VALUE)
        array.addFloat(Float.MAX_VALUE)

        array.addDouble(0.0)
        array.addDouble(Double.MIN_VALUE)
        array.addDouble(Double.MAX_VALUE)

        array.addNumber(null)
        array.addNumber(0)
        array.addNumber(Float.MIN_VALUE)
        array.addNumber(Long.MIN_VALUE)

        array.addString(null)
        array.addString("Harry")

        array.addDate(null)
        array.addDate(Instant.parse(TEST_DATE))

        array.addArray(null)
        array.addArray(simpleArray)

        array.addDictionary(null)
        array.addDictionary(simpleDict)

        return array
    }

    protected fun verifyArray(jArray: JsonArray) {
        assertEquals(27, jArray.size)

        assertEquals(JsonNull, jArray[0])

        assertEquals(true, jArray[1].jsonPrimitive.boolean)
        assertEquals(false, jArray[2].jsonPrimitive.boolean)

        assertEquals(0, jArray[3].jsonPrimitive.int)
        assertEquals(Int.MIN_VALUE, jArray[4].jsonPrimitive.int)
        assertEquals(Int.MAX_VALUE, jArray[5].jsonPrimitive.int)

        assertEquals(0, jArray[6].jsonPrimitive.long)
        assertEquals(Long.MIN_VALUE, jArray[7].jsonPrimitive.long)
        assertEquals(Long.MAX_VALUE, jArray[8].jsonPrimitive.long)

        assertEquals(0.0f, jArray[9].jsonPrimitive.float, 0.001f)
        assertEquals(Float.MIN_VALUE, jArray[10].jsonPrimitive.float, 0.001f)
        assertEquals(Float.MAX_VALUE, jArray[11].jsonPrimitive.float, 100.0f)

        assertEquals(0.0, jArray[12].jsonPrimitive.double, 0.001)
        assertEquals(Double.MIN_VALUE, jArray[13].jsonPrimitive.double, 0.001)
        assertEquals(Double.MAX_VALUE, jArray[14].jsonPrimitive.double, 1.0)

        assertEquals(JsonNull, jArray[15])
        assertEquals(0, jArray[16].jsonPrimitive.long)
        assertEquals(Float.MIN_VALUE.toDouble(), jArray[17].jsonPrimitive.double, 0.001)
        assertEquals(Long.MIN_VALUE, jArray[18].jsonPrimitive.long)

        assertEquals(JsonNull, jArray[19])
        assertEquals("Harry", jArray[20].jsonPrimitive.content)

        assertEquals(JsonNull, jArray[21])
        assertEquals(TEST_DATE, jArray[22].jsonPrimitive.content)

        assertEquals(JsonNull, jArray[23])
        assertTrue(jArray[24] is JsonArray)

        assertEquals(JsonNull, jArray[25])
        assertTrue(jArray[26] is JsonObject)

        assertEquals(JsonNull, jArray[25])
        assertTrue(jArray[26] is JsonObject)
    }

    protected fun verifyArray(array: Array?) {
        assertNotNull(array)

        assertEquals(27, array.count)

        //#0 array.addValue(null);
        assertNull(array.getValue(0))
        assertFalse(array.getBoolean(0))
        assertEquals(0, array.getInt(0))
        assertEquals(0L, array.getLong(0))
        assertEquals(0.0f, array.getFloat(0), 0.001f)
        assertEquals(0.0, array.getDouble(0), 0.001)
        assertNull(array.getNumber(0))
        assertNull(array.getString(0))
        assertNull(array.getDate(0))
        assertNull(array.getBlob(0))
        assertNull(array.getArray(0))
        assertNull(array.getDictionary(0))

        //#1 array.addBoolean(true);
        assertEquals(true, array.getValue(1))
        assertTrue(array.getBoolean(1))
        assertEquals(1, array.getInt(1))
        assertEquals(1L, array.getLong(1))
        assertEquals(1.0f, array.getFloat(1), 0.001f)
        assertEquals(1.0, array.getDouble(1), 0.001)
        assertEquals(1, array.getNumber(1))
        assertNull(array.getString(1))
        assertNull(array.getDate(1))
        assertNull(array.getBlob(1))
        assertNull(array.getArray(1))
        assertNull(array.getDictionary(1))

        //#2 array.addBoolean(false);
        assertEquals(false, array.getValue(2))
        assertFalse(array.getBoolean(2))
        assertEquals(0, array.getInt(2))
        assertEquals(0L, array.getLong(2))
        assertEquals(0.0f, array.getFloat(2), 0.001f)
        assertEquals(0.0, array.getDouble(2), 0.001)
        assertEquals(0, array.getNumber(2))
        assertNull(array.getString(2))
        assertNull(array.getDate(2))
        assertNull(array.getBlob(2))
        assertNull(array.getArray(2))
        assertNull(array.getDictionary(2))

        //#3 array.addInt(0);
        assertEquals(0L, array.getValue(3))
        assertFalse(array.getBoolean(3))
        assertEquals(0, array.getInt(3))
        assertEquals(0L, array.getLong(3))
        assertEquals(0.0f, array.getFloat(3), 0.001f)
        assertEquals(0.0, array.getDouble(3), 0.001)
        assertEquals(0L, array.getNumber(3))
        assertNull(array.getString(3))
        assertNull(array.getDate(3))
        assertNull(array.getBlob(3))
        assertNull(array.getArray(3))
        assertNull(array.getDictionary(3))

        //#4 array.addInt(Integer.MIN_VALUE);
        assertEquals(Int.MIN_VALUE.toLong(), array.getValue(4))
        assertTrue(array.getBoolean(4))
        assertEquals(Int.MIN_VALUE, array.getInt(4))
        assertEquals(Int.MIN_VALUE.toLong(), array.getLong(4))
        assertEquals(Int.MIN_VALUE.toFloat(), array.getFloat(4), 0.001f)
        assertEquals(Int.MIN_VALUE.toDouble(), array.getDouble(4), 0.001)
        assertEquals(Int.MIN_VALUE.toLong(), array.getNumber(4))
        assertNull(array.getString(4))
        assertNull(array.getDate(4))
        assertNull(array.getBlob(4))
        assertNull(array.getArray(4))
        assertNull(array.getDictionary(4))

        //#5 array.addInt(Integer.MAX_VALUE);
        assertEquals(Int.MAX_VALUE.toLong(), array.getValue(5))
        assertTrue(array.getBoolean(5))
        assertEquals(Int.MAX_VALUE, array.getInt(5))
        assertEquals(Int.MAX_VALUE.toLong(), array.getLong(5))
        assertEquals(Int.MAX_VALUE.toFloat(), array.getFloat(5), 100.0f)
        assertEquals(Int.MAX_VALUE.toDouble(), array.getDouble(5), 100.0)
        assertEquals(Int.MAX_VALUE.toLong(), array.getNumber(5))
        assertNull(array.getString(5))
        assertNull(array.getDate(5))
        assertNull(array.getBlob(5))
        assertNull(array.getArray(5))
        assertNull(array.getDictionary(5))

        //#6 array.addLong(0L);
        assertEquals(0L, array.getValue(6))
        assertFalse(array.getBoolean(6))
        assertEquals(0, array.getInt(6))
        assertEquals(0L, array.getLong(6))
        assertEquals(0.0f, array.getFloat(6), 0.001f)
        assertEquals(0.0, array.getDouble(6), 0.001)
        assertEquals(0L, array.getNumber(6))
        assertNull(array.getString(6))
        assertNull(array.getDate(6))
        assertNull(array.getBlob(6))
        assertNull(array.getArray(6))
        assertNull(array.getDictionary(6))

        //#7 array.addLong(Long.MIN_VALUE);
        assertEquals(Long.MIN_VALUE, array.getValue(7))
        // !!! Fails on iOS: assertFalse(array.getBoolean(7)) (any non-zero number should be true, but Long.MIN_VALUE is false on Android)
        assertEquals(Long.MIN_VALUE.toInt(), array.getInt(7))
        assertEquals(Long.MIN_VALUE, array.getLong(7))
        assertEquals(Long.MIN_VALUE.toFloat(), array.getFloat(7), 0.001f)
        assertEquals(Long.MIN_VALUE.toDouble(), array.getDouble(7), 0.001)
        assertEquals(Long.MIN_VALUE, array.getNumber(7))
        assertNull(array.getString(7))
        assertNull(array.getDate(7))
        assertNull(array.getBlob(7))
        assertNull(array.getArray(7))
        assertNull(array.getDictionary(7))

        //#8 array.addLong(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, array.getValue(8))
        assertTrue(array.getBoolean(8))
        assertEquals(Long.MAX_VALUE.toInt(), array.getInt(8))
        assertEquals(Long.MAX_VALUE, array.getLong(8))
        assertEquals(Long.MAX_VALUE.toFloat(), array.getFloat(8), 100.0f)
        assertEquals(Long.MAX_VALUE.toDouble(), array.getDouble(8), 100.0)
        assertEquals(Long.MAX_VALUE, array.getNumber(8))
        assertNull(array.getString(8))
        assertNull(array.getDate(8))
        assertNull(array.getBlob(8))
        assertNull(array.getArray(8))
        assertNull(array.getDictionary(8))

        //#9 array.addFloat(0.0F);
        assertEquals(0.0f, array.getValue(9))
        assertFalse(array.getBoolean(9))
        assertEquals(0, array.getInt(9))
        assertEquals(0L, array.getLong(9))
        assertEquals(0.0f, array.getFloat(9), 0.001f)
        assertEquals(0.0, array.getDouble(9), 0.001)
        assertEquals(0.0f, array.getNumber(9))
        assertNull(array.getString(9))
        assertNull(array.getDate(9))
        assertNull(array.getBlob(9))
        assertNull(array.getArray(9))
        assertNull(array.getDictionary(9))

        //#10 array.addFloat(Float.MIN_VALUE);
        assertEquals(Float.MIN_VALUE, demoteToFloat(array.getValue(10)))
        // !!! Fails on iOS: assertFalse(array.getBoolean(10)) (any non-zero number should be true, but Float.MIN_VALUE is false on Android)
        assertEquals(Float.MIN_VALUE.toInt(), array.getInt(10))
        assertEquals(Float.MIN_VALUE.toLong(), array.getLong(10))
        assertEquals(Float.MIN_VALUE, array.getFloat(10), 0.001f)
        assertEquals(Float.MIN_VALUE.toDouble(), array.getDouble(10), 0.001)
        assertEquals(Float.MIN_VALUE, demoteToFloat(array.getNumber(10)))
        assertNull(array.getString(10))
        assertNull(array.getDate(10))
        assertNull(array.getBlob(10))
        assertNull(array.getArray(10))
        assertNull(array.getDictionary(10))

        //#11 array.addFloat(Float.MAX_VALUE);
        assertEquals(Float.MAX_VALUE, demoteToFloat(array.getValue(11)))
        assertTrue(array.getBoolean(11))
        // ??? Fails: assertEquals(Float.MAX_VALUE.toInt(), array.getInt(11))
        // ??? Fails: assertEquals(Float.MAX_VALUE.toLong(), array.getLong(11))
        assertEquals(Float.MAX_VALUE, array.getFloat(11), 100.0f)
        assertEquals(
            Float.MAX_VALUE.toDouble(),
            demoteToFloat(array.getDouble(11)).toDouble(),
            100.0
        )
        assertEquals(Float.MAX_VALUE, demoteToFloat(array.getNumber(11)))
        assertNull(array.getString(11))
        assertNull(array.getDate(11))
        assertNull(array.getBlob(11))
        assertNull(array.getArray(11))
        assertNull(array.getDictionary(11))

        //#12 array.addDouble(0.0);
        assertEquals(0f, array.getValue(12))
        assertFalse(array.getBoolean(12))
        assertEquals(0, array.getInt(12))
        assertEquals(0L, array.getLong(12))
        assertEquals(0.0f, array.getFloat(12), 0.001f)
        assertEquals(0.0, array.getDouble(12), 0.001)
        assertEquals(0f, array.getNumber(12))
        assertNull(array.getString(12))
        assertNull(array.getDate(12))
        assertNull(array.getBlob(12))
        assertNull(array.getArray(12))
        assertNull(array.getDictionary(12))

        //#13 array.addDouble(Double.MIN_VALUE);
        assertEquals(Double.MIN_VALUE, array.getValue(13))
        // !!! Fails on iOS: assertFalse(array.getBoolean(13)) (any non-zero number should be true, but Double.MIN_VALUE is false on Android)
        assertEquals(Double.MIN_VALUE.toInt(), array.getInt(13))
        assertEquals(Double.MIN_VALUE.toLong(), array.getLong(13))
        assertEquals(Double.MIN_VALUE.toFloat(), array.getFloat(13), 0.001f)
        assertEquals(Double.MIN_VALUE, array.getDouble(13), 0.001)
        assertEquals(Double.MIN_VALUE, array.getNumber(13))
        assertNull(array.getString(13))
        assertNull(array.getDate(13))
        assertNull(array.getBlob(13))
        assertNull(array.getArray(13))
        assertNull(array.getDictionary(13))

        //#14 array.addDouble(Double.MAX_VALUE);
        assertEquals(Double.MAX_VALUE, array.getValue(14))
        assertTrue(array.getBoolean(14))
        // ??? Fails: assertEquals(Double.MAX_VALUE.toInt(), array.getInt(14))
        // ??? Fails: assertEquals(Double.MAX_VALUE.toLong(), array.getLong(14))
        assertEquals(Double.MAX_VALUE.toFloat(), array.getFloat(14), 100.0f)
        assertEquals(Double.MAX_VALUE, array.getDouble(14), 100.0)
        assertEquals(Double.MAX_VALUE, array.getNumber(14))
        assertNull(array.getString(14))
        assertNull(array.getDate(14))
        assertNull(array.getBlob(14))
        assertNull(array.getArray(14))
        assertNull(array.getDictionary(14))

        //#15 array.addNumber(null);
        assertNull(array.getValue(15))
        assertFalse(array.getBoolean(15))
        assertEquals(0, array.getInt(15))
        assertEquals(0L, array.getLong(15))
        assertEquals(0.0f, array.getFloat(15), 0.001f)
        assertEquals(0.0, array.getDouble(15), 0.001)
        assertNull(array.getNumber(15))
        assertNull(array.getString(15))
        assertNull(array.getDate(15))
        assertNull(array.getBlob(15))
        assertNull(array.getArray(15))
        assertNull(array.getDictionary(15))

        //#16 array.addNumber(0);
        assertEquals(0L, array.getValue(16))
        assertFalse(array.getBoolean(16))
        assertEquals(0, array.getInt(16))
        assertEquals(0L, array.getLong(16))
        assertEquals(0.0f, array.getFloat(16), 0.001f)
        assertEquals(0.0, array.getDouble(16), 0.001)
        assertEquals(0L, array.getNumber(16))
        assertNull(array.getString(16))
        assertNull(array.getDate(16))
        assertNull(array.getBlob(16))
        assertNull(array.getArray(16))
        assertNull(array.getDictionary(16))

        //#17 array.addNumber(Float.MIN_VALUE);
        assertEquals(Float.MIN_VALUE, demoteToFloat(array.getValue(17)))
        // !!! Fails on iOS: assertFalse(array.getBoolean(17)) (any non-zero number should be true, but Float.MIN_VALUE is false on Android)
        assertEquals(Float.MIN_VALUE.toInt(), array.getInt(17))
        assertEquals(Float.MIN_VALUE.toLong(), array.getLong(17))
        assertEquals(Float.MIN_VALUE, array.getFloat(17), 0.001f)
        assertEquals(Float.MIN_VALUE.toDouble(), array.getDouble(17), 0.001)
        assertEquals(Float.MIN_VALUE, demoteToFloat(array.getNumber(17)))
        assertNull(array.getString(17))
        assertNull(array.getDate(17))
        assertNull(array.getBlob(17))
        assertNull(array.getArray(17))
        assertNull(array.getDictionary(17))

        //#18 array.addNumber(Long.MIN_VALUE);
        assertEquals(Long.MIN_VALUE, array.getValue(18))
        // !!! Fails on iOS: assertFalse(array.getBoolean(18)) (any non-zero number should be true, but Long.MIN_VALUE is false on Android)
        assertEquals(Long.MIN_VALUE.toInt(), array.getInt(18))
        assertEquals(Long.MIN_VALUE, array.getLong(18))
        assertEquals(Long.MIN_VALUE.toFloat(), array.getFloat(18), 0.001f)
        assertEquals(Long.MIN_VALUE.toDouble(), array.getDouble(18), 0.001)
        assertEquals(Long.MIN_VALUE, array.getNumber(18))
        assertNull(array.getString(18))
        assertNull(array.getDate(18))
        assertNull(array.getBlob(18))
        assertNull(array.getArray(18))
        assertNull(array.getDictionary(18))

        //#19 array.addString(null);
        assertNull(array.getValue(19))
        assertFalse(array.getBoolean(19))
        assertEquals(0, array.getInt(19))
        assertEquals(0L, array.getLong(19))
        assertEquals(0.0f, array.getFloat(19), 0.001f)
        assertEquals(0.0, array.getDouble(19), 0.001)
        assertNull(array.getNumber(19))
        assertNull(array.getString(19))
        assertNull(array.getDate(19))
        assertNull(array.getBlob(19))
        assertNull(array.getArray(19))
        assertNull(array.getDictionary(19))

        //#20 array.addString("Quatro");
        assertEquals("Harry", array.getValue(20))
        assertTrue(array.getBoolean(20))
        assertEquals(0, array.getInt(20))
        assertEquals(0, array.getLong(20))
        assertEquals(0.0f, array.getFloat(20), 0.001f)
        assertEquals(0.0, array.getDouble(20), 0.001)
        assertNull(array.getNumber(20))
        assertEquals("Harry", array.getString(20))
        assertNull(array.getDate(20))
        assertNull(array.getBlob(20))
        assertNull(array.getArray(20))
        assertNull(array.getDictionary(20))

        //#21 array.addDate(null);
        assertNull(array.getValue(21))
        assertFalse(array.getBoolean(21))
        assertEquals(0, array.getInt(21))
        assertEquals(0L, array.getLong(21))
        assertEquals(0.0f, array.getFloat(21), 0.001f)
        assertEquals(0.0, array.getDouble(21), 0.001)
        assertNull(array.getNumber(21))
        assertNull(array.getString(21))
        assertNull(array.getDate(21))
        assertNull(array.getBlob(21))
        assertNull(array.getArray(21))
        assertNull(array.getDictionary(21))

        //#22 array.addDate(Instant.parse(TEST_DATE));
        assertEquals(TEST_DATE, array.getValue(22))
        assertTrue(array.getBoolean(22))
        assertEquals(0, array.getInt(22))
        assertEquals(0L, array.getLong(22))
        assertEquals(0.0f, array.getFloat(22), 0.001f)
        assertEquals(0.0, array.getDouble(22), 0.001)
        assertNull(array.getNumber(22))
        assertEquals(TEST_DATE, array.getString(22))
        assertEquals(Instant.parse(TEST_DATE), array.getDate(22))
        assertNull(array.getBlob(22))
        assertNull(array.getArray(22))
        assertNull(array.getDictionary(22))

        //#23 array.addArray(null);
        assertNull(array.getValue(23))
        assertFalse(array.getBoolean(23))
        assertEquals(0, array.getInt(23))
        assertEquals(0L, array.getLong(23))
        assertEquals(0.0f, array.getFloat(23), 0.001f)
        assertEquals(0.0, array.getDouble(23), 0.001)
        assertNull(array.getNumber(23))
        assertNull(array.getString(23))
        assertNull(array.getDate(23))
        assertNull(array.getBlob(23))
        assertNull(array.getArray(23))
        assertNull(array.getDictionary(23))

        //#24 array.addArray(simpleArray);
        assertTrue(array.getValue(24) is Array)
        assertTrue(array.getBoolean(24))
        assertEquals(0, array.getInt(24))
        assertEquals(0L, array.getLong(24))
        assertEquals(0.0f, array.getFloat(24), 0.001f)
        assertEquals(0.0, array.getDouble(24), 0.001)
        assertNull(array.getNumber(24))
        assertNull(array.getString(24))
        assertNull(array.getDate(24))
        assertNull(array.getBlob(24))
        assertTrue(array.getArray(24) is Array)
        assertNull(array.getDictionary(24))

        //#25 array.addDictionary(null);
        assertNull(array.getValue(25))
        assertFalse(array.getBoolean(25))
        assertEquals(0, array.getInt(25))
        assertEquals(0L, array.getLong(25))
        assertEquals(0.0f, array.getFloat(25), 0.001f)
        assertEquals(0.0, array.getDouble(25), 0.001)
        assertNull(array.getNumber(25))
        assertNull(array.getString(25))
        assertNull(array.getDate(25))
        assertNull(array.getBlob(25))
        assertNull(array.getArray(25))
        assertNull(array.getDictionary(25))

        //#26 array.addDictionary(simpleDict);
        assertTrue(array.getValue(26) is Dictionary)
        assertTrue(array.getBoolean(26))
        assertEquals(0, array.getInt(26))
        assertEquals(0L, array.getLong(26))
        assertEquals(0.0f, array.getFloat(26), 0.001f)
        assertEquals(0.0, array.getDouble(26), 0.001)
        assertNull(array.getNumber(26))
        assertNull(array.getString(26))
        assertNull(array.getDate(26))
        assertNull(array.getBlob(26))
        assertNull(array.getArray(26))
        assertTrue(array.getDictionary(26) is Dictionary)
    }

    protected fun makeDict(): MutableDictionary {
        // A small array
        val simpleArray = MutableArray()
        simpleArray.addInt(54)
        simpleArray.addString("Joplin")

        // A small dictionary
        val simpleDict = MutableDictionary()
        simpleDict.setInt("sdict.1", 58)
        simpleDict.setString("sdict.2", "Winehouse")

        // Dictionary:
        val dict = MutableDictionary()
        dict.setValue("dict-1", null)

        dict.setBoolean("dict-2", true)
        dict.setBoolean("dict-3", false)

        dict.setInt("dict-4", 0)
        dict.setInt("dict-5", Int.MIN_VALUE)
        dict.setInt("dict-6", Int.MAX_VALUE)

        dict.setLong("dict-7", 0L)
        dict.setLong("dict-8", Long.MIN_VALUE)
        dict.setLong("dict-9", Long.MAX_VALUE)

        dict.setFloat("dict-10", 0.0f)
        dict.setFloat("dict-11", Float.MIN_VALUE)
        dict.setFloat("dict-12", Float.MAX_VALUE)

        dict.setDouble("dict-13", 0.0)
        dict.setDouble("dict-14", Double.MIN_VALUE)
        dict.setDouble("dict-15", Double.MAX_VALUE)

        dict.setNumber("dict-16", null)
        dict.setNumber("dict-17", 0)
        dict.setNumber("dict-18", Float.MIN_VALUE)
        dict.setNumber("dict-19", Long.MIN_VALUE)

        dict.setString("dict-20", null)
        dict.setString("dict-21", "Quatro")

        dict.setDate("dict-22", null)
        dict.setDate("dict-23", Instant.parse(TEST_DATE))

        dict.setArray("dict-24", null)
        dict.setArray("dict-25", simpleArray)

        dict.setDictionary("dict-26", null)
        dict.setDictionary("dict-27", simpleDict)

        return dict
    }

    protected fun verifyDict(jObj: JsonObject) {
        assertEquals(27, jObj.size)

        assertEquals(JsonNull, jObj["dict-1"])

        assertEquals(true, jObj["dict-2"]?.jsonPrimitive?.boolean)
        assertEquals(false, jObj["dict-3"]?.jsonPrimitive?.boolean)

        assertEquals(0, jObj["dict-4"]?.jsonPrimitive?.int)
        assertEquals(Int.MIN_VALUE, jObj["dict-5"]?.jsonPrimitive?.int)
        assertEquals(Int.MAX_VALUE, jObj["dict-6"]?.jsonPrimitive?.int)

        assertEquals(0, jObj["dict-7"]?.jsonPrimitive?.long)
        assertEquals(Long.MIN_VALUE, jObj["dict-8"]?.jsonPrimitive?.long)
        assertEquals(Long.MAX_VALUE, jObj["dict-9"]?.jsonPrimitive?.long)

        assertEquals(0.0F, jObj["dict-10"]!!.jsonPrimitive.float, 0.001F)
        assertEquals(Float.MIN_VALUE, jObj["dict-11"]!!.jsonPrimitive.float, 0.001F)
        assertEquals(Float.MAX_VALUE, jObj["dict-12"]!!.jsonPrimitive.float, 100.0F)

        assertEquals(0.0, jObj["dict-13"]!!.jsonPrimitive.double, 0.001)
        assertEquals(Double.MIN_VALUE, jObj["dict-14"]!!.jsonPrimitive.double, 0.001)
        assertEquals(Double.MAX_VALUE, jObj["dict-15"]!!.jsonPrimitive.double, 1.0)

        assertEquals(JsonNull, jObj["dict-16"])
        assertEquals(0, jObj["dict-17"]?.jsonPrimitive?.long)
        assertEquals(Float.MIN_VALUE.toDouble(), jObj["dict-18"]!!.jsonPrimitive.double, 0.001)
        assertEquals(Long.MIN_VALUE, jObj["dict-19"]?.jsonPrimitive?.long)

        assertEquals(JsonNull, jObj["dict-20"])
        assertEquals("Quatro", jObj["dict-21"]?.jsonPrimitive?.content)

        assertEquals(JsonNull, jObj["dict-22"])
        assertEquals(TEST_DATE, jObj["dict-23"]?.jsonPrimitive?.content)

        assertEquals(JsonNull, jObj["dict-24"])
        assertTrue(jObj["dict-25"] is JsonArray)

        assertEquals(JsonNull, jObj["dict-26"])
        assertTrue(jObj["dict-27"] is JsonObject)
    }

    protected fun verifyDict(dict: Dictionary?) {
        assertNotNull(dict)

        assertEquals(27, dict.count)

        //#0 dict.setValue(null);
        assertNull(dict.getValue("dict-1"))
        assertFalse(dict.getBoolean("dict-1"))
        assertEquals(0, dict.getInt("dict-1"))
        assertEquals(0L, dict.getLong("dict-1"))
        assertEquals(0.0f, dict.getFloat("dict-1"), 0.001f)
        assertEquals(0.0, dict.getDouble("dict-1"), 0.001)
        assertNull(dict.getNumber("dict-1"))
        assertNull(dict.getString("dict-1"))
        assertNull(dict.getDate("dict-1"))
        assertNull(dict.getBlob("dict-1"))
        assertNull(dict.getArray("dict-1"))
        assertNull(dict.getDictionary("dict-1"))

        //#1 dict.setBoolean(true);
        assertEquals(true, dict.getValue("dict-2"))
        assertTrue(dict.getBoolean("dict-2"))
        assertEquals(1, dict.getInt("dict-2"))
        assertEquals(1L, dict.getLong("dict-2"))
        assertEquals(1.0f, dict.getFloat("dict-2"), 0.001f)
        assertEquals(1.0, dict.getDouble("dict-2"), 0.001)
        assertEquals(1, dict.getNumber("dict-2"))
        assertNull(dict.getString("dict-2"))
        assertNull(dict.getDate("dict-2"))
        assertNull(dict.getBlob("dict-2"))
        assertNull(dict.getArray("dict-2"))
        assertNull(dict.getDictionary("dict-2"))

        //#2 dict.setBoolean(false);
        assertEquals(false, dict.getValue("dict-3"))
        assertFalse(dict.getBoolean("dict-3"))
        assertEquals(0, dict.getInt("dict-3"))
        assertEquals(0L, dict.getLong("dict-3"))
        assertEquals(0.0f, dict.getFloat("dict-3"), 0.001f)
        assertEquals(0.0, dict.getDouble("dict-3"), 0.001)
        assertEquals(0, dict.getNumber("dict-3"))
        assertNull(dict.getString("dict-3"))
        assertNull(dict.getDate("dict-3"))
        assertNull(dict.getBlob("dict-3"))
        assertNull(dict.getArray("dict-3"))
        assertNull(dict.getDictionary("dict-3"))

        //#3 dict.setInt(0);
        assertEquals(0L, dict.getValue("dict-4"))
        assertFalse(dict.getBoolean("dict-4"))
        assertEquals(0, dict.getInt("dict-4"))
        assertEquals(0L, dict.getLong("dict-4"))
        assertEquals(0.0f, dict.getFloat("dict-4"), 0.001f)
        assertEquals(0.0, dict.getDouble("dict-4"), 0.001)
        assertEquals(0L, dict.getNumber("dict-4"))
        assertNull(dict.getString("dict-4"))
        assertNull(dict.getDate("dict-4"))
        assertNull(dict.getBlob("dict-4"))
        assertNull(dict.getArray("dict-4"))
        assertNull(dict.getDictionary("dict-4"))

        //#4 dict.setInt(Integer.MIN_VALUE);
        assertEquals(Int.MIN_VALUE.toLong(), dict.getValue("dict-5"))
        assertTrue(dict.getBoolean("dict-5"))
        assertEquals(Int.MIN_VALUE, dict.getInt("dict-5"))
        assertEquals(Int.MIN_VALUE.toLong(), dict.getLong("dict-5"))
        assertEquals(Int.MIN_VALUE.toFloat(), dict.getFloat("dict-5"), 0.001f)
        assertEquals(Int.MIN_VALUE.toDouble(), dict.getDouble("dict-5"), 0.001)
        assertEquals(Int.MIN_VALUE.toLong(), dict.getNumber("dict-5"))
        assertNull(dict.getString("dict-5"))
        assertNull(dict.getDate("dict-5"))
        assertNull(dict.getBlob("dict-5"))
        assertNull(dict.getArray("dict-5"))
        assertNull(dict.getDictionary("dict-5"))

        //#5 dict.setInt(Integer.MAX_VALUE);
        assertEquals(Int.MAX_VALUE.toLong(), dict.getValue("dict-6"))
        assertTrue(dict.getBoolean("dict-6"))
        assertEquals(Int.MAX_VALUE, dict.getInt("dict-6"))
        assertEquals(Int.MAX_VALUE.toLong(), dict.getLong("dict-6"))
        assertEquals(Int.MAX_VALUE.toFloat(), dict.getFloat("dict-6"), 100.0f)
        assertEquals(Int.MAX_VALUE.toDouble(), dict.getDouble("dict-6"), 100.0)
        assertEquals(Int.MAX_VALUE.toLong(), dict.getNumber("dict-6"))
        assertNull(dict.getString("dict-6"))
        assertNull(dict.getDate("dict-6"))
        assertNull(dict.getBlob("dict-6"))
        assertNull(dict.getArray("dict-6"))
        assertNull(dict.getDictionary("dict-6"))

        //#6 dict.setLong(0L);
        assertEquals(0L, dict.getValue("dict-7"))
        assertFalse(dict.getBoolean("dict-7"))
        assertEquals(0, dict.getInt("dict-7"))
        assertEquals(0L, dict.getLong("dict-7"))
        assertEquals(0.0f, dict.getFloat("dict-7"), 0.001f)
        assertEquals(0.0, dict.getDouble("dict-7"), 0.001)
        assertEquals(0L, dict.getNumber("dict-7"))
        assertNull(dict.getString("dict-7"))
        assertNull(dict.getDate("dict-7"))
        assertNull(dict.getBlob("dict-7"))
        assertNull(dict.getArray("dict-7"))
        assertNull(dict.getDictionary("dict-7"))

        //#7 dict.setLong(Long.MIN_VALUE);
        assertEquals(Long.MIN_VALUE, dict.getValue("dict-8"))
        // !!! Fails on iOS: assertFalse(dict.getBoolean("dict-8")) (any non-zero number should be true, but Long.MIN_VALUE is false on Android)
        assertEquals(Long.MIN_VALUE.toInt(), dict.getInt("dict-8"))
        assertEquals(Long.MIN_VALUE, dict.getLong("dict-8"))
        assertEquals(Long.MIN_VALUE.toFloat(), dict.getFloat("dict-8"), 0.001f)
        assertEquals(Long.MIN_VALUE.toDouble(), dict.getDouble("dict-8"), 0.001)
        assertEquals(Long.MIN_VALUE, dict.getNumber("dict-8"))
        assertNull(dict.getString("dict-8"))
        assertNull(dict.getDate("dict-8"))
        assertNull(dict.getBlob("dict-8"))
        assertNull(dict.getArray("dict-8"))
        assertNull(dict.getDictionary("dict-8"))

        //#8 dict.setLong(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, dict.getValue("dict-9"))
        assertTrue(dict.getBoolean("dict-9"))
        assertEquals(Long.MAX_VALUE.toInt(), dict.getInt("dict-9"))
        assertEquals(Long.MAX_VALUE, dict.getLong("dict-9"))
        assertEquals(Long.MAX_VALUE.toFloat(), dict.getFloat("dict-9"), 100.0f)
        assertEquals(Long.MAX_VALUE.toDouble(), dict.getDouble("dict-9"), 100.0)
        assertEquals(Long.MAX_VALUE, dict.getNumber("dict-9"))
        assertNull(dict.getString("dict-9"))
        assertNull(dict.getDate("dict-9"))
        assertNull(dict.getBlob("dict-9"))
        assertNull(dict.getArray("dict-9"))
        assertNull(dict.getDictionary("dict-9"))

        //#9 dict.setFloat(0.0F);
        assertEquals(0.0f, dict.getValue("dict-10"))
        assertFalse(dict.getBoolean("dict-10"))
        assertEquals(0, dict.getInt("dict-10"))
        assertEquals(0L, dict.getLong("dict-10"))
        assertEquals(0.0f, dict.getFloat("dict-10"), 0.001f)
        assertEquals(0.0, dict.getDouble("dict-10"), 0.001)
        assertEquals(0.0f, dict.getNumber("dict-10"))
        assertNull(dict.getString("dict-10"))
        assertNull(dict.getDate("dict-10"))
        assertNull(dict.getBlob("dict-10"))
        assertNull(dict.getArray("dict-10"))
        assertNull(dict.getDictionary("dict-10"))

        //#10 dict.setFloat(Float.MIN_VALUE);
        assertEquals(Float.MIN_VALUE, demoteToFloat(dict.getValue("dict-11")))
        // !!! Fails on iOS: assertFalse(dict.getBoolean("dict-11")) (any non-zero number should be true, but Float.MIN_VALUE is false on Android)
        assertEquals(Float.MIN_VALUE.toInt(), dict.getInt("dict-11"))
        assertEquals(Float.MIN_VALUE.toLong(), dict.getLong("dict-11"))
        assertEquals(Float.MIN_VALUE, dict.getFloat("dict-11"), 0.001f)
        assertEquals(Float.MIN_VALUE.toDouble(), dict.getDouble("dict-11"), 0.001)
        assertEquals(Float.MIN_VALUE, demoteToFloat(dict.getNumber("dict-11")))
        assertNull(dict.getString("dict-11"))
        assertNull(dict.getDate("dict-11"))
        assertNull(dict.getBlob("dict-11"))
        assertNull(dict.getArray("dict-11"))
        assertNull(dict.getDictionary("dict-11"))

        //#11 dict.setFloat(Float.MAX_VALUE);
        assertEquals(Float.MAX_VALUE, demoteToFloat(dict.getValue("dict-12")))
        assertTrue(dict.getBoolean("dict-12"))
        // ??? Fails: assertEquals(Float.MAX_VALUE.toInt(), dict.getInt("dict-12"))
        // ??? Fails: assertEquals(Float.MAX_VALUE.toLong(), dict.getLong("dict-12"))
        assertEquals(
            Float.MAX_VALUE.toDouble(),
            dict.getFloat("dict-12").toDouble(),
            1.0E32
        )
        assertEquals(Float.MAX_VALUE.toDouble(), dict.getDouble("dict-12"), 1.0E32)
        assertEquals(Float.MAX_VALUE, demoteToFloat(dict.getNumber("dict-12")))
        assertNull(dict.getString("dict-12"))
        assertNull(dict.getDate("dict-12"))
        assertNull(dict.getBlob("dict-12"))
        assertNull(dict.getArray("dict-12"))
        assertNull(dict.getDictionary("dict-12"))

        //#12 dict.setDouble(0.0);
        assertEquals(0f, dict.getValue("dict-13"))
        assertFalse(dict.getBoolean("dict-13"))
        assertEquals(0, dict.getInt("dict-13"))
        assertEquals(0L, dict.getLong("dict-13"))
        assertEquals(0.0f, dict.getFloat("dict-13"), 0.001f)
        assertEquals(0.0, dict.getDouble("dict-13"), 0.001)
        assertEquals(0f, dict.getNumber("dict-13"))
        assertNull(dict.getString("dict-13"))
        assertNull(dict.getDate("dict-13"))
        assertNull(dict.getBlob("dict-13"))
        assertNull(dict.getArray("dict-13"))
        assertNull(dict.getDictionary("dict-13"))

        //#13 dict.setDouble(Double.MIN_VALUE);
        assertEquals(Double.MIN_VALUE, dict.getValue("dict-14"))
        // !!! Fails on iOS: assertFalse(dict.getBoolean("dict-14")) (any non-zero number should be true, but Double.MIN_VALUE is false on Android)
        assertEquals(Double.MIN_VALUE.toInt(), dict.getInt("dict-14"))
        assertEquals(Double.MIN_VALUE.toLong(), dict.getLong("dict-14"))
        assertEquals(Double.MIN_VALUE.toFloat(), dict.getFloat("dict-14"), 0.001f)
        assertEquals(Double.MIN_VALUE, dict.getDouble("dict-14"), 0.001)
        assertEquals(Double.MIN_VALUE, dict.getNumber("dict-14"))
        assertNull(dict.getString("dict-14"))
        assertNull(dict.getDate("dict-14"))
        assertNull(dict.getBlob("dict-14"))
        assertNull(dict.getArray("dict-14"))
        assertNull(dict.getDictionary("dict-14"))

        //#14 dict.setDouble(Double.MAX_VALUE);
        assertEquals(Double.MAX_VALUE, dict.getValue("dict-15"))
        assertTrue(dict.getBoolean("dict-15"))
        // ??? Fails: assertEquals(Double.valueOf(Double.MAX_VALUE).intValue(), dict.getInt("dict-15"));
        // ??? Fails: assertEquals(Double.valueOf(Double.MAX_VALUE).longValue(), dict.getLong("dict-15"));
        assertEquals(Double.MAX_VALUE.toFloat(), dict.getFloat("dict-15"), 100.0f)
        assertEquals(Double.MAX_VALUE, dict.getDouble("dict-15"), 100.0)
        assertEquals(Double.MAX_VALUE, dict.getNumber("dict-15"))
        assertNull(dict.getString("dict-15"))
        assertNull(dict.getDate("dict-15"))
        assertNull(dict.getBlob("dict-15"))
        assertNull(dict.getArray("dict-15"))
        assertNull(dict.getDictionary("dict-15"))

        //#15 dict.setNumber(null);
        assertNull(dict.getValue("dict-16"))
        assertFalse(dict.getBoolean("dict-16"))
        assertEquals(0, dict.getInt("dict-16"))
        assertEquals(0L, dict.getLong("dict-16"))
        assertEquals(0.0f, dict.getFloat("dict-16"), 0.001f)
        assertEquals(0.0, dict.getDouble("dict-16"), 0.001)
        assertNull(dict.getNumber("dict-16"))
        assertNull(dict.getString("dict-16"))
        assertNull(dict.getDate("dict-16"))
        assertNull(dict.getBlob("dict-16"))
        assertNull(dict.getArray("dict-16"))
        assertNull(dict.getDictionary("dict-16"))

        //#16 dict.setNumber(0);
        assertEquals(0L, dict.getValue("dict-17"))
        assertFalse(dict.getBoolean("dict-17"))
        assertEquals(0, dict.getInt("dict-17"))
        assertEquals(0L, dict.getLong("dict-17"))
        assertEquals(0.0f, dict.getFloat("dict-17"), 0.001f)
        assertEquals(0.0, dict.getDouble("dict-17"), 0.001)
        assertEquals(0L, dict.getNumber("dict-17"))
        assertNull(dict.getString("dict-17"))
        assertNull(dict.getDate("dict-17"))
        assertNull(dict.getBlob("dict-17"))
        assertNull(dict.getArray("dict-17"))
        assertNull(dict.getDictionary("dict-17"))

        //#17 dict.setNumber(Float.MIN_VALUE);
        assertEquals(Float.MIN_VALUE, demoteToFloat(dict.getValue("dict-18")))
        // !!! Fails on iOS: assertFalse(dict.getBoolean("dict-18")) (any non-zero number should be true, but Float.MIN_VALUE is false on Android)
        assertEquals(Float.MIN_VALUE.toInt(), dict.getInt("dict-18"))
        assertEquals(Float.MIN_VALUE.toLong(), dict.getLong("dict-18"))
        assertEquals(Float.MIN_VALUE, dict.getFloat("dict-18"), 0.001f)
        assertEquals(Float.MIN_VALUE.toDouble(), dict.getDouble("dict-18"), 0.001)
        assertEquals(Float.MIN_VALUE, demoteToFloat(dict.getNumber("dict-18")))
        assertNull(dict.getString("dict-18"))
        assertNull(dict.getDate("dict-18"))
        assertNull(dict.getBlob("dict-18"))
        assertNull(dict.getArray("dict-18"))
        assertNull(dict.getDictionary("dict-18"))

        //#18 dict.setNumber(Long.MIN_VALUE);
        assertEquals(Long.MIN_VALUE, dict.getValue("dict-19"))
        // !!! Fails on iOS: assertFalse(dict.getBoolean("dict-19")) (any non-zero number should be true, but Long.MIN_VALUE is false on Android)
        assertEquals(Long.MIN_VALUE.toInt(), dict.getInt("dict-19"))
        assertEquals(Long.MIN_VALUE, dict.getLong("dict-19"))
        assertEquals(Long.MIN_VALUE.toFloat(), dict.getFloat("dict-19"), 0.001f)
        assertEquals(Long.MIN_VALUE.toDouble(), dict.getDouble("dict-19"), 0.001)
        assertEquals(Long.MIN_VALUE, dict.getNumber("dict-19"))
        assertNull(dict.getString("dict-19"))
        assertNull(dict.getDate("dict-19"))
        assertNull(dict.getBlob("dict-19"))
        assertNull(dict.getArray("dict-19"))
        assertNull(dict.getDictionary("dict-19"))

        //#19 dict.setString(null);
        assertNull(dict.getValue("dict-20"))
        assertFalse(dict.getBoolean("dict-20"))
        assertEquals(0, dict.getInt("dict-20"))
        assertEquals(0L, dict.getLong("dict-20"))
        assertEquals(0.0f, dict.getFloat("dict-20"), 0.001f)
        assertEquals(0.0, dict.getDouble("dict-20"), 0.001)
        assertNull(dict.getNumber("dict-20"))
        assertNull(dict.getString("dict-20"))
        assertNull(dict.getDate("dict-20"))
        assertNull(dict.getBlob("dict-20"))
        assertNull(dict.getArray("dict-20"))
        assertNull(dict.getDictionary("dict-20"))

        //#20 dict.setString("Quatro");
        assertEquals("Quatro", dict.getValue("dict-21"))
        assertTrue(dict.getBoolean("dict-21"))
        assertEquals(0, dict.getInt("dict-21"))
        assertEquals(0, dict.getLong("dict-21"))
        assertEquals(0.0f, dict.getFloat("dict-21"), 0.001f)
        assertEquals(0.0, dict.getDouble("dict-21"), 0.001)
        assertNull(dict.getNumber("dict-21"))
        assertEquals("Quatro", dict.getString("dict-21"))
        assertNull(dict.getDate("dict-21"))
        assertNull(dict.getBlob("dict-21"))
        assertNull(dict.getArray("dict-21"))
        assertNull(dict.getDictionary("dict-21"))

        //#21 dict.setDate(null);
        assertNull(dict.getValue("dict-22"))
        assertFalse(dict.getBoolean("dict-22"))
        assertEquals(0, dict.getInt("dict-22"))
        assertEquals(0L, dict.getLong("dict-22"))
        assertEquals(0.0f, dict.getFloat("dict-22"), 0.001f)
        assertEquals(0.0, dict.getDouble("dict-22"), 0.001)
        assertNull(dict.getNumber("dict-22"))
        assertNull(dict.getString("dict-22"))
        assertNull(dict.getDate("dict-22"))
        assertNull(dict.getBlob("dict-22"))
        assertNull(dict.getArray("dict-22"))
        assertNull(dict.getDictionary("dict-22"))

        //#22 dict.setDate(Instant.parse(TEST_DATE));
        assertEquals(TEST_DATE, dict.getValue("dict-23"))
        assertTrue(dict.getBoolean("dict-23"))
        assertEquals(0, dict.getInt("dict-23"))
        assertEquals(0L, dict.getLong("dict-23"))
        assertEquals(0.0f, dict.getFloat("dict-23"), 0.001f)
        assertEquals(0.0, dict.getDouble("dict-23"), 0.001)
        assertNull(dict.getNumber("dict-23"))
        assertEquals(TEST_DATE, dict.getString("dict-23"))
        assertEquals(Instant.parse(TEST_DATE), dict.getDate("dict-23"))
        assertNull(dict.getBlob("dict-23"))
        assertNull(dict.getArray("dict-23"))
        assertNull(dict.getDictionary("dict-23"))

        //#23 dict.setArray(null);
        assertNull(dict.getValue("dict-24"))
        assertFalse(dict.getBoolean("dict-24"))
        assertEquals(0, dict.getInt("dict-24"))
        assertEquals(0L, dict.getLong("dict-24"))
        assertEquals(0.0f, dict.getFloat("dict-24"), 0.001f)
        assertEquals(0.0, dict.getDouble("dict-24"), 0.001)
        assertNull(dict.getNumber("dict-24"))
        assertNull(dict.getString("dict-24"))
        assertNull(dict.getDate("dict-24"))
        assertNull(dict.getBlob("dict-24"))
        assertNull(dict.getArray("dict-24"))
        assertNull(dict.getDictionary("dict-24"))

        //#24 dict.setArray(simpleArray);
        assertTrue(dict.getValue("dict-25") is Array)
        assertTrue(dict.getBoolean("dict-25"))
        assertEquals(0, dict.getInt("dict-25"))
        assertEquals(0L, dict.getLong("dict-25"))
        assertEquals(0.0f, dict.getFloat("dict-25"), 0.001f)
        assertEquals(0.0, dict.getDouble("dict-25"), 0.001)
        assertNull(dict.getNumber("dict-25"))
        assertNull(dict.getString("dict-25"))
        assertNull(dict.getDate("dict-25"))
        assertNull(dict.getBlob("dict-25"))
        assertTrue(dict.getArray("dict-25") is Array)
        assertNull(dict.getDictionary("dict-25"))

        //#25 dict.setDictionary(null);
        assertNull(dict.getValue("dict-26"))
        assertFalse(dict.getBoolean("dict-26"))
        assertEquals(0, dict.getInt("dict-26"))
        assertEquals(0L, dict.getLong("dict-26"))
        assertEquals(0.0f, dict.getFloat("dict-26"), 0.001f)
        assertEquals(0.0, dict.getDouble("dict-26"), 0.001)
        assertNull(dict.getNumber("dict-26"))
        assertNull(dict.getString("dict-26"))
        assertNull(dict.getDate("dict-26"))
        assertNull(dict.getBlob("dict-26"))
        assertNull(dict.getArray("dict-26"))
        assertNull(dict.getDictionary("dict-26"))

        //#26 dict.setDictionary(simpleDict);
        assertTrue(dict.getValue("dict-27") is Dictionary)
        assertTrue(dict.getBoolean("dict-27"))
        assertEquals(0, dict.getInt("dict-27"))
        assertEquals(0L, dict.getLong("dict-27"))
        assertEquals(0.0f, dict.getFloat("dict-27"), 0.001f)
        assertEquals(0.0, dict.getDouble("dict-27"), 0.001)
        assertNull(dict.getNumber("dict-27"))
        assertNull(dict.getString("dict-27"))
        assertNull(dict.getDate("dict-27"))
        assertNull(dict.getBlob("dict-27"))
        assertNull(dict.getArray("dict-27"))
        assertTrue(dict.getDictionary("dict-27") is Dictionary)
    }

    protected fun makeBlob(): Blob {
        return Blob("text/plain", BLOB_CONTENT.encodeToByteArray())
    }

    protected fun verifyBlob(jBlob: JsonObject) {
        assertEquals(4, jBlob.size)
        // Blob.TYPE_BLOB Blob.META_PROP_TYPE
        assertEquals("blob", jBlob["@type"]?.jsonPrimitive?.content)
        // Blob.PROP_DIGEST
        assertEquals("sha1-C+QguVamTgLjyDQ9RzRtyCv6x60=", jBlob["digest"]?.jsonPrimitive?.content)
        // Blob.PROP_LENGTH
        assertEquals(59, jBlob["length"]?.jsonPrimitive?.long)
        // Blob.PROP_CONTENT_TYPE
        assertEquals("text/plain", jBlob["content_type"]?.jsonPrimitive?.content)
    }

    protected fun verifyBlob(blob: Blob?) {
        assertNotNull(blob)
        assertEquals("sha1-C+QguVamTgLjyDQ9RzRtyCv6x60=", blob.digest)
        assertEquals(59, blob.length)
        assertEquals("text/plain", blob.contentType)
        assertEquals(BLOB_CONTENT, blob.content?.decodeToString())
    }

    protected fun makeDocument(): MutableDocument {
        // Dictionary:
        val mDoc = MutableDocument()
        mDoc.setValue("doc-1", null)

        mDoc.setBoolean("doc-2", true)
        mDoc.setBoolean("doc-3", false)

        mDoc.setInt("doc-4", 0)
        mDoc.setInt("doc-5", Int.MIN_VALUE)
        mDoc.setInt("doc-6", Int.MAX_VALUE)

        mDoc.setLong("doc-7", 0L)
        mDoc.setLong("doc-8", Long.MIN_VALUE)
        mDoc.setLong("doc-9", Long.MAX_VALUE)

        mDoc.setFloat("doc-10", 0.0f)
        mDoc.setFloat("doc-11", Float.MIN_VALUE)
        mDoc.setFloat("doc-12", Float.MAX_VALUE)

        mDoc.setDouble("doc-13", 0.0)
        mDoc.setDouble("doc-14", Double.MIN_VALUE)
        mDoc.setDouble("doc-15", Double.MAX_VALUE)

        mDoc.setNumber("doc-16", null)
        mDoc.setNumber("doc-17", 0)
        mDoc.setNumber("doc-18", Float.MIN_VALUE)
        mDoc.setNumber("doc-19", Long.MIN_VALUE)

        mDoc.setString("doc-20", null)
        mDoc.setString("doc-21", "Jett")

        mDoc.setDate("doc-22", null)
        mDoc.setDate("doc-23", Instant.parse(TEST_DATE))

        mDoc.setArray("doc-24", null)
        mDoc.setArray("doc-25", makeArray())

        mDoc.setDictionary("doc-26", null)
        mDoc.setDictionary("doc-27", makeDict())

        mDoc.setBlob("doc-28", null)
        mDoc.setBlob("doc-29", makeBlob())

        return mDoc
    }

    protected fun verifyDocument(jObj: JsonObject) {
        assertEquals(29, jObj.size)

        assertEquals(JsonNull, jObj["doc-1"])

        assertEquals(true, jObj["doc-2"]?.jsonPrimitive?.boolean)
        assertEquals(false, jObj["doc-3"]?.jsonPrimitive?.boolean)

        assertEquals(0, jObj["doc-4"]?.jsonPrimitive?.int)
        assertEquals(Int.MIN_VALUE, jObj["doc-5"]?.jsonPrimitive?.int)
        assertEquals(Int.MAX_VALUE, jObj["doc-6"]?.jsonPrimitive?.int)

        assertEquals(0, jObj["doc-7"]?.jsonPrimitive?.long)
        assertEquals(Long.MIN_VALUE, jObj["doc-8"]?.jsonPrimitive?.long)
        assertEquals(Long.MAX_VALUE, jObj["doc-9"]?.jsonPrimitive?.long)

        assertEquals(0.0F, jObj["doc-10"]!!.jsonPrimitive.float, 0.001F)
        assertEquals(Float.MIN_VALUE, jObj["doc-11"]!!.jsonPrimitive.float, 0.001F)
        assertEquals(Float.MAX_VALUE, jObj["doc-12"]!!.jsonPrimitive.float, 100.0F)

        assertEquals(0.0, jObj["doc-13"]!!.jsonPrimitive.double, 0.001)
        assertEquals(Double.MIN_VALUE, jObj["doc-14"]!!.jsonPrimitive.double, 0.001)
        assertEquals(Double.MAX_VALUE, jObj["doc-15"]!!.jsonPrimitive.double, 1.0)

        assertEquals(JsonNull, jObj["doc-16"])
        assertEquals(0, jObj["doc-17"]?.jsonPrimitive?.long)
        assertEquals(Float.MIN_VALUE.toDouble(), jObj["doc-18"]!!.jsonPrimitive.double, 0.001)
        assertEquals(Long.MIN_VALUE, jObj["doc-19"]?.jsonPrimitive?.long)

        assertEquals(JsonNull, jObj["doc-20"])
        assertEquals("Jett", jObj["doc-21"]?.jsonPrimitive?.content)

        assertEquals(JsonNull, jObj["doc-22"])
        assertEquals(TEST_DATE, jObj["doc-23"]?.jsonPrimitive?.content)

        assertEquals(JsonNull, jObj["doc-24"])
        verifyArray(jObj["doc-25"] as JsonArray)

        assertEquals(JsonNull, jObj["doc-26"])
        verifyDict(jObj["doc-27"] as JsonObject)

        assertEquals(JsonNull, jObj["doc-28"])
        verifyBlob(jObj["doc-29"]!!.jsonObject)
    }

    protected fun verifyDocument(doc: Dictionary) {
        assertEquals(29, doc.count)

        //#0 doc.setValue(null);
        assertNull(doc.getValue("doc-1"))
        assertFalse(doc.getBoolean("doc-1"))
        assertEquals(0, doc.getInt("doc-1"))
        assertEquals(0L, doc.getLong("doc-1"))
        assertEquals(0.0f, doc.getFloat("doc-1"), 0.001f)
        assertEquals(0.0, doc.getDouble("doc-1"), 0.001)
        assertNull(doc.getNumber("doc-1"))
        assertNull(doc.getString("doc-1"))
        assertNull(doc.getDate("doc-1"))
        assertNull(doc.getBlob("doc-1"))
        assertNull(doc.getArray("doc-1"))
        assertNull(doc.getDictionary("doc-1"))

        //#1 doc.setBoolean(true);
        assertEquals(true, doc.getValue("doc-2"))
        assertTrue(doc.getBoolean("doc-2"))
        assertEquals(1, doc.getInt("doc-2"))
        assertEquals(1L, doc.getLong("doc-2"))
        assertEquals(1.0f, doc.getFloat("doc-2"), 0.001f)
        assertEquals(1.0, doc.getDouble("doc-2"), 0.001)
        assertEquals(1, doc.getNumber("doc-2"))
        assertNull(doc.getString("doc-2"))
        assertNull(doc.getDate("doc-2"))
        assertNull(doc.getBlob("doc-2"))
        assertNull(doc.getArray("doc-2"))
        assertNull(doc.getDictionary("doc-2"))

        //#2 doc.setBoolean(false);
        assertEquals(false, doc.getValue("doc-3"))
        assertFalse(doc.getBoolean("doc-3"))
        assertEquals(0, doc.getInt("doc-3"))
        assertEquals(0L, doc.getLong("doc-3"))
        assertEquals(0.0f, doc.getFloat("doc-3"), 0.001f)
        assertEquals(0.0, doc.getDouble("doc-3"), 0.001)
        assertEquals(0, doc.getNumber("doc-3"))
        assertNull(doc.getString("doc-3"))
        assertNull(doc.getDate("doc-3"))
        assertNull(doc.getBlob("doc-3"))
        assertNull(doc.getArray("doc-3"))
        assertNull(doc.getDictionary("doc-3"))

        //#3 doc.setInt(0);
        assertEquals(0L, doc.getValue("doc-4"))
        assertFalse(doc.getBoolean("doc-4"))
        assertEquals(0, doc.getInt("doc-4"))
        assertEquals(0L, doc.getLong("doc-4"))
        assertEquals(0.0f, doc.getFloat("doc-4"), 0.001f)
        assertEquals(0.0, doc.getDouble("doc-4"), 0.001)
        assertEquals(0L, doc.getNumber("doc-4"))
        assertNull(doc.getString("doc-4"))
        assertNull(doc.getDate("doc-4"))
        assertNull(doc.getBlob("doc-4"))
        assertNull(doc.getArray("doc-4"))
        assertNull(doc.getDictionary("doc-4"))

        //#4 doc.setInt(Integer.MIN_VALUE);
        assertEquals(Int.MIN_VALUE.toLong(), doc.getValue("doc-5"))
        assertTrue(doc.getBoolean("doc-5"))
        assertEquals(Int.MIN_VALUE, doc.getInt("doc-5"))
        assertEquals(Int.MIN_VALUE.toLong(), doc.getLong("doc-5"))
        assertEquals(Int.MIN_VALUE.toFloat(), doc.getFloat("doc-5"), 0.001f)
        assertEquals(Int.MIN_VALUE.toDouble(), doc.getDouble("doc-5"), 0.001)
        assertEquals(Int.MIN_VALUE.toLong(), doc.getNumber("doc-5"))
        assertNull(doc.getString("doc-5"))
        assertNull(doc.getDate("doc-5"))
        assertNull(doc.getBlob("doc-5"))
        assertNull(doc.getArray("doc-5"))
        assertNull(doc.getDictionary("doc-5"))

        //#5 doc.setInt(Integer.MAX_VALUE);
        assertEquals(Int.MAX_VALUE.toLong(), doc.getValue("doc-6"))
        assertTrue(doc.getBoolean("doc-6"))
        assertEquals(Int.MAX_VALUE, doc.getInt("doc-6"))
        assertEquals(Int.MAX_VALUE.toLong(), doc.getLong("doc-6"))
        assertEquals(Int.MAX_VALUE.toFloat(), doc.getFloat("doc-6"), 100.0f)
        assertEquals(Int.MAX_VALUE.toDouble(), doc.getDouble("doc-6"), 100.0)
        assertEquals(Int.MAX_VALUE.toLong(), doc.getNumber("doc-6"))
        assertNull(doc.getString("doc-6"))
        assertNull(doc.getDate("doc-6"))
        assertNull(doc.getBlob("doc-6"))
        assertNull(doc.getArray("doc-6"))
        assertNull(doc.getDictionary("doc-6"))

        //#6 doc.setLong(0L);
        assertEquals(0L, doc.getValue("doc-7"))
        assertFalse(doc.getBoolean("doc-7"))
        assertEquals(0, doc.getInt("doc-7"))
        assertEquals(0L, doc.getLong("doc-7"))
        assertEquals(0.0f, doc.getFloat("doc-7"), 0.001f)
        assertEquals(0.0, doc.getDouble("doc-7"), 0.001)
        assertEquals(0L, doc.getNumber("doc-7"))
        assertNull(doc.getString("doc-7"))
        assertNull(doc.getDate("doc-7"))
        assertNull(doc.getBlob("doc-7"))
        assertNull(doc.getArray("doc-7"))
        assertNull(doc.getDictionary("doc-7"))

        //#7 doc.setLong(Long.MIN_VALUE);
        assertEquals(Long.MIN_VALUE, doc.getValue("doc-8"))
        // ??? Value differs for Documents and Results: assertTrue(doc.getBoolean("doc-8"));
        assertEquals(Long.MIN_VALUE.toInt(), doc.getInt("doc-8"))
        assertEquals(Long.MIN_VALUE, doc.getLong("doc-8"))
        assertEquals(Long.MIN_VALUE.toFloat(), doc.getFloat("doc-8"), 0.001f)
        assertEquals(Long.MIN_VALUE.toDouble(), doc.getDouble("doc-8"), 0.001)
        assertEquals(Long.MIN_VALUE, doc.getNumber("doc-8"))
        assertNull(doc.getString("doc-8"))
        assertNull(doc.getDate("doc-8"))
        assertNull(doc.getBlob("doc-8"))
        assertNull(doc.getArray("doc-8"))
        assertNull(doc.getDictionary("doc-8"))

        //#8 doc.setLong(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, doc.getValue("doc-9"))
        assertTrue(doc.getBoolean("doc-9"))
        assertEquals(Long.MAX_VALUE.toInt(), doc.getInt("doc-9"))
        assertEquals(Long.MAX_VALUE, doc.getLong("doc-9"))
        assertEquals(Long.MAX_VALUE.toFloat(), doc.getFloat("doc-9"), 100.0f)
        assertEquals(Long.MAX_VALUE.toDouble(), doc.getDouble("doc-9"), 100.0)
        assertEquals(Long.MAX_VALUE, doc.getNumber("doc-9"))
        assertNull(doc.getString("doc-9"))
        assertNull(doc.getDate("doc-9"))
        assertNull(doc.getBlob("doc-9"))
        assertNull(doc.getArray("doc-9"))
        assertNull(doc.getDictionary("doc-9"))

        //#9 doc.setFloat(0.0F);
        assertEquals(0.0f, doc.getValue("doc-10"))
        assertFalse(doc.getBoolean("doc-10"))
        assertEquals(0, doc.getInt("doc-10"))
        assertEquals(0L, doc.getLong("doc-10"))
        assertEquals(0.0f, doc.getFloat("doc-10"), 0.001f)
        assertEquals(0.0, doc.getDouble("doc-10"), 0.001)
        assertEquals(0.0f, doc.getNumber("doc-10"))
        assertNull(doc.getString("doc-10"))
        assertNull(doc.getDate("doc-10"))
        assertNull(doc.getBlob("doc-10"))
        assertNull(doc.getArray("doc-10"))
        assertNull(doc.getDictionary("doc-10"))

        //#10 doc.setFloat(Float.MIN_VALUE);
        assertEquals(Float.MIN_VALUE, demoteToFloat(doc.getValue("doc-11")))
        // !!! Fails on iOS: assertFalse(doc.getBoolean("doc-11")) (any non-zero number should be true, but Float.MIN_VALUE is false on Android)
        assertEquals(Float.MIN_VALUE.toInt(), doc.getInt("doc-11"))
        assertEquals(Float.MIN_VALUE.toLong(), doc.getLong("doc-11"))
        assertEquals(Float.MIN_VALUE, doc.getFloat("doc-11"), 0.001f)
        assertEquals(Float.MIN_VALUE.toDouble(), doc.getDouble("doc-11"), 0.001)
        assertEquals(Float.MIN_VALUE, demoteToFloat(doc.getValue("doc-11")))
        assertNull(doc.getString("doc-11"))
        assertNull(doc.getDate("doc-11"))
        assertNull(doc.getBlob("doc-11"))
        assertNull(doc.getArray("doc-11"))
        assertNull(doc.getDictionary("doc-11"))

        //#11 doc.setFloat(Float.MAX_VALUE);
        assertEquals(Float.MAX_VALUE, demoteToFloat(doc.getValue("doc-12")))
        assertTrue(doc.getBoolean("doc-12"))
        // ??? Fails: assertEquals(Float.MAX_VALUE.toInt(), doc.getInt("doc-12"))
        // ??? Fails: assertEquals(Float.MAX_VALUE.toLong(), doc.getLong("doc-12"))
        assertEquals(Float.MAX_VALUE, doc.getFloat("doc-12"), 1.0E32F)
        assertEquals(Float.MAX_VALUE.toDouble(), doc.getDouble("doc-12"), 1.0E32)
        assertEquals(Float.MAX_VALUE, demoteToFloat(doc.getNumber("doc-12")))
        assertNull(doc.getString("doc-12"))
        assertNull(doc.getDate("doc-12"))
        assertNull(doc.getBlob("doc-12"))
        assertNull(doc.getArray("doc-12"))
        assertNull(doc.getDictionary("doc-12"))

        //#12 doc.setDouble(0.0);
        assertEquals(0f, doc.getValue("doc-13"))
        assertFalse(doc.getBoolean("doc-13"))
        assertEquals(0, doc.getInt("doc-13"))
        assertEquals(0L, doc.getLong("doc-13"))
        assertEquals(0.0f, doc.getFloat("doc-13"), 0.001f)
        assertEquals(0.0, doc.getDouble("doc-13"), 0.001)
        assertEquals(0f, doc.getNumber("doc-13"))
        assertNull(doc.getString("doc-13"))
        assertNull(doc.getDate("doc-13"))
        assertNull(doc.getBlob("doc-13"))
        assertNull(doc.getArray("doc-13"))
        assertNull(doc.getDictionary("doc-13"))

        //#13 doc.setDouble(Double.MIN_VALUE);
        assertEquals(Double.MIN_VALUE, doc.getValue("doc-14"))
        // !!! Fails on iOS: assertFalse(doc.getBoolean("doc-14")) (any non-zero number should be true, but Double.MIN_VALUE is false on Android)
        assertEquals(Double.MIN_VALUE.toInt(), doc.getInt("doc-14"))
        assertEquals(Double.MIN_VALUE.toLong(), doc.getLong("doc-14"))
        assertEquals(Double.MIN_VALUE.toFloat(), doc.getFloat("doc-14"), 0.001f)
        assertEquals(Double.MIN_VALUE, doc.getDouble("doc-14"), 0.001)
        assertEquals(Double.MIN_VALUE, doc.getNumber("doc-14"))
        assertNull(doc.getString("doc-14"))
        assertNull(doc.getDate("doc-14"))
        assertNull(doc.getBlob("doc-14"))
        assertNull(doc.getArray("doc-14"))
        assertNull(doc.getDictionary("doc-14"))

        //#14 doc.setDouble(Double.MAX_VALUE);
        assertEquals(Double.MAX_VALUE, doc.getValue("doc-15"))
        // ??? Fails: assertEquals(Double.MAX_VALUE.toInt(), doc.getInt("doc-15"))
        // ??? Fails: assertEquals(Double.MAX_VALUE.toLong(), doc.getLong("doc-15"))
        assertEquals(Double.MAX_VALUE.toFloat(), doc.getFloat("doc-15"), 100.0f)
        assertEquals(Double.MAX_VALUE, doc.getDouble("doc-15"), 100.0)
        assertEquals(Double.MAX_VALUE, doc.getNumber("doc-15"))
        assertNull(doc.getString("doc-15"))
        assertNull(doc.getDate("doc-15"))
        assertNull(doc.getBlob("doc-15"))
        assertNull(doc.getArray("doc-15"))
        assertNull(doc.getDictionary("doc-15"))

        //#15 doc.setNumber(null);
        assertNull(doc.getValue("doc-16"))
        assertFalse(doc.getBoolean("doc-16"))
        assertEquals(0, doc.getInt("doc-16"))
        assertEquals(0L, doc.getLong("doc-16"))
        assertEquals(0.0f, doc.getFloat("doc-16"), 0.001f)
        assertEquals(0.0, doc.getDouble("doc-16"), 0.001)
        assertNull(doc.getNumber("doc-16"))
        assertNull(doc.getString("doc-16"))
        assertNull(doc.getDate("doc-16"))
        assertNull(doc.getBlob("doc-16"))
        assertNull(doc.getArray("doc-16"))
        assertNull(doc.getDictionary("doc-16"))

        //#16 doc.setNumber(0);
        assertEquals(0L, doc.getValue("doc-17"))
        assertFalse(doc.getBoolean("doc-17"))
        assertEquals(0, doc.getInt("doc-17"))
        assertEquals(0L, doc.getLong("doc-17"))
        assertEquals(0.0f, doc.getFloat("doc-17"), 0.001f)
        assertEquals(0.0, doc.getDouble("doc-17"), 0.001)
        assertEquals(0L, doc.getNumber("doc-17"))
        assertNull(doc.getString("doc-17"))
        assertNull(doc.getDate("doc-17"))
        assertNull(doc.getBlob("doc-17"))
        assertNull(doc.getArray("doc-17"))
        assertNull(doc.getDictionary("doc-17"))

        //#17 doc.setNumber(Float.MIN_VALUE);
        assertEquals(Float.MIN_VALUE, demoteToFloat(doc.getValue("doc-18")))
        // !!! Fails on iOS: assertFalse(doc.getBoolean("doc-18")) (any non-zero number should be true, but Float.MIN_VALUE is false on Android)
        assertEquals(Float.MIN_VALUE.toInt(), doc.getInt("doc-18"))
        assertEquals(Float.MIN_VALUE.toLong(), doc.getLong("doc-18"))
        assertEquals(Float.MIN_VALUE, doc.getFloat("doc-18"), 0.001f)
        assertEquals(Float.MIN_VALUE.toDouble(), doc.getDouble("doc-18"), 0.001)
        assertEquals(Float.MIN_VALUE, demoteToFloat(doc.getNumber("doc-18")))
        assertNull(doc.getString("doc-18"))
        assertNull(doc.getDate("doc-18"))
        assertNull(doc.getBlob("doc-18"))
        assertNull(doc.getArray("doc-18"))
        assertNull(doc.getDictionary("doc-18"))

        //#18 doc.setNumber(Long.MIN_VALUE);
        assertEquals(Long.MIN_VALUE, doc.getValue("doc-19"))
        // ??? Value differs for Documents and Results: assertTrue(doc.getBoolean("doc-19"))
        assertEquals(Long.MIN_VALUE.toInt(), doc.getInt("doc-19"))
        assertEquals(Long.MIN_VALUE, doc.getLong("doc-19"))
        assertEquals(Long.MIN_VALUE.toFloat(), doc.getFloat("doc-19"), 0.001f)
        assertEquals(Long.MIN_VALUE.toDouble(), doc.getDouble("doc-19"), 0.001)
        assertEquals(Long.MIN_VALUE, doc.getNumber("doc-19"))
        assertNull(doc.getString("doc-19"))
        assertNull(doc.getDate("doc-19"))
        assertNull(doc.getBlob("doc-19"))
        assertNull(doc.getArray("doc-19"))
        assertNull(doc.getDictionary("doc-19"))

        //#19 doc.setString(null);
        assertNull(doc.getValue("doc-20"))
        assertFalse(doc.getBoolean("doc-20"))
        assertEquals(0, doc.getInt("doc-20"))
        assertEquals(0L, doc.getLong("doc-20"))
        assertEquals(0.0f, doc.getFloat("doc-20"), 0.001f)
        assertEquals(0.0, doc.getDouble("doc-20"), 0.001)
        assertNull(doc.getNumber("doc-20"))
        assertNull(doc.getString("doc-20"))
        assertNull(doc.getDate("doc-20"))
        assertNull(doc.getBlob("doc-20"))
        assertNull(doc.getArray("doc-20"))
        assertNull(doc.getDictionary("doc-20"))

        //#20 doc.setString("Quatro");
        assertEquals("Jett", doc.getValue("doc-21"))
        assertTrue(doc.getBoolean("doc-21"))
        assertEquals(0, doc.getInt("doc-21"))
        assertEquals(0, doc.getLong("doc-21"))
        assertEquals(0.0f, doc.getFloat("doc-21"), 0.001f)
        assertEquals(0.0, doc.getDouble("doc-21"), 0.001)
        assertNull(doc.getNumber("doc-21"))
        assertEquals("Jett", doc.getString("doc-21"))
        assertNull(doc.getDate("doc-21"))
        assertNull(doc.getBlob("doc-21"))
        assertNull(doc.getArray("doc-21"))
        assertNull(doc.getDictionary("doc-21"))

        //#21 doc.setDate(null);
        assertNull(doc.getValue("doc-22"))
        assertFalse(doc.getBoolean("doc-22"))
        assertEquals(0, doc.getInt("doc-22"))
        assertEquals(0L, doc.getLong("doc-22"))
        assertEquals(0.0f, doc.getFloat("doc-22"), 0.001f)
        assertEquals(0.0, doc.getDouble("doc-22"), 0.001)
        assertNull(doc.getNumber("doc-22"))
        assertNull(doc.getString("doc-22"))
        assertNull(doc.getDate("doc-22"))
        assertNull(doc.getBlob("doc-22"))
        assertNull(doc.getArray("doc-22"))
        assertNull(doc.getDictionary("doc-22"))

        //#22 doc.setDate(Instant.parse(TEST_DATE));
        assertEquals(TEST_DATE, doc.getValue("doc-23"))
        assertTrue(doc.getBoolean("doc-23"))
        assertEquals(0, doc.getInt("doc-23"))
        assertEquals(0L, doc.getLong("doc-23"))
        assertEquals(0.0f, doc.getFloat("doc-23"), 0.001f)
        assertEquals(0.0, doc.getDouble("doc-23"), 0.001)
        assertNull(doc.getNumber("doc-23"))
        assertEquals(TEST_DATE, doc.getString("doc-23"))
        assertEquals(Instant.parse(TEST_DATE), doc.getDate("doc-23"))
        assertNull(doc.getBlob("doc-23"))
        assertNull(doc.getArray("doc-23"))
        assertNull(doc.getDictionary("doc-23"))

        //#23 doc.setArray(null);
        assertNull(doc.getValue("doc-24"))
        assertFalse(doc.getBoolean("doc-24"))
        assertEquals(0, doc.getInt("doc-24"))
        assertEquals(0L, doc.getLong("doc-24"))
        assertEquals(0.0f, doc.getFloat("doc-24"), 0.001f)
        assertEquals(0.0, doc.getDouble("doc-24"), 0.001)
        assertNull(doc.getNumber("doc-24"))
        assertNull(doc.getString("doc-24"))
        assertNull(doc.getDate("doc-24"))
        assertNull(doc.getBlob("doc-24"))
        assertNull(doc.getArray("doc-24"))
        assertNull(doc.getDictionary("doc-24"))

        //#24 doc.setDictionary(null);
        verifyArray(doc.getArray("doc-25"))

        //#25 doc.setDictionary(null);
        assertNull(doc.getValue("doc-26"))
        assertFalse(doc.getBoolean("doc-26"))
        assertEquals(0, doc.getInt("doc-26"))
        assertEquals(0L, doc.getLong("doc-26"))
        assertEquals(0.0f, doc.getFloat("doc-26"), 0.001f)
        assertEquals(0.0, doc.getDouble("doc-26"), 0.001)
        assertNull(doc.getNumber("doc-26"))
        assertNull(doc.getString("doc-26"))
        assertNull(doc.getDate("doc-26"))
        assertNull(doc.getBlob("doc-26"))
        assertNull(doc.getArray("doc-26"))
        assertNull(doc.getDictionary("doc-26"))

        //#26 doc.setDictionary(simpleDict);
        verifyDict(doc.getDictionary("doc-27"))

        //#27 doc.setDictionary(null);
        assertNull(doc.getValue("doc-28"))
        assertFalse(doc.getBoolean("doc-28"))
        assertEquals(0, doc.getInt("doc-28"))
        assertEquals(0L, doc.getLong("doc-28"))
        assertEquals(0.0f, doc.getFloat("doc-28"), 0.001f)
        assertEquals(0.0, doc.getDouble("doc-28"), 0.001)
        assertNull(doc.getNumber("doc-28"))
        assertNull(doc.getString("doc-28"))
        assertNull(doc.getDate("doc-28"))
        assertNull(doc.getBlob("doc-28"))
        assertNull(doc.getArray("doc-28"))
        assertNull(doc.getDictionary("doc-28"))
        verifyBlob(doc.getBlob("doc-29"))
    }

    // identical to verifyDocument(doc: Dictionary) (since publicly they don't share an interface)
    protected fun verifyDocument(doc: Result) {
        assertEquals(29, doc.count)

        //#0 doc.setValue(null);
        assertNull(doc.getValue("doc-1"))
        assertFalse(doc.getBoolean("doc-1"))
        assertEquals(0, doc.getInt("doc-1"))
        assertEquals(0L, doc.getLong("doc-1"))
        assertEquals(0.0f, doc.getFloat("doc-1"), 0.001f)
        assertEquals(0.0, doc.getDouble("doc-1"), 0.001)
        assertNull(doc.getNumber("doc-1"))
        assertNull(doc.getString("doc-1"))
        assertNull(doc.getDate("doc-1"))
        assertNull(doc.getBlob("doc-1"))
        assertNull(doc.getArray("doc-1"))
        assertNull(doc.getDictionary("doc-1"))

        //#1 doc.setBoolean(true);
        assertEquals(true, doc.getValue("doc-2"))
        assertTrue(doc.getBoolean("doc-2"))
        assertEquals(1, doc.getInt("doc-2"))
        assertEquals(1L, doc.getLong("doc-2"))
        assertEquals(1.0f, doc.getFloat("doc-2"), 0.001f)
        assertEquals(1.0, doc.getDouble("doc-2"), 0.001)
        assertEquals(1, doc.getNumber("doc-2"))
        assertNull(doc.getString("doc-2"))
        assertNull(doc.getDate("doc-2"))
        assertNull(doc.getBlob("doc-2"))
        assertNull(doc.getArray("doc-2"))
        assertNull(doc.getDictionary("doc-2"))

        //#2 doc.setBoolean(false);
        assertEquals(false, doc.getValue("doc-3"))
        assertFalse(doc.getBoolean("doc-3"))
        assertEquals(0, doc.getInt("doc-3"))
        assertEquals(0L, doc.getLong("doc-3"))
        assertEquals(0.0f, doc.getFloat("doc-3"), 0.001f)
        assertEquals(0.0, doc.getDouble("doc-3"), 0.001)
        assertEquals(0, doc.getNumber("doc-3"))
        assertNull(doc.getString("doc-3"))
        assertNull(doc.getDate("doc-3"))
        assertNull(doc.getBlob("doc-3"))
        assertNull(doc.getArray("doc-3"))
        assertNull(doc.getDictionary("doc-3"))

        //#3 doc.setInt(0);
        assertEquals(0L, doc.getValue("doc-4"))
        assertFalse(doc.getBoolean("doc-4"))
        assertEquals(0, doc.getInt("doc-4"))
        assertEquals(0L, doc.getLong("doc-4"))
        assertEquals(0.0f, doc.getFloat("doc-4"), 0.001f)
        assertEquals(0.0, doc.getDouble("doc-4"), 0.001)
        assertEquals(0L, doc.getNumber("doc-4"))
        assertNull(doc.getString("doc-4"))
        assertNull(doc.getDate("doc-4"))
        assertNull(doc.getBlob("doc-4"))
        assertNull(doc.getArray("doc-4"))
        assertNull(doc.getDictionary("doc-4"))

        //#4 doc.setInt(Integer.MIN_VALUE);
        assertEquals(Int.MIN_VALUE.toLong(), doc.getValue("doc-5"))
        assertTrue(doc.getBoolean("doc-5"))
        assertEquals(Int.MIN_VALUE, doc.getInt("doc-5"))
        assertEquals(Int.MIN_VALUE.toLong(), doc.getLong("doc-5"))
        assertEquals(Int.MIN_VALUE.toFloat(), doc.getFloat("doc-5"), 0.001f)
        assertEquals(Int.MIN_VALUE.toDouble(), doc.getDouble("doc-5"), 0.001)
        assertEquals(Int.MIN_VALUE.toLong(), doc.getNumber("doc-5"))
        assertNull(doc.getString("doc-5"))
        assertNull(doc.getDate("doc-5"))
        assertNull(doc.getBlob("doc-5"))
        assertNull(doc.getArray("doc-5"))
        assertNull(doc.getDictionary("doc-5"))

        //#5 doc.setInt(Integer.MAX_VALUE);
        assertEquals(Int.MAX_VALUE.toLong(), doc.getValue("doc-6"))
        assertTrue(doc.getBoolean("doc-6"))
        assertEquals(Int.MAX_VALUE, doc.getInt("doc-6"))
        assertEquals(Int.MAX_VALUE.toLong(), doc.getLong("doc-6"))
        assertEquals(Int.MAX_VALUE.toFloat(), doc.getFloat("doc-6"), 100.0f)
        assertEquals(Int.MAX_VALUE.toDouble(), doc.getDouble("doc-6"), 100.0)
        assertEquals(Int.MAX_VALUE.toLong(), doc.getNumber("doc-6"))
        assertNull(doc.getString("doc-6"))
        assertNull(doc.getDate("doc-6"))
        assertNull(doc.getBlob("doc-6"))
        assertNull(doc.getArray("doc-6"))
        assertNull(doc.getDictionary("doc-6"))

        //#6 doc.setLong(0L);
        assertEquals(0L, doc.getValue("doc-7"))
        assertFalse(doc.getBoolean("doc-7"))
        assertEquals(0, doc.getInt("doc-7"))
        assertEquals(0L, doc.getLong("doc-7"))
        assertEquals(0.0f, doc.getFloat("doc-7"), 0.001f)
        assertEquals(0.0, doc.getDouble("doc-7"), 0.001)
        assertEquals(0L, doc.getNumber("doc-7"))
        assertNull(doc.getString("doc-7"))
        assertNull(doc.getDate("doc-7"))
        assertNull(doc.getBlob("doc-7"))
        assertNull(doc.getArray("doc-7"))
        assertNull(doc.getDictionary("doc-7"))

        //#7 doc.setLong(Long.MIN_VALUE);
        assertEquals(Long.MIN_VALUE, doc.getValue("doc-8"))
        // ??? Value differs for Documents and Results: assertTrue(doc.getBoolean("doc-8"));
        assertEquals(Long.MIN_VALUE.toInt(), doc.getInt("doc-8"))
        assertEquals(Long.MIN_VALUE, doc.getLong("doc-8"))
        assertEquals(Long.MIN_VALUE.toFloat(), doc.getFloat("doc-8"), 0.001f)
        assertEquals(Long.MIN_VALUE.toDouble(), doc.getDouble("doc-8"), 0.001)
        assertEquals(Long.MIN_VALUE, doc.getNumber("doc-8"))
        assertNull(doc.getString("doc-8"))
        assertNull(doc.getDate("doc-8"))
        assertNull(doc.getBlob("doc-8"))
        assertNull(doc.getArray("doc-8"))
        assertNull(doc.getDictionary("doc-8"))

        //#8 doc.setLong(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, doc.getValue("doc-9"))
        assertTrue(doc.getBoolean("doc-9"))
        assertEquals(Long.MAX_VALUE.toInt(), doc.getInt("doc-9"))
        assertEquals(Long.MAX_VALUE, doc.getLong("doc-9"))
        assertEquals(Long.MAX_VALUE.toFloat(), doc.getFloat("doc-9"), 100.0f)
        assertEquals(Long.MAX_VALUE.toDouble(), doc.getDouble("doc-9"), 100.0)
        assertEquals(Long.MAX_VALUE, doc.getNumber("doc-9"))
        assertNull(doc.getString("doc-9"))
        assertNull(doc.getDate("doc-9"))
        assertNull(doc.getBlob("doc-9"))
        assertNull(doc.getArray("doc-9"))
        assertNull(doc.getDictionary("doc-9"))

        //#9 doc.setFloat(0.0F);
        assertEquals(0.0f, doc.getValue("doc-10"))
        assertFalse(doc.getBoolean("doc-10"))
        assertEquals(0, doc.getInt("doc-10"))
        assertEquals(0L, doc.getLong("doc-10"))
        assertEquals(0.0f, doc.getFloat("doc-10"), 0.001f)
        assertEquals(0.0, doc.getDouble("doc-10"), 0.001)
        assertEquals(0.0f, doc.getNumber("doc-10"))
        assertNull(doc.getString("doc-10"))
        assertNull(doc.getDate("doc-10"))
        assertNull(doc.getBlob("doc-10"))
        assertNull(doc.getArray("doc-10"))
        assertNull(doc.getDictionary("doc-10"))

        //#10 doc.setFloat(Float.MIN_VALUE);
        assertEquals(Float.MIN_VALUE, demoteToFloat(doc.getValue("doc-11")))
        // !!! Fails on iOS: assertFalse(doc.getBoolean("doc-11")) (any non-zero number should be true, but Float.MIN_VALUE is false on Android)
        assertEquals(Float.MIN_VALUE.toInt(), doc.getInt("doc-11"))
        assertEquals(Float.MIN_VALUE.toLong(), doc.getLong("doc-11"))
        assertEquals(Float.MIN_VALUE, doc.getFloat("doc-11"), 0.001f)
        assertEquals(Float.MIN_VALUE.toDouble(), doc.getDouble("doc-11"), 0.001)
        assertEquals(Float.MIN_VALUE, demoteToFloat(doc.getValue("doc-11")))
        assertNull(doc.getString("doc-11"))
        assertNull(doc.getDate("doc-11"))
        assertNull(doc.getBlob("doc-11"))
        assertNull(doc.getArray("doc-11"))
        assertNull(doc.getDictionary("doc-11"))

        //#11 doc.setFloat(Float.MAX_VALUE);
        assertEquals(Float.MAX_VALUE, demoteToFloat(doc.getValue("doc-12")))
        assertTrue(doc.getBoolean("doc-12"))
        // ??? Fails: assertEquals(Float.MAX_VALUE.toInt(), doc.getInt("doc-12"))
        // ??? Fails: assertEquals(Float.MAX_VALUE.toLong(), doc.getLong("doc-12"))
        assertEquals(Float.MAX_VALUE, doc.getFloat("doc-12"), 1.0E32F)
        assertEquals(Float.MAX_VALUE.toDouble(), doc.getDouble("doc-12"), 1.0E32)
        assertEquals(Float.MAX_VALUE, demoteToFloat(doc.getNumber("doc-12")))
        assertNull(doc.getString("doc-12"))
        assertNull(doc.getDate("doc-12"))
        assertNull(doc.getBlob("doc-12"))
        assertNull(doc.getArray("doc-12"))
        assertNull(doc.getDictionary("doc-12"))

        //#12 doc.setDouble(0.0);
        assertEquals(0f, doc.getValue("doc-13"))
        assertFalse(doc.getBoolean("doc-13"))
        assertEquals(0, doc.getInt("doc-13"))
        assertEquals(0L, doc.getLong("doc-13"))
        assertEquals(0.0f, doc.getFloat("doc-13"), 0.001f)
        assertEquals(0.0, doc.getDouble("doc-13"), 0.001)
        assertEquals(0f, doc.getNumber("doc-13"))
        assertNull(doc.getString("doc-13"))
        assertNull(doc.getDate("doc-13"))
        assertNull(doc.getBlob("doc-13"))
        assertNull(doc.getArray("doc-13"))
        assertNull(doc.getDictionary("doc-13"))

        //#13 doc.setDouble(Double.MIN_VALUE);
        assertEquals(Double.MIN_VALUE, doc.getValue("doc-14"))
        // !!! Fails on iOS: assertFalse(doc.getBoolean("doc-14")) (any non-zero number should be true, but Double.MIN_VALUE is false on Android)
        assertEquals(Double.MIN_VALUE.toInt(), doc.getInt("doc-14"))
        assertEquals(Double.MIN_VALUE.toLong(), doc.getLong("doc-14"))
        assertEquals(Double.MIN_VALUE.toFloat(), doc.getFloat("doc-14"), 0.001f)
        assertEquals(Double.MIN_VALUE, doc.getDouble("doc-14"), 0.001)
        assertEquals(Double.MIN_VALUE, doc.getNumber("doc-14"))
        assertNull(doc.getString("doc-14"))
        assertNull(doc.getDate("doc-14"))
        assertNull(doc.getBlob("doc-14"))
        assertNull(doc.getArray("doc-14"))
        assertNull(doc.getDictionary("doc-14"))

        //#14 doc.setDouble(Double.MAX_VALUE);
        assertEquals(Double.MAX_VALUE, doc.getValue("doc-15"))
        // ??? Fails: assertEquals(Double.MAX_VALUE.toInt(), doc.getInt("doc-15"))
        // ??? Fails: assertEquals(Double.MAX_VALUE.toLong(), doc.getLong("doc-15"))
        assertEquals(Double.MAX_VALUE.toFloat(), doc.getFloat("doc-15"), 100.0f)
        assertEquals(Double.MAX_VALUE, doc.getDouble("doc-15"), 100.0)
        assertEquals(Double.MAX_VALUE, doc.getNumber("doc-15"))
        assertNull(doc.getString("doc-15"))
        assertNull(doc.getDate("doc-15"))
        assertNull(doc.getBlob("doc-15"))
        assertNull(doc.getArray("doc-15"))
        assertNull(doc.getDictionary("doc-15"))

        //#15 doc.setNumber(null);
        assertNull(doc.getValue("doc-16"))
        assertFalse(doc.getBoolean("doc-16"))
        assertEquals(0, doc.getInt("doc-16"))
        assertEquals(0L, doc.getLong("doc-16"))
        assertEquals(0.0f, doc.getFloat("doc-16"), 0.001f)
        assertEquals(0.0, doc.getDouble("doc-16"), 0.001)
        assertNull(doc.getNumber("doc-16"))
        assertNull(doc.getString("doc-16"))
        assertNull(doc.getDate("doc-16"))
        assertNull(doc.getBlob("doc-16"))
        assertNull(doc.getArray("doc-16"))
        assertNull(doc.getDictionary("doc-16"))

        //#16 doc.setNumber(0);
        assertEquals(0L, doc.getValue("doc-17"))
        assertFalse(doc.getBoolean("doc-17"))
        assertEquals(0, doc.getInt("doc-17"))
        assertEquals(0L, doc.getLong("doc-17"))
        assertEquals(0.0f, doc.getFloat("doc-17"), 0.001f)
        assertEquals(0.0, doc.getDouble("doc-17"), 0.001)
        assertEquals(0L, doc.getNumber("doc-17"))
        assertNull(doc.getString("doc-17"))
        assertNull(doc.getDate("doc-17"))
        assertNull(doc.getBlob("doc-17"))
        assertNull(doc.getArray("doc-17"))
        assertNull(doc.getDictionary("doc-17"))

        //#17 doc.setNumber(Float.MIN_VALUE);
        assertEquals(Float.MIN_VALUE, demoteToFloat(doc.getValue("doc-18")))
        // !!! Fails on iOS: assertFalse(doc.getBoolean("doc-18")) (any non-zero number should be true, but Float.MIN_VALUE is false on Android)
        assertEquals(Float.MIN_VALUE.toInt(), doc.getInt("doc-18"))
        assertEquals(Float.MIN_VALUE.toLong(), doc.getLong("doc-18"))
        assertEquals(Float.MIN_VALUE, doc.getFloat("doc-18"), 0.001f)
        assertEquals(Float.MIN_VALUE.toDouble(), doc.getDouble("doc-18"), 0.001)
        assertEquals(Float.MIN_VALUE, demoteToFloat(doc.getNumber("doc-18")))
        assertNull(doc.getString("doc-18"))
        assertNull(doc.getDate("doc-18"))
        assertNull(doc.getBlob("doc-18"))
        assertNull(doc.getArray("doc-18"))
        assertNull(doc.getDictionary("doc-18"))

        //#18 doc.setNumber(Long.MIN_VALUE);
        assertEquals(Long.MIN_VALUE, doc.getValue("doc-19"))
        // ??? Value differs for Documents and Results: assertTrue(doc.getBoolean("doc-19"))
        assertEquals(Long.MIN_VALUE.toInt(), doc.getInt("doc-19"))
        assertEquals(Long.MIN_VALUE, doc.getLong("doc-19"))
        assertEquals(Long.MIN_VALUE.toFloat(), doc.getFloat("doc-19"), 0.001f)
        assertEquals(Long.MIN_VALUE.toDouble(), doc.getDouble("doc-19"), 0.001)
        assertEquals(Long.MIN_VALUE, doc.getNumber("doc-19"))
        assertNull(doc.getString("doc-19"))
        assertNull(doc.getDate("doc-19"))
        assertNull(doc.getBlob("doc-19"))
        assertNull(doc.getArray("doc-19"))
        assertNull(doc.getDictionary("doc-19"))

        //#19 doc.setString(null);
        assertNull(doc.getValue("doc-20"))
        assertFalse(doc.getBoolean("doc-20"))
        assertEquals(0, doc.getInt("doc-20"))
        assertEquals(0L, doc.getLong("doc-20"))
        assertEquals(0.0f, doc.getFloat("doc-20"), 0.001f)
        assertEquals(0.0, doc.getDouble("doc-20"), 0.001)
        assertNull(doc.getNumber("doc-20"))
        assertNull(doc.getString("doc-20"))
        assertNull(doc.getDate("doc-20"))
        assertNull(doc.getBlob("doc-20"))
        assertNull(doc.getArray("doc-20"))
        assertNull(doc.getDictionary("doc-20"))

        //#20 doc.setString("Quatro");
        assertEquals("Jett", doc.getValue("doc-21"))
        assertTrue(doc.getBoolean("doc-21"))
        assertEquals(0, doc.getInt("doc-21"))
        assertEquals(0, doc.getLong("doc-21"))
        assertEquals(0.0f, doc.getFloat("doc-21"), 0.001f)
        assertEquals(0.0, doc.getDouble("doc-21"), 0.001)
        assertNull(doc.getNumber("doc-21"))
        assertEquals("Jett", doc.getString("doc-21"))
        assertNull(doc.getDate("doc-21"))
        assertNull(doc.getBlob("doc-21"))
        assertNull(doc.getArray("doc-21"))
        assertNull(doc.getDictionary("doc-21"))

        //#21 doc.setDate(null);
        assertNull(doc.getValue("doc-22"))
        assertFalse(doc.getBoolean("doc-22"))
        assertEquals(0, doc.getInt("doc-22"))
        assertEquals(0L, doc.getLong("doc-22"))
        assertEquals(0.0f, doc.getFloat("doc-22"), 0.001f)
        assertEquals(0.0, doc.getDouble("doc-22"), 0.001)
        assertNull(doc.getNumber("doc-22"))
        assertNull(doc.getString("doc-22"))
        assertNull(doc.getDate("doc-22"))
        assertNull(doc.getBlob("doc-22"))
        assertNull(doc.getArray("doc-22"))
        assertNull(doc.getDictionary("doc-22"))

        //#22 doc.setDate(Instant.parse(TEST_DATE));
        assertEquals(TEST_DATE, doc.getValue("doc-23"))
        assertTrue(doc.getBoolean("doc-23"))
        assertEquals(0, doc.getInt("doc-23"))
        assertEquals(0L, doc.getLong("doc-23"))
        assertEquals(0.0f, doc.getFloat("doc-23"), 0.001f)
        assertEquals(0.0, doc.getDouble("doc-23"), 0.001)
        assertNull(doc.getNumber("doc-23"))
        assertEquals(TEST_DATE, doc.getString("doc-23"))
        assertEquals(Instant.parse(TEST_DATE), doc.getDate("doc-23"))
        assertNull(doc.getBlob("doc-23"))
        assertNull(doc.getArray("doc-23"))
        assertNull(doc.getDictionary("doc-23"))

        //#23 doc.setArray(null);
        assertNull(doc.getValue("doc-24"))
        assertFalse(doc.getBoolean("doc-24"))
        assertEquals(0, doc.getInt("doc-24"))
        assertEquals(0L, doc.getLong("doc-24"))
        assertEquals(0.0f, doc.getFloat("doc-24"), 0.001f)
        assertEquals(0.0, doc.getDouble("doc-24"), 0.001)
        assertNull(doc.getNumber("doc-24"))
        assertNull(doc.getString("doc-24"))
        assertNull(doc.getDate("doc-24"))
        assertNull(doc.getBlob("doc-24"))
        assertNull(doc.getArray("doc-24"))
        assertNull(doc.getDictionary("doc-24"))

        //#24 doc.setDictionary(null);
        verifyArray(doc.getArray("doc-25"))

        //#25 doc.setDictionary(null);
        assertNull(doc.getValue("doc-26"))
        assertFalse(doc.getBoolean("doc-26"))
        assertEquals(0, doc.getInt("doc-26"))
        assertEquals(0L, doc.getLong("doc-26"))
        assertEquals(0.0f, doc.getFloat("doc-26"), 0.001f)
        assertEquals(0.0, doc.getDouble("doc-26"), 0.001)
        assertNull(doc.getNumber("doc-26"))
        assertNull(doc.getString("doc-26"))
        assertNull(doc.getDate("doc-26"))
        assertNull(doc.getBlob("doc-26"))
        assertNull(doc.getArray("doc-26"))
        assertNull(doc.getDictionary("doc-26"))

        //#26 doc.setDictionary(simpleDict);
        verifyDict(doc.getDictionary("doc-27"))

        //#27 doc.setDictionary(null);
        assertNull(doc.getValue("doc-28"))
        assertFalse(doc.getBoolean("doc-28"))
        assertEquals(0, doc.getInt("doc-28"))
        assertEquals(0L, doc.getLong("doc-28"))
        assertEquals(0.0f, doc.getFloat("doc-28"), 0.001f)
        assertEquals(0.0, doc.getDouble("doc-28"), 0.001)
        assertNull(doc.getNumber("doc-28"))
        assertNull(doc.getString("doc-28"))
        assertNull(doc.getDate("doc-28"))
        assertNull(doc.getBlob("doc-28"))
        assertNull(doc.getArray("doc-28"))
        assertNull(doc.getDictionary("doc-28"))
        verifyBlob(doc.getBlob("doc-29"))
    }

    protected open fun openDatabase(): Database {
        return verifyOrDeleteDb(createDb(getUniqueName("test_db")))
    }

    protected fun reopenBaseTestDb() {
        baseTestDb = reopenDb(baseTestDb)
    }

    protected fun recreateBastTestDb() {
        baseTestDb = recreateDb(baseTestDb)
    }

    protected open fun duplicateBaseTestDb(): Database {
        return verifyOrDeleteDb(duplicateDb(baseTestDb))
    }

    // Some JSON encoding will promote a Float to a Double.
    protected fun demoteToFloat(`val`: Any?): Float {
        if (`val` is Float) {
            return `val`
        }
        if (`val` is Double) {
            return `val`.toFloat()
        }
        throw IllegalArgumentException("expected a floating point value")
    }

    private fun verifyOrDeleteDb(db: Database): Database {
        return try {
            assertNotNull(db)
            assertTrue(FileUtils.getCanonicalPath(db.path!!).endsWith(DB_EXTENSION))
            db
        } catch (e: IOException) {
            deleteDb(db)
            throw AssertionError("Unable to get db path", e)
        } catch (e: AssertionError) {
            deleteDb(db)
            throw e
        }
    }

    companion object {
        const val TEST_DATE = "2019-02-21T05:37:22.014Z"
        const val BLOB_CONTENT = "Knox on fox in socks in box. Socks on Knox and Knox in box."
    }
}
