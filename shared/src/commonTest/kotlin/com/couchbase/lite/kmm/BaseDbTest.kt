//package com.couchbase.lite.kmm
//import com.couchbase.lite.DictionaryInterface
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
//import com.couchbase.lite.internal.utils.Report
//
//
//abstract class BaseDbTest : BaseTest() {
//    @java.lang.FunctionalInterface
//    interface DocValidator : ConsumerThrows<Document?, CouchbaseLiteException?>
//
//    protected var baseTestDb: Database? = null
//    @Before
//    @Throws(CouchbaseLiteException::class)
//    fun setUpBaseDbTest() {
//        baseTestDb = createDb("base_db")
//        Report.log(LogLevel.INFO, "Created base test DB: $baseTestDb")
//        org.junit.Assert.assertNotNull(baseTestDb)
//        synchronized(baseTestDb.getDbLock()) { org.junit.Assert.assertTrue(baseTestDb.isOpen()) }
//    }
//
//    @After
//    fun tearDownBaseDbTest() {
//        deleteDb(baseTestDb)
//        Report.log(LogLevel.INFO, "Deleted baseTestDb: $baseTestDb")
//    }
//
//    @Throws(CouchbaseLiteException::class)
//    protected fun createSingleDocInBaseTestDb(docID: String?): Document? {
//        val n = baseTestDb!!.count
//        val doc = MutableDocument(docID)
//        doc.setValue("key", 1)
//        val savedDoc = saveDocInBaseTestDb(doc)
//        org.junit.Assert.assertEquals(n + 1, baseTestDb!!.count)
//        org.junit.Assert.assertEquals(1, savedDoc!!.sequence)
//        return savedDoc
//    }
//
//    @Throws(CouchbaseLiteException::class)
//    protected fun createDocsInDb(first: Int, count: Int, db: Database) {
//        db.inBatch(kotlin.jvm.functions.Function0<Unit><Unit> {
//            for (i in first until first + count) {
//                val doc =
//                    MutableDocument("doc-$i")
//                doc.setNumber("count", i)
//                doc.setString("inverse", "minus-$i")
//                db.save(doc)
//            }
//        })
//    }
//
//    @Throws(CouchbaseLiteException::class)
//    protected fun saveDocInBaseTestDb(doc: MutableDocument): Document? {
//        baseTestDb!!.save(doc)
//        val savedDoc = baseTestDb!!.getDocument(doc.id)
//        org.junit.Assert.assertNotNull(savedDoc)
//        assertEquals(doc.id, savedDoc!!.id)
//        return savedDoc
//    }
//
//    @Throws(CouchbaseLiteException::class)
//    protected fun saveDocInBaseTestDb(
//        doc: MutableDocument,
//        validator: BaseDbTest.DocValidator
//    ): Document? {
//        validator.accept(doc)
//        val savedDoc = saveDocInBaseTestDb(doc)
//        validator.accept(doc)
//        validator.accept(savedDoc)
//        return savedDoc
//    }
//
//    // used from other package's tests
//    protected fun populateData(doc: MutableDocument) {
//        doc.setValue("true", true)
//        doc.setValue("false", false)
//        doc.setValue("string", "string")
//        doc.setValue("zero", 0)
//        doc.setValue("one", 1)
//        doc.setValue("minus_one", -1)
//        doc.setValue("one_dot_one", 1.1)
//        doc.setValue("date", JSONUtils.toDate(BaseDbTest.Companion.TEST_DATE))
//        doc.setValue("null", null)
//
//        // Dictionary:
//        val dict = MutableDictionary()
//        dict.setValue("street", "1 Main street")
//        dict.setValue("city", "Mountain View")
//        dict.setValue("state", "CA")
//        doc.setValue("dict", dict)
//
//        // Array:
//        val array = MutableArray()
//        array.addValue("650-123-0001")
//        array.addValue("650-123-0002")
//        doc.setValue("array", array)
//
//        // Blob:
//        doc.setValue(
//            "blob",
//            Blob(
//                "text/plain",
//                BaseDbTest.Companion.BLOB_CONTENT.toByteArray(java.nio.charset.StandardCharsets.UTF_8)
//            )
//        )
//    }
//
//    // used from other package's tests
//    protected fun populateDataByTypedSetter(doc: MutableDocument) {
//        doc.setBoolean("true", true)
//        doc.setBoolean("false", false)
//        doc.setString("string", "string")
//        doc.setNumber("zero", 0)
//        doc.setInt("one", 1)
//        doc.setLong("minus_one", -1)
//        doc.setDouble("one_dot_one", 1.1)
//        doc.setDate("date", JSONUtils.toDate(BaseDbTest.Companion.TEST_DATE))
//        doc.setString("null", null)
//
//        // Dictionary:
//        val dict = MutableDictionary()
//        dict.setString("street", "1 Main street")
//        dict.setString("city", "Mountain View")
//        dict.setString("state", "CA")
//        doc.setDictionary("dict", dict)
//
//        // Array:
//        val array = MutableArray()
//        array.addString("650-123-0001")
//        array.addString("650-123-0002")
//        doc.setArray("array", array)
//
//        // Blob:
//        doc.setValue(
//            "blob",
//            Blob(
//                "text/plain",
//                BaseDbTest.Companion.BLOB_CONTENT.toByteArray(java.nio.charset.StandardCharsets.UTF_8)
//            )
//        )
//    }
//
//    // file is one JSON object per line
//    @Throws(
//        java.io.IOException::class,
//        org.json.JSONException::class,
//        CouchbaseLiteException::class
//    )
//    protected fun loadJSONResource(name: String?) {
//        java.io.BufferedReader(java.io.InputStreamReader(PlatformUtils.getAsset(name)))
//            .use { `in` ->
//                var n = 1
//                var line: String
//                while (`in`.readLine().also { line = it } != null) {
//                    if (line.trim { it <= ' ' }.isEmpty()) {
//                        continue
//                    }
//                    val doc: MutableDocument =
//                        MutableDocument(
//                            String.format(
//                                java.util.Locale.ENGLISH,
//                                "doc-%03d",
//                                n++
//                            )
//                        )
//                    doc.setData(JSONUtils.fromJSON(JSONObject(line)))
//                    saveDocInBaseTestDb(doc)
//                }
//            }
//    }
//
//    @Throws(java.io.IOException::class)
//    protected fun readJSONResource(name: String?): String {
//        val buf: java.lang.StringBuilder = java.lang.StringBuilder()
//        java.io.BufferedReader(java.io.InputStreamReader(PlatformUtils.getAsset(name)))
//            .use { `in` ->
//                var line: String
//                while (`in`.readLine().also { line = it } != null) {
//                    if (!line.trim { it <= ' ' }.isEmpty()) {
//                        buf.append(line)
//                    }
//                }
//            }
//        return buf.toString()
//    }
//
//    @NonNull
//    protected fun makeArray(): MutableArray {
//        // A small array
//        val simpleArray = MutableArray()
//        simpleArray.addInt(54)
//        simpleArray.addString("Joplin")
//
//        // A small dictionary
//        val simpleDict = MutableDictionary()
//        simpleDict.setInt("sdict-1", 58)
//        simpleDict.setString("sdict-2", "Winehouse")
//        val array = MutableArray()
//        array.addValue(null)
//        array.addBoolean(true)
//        array.addBoolean(false)
//        array.addInt(0)
//        array.addInt(Int.MIN_VALUE)
//        array.addInt(Int.MAX_VALUE)
//        array.addLong(0L)
//        array.addLong(Long.MIN_VALUE)
//        array.addLong(Long.MAX_VALUE)
//        array.addFloat(0.0f)
//        array.addFloat(Float.MIN_VALUE)
//        array.addFloat(Float.MAX_VALUE)
//        array.addDouble(0.0)
//        array.addDouble(Double.MIN_VALUE)
//        array.addDouble(Double.MAX_VALUE)
//        array.addNumber(null)
//        array.addNumber(0)
//        array.addNumber(Float.MIN_VALUE)
//        array.addNumber(Long.MIN_VALUE)
//        array.addString(null)
//        array.addString("Harry")
//        array.addDate(null)
//        array.addDate(JSONUtils.toDate(BaseDbTest.Companion.TEST_DATE))
//        array.addArray(null)
//        array.addArray(simpleArray)
//        array.addDictionary(null)
//        array.addDictionary(simpleDict)
//        return array
//    }
//
//    @Throws(org.json.JSONException::class)
//    protected fun verifyArray(jArray: JSONArray) {
//        org.junit.Assert.assertEquals(27, jArray.length().toLong())
//        org.junit.Assert.assertEquals(JSONObject.NULL, jArray.get(0))
//        org.junit.Assert.assertEquals(true, jArray.get(1))
//        org.junit.Assert.assertEquals(false, jArray.get(2))
//        org.junit.Assert.assertEquals(0, jArray.get(3))
//        org.junit.Assert.assertEquals(Int.MIN_VALUE, jArray.get(4))
//        org.junit.Assert.assertEquals(Int.MAX_VALUE, jArray.get(5))
//        org.junit.Assert.assertEquals(0, jArray.get(6))
//        org.junit.Assert.assertEquals(Long.MIN_VALUE, jArray.get(7))
//        org.junit.Assert.assertEquals(Long.MAX_VALUE, jArray.get(8))
//        org.junit.Assert.assertEquals(0.0, jArray.getDouble(9), 0.001)
//        org.junit.Assert.assertEquals(
//            Float.MIN_VALUE.toDouble(),
//            jArray.getDouble(10).toFloat().toDouble(),
//            0.001
//        )
//        org.junit.Assert.assertEquals(
//            Float.MAX_VALUE.toDouble(),
//            jArray.getDouble(11).toFloat().toDouble(),
//            100.0
//        )
//        org.junit.Assert.assertEquals(0.0, jArray.getDouble(12), 0.001)
//        org.junit.Assert.assertEquals(Double.MIN_VALUE, jArray.getDouble(13), 0.001)
//        org.junit.Assert.assertEquals(Double.MAX_VALUE, jArray.getDouble(14), 1.0)
//        org.junit.Assert.assertEquals(JSONObject.NULL, jArray.get(15))
//        org.junit.Assert.assertEquals(0, jArray.getLong(16))
//        org.junit.Assert.assertEquals(Float.MIN_VALUE.toDouble(), jArray.getDouble(17), 0.001)
//        org.junit.Assert.assertEquals(
//            Long.MIN_VALUE.toDouble(),
//            jArray.getLong(18).toDouble(),
//            0.001
//        )
//        org.junit.Assert.assertEquals(JSONObject.NULL, jArray.get(19))
//        org.junit.Assert.assertEquals("Harry", jArray.get(20))
//        org.junit.Assert.assertEquals(JSONObject.NULL, jArray.get(21))
//        org.junit.Assert.assertEquals(BaseDbTest.Companion.TEST_DATE, jArray.get(22))
//        org.junit.Assert.assertEquals(JSONObject.NULL, jArray.get(23))
//        org.junit.Assert.assertEquals(JSONArray::class.java, jArray.get(24).javaClass)
//        org.junit.Assert.assertEquals(JSONObject.NULL, jArray.get(25))
//        org.junit.Assert.assertEquals(JSONObject::class.java, jArray.get(26).javaClass)
//        org.junit.Assert.assertEquals(JSONObject.NULL, jArray.get(25))
//        org.junit.Assert.assertEquals(JSONObject::class.java, jArray.get(26).javaClass)
//    }
//
//    protected fun verifyArray(@Nullable array: ArrayInterface) {
//        org.junit.Assert.assertNotNull(array)
//        assertEquals(27, array.count())
//
//        //#0 array.addValue(null);
//        org.junit.Assert.assertNull(array.getValue(0))
//        org.junit.Assert.assertFalse(array.getBoolean(0))
//        assertEquals(0, array.getInt(0))
//        assertEquals(0L, array.getLong(0))
//        assertEquals(0.0f, array.getFloat(0), 0.001f)
//        assertEquals(0.0, array.getDouble(0), 0.001)
//        org.junit.Assert.assertNull(array.getNumber(0))
//        org.junit.Assert.assertNull(array.getString(0))
//        org.junit.Assert.assertNull(array.getDate(0))
//        org.junit.Assert.assertNull(array.getBlob(0))
//        org.junit.Assert.assertNull(array.getArray(0))
//        org.junit.Assert.assertNull(array.getDictionary(0))
//
//        //#1 array.addBoolean(true);
//        assertEquals(java.lang.Boolean.TRUE, array.getValue(1))
//        org.junit.Assert.assertTrue(array.getBoolean(1))
//        assertEquals(1, array.getInt(1))
//        assertEquals(1L, array.getLong(1))
//        assertEquals(1.0f, array.getFloat(1), 0.001f)
//        assertEquals(1.0, array.getDouble(1), 0.001)
//        assertEquals(1, array.getNumber(1))
//        org.junit.Assert.assertNull(array.getString(1))
//        org.junit.Assert.assertNull(array.getDate(1))
//        org.junit.Assert.assertNull(array.getBlob(1))
//        org.junit.Assert.assertNull(array.getArray(1))
//        org.junit.Assert.assertNull(array.getDictionary(1))
//
//        //#2 array.addBoolean(false);
//        assertEquals(java.lang.Boolean.FALSE, array.getValue(2))
//        org.junit.Assert.assertFalse(array.getBoolean(2))
//        assertEquals(0, array.getInt(2))
//        assertEquals(0L, array.getLong(2))
//        assertEquals(0.0f, array.getFloat(2), 0.001f)
//        assertEquals(0.0, array.getDouble(2), 0.001)
//        assertEquals(0, array.getNumber(2))
//        org.junit.Assert.assertNull(array.getString(2))
//        org.junit.Assert.assertNull(array.getDate(2))
//        org.junit.Assert.assertNull(array.getBlob(2))
//        org.junit.Assert.assertNull(array.getArray(2))
//        org.junit.Assert.assertNull(array.getDictionary(2))
//
//        //#3 array.addInt(0);
//        assertEquals(0L, array.getValue(3))
//        org.junit.Assert.assertFalse(array.getBoolean(3))
//        assertEquals(0, array.getInt(3))
//        assertEquals(0L, array.getLong(3))
//        assertEquals(0.0f, array.getFloat(3), 0.001f)
//        assertEquals(0.0, array.getDouble(3), 0.001)
//        assertEquals(0L, array.getNumber(3))
//        org.junit.Assert.assertNull(array.getString(3))
//        org.junit.Assert.assertNull(array.getDate(3))
//        org.junit.Assert.assertNull(array.getBlob(3))
//        org.junit.Assert.assertNull(array.getArray(3))
//        org.junit.Assert.assertNull(array.getDictionary(3))
//
//        //#4 array.addInt(Integer.MIN_VALUE);
//        assertEquals(java.lang.Long.valueOf(Int.MIN_VALUE.toLong()), array.getValue(4))
//        org.junit.Assert.assertTrue(array.getBoolean(4))
//        assertEquals(Int.MIN_VALUE, array.getInt(4))
//        assertEquals(Int.MIN_VALUE.toLong(), array.getLong(4))
//        assertEquals(Int.MIN_VALUE.toFloat(), array.getFloat(4), 0.001f)
//        assertEquals(Int.MIN_VALUE.toDouble(), array.getDouble(4), 0.001)
//        assertEquals(java.lang.Long.valueOf(Int.MIN_VALUE.toLong()), array.getNumber(4))
//        org.junit.Assert.assertNull(array.getString(4))
//        org.junit.Assert.assertNull(array.getDate(4))
//        org.junit.Assert.assertNull(array.getBlob(4))
//        org.junit.Assert.assertNull(array.getArray(4))
//        org.junit.Assert.assertNull(array.getDictionary(4))
//
//        //#5 array.addInt(Integer.MAX_VALUE);
//        assertEquals(java.lang.Long.valueOf(Int.MAX_VALUE.toLong()), array.getValue(5))
//        org.junit.Assert.assertTrue(array.getBoolean(5))
//        assertEquals(Int.MAX_VALUE, array.getInt(5))
//        assertEquals(Int.MAX_VALUE.toLong(), array.getLong(5))
//        assertEquals(Int.MAX_VALUE.toFloat(), array.getFloat(5), 100.0f)
//        assertEquals(Int.MAX_VALUE.toDouble(), array.getDouble(5), 100.0)
//        assertEquals(java.lang.Long.valueOf(Int.MAX_VALUE.toLong()), array.getNumber(5))
//        org.junit.Assert.assertNull(array.getString(5))
//        org.junit.Assert.assertNull(array.getDate(5))
//        org.junit.Assert.assertNull(array.getBlob(5))
//        org.junit.Assert.assertNull(array.getArray(5))
//        org.junit.Assert.assertNull(array.getDictionary(5))
//
//        //#6 array.addLong(0L);
//        assertEquals(java.lang.Long.valueOf(0L), array.getValue(6))
//        org.junit.Assert.assertFalse(array.getBoolean(6))
//        assertEquals(0, array.getInt(6))
//        assertEquals(0L, array.getLong(6))
//        assertEquals(0.0f, array.getFloat(6), 0.001f)
//        assertEquals(0.0, array.getDouble(6), 0.001)
//        assertEquals(java.lang.Long.valueOf(0L), array.getNumber(6))
//        org.junit.Assert.assertNull(array.getString(6))
//        org.junit.Assert.assertNull(array.getDate(6))
//        org.junit.Assert.assertNull(array.getBlob(6))
//        org.junit.Assert.assertNull(array.getArray(6))
//        org.junit.Assert.assertNull(array.getDictionary(6))
//
//        //#7 array.addLong(Long.MIN_VALUE);
//        assertEquals(java.lang.Long.valueOf(Long.MIN_VALUE), array.getValue(7))
//        org.junit.Assert.assertFalse(array.getBoolean(7))
//        assertEquals(java.lang.Long.valueOf(Long.MIN_VALUE).toInt(), array.getInt(7))
//        assertEquals(Long.MIN_VALUE, array.getLong(7))
//        assertEquals(Long.MIN_VALUE.toFloat(), array.getFloat(7), 0.001f)
//        assertEquals(Long.MIN_VALUE.toDouble(), array.getDouble(7), 0.001)
//        assertEquals(java.lang.Long.valueOf(Long.MIN_VALUE), array.getNumber(7))
//        org.junit.Assert.assertNull(array.getString(7))
//        org.junit.Assert.assertNull(array.getDate(7))
//        org.junit.Assert.assertNull(array.getBlob(7))
//        org.junit.Assert.assertNull(array.getArray(7))
//        org.junit.Assert.assertNull(array.getDictionary(7))
//
//        //#8 array.addLong(Long.MAX_VALUE);
//        assertEquals(java.lang.Long.valueOf(Long.MAX_VALUE), array.getValue(8))
//        org.junit.Assert.assertTrue(array.getBoolean(8))
//        assertEquals(java.lang.Long.valueOf(Long.MAX_VALUE).toInt(), array.getInt(8))
//        assertEquals(Long.MAX_VALUE, array.getLong(8))
//        assertEquals(Long.MAX_VALUE.toFloat(), array.getFloat(8), 100.0f)
//        assertEquals(Long.MAX_VALUE.toDouble(), array.getDouble(8), 100.0)
//        assertEquals(java.lang.Long.valueOf(Long.MAX_VALUE), array.getNumber(8))
//        org.junit.Assert.assertNull(array.getString(8))
//        org.junit.Assert.assertNull(array.getDate(8))
//        org.junit.Assert.assertNull(array.getBlob(8))
//        org.junit.Assert.assertNull(array.getArray(8))
//        org.junit.Assert.assertNull(array.getDictionary(8))
//
//        //#9 array.addFloat(0.0F);
//        assertEquals(java.lang.Float.valueOf(0.0f), array.getValue(9))
//        org.junit.Assert.assertFalse(array.getBoolean(9))
//        assertEquals(0, array.getInt(9))
//        assertEquals(0L, array.getLong(9))
//        assertEquals(0.0f, array.getFloat(9), 0.001f)
//        assertEquals(0.0, array.getDouble(9), 0.001)
//        assertEquals(java.lang.Float.valueOf(0.0f), array.getNumber(9))
//        org.junit.Assert.assertNull(array.getString(9))
//        org.junit.Assert.assertNull(array.getDate(9))
//        org.junit.Assert.assertNull(array.getBlob(9))
//        org.junit.Assert.assertNull(array.getArray(9))
//        org.junit.Assert.assertNull(array.getDictionary(9))
//
//        //#10 array.addFloat(Float.MIN_VALUE);
//        org.junit.Assert.assertEquals(
//            java.lang.Float.valueOf(Float.MIN_VALUE),
//            demoteToFloat(array.getValue(10))
//        )
//        org.junit.Assert.assertFalse(array.getBoolean(10))
//        assertEquals(java.lang.Float.valueOf(Float.MIN_VALUE).toInt(), array.getInt(10), 0.001f)
//        assertEquals(java.lang.Float.valueOf(Float.MIN_VALUE).toLong(), array.getLong(10), 0.001f)
//        assertEquals(Float.MIN_VALUE, array.getFloat(10), 0.001f)
//        assertEquals(Float.MIN_VALUE.toDouble(), array.getDouble(10), 0.001)
//        org.junit.Assert.assertEquals(
//            java.lang.Float.valueOf(Float.MIN_VALUE),
//            demoteToFloat(array.getNumber(10))
//        )
//        org.junit.Assert.assertNull(array.getString(10))
//        org.junit.Assert.assertNull(array.getDate(10))
//        org.junit.Assert.assertNull(array.getBlob(10))
//        org.junit.Assert.assertNull(array.getArray(10))
//        org.junit.Assert.assertNull(array.getDictionary(10))
//
//        //#11 array.addFloat(Float.MAX_VALUE);
//        org.junit.Assert.assertEquals(
//            java.lang.Float.valueOf(Float.MAX_VALUE),
//            demoteToFloat(array.getValue(11))
//        )
//        org.junit.Assert.assertTrue(array.getBoolean(11))
//        // !!! Fails: assertEquals(Float.valueOf(Float.MAX_VALUE).intValue(), array.getInt(11));
//        // !!! Fails: assertEquals(Float.valueOf(Float.MAX_VALUE).longValue(), array.getLong(11));
//        assertEquals(Float.MAX_VALUE, array.getFloat(11), 100.0f)
//        org.junit.Assert.assertEquals(
//            Float.MAX_VALUE.toDouble(),
//            demoteToFloat(array.getDouble(11)).toDouble(),
//            100.0
//        )
//        org.junit.Assert.assertEquals(
//            java.lang.Float.valueOf(Float.MAX_VALUE),
//            demoteToFloat(array.getNumber(11))
//        )
//        org.junit.Assert.assertNull(array.getString(11))
//        org.junit.Assert.assertNull(array.getDate(11))
//        org.junit.Assert.assertNull(array.getBlob(11))
//        org.junit.Assert.assertNull(array.getArray(11))
//        org.junit.Assert.assertNull(array.getDictionary(11))
//
//        //#12 array.addDouble(0.0);
//        assertEquals(java.lang.Float.valueOf(0f), array.getValue(12))
//        org.junit.Assert.assertFalse(array.getBoolean(12))
//        assertEquals(0, array.getInt(12))
//        assertEquals(0L, array.getLong(12))
//        assertEquals(0.0f, array.getFloat(12), 0.001f)
//        assertEquals(0.0, array.getDouble(12), 0.001)
//        assertEquals(java.lang.Float.valueOf(0f), array.getNumber(12))
//        org.junit.Assert.assertNull(array.getString(12))
//        org.junit.Assert.assertNull(array.getDate(12))
//        org.junit.Assert.assertNull(array.getBlob(12))
//        org.junit.Assert.assertNull(array.getArray(12))
//        org.junit.Assert.assertNull(array.getDictionary(12))
//
//        //#13 array.addDouble(Double.MIN_VALUE);
//        assertEquals(java.lang.Double.valueOf(Double.MIN_VALUE), array.getValue(13))
//        org.junit.Assert.assertFalse(array.getBoolean(13))
//        assertEquals(java.lang.Double.valueOf(Double.MIN_VALUE).toInt(), array.getInt(13))
//        assertEquals(java.lang.Double.valueOf(Double.MIN_VALUE).toLong(), array.getLong(13))
//        assertEquals(
//            java.lang.Double.valueOf(Double.MIN_VALUE).toFloat(),
//            array.getFloat(13),
//            0.001f
//        )
//        assertEquals(Double.MIN_VALUE, array.getDouble(13), 0.001)
//        assertEquals(java.lang.Double.valueOf(Double.MIN_VALUE), array.getNumber(13))
//        org.junit.Assert.assertNull(array.getString(13))
//        org.junit.Assert.assertNull(array.getDate(13))
//        org.junit.Assert.assertNull(array.getBlob(13))
//        org.junit.Assert.assertNull(array.getArray(13))
//        org.junit.Assert.assertNull(array.getDictionary(13))
//
//        //#14 array.addDouble(Double.MAX_VALUE);
//        assertEquals(java.lang.Double.valueOf(Double.MAX_VALUE), array.getValue(14))
//        org.junit.Assert.assertTrue(array.getBoolean(14))
//        // !!! Fails: assertEquals(Double.valueOf(Double.MAX_VALUE).intValue(), array.getInt(14));
//        // !!! Fails: assertEquals(Double.valueOf(Double.MAX_VALUE).longValue(), array.getLong(14));
//        assertEquals(
//            java.lang.Double.valueOf(Double.MAX_VALUE).toFloat(),
//            array.getFloat(14),
//            100.0f
//        )
//        assertEquals(Double.MAX_VALUE, array.getDouble(14), 100.0)
//        assertEquals(java.lang.Double.valueOf(Double.MAX_VALUE), array.getNumber(14))
//        org.junit.Assert.assertNull(array.getString(14))
//        org.junit.Assert.assertNull(array.getDate(14))
//        org.junit.Assert.assertNull(array.getBlob(14))
//        org.junit.Assert.assertNull(array.getArray(14))
//        org.junit.Assert.assertNull(array.getDictionary(14))
//
//        //#15 array.addNumber(null);
//        org.junit.Assert.assertNull(array.getValue(15))
//        org.junit.Assert.assertFalse(array.getBoolean(15))
//        assertEquals(0, array.getInt(15))
//        assertEquals(0L, array.getLong(15))
//        assertEquals(0.0f, array.getFloat(15), 0.001f)
//        assertEquals(0.0, array.getDouble(15), 0.001)
//        org.junit.Assert.assertNull(array.getNumber(15))
//        org.junit.Assert.assertNull(array.getString(15))
//        org.junit.Assert.assertNull(array.getDate(15))
//        org.junit.Assert.assertNull(array.getBlob(15))
//        org.junit.Assert.assertNull(array.getArray(15))
//        org.junit.Assert.assertNull(array.getDictionary(15))
//
//        //#16 array.addNumber(0);
//        assertEquals(0L, array.getValue(16))
//        org.junit.Assert.assertFalse(array.getBoolean(16))
//        assertEquals(0, array.getInt(16))
//        assertEquals(0L, array.getLong(16))
//        assertEquals(0.0f, array.getFloat(16), 0.001f)
//        assertEquals(0.0, array.getDouble(16), 0.001)
//        assertEquals(0L, array.getNumber(16))
//        org.junit.Assert.assertNull(array.getString(16))
//        org.junit.Assert.assertNull(array.getDate(16))
//        org.junit.Assert.assertNull(array.getBlob(16))
//        org.junit.Assert.assertNull(array.getArray(16))
//        org.junit.Assert.assertNull(array.getDictionary(16))
//
//        //#17 array.addNumber(Float.MIN_VALUE);
//        org.junit.Assert.assertEquals(
//            java.lang.Float.valueOf(Float.MIN_VALUE),
//            demoteToFloat(array.getValue(17))
//        )
//        org.junit.Assert.assertFalse(array.getBoolean(17))
//        assertEquals(java.lang.Float.valueOf(Float.MIN_VALUE).toInt(), array.getInt(17), 0.001f)
//        assertEquals(java.lang.Float.valueOf(Float.MIN_VALUE).toLong(), array.getLong(17), 0.001f)
//        assertEquals(Float.MIN_VALUE, array.getFloat(17), 0.001f)
//        assertEquals(Float.MIN_VALUE.toDouble(), array.getDouble(17), 0.001)
//        org.junit.Assert.assertEquals(
//            java.lang.Float.valueOf(Float.MIN_VALUE),
//            demoteToFloat(array.getNumber(17))
//        )
//        org.junit.Assert.assertNull(array.getString(17))
//        org.junit.Assert.assertNull(array.getDate(17))
//        org.junit.Assert.assertNull(array.getBlob(17))
//        org.junit.Assert.assertNull(array.getArray(17))
//        org.junit.Assert.assertNull(array.getDictionary(17))
//
//        //#18 array.addNumber(Long.MIN_VALUE);
//        assertEquals(java.lang.Long.valueOf(Long.MIN_VALUE), array.getValue(18))
//        org.junit.Assert.assertFalse(array.getBoolean(18))
//        assertEquals(java.lang.Long.valueOf(Long.MIN_VALUE).toInt(), array.getInt(18))
//        assertEquals(Long.MIN_VALUE, array.getLong(18))
//        assertEquals(Long.MIN_VALUE.toFloat(), array.getFloat(18), 0.001f)
//        assertEquals(Long.MIN_VALUE.toDouble(), array.getDouble(18), 0.001)
//        assertEquals(java.lang.Long.valueOf(Long.MIN_VALUE), array.getNumber(18))
//        org.junit.Assert.assertNull(array.getString(18))
//        org.junit.Assert.assertNull(array.getDate(18))
//        org.junit.Assert.assertNull(array.getBlob(18))
//        org.junit.Assert.assertNull(array.getArray(18))
//        org.junit.Assert.assertNull(array.getDictionary(18))
//
//        //#19 array.addString(null);
//        org.junit.Assert.assertNull(array.getValue(19))
//        org.junit.Assert.assertFalse(array.getBoolean(19))
//        assertEquals(0, array.getInt(19))
//        assertEquals(0L, array.getLong(19))
//        assertEquals(0.0f, array.getFloat(19), 0.001f)
//        assertEquals(0.0, array.getDouble(19), 0.001)
//        org.junit.Assert.assertNull(array.getNumber(19))
//        org.junit.Assert.assertNull(array.getString(19))
//        org.junit.Assert.assertNull(array.getDate(19))
//        org.junit.Assert.assertNull(array.getBlob(19))
//        org.junit.Assert.assertNull(array.getArray(19))
//        org.junit.Assert.assertNull(array.getDictionary(19))
//
//        //#20 array.addString("Quatro");
//        assertEquals("Harry", array.getValue(20))
//        org.junit.Assert.assertTrue(array.getBoolean(20))
//        assertEquals(0, array.getInt(20))
//        assertEquals(0, array.getLong(20))
//        assertEquals(0.0f, array.getFloat(20), 0.001f)
//        assertEquals(0.0, array.getDouble(20), 0.001)
//        org.junit.Assert.assertNull(array.getNumber(20))
//        assertEquals("Harry", array.getString(20))
//        org.junit.Assert.assertNull(array.getDate(20))
//        org.junit.Assert.assertNull(array.getBlob(20))
//        org.junit.Assert.assertNull(array.getArray(20))
//        org.junit.Assert.assertNull(array.getDictionary(20))
//
//        //#21 array.addDate(null);
//        org.junit.Assert.assertNull(array.getValue(21))
//        org.junit.Assert.assertFalse(array.getBoolean(21))
//        assertEquals(0, array.getInt(21))
//        assertEquals(0L, array.getLong(21))
//        assertEquals(0.0f, array.getFloat(21), 0.001f)
//        assertEquals(0.0, array.getDouble(21), 0.001)
//        org.junit.Assert.assertNull(array.getNumber(21))
//        org.junit.Assert.assertNull(array.getString(21))
//        org.junit.Assert.assertNull(array.getDate(21))
//        org.junit.Assert.assertNull(array.getBlob(21))
//        org.junit.Assert.assertNull(array.getArray(21))
//        org.junit.Assert.assertNull(array.getDictionary(21))
//
//        //#22 array.addDate(JSONUtils.toDate(TEST_DATE));
//        assertEquals(BaseDbTest.Companion.TEST_DATE, array.getValue(22))
//        org.junit.Assert.assertTrue(array.getBoolean(22))
//        assertEquals(0, array.getInt(22))
//        assertEquals(0L, array.getLong(22))
//        assertEquals(0.0f, array.getFloat(22), 0.001f)
//        assertEquals(0.0, array.getDouble(22), 0.001)
//        org.junit.Assert.assertNull(array.getNumber(22))
//        assertEquals(BaseDbTest.Companion.TEST_DATE, array.getString(22))
//        assertEquals(JSONUtils.toDate(BaseDbTest.Companion.TEST_DATE), array.getDate(22))
//        org.junit.Assert.assertNull(array.getBlob(22))
//        org.junit.Assert.assertNull(array.getArray(22))
//        org.junit.Assert.assertNull(array.getDictionary(22))
//
//        //#23 array.addArray(null);
//        org.junit.Assert.assertNull(array.getValue(23))
//        org.junit.Assert.assertFalse(array.getBoolean(23))
//        assertEquals(0, array.getInt(23))
//        assertEquals(0L, array.getLong(23))
//        assertEquals(0.0f, array.getFloat(23), 0.001f)
//        assertEquals(0.0, array.getDouble(23), 0.001)
//        org.junit.Assert.assertNull(array.getNumber(23))
//        org.junit.Assert.assertNull(array.getString(23))
//        org.junit.Assert.assertNull(array.getDate(23))
//        org.junit.Assert.assertNull(array.getBlob(23))
//        org.junit.Assert.assertNull(array.getArray(23))
//        org.junit.Assert.assertNull(array.getDictionary(23))
//
//        //#24 array.addArray(simpleArray);
//        org.junit.Assert.assertTrue(array.getValue(24) is Array)
//        org.junit.Assert.assertTrue(array.getBoolean(24))
//        assertEquals(0, array.getInt(24))
//        assertEquals(0L, array.getLong(24))
//        assertEquals(0.0f, array.getFloat(24), 0.001f)
//        assertEquals(0.0, array.getDouble(24), 0.001)
//        org.junit.Assert.assertNull(array.getNumber(24))
//        org.junit.Assert.assertNull(array.getString(24))
//        org.junit.Assert.assertNull(array.getDate(24))
//        org.junit.Assert.assertNull(array.getBlob(24))
//        org.junit.Assert.assertTrue(array.getArray(24) is Array)
//        org.junit.Assert.assertNull(array.getDictionary(24))
//
//        //#25 array.addDictionary(null);
//        org.junit.Assert.assertNull(array.getValue(25))
//        org.junit.Assert.assertFalse(array.getBoolean(25))
//        assertEquals(0, array.getInt(25))
//        assertEquals(0L, array.getLong(25))
//        assertEquals(0.0f, array.getFloat(25), 0.001f)
//        assertEquals(0.0, array.getDouble(25), 0.001)
//        org.junit.Assert.assertNull(array.getNumber(25))
//        org.junit.Assert.assertNull(array.getString(25))
//        org.junit.Assert.assertNull(array.getDate(25))
//        org.junit.Assert.assertNull(array.getBlob(25))
//        org.junit.Assert.assertNull(array.getArray(25))
//        org.junit.Assert.assertNull(array.getDictionary(25))
//
//        //#26 array.addDictionary(simpleDict);
//        org.junit.Assert.assertTrue(array.getValue(26) is Dictionary)
//        org.junit.Assert.assertTrue(array.getBoolean(26))
//        assertEquals(0, array.getInt(26))
//        assertEquals(0L, array.getLong(26))
//        assertEquals(0.0f, array.getFloat(26), 0.001f)
//        assertEquals(0.0, array.getDouble(26), 0.001)
//        org.junit.Assert.assertNull(array.getNumber(26))
//        org.junit.Assert.assertNull(array.getString(26))
//        org.junit.Assert.assertNull(array.getDate(26))
//        org.junit.Assert.assertNull(array.getBlob(26))
//        org.junit.Assert.assertNull(array.getArray(26))
//        org.junit.Assert.assertTrue(array.getDictionary(26) is Dictionary)
//    }
//
//    @NonNull
//    protected fun makeDict(): MutableDictionary {
//        // A small array
//        val simpleArray = MutableArray()
//        simpleArray.addInt(54)
//        simpleArray.addString("Joplin")
//
//        // A small dictionary
//        val simpleDict = MutableDictionary()
//        simpleDict.setInt("sdict.1", 58)
//        simpleDict.setString("sdict.2", "Winehouse")
//
//        // Dictionary:
//        val dict = MutableDictionary()
//        dict.setValue("dict-1", null)
//        dict.setBoolean("dict-2", true)
//        dict.setBoolean("dict-3", false)
//        dict.setInt("dict-4", 0)
//        dict.setInt("dict-5", Int.MIN_VALUE)
//        dict.setInt("dict-6", Int.MAX_VALUE)
//        dict.setLong("dict-7", 0L)
//        dict.setLong("dict-8", Long.MIN_VALUE)
//        dict.setLong("dict-9", Long.MAX_VALUE)
//        dict.setFloat("dict-10", 0.0f)
//        dict.setFloat("dict-11", Float.MIN_VALUE)
//        dict.setFloat("dict-12", Float.MAX_VALUE)
//        dict.setDouble("dict-13", 0.0)
//        dict.setDouble("dict-14", Double.MIN_VALUE)
//        dict.setDouble("dict-15", Double.MAX_VALUE)
//        dict.setNumber("dict-16", null)
//        dict.setNumber("dict-17", 0)
//        dict.setNumber("dict-18", Float.MIN_VALUE)
//        dict.setNumber("dict-19", Long.MIN_VALUE)
//        dict.setString("dict-20", null)
//        dict.setString("dict-21", "Quatro")
//        dict.setDate("dict-22", null)
//        dict.setDate("dict-23", JSONUtils.toDate(BaseDbTest.Companion.TEST_DATE))
//        dict.setArray("dict-24", null)
//        dict.setArray("dict-25", simpleArray)
//        dict.setDictionary("dict-26", null)
//        dict.setDictionary("dict-27", simpleDict)
//        return dict
//    }
//
//    @Throws(org.json.JSONException::class)
//    protected fun verifyDict(jObj: JSONObject) {
//        org.junit.Assert.assertEquals(27, jObj.length().toLong())
//        org.junit.Assert.assertEquals(JSONObject.NULL, jObj.get("dict-1"))
//        org.junit.Assert.assertEquals(true, jObj.get("dict-2"))
//        org.junit.Assert.assertEquals(false, jObj.get("dict-3"))
//        org.junit.Assert.assertEquals(0, jObj.get("dict-4"))
//        org.junit.Assert.assertEquals(Int.MIN_VALUE, jObj.get("dict-5"))
//        org.junit.Assert.assertEquals(Int.MAX_VALUE, jObj.get("dict-6"))
//        org.junit.Assert.assertEquals(0, jObj.get("dict-7"))
//        org.junit.Assert.assertEquals(Long.MIN_VALUE, jObj.get("dict-8"))
//        org.junit.Assert.assertEquals(Long.MAX_VALUE, jObj.get("dict-9"))
//        org.junit.Assert.assertEquals(0.0, jObj.getDouble("dict-10"), 0.001)
//        org.junit.Assert.assertEquals(
//            Float.MIN_VALUE.toDouble(),
//            jObj.getDouble("dict-11").toFloat().toDouble(),
//            0.001
//        )
//        org.junit.Assert.assertEquals(
//            Float.MAX_VALUE.toDouble(),
//            jObj.getDouble("dict-12").toFloat().toDouble(),
//            100.0
//        )
//        org.junit.Assert.assertEquals(0.0, jObj.getDouble("dict-13"), 0.001)
//        org.junit.Assert.assertEquals(Double.MIN_VALUE, jObj.getDouble("dict-14"), 0.001)
//        org.junit.Assert.assertEquals(Double.MAX_VALUE, jObj.getDouble("dict-15"), 1.0)
//        org.junit.Assert.assertEquals(JSONObject.NULL, jObj.get("dict-16"))
//        org.junit.Assert.assertEquals(0, jObj.getLong("dict-17"))
//        org.junit.Assert.assertEquals(Float.MIN_VALUE.toDouble(), jObj.getDouble("dict-18"), 0.001)
//        org.junit.Assert.assertEquals(
//            Long.MIN_VALUE.toDouble(),
//            jObj.getLong("dict-19").toDouble(),
//            0.001
//        )
//        org.junit.Assert.assertEquals(JSONObject.NULL, jObj.get("dict-20"))
//        org.junit.Assert.assertEquals("Quatro", jObj.get("dict-21"))
//        org.junit.Assert.assertEquals(JSONObject.NULL, jObj.get("dict-22"))
//        org.junit.Assert.assertEquals(BaseDbTest.Companion.TEST_DATE, jObj.get("dict-23"))
//        org.junit.Assert.assertEquals(JSONObject.NULL, jObj.get("dict-24"))
//        org.junit.Assert.assertEquals(JSONArray::class.java, jObj.get("dict-25").javaClass)
//        org.junit.Assert.assertEquals(JSONObject.NULL, jObj.get("dict-26"))
//        org.junit.Assert.assertEquals(JSONObject::class.java, jObj.get("dict-27").javaClass)
//    }
//
//    protected fun verifyDict(@Nullable dict: DictionaryInterface) {
//        org.junit.Assert.assertNotNull(dict)
//        org.junit.Assert.assertEquals(27, dict.count().toLong())
//
//        //#0 dict.setValue(null);
//        org.junit.Assert.assertNull(dict.getValue("dict-1"))
//        org.junit.Assert.assertFalse(dict.getBoolean("dict-1"))
//        org.junit.Assert.assertEquals(0, dict.getInt("dict-1").toLong())
//        org.junit.Assert.assertEquals(0L, dict.getLong("dict-1"))
//        org.junit.Assert.assertEquals(0.0f, dict.getFloat("dict-1"), 0.001f)
//        org.junit.Assert.assertEquals(0.0, dict.getDouble("dict-1"), 0.001)
//        org.junit.Assert.assertNull(dict.getNumber("dict-1"))
//        org.junit.Assert.assertNull(dict.getString("dict-1"))
//        org.junit.Assert.assertNull(dict.getDate("dict-1"))
//        org.junit.Assert.assertNull(dict.getBlob("dict-1"))
//        org.junit.Assert.assertNull(dict.getArray("dict-1"))
//        org.junit.Assert.assertNull(dict.getDictionary("dict-1"))
//
//        //#1 dict.setBoolean(true);
//        org.junit.Assert.assertEquals(java.lang.Boolean.TRUE, dict.getValue("dict-2"))
//        org.junit.Assert.assertTrue(dict.getBoolean("dict-2"))
//        org.junit.Assert.assertEquals(1, dict.getInt("dict-2").toLong())
//        org.junit.Assert.assertEquals(1L, dict.getLong("dict-2"))
//        org.junit.Assert.assertEquals(1.0f, dict.getFloat("dict-2"), 0.001f)
//        org.junit.Assert.assertEquals(1.0, dict.getDouble("dict-2"), 0.001)
//        org.junit.Assert.assertEquals(1, dict.getNumber("dict-2"))
//        org.junit.Assert.assertNull(dict.getString("dict-2"))
//        org.junit.Assert.assertNull(dict.getDate("dict-2"))
//        org.junit.Assert.assertNull(dict.getBlob("dict-2"))
//        org.junit.Assert.assertNull(dict.getArray("dict-2"))
//        org.junit.Assert.assertNull(dict.getDictionary("dict-2"))
//
//        //#2 dict.setBoolean(false);
//        org.junit.Assert.assertEquals(java.lang.Boolean.FALSE, dict.getValue("dict-3"))
//        org.junit.Assert.assertFalse(dict.getBoolean("dict-3"))
//        org.junit.Assert.assertEquals(0, dict.getInt("dict-3").toLong())
//        org.junit.Assert.assertEquals(0L, dict.getLong("dict-3"))
//        org.junit.Assert.assertEquals(0.0f, dict.getFloat("dict-3"), 0.001f)
//        org.junit.Assert.assertEquals(0.0, dict.getDouble("dict-3"), 0.001)
//        org.junit.Assert.assertEquals(0, dict.getNumber("dict-3"))
//        org.junit.Assert.assertNull(dict.getString("dict-3"))
//        org.junit.Assert.assertNull(dict.getDate("dict-3"))
//        org.junit.Assert.assertNull(dict.getBlob("dict-3"))
//        org.junit.Assert.assertNull(dict.getArray("dict-3"))
//        org.junit.Assert.assertNull(dict.getDictionary("dict-3"))
//
//        //#3 dict.setInt(0);
//        org.junit.Assert.assertEquals(0L, dict.getValue("dict-4"))
//        org.junit.Assert.assertFalse(dict.getBoolean("dict-4"))
//        org.junit.Assert.assertEquals(0, dict.getInt("dict-4").toLong())
//        org.junit.Assert.assertEquals(0L, dict.getLong("dict-4"))
//        org.junit.Assert.assertEquals(0.0f, dict.getFloat("dict-4"), 0.001f)
//        org.junit.Assert.assertEquals(0.0, dict.getDouble("dict-4"), 0.001)
//        org.junit.Assert.assertEquals(0L, dict.getNumber("dict-4"))
//        org.junit.Assert.assertNull(dict.getString("dict-4"))
//        org.junit.Assert.assertNull(dict.getDate("dict-4"))
//        org.junit.Assert.assertNull(dict.getBlob("dict-4"))
//        org.junit.Assert.assertNull(dict.getArray("dict-4"))
//        org.junit.Assert.assertNull(dict.getDictionary("dict-4"))
//
//        //#4 dict.setInt(Integer.MIN_VALUE);
//        org.junit.Assert.assertEquals(
//            java.lang.Long.valueOf(Int.MIN_VALUE.toLong()),
//            dict.getValue("dict-5")
//        )
//        org.junit.Assert.assertTrue(dict.getBoolean("dict-5"))
//        org.junit.Assert.assertEquals(Int.MIN_VALUE.toLong(), dict.getInt("dict-5").toLong())
//        org.junit.Assert.assertEquals(Int.MIN_VALUE.toLong(), dict.getLong("dict-5"))
//        org.junit.Assert.assertEquals(Int.MIN_VALUE.toFloat(), dict.getFloat("dict-5"), 0.001f)
//        org.junit.Assert.assertEquals(Int.MIN_VALUE.toDouble(), dict.getDouble("dict-5"), 0.001)
//        org.junit.Assert.assertEquals(
//            java.lang.Long.valueOf(Int.MIN_VALUE.toLong()),
//            dict.getNumber("dict-5")
//        )
//        org.junit.Assert.assertNull(dict.getString("dict-5"))
//        org.junit.Assert.assertNull(dict.getDate("dict-5"))
//        org.junit.Assert.assertNull(dict.getBlob("dict-5"))
//        org.junit.Assert.assertNull(dict.getArray("dict-5"))
//        org.junit.Assert.assertNull(dict.getDictionary("dict-5"))
//
//        //#5 dict.setInt(Integer.MAX_VALUE);
//        org.junit.Assert.assertEquals(
//            java.lang.Long.valueOf(Int.MAX_VALUE.toLong()),
//            dict.getValue("dict-6")
//        )
//        org.junit.Assert.assertTrue(dict.getBoolean("dict-6"))
//        org.junit.Assert.assertEquals(Int.MAX_VALUE.toLong(), dict.getInt("dict-6").toLong())
//        org.junit.Assert.assertEquals(Int.MAX_VALUE.toLong(), dict.getLong("dict-6"))
//        org.junit.Assert.assertEquals(Int.MAX_VALUE.toFloat(), dict.getFloat("dict-6"), 100.0f)
//        org.junit.Assert.assertEquals(Int.MAX_VALUE.toDouble(), dict.getDouble("dict-6"), 100.0)
//        org.junit.Assert.assertEquals(
//            java.lang.Long.valueOf(Int.MAX_VALUE.toLong()),
//            dict.getNumber("dict-6")
//        )
//        org.junit.Assert.assertNull(dict.getString("dict-6"))
//        org.junit.Assert.assertNull(dict.getDate("dict-6"))
//        org.junit.Assert.assertNull(dict.getBlob("dict-6"))
//        org.junit.Assert.assertNull(dict.getArray("dict-6"))
//        org.junit.Assert.assertNull(dict.getDictionary("dict-6"))
//
//        //#6 dict.setLong(0L);
//        org.junit.Assert.assertEquals(java.lang.Long.valueOf(0L), dict.getValue("dict-7"))
//        org.junit.Assert.assertFalse(dict.getBoolean("dict-7"))
//        org.junit.Assert.assertEquals(0, dict.getInt("dict-7").toLong())
//        org.junit.Assert.assertEquals(0L, dict.getLong("dict-7"))
//        org.junit.Assert.assertEquals(0.0f, dict.getFloat("dict-7"), 0.001f)
//        org.junit.Assert.assertEquals(0.0, dict.getDouble("dict-7"), 0.001)
//        org.junit.Assert.assertEquals(java.lang.Long.valueOf(0L), dict.getNumber("dict-7"))
//        org.junit.Assert.assertNull(dict.getString("dict-7"))
//        org.junit.Assert.assertNull(dict.getDate("dict-7"))
//        org.junit.Assert.assertNull(dict.getBlob("dict-7"))
//        org.junit.Assert.assertNull(dict.getArray("dict-7"))
//        org.junit.Assert.assertNull(dict.getDictionary("dict-7"))
//
//        //#7 dict.setLong(Long.MIN_VALUE);
//        org.junit.Assert.assertEquals(
//            java.lang.Long.valueOf(Long.MIN_VALUE),
//            dict.getValue("dict-8")
//        )
//        org.junit.Assert.assertFalse(dict.getBoolean("dict-8"))
//        org.junit.Assert.assertEquals(
//            java.lang.Long.valueOf(Long.MIN_VALUE).toInt().toLong(),
//            dict.getInt("dict-8").toLong()
//        )
//        org.junit.Assert.assertEquals(Long.MIN_VALUE, dict.getLong("dict-8"))
//        org.junit.Assert.assertEquals(Long.MIN_VALUE.toFloat(), dict.getFloat("dict-8"), 0.001f)
//        org.junit.Assert.assertEquals(Long.MIN_VALUE.toDouble(), dict.getDouble("dict-8"), 0.001)
//        org.junit.Assert.assertEquals(
//            java.lang.Long.valueOf(Long.MIN_VALUE),
//            dict.getNumber("dict-8")
//        )
//        org.junit.Assert.assertNull(dict.getString("dict-8"))
//        org.junit.Assert.assertNull(dict.getDate("dict-8"))
//        org.junit.Assert.assertNull(dict.getBlob("dict-8"))
//        org.junit.Assert.assertNull(dict.getArray("dict-8"))
//        org.junit.Assert.assertNull(dict.getDictionary("dict-8"))
//
//        //#8 dict.setLong(Long.MAX_VALUE);
//        org.junit.Assert.assertEquals(
//            java.lang.Long.valueOf(Long.MAX_VALUE),
//            dict.getValue("dict-9")
//        )
//        org.junit.Assert.assertTrue(dict.getBoolean("dict-9"))
//        org.junit.Assert.assertEquals(
//            java.lang.Long.valueOf(Long.MAX_VALUE).toInt().toLong(),
//            dict.getInt("dict-9").toLong()
//        )
//        org.junit.Assert.assertEquals(Long.MAX_VALUE, dict.getLong("dict-9"))
//        org.junit.Assert.assertEquals(Long.MAX_VALUE.toFloat(), dict.getFloat("dict-9"), 100.0f)
//        org.junit.Assert.assertEquals(Long.MAX_VALUE.toDouble(), dict.getDouble("dict-9"), 100.0)
//        org.junit.Assert.assertEquals(
//            java.lang.Long.valueOf(Long.MAX_VALUE),
//            dict.getNumber("dict-9")
//        )
//        org.junit.Assert.assertNull(dict.getString("dict-9"))
//        org.junit.Assert.assertNull(dict.getDate("dict-9"))
//        org.junit.Assert.assertNull(dict.getBlob("dict-9"))
//        org.junit.Assert.assertNull(dict.getArray("dict-9"))
//        org.junit.Assert.assertNull(dict.getDictionary("dict-9"))
//
//        //#9 dict.setFloat(0.0F);
//        org.junit.Assert.assertEquals(java.lang.Float.valueOf(0.0f), dict.getValue("dict-10"))
//        org.junit.Assert.assertFalse(dict.getBoolean("dict-10"))
//        org.junit.Assert.assertEquals(0, dict.getInt("dict-10").toLong())
//        org.junit.Assert.assertEquals(0L, dict.getLong("dict-10"))
//        org.junit.Assert.assertEquals(0.0f, dict.getFloat("dict-10"), 0.001f)
//        org.junit.Assert.assertEquals(0.0, dict.getDouble("dict-10"), 0.001)
//        org.junit.Assert.assertEquals(java.lang.Float.valueOf(0.0f), dict.getNumber("dict-10"))
//        org.junit.Assert.assertNull(dict.getString("dict-10"))
//        org.junit.Assert.assertNull(dict.getDate("dict-10"))
//        org.junit.Assert.assertNull(dict.getBlob("dict-10"))
//        org.junit.Assert.assertNull(dict.getArray("dict-10"))
//        org.junit.Assert.assertNull(dict.getDictionary("dict-10"))
//
//        //#10 dict.setFloat(Float.MIN_VALUE);
//        org.junit.Assert.assertEquals(
//            java.lang.Float.valueOf(Float.MIN_VALUE),
//            demoteToFloat(dict.getValue("dict-11"))
//        )
//        org.junit.Assert.assertFalse(dict.getBoolean("dict-11"))
//        org.junit.Assert.assertEquals(
//            java.lang.Float.valueOf(Float.MIN_VALUE).toInt().toFloat(),
//            dict.getInt("dict-11").toFloat(),
//            0.001f
//        )
//        org.junit.Assert.assertEquals(
//            java.lang.Float.valueOf(Float.MIN_VALUE).toLong().toFloat(),
//            dict.getLong("dict-11").toFloat(),
//            0.001f
//        )
//        org.junit.Assert.assertEquals(Float.MIN_VALUE, dict.getFloat("dict-11"), 0.001f)
//        org.junit.Assert.assertEquals(Float.MIN_VALUE.toDouble(), dict.getDouble("dict-11"), 0.001)
//        org.junit.Assert.assertEquals(
//            java.lang.Float.valueOf(Float.MIN_VALUE),
//            demoteToFloat(dict.getNumber("dict-11"))
//        )
//        org.junit.Assert.assertNull(dict.getString("dict-11"))
//        org.junit.Assert.assertNull(dict.getDate("dict-11"))
//        org.junit.Assert.assertNull(dict.getBlob("dict-11"))
//        org.junit.Assert.assertNull(dict.getArray("dict-11"))
//        org.junit.Assert.assertNull(dict.getDictionary("dict-11"))
//
//        //#11 dict.setFloat(Float.MAX_VALUE);
//        org.junit.Assert.assertEquals(
//            java.lang.Float.valueOf(Float.MAX_VALUE),
//            demoteToFloat(dict.getValue("dict-12"))
//        )
//        org.junit.Assert.assertTrue(dict.getBoolean("dict-12"))
//        // !!! Fails: assertEquals(Float.valueOf(Float.MAX_VALUE).intValue(), dict.getInt("dict-12"));
//        // !!! Fails: assertEquals(Float.valueOf(Float.MAX_VALUE).longValue(), dict.getLong("dict-12"));
//        org.junit.Assert.assertEquals(
//            Float.MAX_VALUE.toDouble(),
//            dict.getFloat("dict-12").toDouble(),
//            1.0E32
//        )
//        org.junit.Assert.assertEquals(Float.MAX_VALUE.toDouble(), dict.getDouble("dict-12"), 1.0E32)
//        org.junit.Assert.assertEquals(
//            java.lang.Float.valueOf(Float.MAX_VALUE),
//            demoteToFloat(dict.getNumber("dict-12"))
//        )
//        org.junit.Assert.assertNull(dict.getString("dict-12"))
//        org.junit.Assert.assertNull(dict.getDate("dict-12"))
//        org.junit.Assert.assertNull(dict.getBlob("dict-12"))
//        org.junit.Assert.assertNull(dict.getArray("dict-12"))
//        org.junit.Assert.assertNull(dict.getDictionary("dict-12"))
//
//        //#12 dict.setDouble(0.0);
//        org.junit.Assert.assertEquals(java.lang.Float.valueOf(0f), dict.getValue("dict-13"))
//        org.junit.Assert.assertFalse(dict.getBoolean("dict-13"))
//        org.junit.Assert.assertEquals(0, dict.getInt("dict-13").toLong())
//        org.junit.Assert.assertEquals(0L, dict.getLong("dict-13"))
//        org.junit.Assert.assertEquals(0.0f, dict.getFloat("dict-13"), 0.001f)
//        org.junit.Assert.assertEquals(0.0, dict.getDouble("dict-13"), 0.001)
//        org.junit.Assert.assertEquals(java.lang.Float.valueOf(0f), dict.getNumber("dict-13"))
//        org.junit.Assert.assertNull(dict.getString("dict-13"))
//        org.junit.Assert.assertNull(dict.getDate("dict-13"))
//        org.junit.Assert.assertNull(dict.getBlob("dict-13"))
//        org.junit.Assert.assertNull(dict.getArray("dict-13"))
//        org.junit.Assert.assertNull(dict.getDictionary("dict-13"))
//
//        //#13 dict.setDouble(Double.MIN_VALUE);
//        org.junit.Assert.assertEquals(
//            java.lang.Double.valueOf(Double.MIN_VALUE),
//            dict.getValue("dict-14")
//        )
//        org.junit.Assert.assertFalse(dict.getBoolean("dict-14"))
//        org.junit.Assert.assertEquals(
//            java.lang.Double.valueOf(Double.MIN_VALUE).toInt().toLong(),
//            dict.getInt("dict-14").toLong()
//        )
//        org.junit.Assert.assertEquals(
//            java.lang.Double.valueOf(Double.MIN_VALUE).toLong(),
//            dict.getLong("dict-14")
//        )
//        org.junit.Assert.assertEquals(
//            java.lang.Double.valueOf(Double.MIN_VALUE).toFloat(),
//            dict.getFloat("dict-14"),
//            0.001f
//        )
//        org.junit.Assert.assertEquals(Double.MIN_VALUE, dict.getDouble("dict-14"), 0.001)
//        org.junit.Assert.assertEquals(
//            java.lang.Double.valueOf(Double.MIN_VALUE),
//            dict.getNumber("dict-14")
//        )
//        org.junit.Assert.assertNull(dict.getString("dict-14"))
//        org.junit.Assert.assertNull(dict.getDate("dict-14"))
//        org.junit.Assert.assertNull(dict.getBlob("dict-14"))
//        org.junit.Assert.assertNull(dict.getArray("dict-14"))
//        org.junit.Assert.assertNull(dict.getDictionary("dict-14"))
//
//        //#14 dict.setDouble(Double.MAX_VALUE);
//        org.junit.Assert.assertEquals(
//            java.lang.Double.valueOf(Double.MAX_VALUE),
//            dict.getValue("dict-15")
//        )
//        org.junit.Assert.assertTrue(dict.getBoolean("dict-15"))
//        // !!! Fails: assertEquals(Double.valueOf(Double.MAX_VALUE).intValue(), dict.getInt("dict-15"));
//        // !!! Fails: assertEquals(Double.valueOf(Double.MAX_VALUE).longValue(), dict.getLong("dict-15"));
//        org.junit.Assert.assertEquals(
//            java.lang.Double.valueOf(Double.MAX_VALUE).toFloat(),
//            dict.getFloat("dict-15"),
//            100.0f
//        )
//        org.junit.Assert.assertEquals(Double.MAX_VALUE, dict.getDouble("dict-15"), 100.0)
//        org.junit.Assert.assertEquals(
//            java.lang.Double.valueOf(Double.MAX_VALUE),
//            dict.getNumber("dict-15")
//        )
//        org.junit.Assert.assertNull(dict.getString("dict-15"))
//        org.junit.Assert.assertNull(dict.getDate("dict-15"))
//        org.junit.Assert.assertNull(dict.getBlob("dict-15"))
//        org.junit.Assert.assertNull(dict.getArray("dict-15"))
//        org.junit.Assert.assertNull(dict.getDictionary("dict-15"))
//
//        //#15 dict.setNumber(null);
//        org.junit.Assert.assertNull(dict.getValue("dict-16"))
//        org.junit.Assert.assertFalse(dict.getBoolean("dict-16"))
//        org.junit.Assert.assertEquals(0, dict.getInt("dict-16").toLong())
//        org.junit.Assert.assertEquals(0L, dict.getLong("dict-16"))
//        org.junit.Assert.assertEquals(0.0f, dict.getFloat("dict-16"), 0.001f)
//        org.junit.Assert.assertEquals(0.0, dict.getDouble("dict-16"), 0.001)
//        org.junit.Assert.assertNull(dict.getNumber("dict-16"))
//        org.junit.Assert.assertNull(dict.getString("dict-16"))
//        org.junit.Assert.assertNull(dict.getDate("dict-16"))
//        org.junit.Assert.assertNull(dict.getBlob("dict-16"))
//        org.junit.Assert.assertNull(dict.getArray("dict-16"))
//        org.junit.Assert.assertNull(dict.getDictionary("dict-16"))
//
//        //#16 dict.setNumber(0);
//        org.junit.Assert.assertEquals(0L, dict.getValue("dict-17"))
//        org.junit.Assert.assertFalse(dict.getBoolean("dict-17"))
//        org.junit.Assert.assertEquals(0, dict.getInt("dict-17").toLong())
//        org.junit.Assert.assertEquals(0L, dict.getLong("dict-17"))
//        org.junit.Assert.assertEquals(0.0f, dict.getFloat("dict-17"), 0.001f)
//        org.junit.Assert.assertEquals(0.0, dict.getDouble("dict-17"), 0.001)
//        org.junit.Assert.assertEquals(0L, dict.getNumber("dict-17"))
//        org.junit.Assert.assertNull(dict.getString("dict-17"))
//        org.junit.Assert.assertNull(dict.getDate("dict-17"))
//        org.junit.Assert.assertNull(dict.getBlob("dict-17"))
//        org.junit.Assert.assertNull(dict.getArray("dict-17"))
//        org.junit.Assert.assertNull(dict.getDictionary("dict-17"))
//
//        //#17 dict.setNumber(Float.MIN_VALUE);
//        org.junit.Assert.assertEquals(
//            java.lang.Float.valueOf(Float.MIN_VALUE),
//            demoteToFloat(dict.getValue("dict-18"))
//        )
//        org.junit.Assert.assertFalse(dict.getBoolean("dict-18"))
//        org.junit.Assert.assertEquals(
//            java.lang.Float.valueOf(Float.MIN_VALUE).toInt().toLong(),
//            dict.getInt("dict-18").toLong()
//        )
//        org.junit.Assert.assertEquals(
//            java.lang.Float.valueOf(Float.MIN_VALUE).toLong(),
//            dict.getLong("dict-18")
//        )
//        org.junit.Assert.assertEquals(Float.MIN_VALUE, dict.getFloat("dict-18"), 0.001f)
//        org.junit.Assert.assertEquals(Float.MIN_VALUE.toDouble(), dict.getDouble("dict-18"), 0.001)
//        org.junit.Assert.assertEquals(
//            java.lang.Float.valueOf(Float.MIN_VALUE),
//            demoteToFloat(dict.getNumber("dict-18"))
//        )
//        org.junit.Assert.assertNull(dict.getString("dict-18"))
//        org.junit.Assert.assertNull(dict.getDate("dict-18"))
//        org.junit.Assert.assertNull(dict.getBlob("dict-18"))
//        org.junit.Assert.assertNull(dict.getArray("dict-18"))
//        org.junit.Assert.assertNull(dict.getDictionary("dict-18"))
//
//        //#18 dict.setNumber(Long.MIN_VALUE);
//        org.junit.Assert.assertEquals(
//            java.lang.Long.valueOf(Long.MIN_VALUE),
//            dict.getValue("dict-19")
//        )
//        org.junit.Assert.assertFalse(dict.getBoolean("dict-19"))
//        org.junit.Assert.assertEquals(
//            java.lang.Long.valueOf(Long.MIN_VALUE).toInt().toLong(),
//            dict.getInt("dict-19").toLong()
//        )
//        org.junit.Assert.assertEquals(Long.MIN_VALUE, dict.getLong("dict-19"))
//        org.junit.Assert.assertEquals(Long.MIN_VALUE.toFloat(), dict.getFloat("dict-19"), 0.001f)
//        org.junit.Assert.assertEquals(Long.MIN_VALUE.toDouble(), dict.getDouble("dict-19"), 0.001)
//        org.junit.Assert.assertEquals(
//            java.lang.Long.valueOf(Long.MIN_VALUE),
//            dict.getNumber("dict-19")
//        )
//        org.junit.Assert.assertNull(dict.getString("dict-19"))
//        org.junit.Assert.assertNull(dict.getDate("dict-19"))
//        org.junit.Assert.assertNull(dict.getBlob("dict-19"))
//        org.junit.Assert.assertNull(dict.getArray("dict-19"))
//        org.junit.Assert.assertNull(dict.getDictionary("dict-19"))
//
//        //#19 dict.setString(null);
//        org.junit.Assert.assertNull(dict.getValue("dict-20"))
//        org.junit.Assert.assertFalse(dict.getBoolean("dict-20"))
//        org.junit.Assert.assertEquals(0, dict.getInt("dict-20").toLong())
//        org.junit.Assert.assertEquals(0L, dict.getLong("dict-20"))
//        org.junit.Assert.assertEquals(0.0f, dict.getFloat("dict-20"), 0.001f)
//        org.junit.Assert.assertEquals(0.0, dict.getDouble("dict-20"), 0.001)
//        org.junit.Assert.assertNull(dict.getNumber("dict-20"))
//        org.junit.Assert.assertNull(dict.getString("dict-20"))
//        org.junit.Assert.assertNull(dict.getDate("dict-20"))
//        org.junit.Assert.assertNull(dict.getBlob("dict-20"))
//        org.junit.Assert.assertNull(dict.getArray("dict-20"))
//        org.junit.Assert.assertNull(dict.getDictionary("dict-20"))
//
//        //#20 dict.setString("Quatro");
//        org.junit.Assert.assertEquals("Quatro", dict.getValue("dict-21"))
//        org.junit.Assert.assertTrue(dict.getBoolean("dict-21"))
//        org.junit.Assert.assertEquals(0, dict.getInt("dict-21").toLong())
//        org.junit.Assert.assertEquals(0, dict.getLong("dict-21"))
//        org.junit.Assert.assertEquals(0.0f, dict.getFloat("dict-21"), 0.001f)
//        org.junit.Assert.assertEquals(0.0, dict.getDouble("dict-21"), 0.001)
//        org.junit.Assert.assertNull(dict.getNumber("dict-21"))
//        org.junit.Assert.assertEquals("Quatro", dict.getString("dict-21"))
//        org.junit.Assert.assertNull(dict.getDate("dict-21"))
//        org.junit.Assert.assertNull(dict.getBlob("dict-21"))
//        org.junit.Assert.assertNull(dict.getArray("dict-21"))
//        org.junit.Assert.assertNull(dict.getDictionary("dict-21"))
//
//        //#21 dict.setDate(null);
//        org.junit.Assert.assertNull(dict.getValue("dict-22"))
//        org.junit.Assert.assertFalse(dict.getBoolean("dict-22"))
//        org.junit.Assert.assertEquals(0, dict.getInt("dict-22").toLong())
//        org.junit.Assert.assertEquals(0L, dict.getLong("dict-22"))
//        org.junit.Assert.assertEquals(0.0f, dict.getFloat("dict-22"), 0.001f)
//        org.junit.Assert.assertEquals(0.0, dict.getDouble("dict-22"), 0.001)
//        org.junit.Assert.assertNull(dict.getNumber("dict-22"))
//        org.junit.Assert.assertNull(dict.getString("dict-22"))
//        org.junit.Assert.assertNull(dict.getDate("dict-22"))
//        org.junit.Assert.assertNull(dict.getBlob("dict-22"))
//        org.junit.Assert.assertNull(dict.getArray("dict-22"))
//        org.junit.Assert.assertNull(dict.getDictionary("dict-22"))
//
//        //#22 dict.setDate(JSONUtils.toDate(TEST_DATE));
//        org.junit.Assert.assertEquals(BaseDbTest.Companion.TEST_DATE, dict.getValue("dict-23"))
//        org.junit.Assert.assertTrue(dict.getBoolean("dict-23"))
//        org.junit.Assert.assertEquals(0, dict.getInt("dict-23").toLong())
//        org.junit.Assert.assertEquals(0L, dict.getLong("dict-23"))
//        org.junit.Assert.assertEquals(0.0f, dict.getFloat("dict-23"), 0.001f)
//        org.junit.Assert.assertEquals(0.0, dict.getDouble("dict-23"), 0.001)
//        org.junit.Assert.assertNull(dict.getNumber("dict-23"))
//        org.junit.Assert.assertEquals(BaseDbTest.Companion.TEST_DATE, dict.getString("dict-23"))
//        org.junit.Assert.assertEquals(
//            JSONUtils.toDate(BaseDbTest.Companion.TEST_DATE),
//            dict.getDate("dict-23")
//        )
//        org.junit.Assert.assertNull(dict.getBlob("dict-23"))
//        org.junit.Assert.assertNull(dict.getArray("dict-23"))
//        org.junit.Assert.assertNull(dict.getDictionary("dict-23"))
//
//        //#23 dict.setArray(null);
//        org.junit.Assert.assertNull(dict.getValue("dict-24"))
//        org.junit.Assert.assertFalse(dict.getBoolean("dict-24"))
//        org.junit.Assert.assertEquals(0, dict.getInt("dict-24").toLong())
//        org.junit.Assert.assertEquals(0L, dict.getLong("dict-24"))
//        org.junit.Assert.assertEquals(0.0f, dict.getFloat("dict-24"), 0.001f)
//        org.junit.Assert.assertEquals(0.0, dict.getDouble("dict-24"), 0.001)
//        org.junit.Assert.assertNull(dict.getNumber("dict-24"))
//        org.junit.Assert.assertNull(dict.getString("dict-24"))
//        org.junit.Assert.assertNull(dict.getDate("dict-24"))
//        org.junit.Assert.assertNull(dict.getBlob("dict-24"))
//        org.junit.Assert.assertNull(dict.getArray("dict-24"))
//        org.junit.Assert.assertNull(dict.getDictionary("dict-24"))
//
//        //#24 dict.setArray(simpleArray);
//        org.junit.Assert.assertTrue(dict.getValue("dict-25") is Array)
//        org.junit.Assert.assertTrue(dict.getBoolean("dict-25"))
//        org.junit.Assert.assertEquals(0, dict.getInt("dict-25").toLong())
//        org.junit.Assert.assertEquals(0L, dict.getLong("dict-25"))
//        org.junit.Assert.assertEquals(0.0f, dict.getFloat("dict-25"), 0.001f)
//        org.junit.Assert.assertEquals(0.0, dict.getDouble("dict-25"), 0.001)
//        org.junit.Assert.assertNull(dict.getNumber("dict-25"))
//        org.junit.Assert.assertNull(dict.getString("dict-25"))
//        org.junit.Assert.assertNull(dict.getDate("dict-25"))
//        org.junit.Assert.assertNull(dict.getBlob("dict-25"))
//        org.junit.Assert.assertTrue(dict.getArray("dict-25") is Array)
//        org.junit.Assert.assertNull(dict.getDictionary("dict-25"))
//
//        //#25 dict.setDictionary(null);
//        org.junit.Assert.assertNull(dict.getValue("dict-26"))
//        org.junit.Assert.assertFalse(dict.getBoolean("dict-26"))
//        org.junit.Assert.assertEquals(0, dict.getInt("dict-26").toLong())
//        org.junit.Assert.assertEquals(0L, dict.getLong("dict-26"))
//        org.junit.Assert.assertEquals(0.0f, dict.getFloat("dict-26"), 0.001f)
//        org.junit.Assert.assertEquals(0.0, dict.getDouble("dict-26"), 0.001)
//        org.junit.Assert.assertNull(dict.getNumber("dict-26"))
//        org.junit.Assert.assertNull(dict.getString("dict-26"))
//        org.junit.Assert.assertNull(dict.getDate("dict-26"))
//        org.junit.Assert.assertNull(dict.getBlob("dict-26"))
//        org.junit.Assert.assertNull(dict.getArray("dict-26"))
//        org.junit.Assert.assertNull(dict.getDictionary("dict-26"))
//
//        //#26 dict.setDictionary(simpleDict);
//        org.junit.Assert.assertTrue(dict.getValue("dict-27") is Dictionary)
//        org.junit.Assert.assertTrue(dict.getBoolean("dict-27"))
//        org.junit.Assert.assertEquals(0, dict.getInt("dict-27").toLong())
//        org.junit.Assert.assertEquals(0L, dict.getLong("dict-27"))
//        org.junit.Assert.assertEquals(0.0f, dict.getFloat("dict-27"), 0.001f)
//        org.junit.Assert.assertEquals(0.0, dict.getDouble("dict-27"), 0.001)
//        org.junit.Assert.assertNull(dict.getNumber("dict-27"))
//        org.junit.Assert.assertNull(dict.getString("dict-27"))
//        org.junit.Assert.assertNull(dict.getDate("dict-27"))
//        org.junit.Assert.assertNull(dict.getBlob("dict-27"))
//        org.junit.Assert.assertNull(dict.getArray("dict-27"))
//        org.junit.Assert.assertTrue(dict.getDictionary("dict-27") is Dictionary)
//    }
//
//    protected fun makeBlob(): Blob {
//        return Blob(
//            "text/plain",
//            BaseDbTest.Companion.BLOB_CONTENT.toByteArray(java.nio.charset.StandardCharsets.UTF_8)
//        )
//    }
//
//    @Throws(org.json.JSONException::class)
//    protected fun verifyBlob(jBlob: JSONObject) {
//        org.junit.Assert.assertEquals(4, jBlob.length().toLong())
//        assertEquals(Blob.TYPE_BLOB, jBlob.get(Blob.META_PROP_TYPE))
//        org.junit.Assert.assertEquals(
//            "sha1-C+QguVamTgLjyDQ9RzRtyCv6x60=",
//            jBlob.get(Blob.PROP_DIGEST)
//        )
//        org.junit.Assert.assertEquals(59, jBlob.getLong(Blob.PROP_LENGTH))
//        org.junit.Assert.assertEquals("text/plain", jBlob.get(Blob.PROP_CONTENT_TYPE))
//    }
//
//    protected fun verifyBlob(@Nullable blob: Blob) {
//        org.junit.Assert.assertNotNull(blob)
//        assertEquals("sha1-C+QguVamTgLjyDQ9RzRtyCv6x60=", blob.digest())
//        org.junit.Assert.assertEquals(59, blob.length())
//        assertEquals("text/plain", blob.getContentType())
//        org.junit.Assert.assertEquals(BaseDbTest.Companion.BLOB_CONTENT, String(blob.getContent()))
//    }
//
//    protected fun makeDocument(): MutableDocument {
//        // Dictionary:
//        val mDoc = MutableDocument()
//        mDoc.setValue("doc-1", null)
//        mDoc.setBoolean("doc-2", true)
//        mDoc.setBoolean("doc-3", false)
//        mDoc.setInt("doc-4", 0)
//        mDoc.setInt("doc-5", Int.MIN_VALUE)
//        mDoc.setInt("doc-6", Int.MAX_VALUE)
//        mDoc.setLong("doc-7", 0L)
//        mDoc.setLong("doc-8", Long.MIN_VALUE)
//        mDoc.setLong("doc-9", Long.MAX_VALUE)
//        mDoc.setFloat("doc-10", 0.0f)
//        mDoc.setFloat("doc-11", Float.MIN_VALUE)
//        mDoc.setFloat("doc-12", Float.MAX_VALUE)
//        mDoc.setDouble("doc-13", 0.0)
//        mDoc.setDouble("doc-14", Double.MIN_VALUE)
//        mDoc.setDouble("doc-15", Double.MAX_VALUE)
//        mDoc.setNumber("doc-16", null)
//        mDoc.setNumber("doc-17", 0)
//        mDoc.setNumber("doc-18", Float.MIN_VALUE)
//        mDoc.setNumber("doc-19", Long.MIN_VALUE)
//        mDoc.setString("doc-20", null)
//        mDoc.setString("doc-21", "Jett")
//        mDoc.setDate("doc-22", null)
//        mDoc.setDate("doc-23", JSONUtils.toDate(BaseDbTest.Companion.TEST_DATE))
//        mDoc.setArray("doc-24", null)
//        mDoc.setArray("doc-25", makeArray())
//        mDoc.setDictionary("doc-26", null)
//        mDoc.setDictionary("doc-27", makeDict())
//        mDoc.setBlob("doc-28", null)
//        mDoc.setBlob("doc-29", makeBlob())
//        return mDoc
//    }
//
//    @Throws(org.json.JSONException::class)
//    protected fun verifyDocument(jObj: JSONObject) {
//        org.junit.Assert.assertEquals(29, jObj.length().toLong())
//        org.junit.Assert.assertEquals(JSONObject.NULL, jObj.get("doc-1"))
//        org.junit.Assert.assertEquals(true, jObj.get("doc-2"))
//        org.junit.Assert.assertEquals(false, jObj.get("doc-3"))
//        org.junit.Assert.assertEquals(0, jObj.get("doc-4"))
//        org.junit.Assert.assertEquals(Int.MIN_VALUE, jObj.get("doc-5"))
//        org.junit.Assert.assertEquals(Int.MAX_VALUE, jObj.get("doc-6"))
//        org.junit.Assert.assertEquals(0, jObj.get("doc-7"))
//        org.junit.Assert.assertEquals(Long.MIN_VALUE, jObj.get("doc-8"))
//        org.junit.Assert.assertEquals(Long.MAX_VALUE, jObj.get("doc-9"))
//        org.junit.Assert.assertEquals(0.0, jObj.getDouble("doc-10"), 0.001)
//        org.junit.Assert.assertEquals(
//            Float.MIN_VALUE.toDouble(),
//            jObj.getDouble("doc-11").toFloat().toDouble(),
//            0.001
//        )
//        org.junit.Assert.assertEquals(
//            Float.MAX_VALUE.toDouble(),
//            jObj.getDouble("doc-12").toFloat().toDouble(),
//            100.0
//        )
//        org.junit.Assert.assertEquals(0.0, jObj.getDouble("doc-13"), 0.001)
//        org.junit.Assert.assertEquals(Double.MIN_VALUE, jObj.getDouble("doc-14"), 0.001)
//        org.junit.Assert.assertEquals(Double.MAX_VALUE, jObj.getDouble("doc-15"), 1.0)
//        org.junit.Assert.assertEquals(JSONObject.NULL, jObj.get("doc-16"))
//        org.junit.Assert.assertEquals(0, jObj.getLong("doc-17"))
//        org.junit.Assert.assertEquals(Float.MIN_VALUE.toDouble(), jObj.getDouble("doc-18"), 0.001)
//        org.junit.Assert.assertEquals(
//            Long.MIN_VALUE.toDouble(),
//            jObj.getLong("doc-19").toDouble(),
//            0.001
//        )
//        org.junit.Assert.assertEquals(JSONObject.NULL, jObj.get("doc-20"))
//        org.junit.Assert.assertEquals("Jett", jObj.get("doc-21"))
//        org.junit.Assert.assertEquals(JSONObject.NULL, jObj.get("doc-22"))
//        org.junit.Assert.assertEquals(BaseDbTest.Companion.TEST_DATE, jObj.get("doc-23"))
//        org.junit.Assert.assertEquals(JSONObject.NULL, jObj.get("doc-24"))
//        verifyArray(jObj.get("doc-25") as JSONArray)
//        org.junit.Assert.assertEquals(JSONObject.NULL, jObj.get("doc-26"))
//        verifyDict(jObj.get("doc-27") as JSONObject)
//        org.junit.Assert.assertEquals(JSONObject.NULL, jObj.get("doc-28"))
//        verifyBlob(jObj.getJSONObject("doc-29"))
//    }
//
//    protected fun verifyDocument(doc: DictionaryInterface) {
//        org.junit.Assert.assertEquals(29, doc.count().toLong())
//
//        //#0 doc.setValue(null);
//        org.junit.Assert.assertNull(doc.getValue("doc-1"))
//        org.junit.Assert.assertFalse(doc.getBoolean("doc-1"))
//        org.junit.Assert.assertEquals(0, doc.getInt("doc-1").toLong())
//        org.junit.Assert.assertEquals(0L, doc.getLong("doc-1"))
//        org.junit.Assert.assertEquals(0.0f, doc.getFloat("doc-1"), 0.001f)
//        org.junit.Assert.assertEquals(0.0, doc.getDouble("doc-1"), 0.001)
//        org.junit.Assert.assertNull(doc.getNumber("doc-1"))
//        org.junit.Assert.assertNull(doc.getString("doc-1"))
//        org.junit.Assert.assertNull(doc.getDate("doc-1"))
//        org.junit.Assert.assertNull(doc.getBlob("doc-1"))
//        org.junit.Assert.assertNull(doc.getArray("doc-1"))
//        org.junit.Assert.assertNull(doc.getDictionary("doc-1"))
//
//        //#1 doc.setBoolean(true);
//        org.junit.Assert.assertEquals(java.lang.Boolean.TRUE, doc.getValue("doc-2"))
//        org.junit.Assert.assertTrue(doc.getBoolean("doc-2"))
//        org.junit.Assert.assertEquals(1, doc.getInt("doc-2").toLong())
//        org.junit.Assert.assertEquals(1L, doc.getLong("doc-2"))
//        org.junit.Assert.assertEquals(1.0f, doc.getFloat("doc-2"), 0.001f)
//        org.junit.Assert.assertEquals(1.0, doc.getDouble("doc-2"), 0.001)
//        org.junit.Assert.assertEquals(1, doc.getNumber("doc-2"))
//        org.junit.Assert.assertNull(doc.getString("doc-2"))
//        org.junit.Assert.assertNull(doc.getDate("doc-2"))
//        org.junit.Assert.assertNull(doc.getBlob("doc-2"))
//        org.junit.Assert.assertNull(doc.getArray("doc-2"))
//        org.junit.Assert.assertNull(doc.getDictionary("doc-2"))
//
//        //#2 doc.setBoolean(false);
//        org.junit.Assert.assertEquals(java.lang.Boolean.FALSE, doc.getValue("doc-3"))
//        org.junit.Assert.assertFalse(doc.getBoolean("doc-3"))
//        org.junit.Assert.assertEquals(0, doc.getInt("doc-3").toLong())
//        org.junit.Assert.assertEquals(0L, doc.getLong("doc-3"))
//        org.junit.Assert.assertEquals(0.0f, doc.getFloat("doc-3"), 0.001f)
//        org.junit.Assert.assertEquals(0.0, doc.getDouble("doc-3"), 0.001)
//        org.junit.Assert.assertEquals(0, doc.getNumber("doc-3"))
//        org.junit.Assert.assertNull(doc.getString("doc-3"))
//        org.junit.Assert.assertNull(doc.getDate("doc-3"))
//        org.junit.Assert.assertNull(doc.getBlob("doc-3"))
//        org.junit.Assert.assertNull(doc.getArray("doc-3"))
//        org.junit.Assert.assertNull(doc.getDictionary("doc-3"))
//
//        //#3 doc.setInt(0);
//        org.junit.Assert.assertEquals(0L, doc.getValue("doc-4"))
//        org.junit.Assert.assertFalse(doc.getBoolean("doc-4"))
//        org.junit.Assert.assertEquals(0, doc.getInt("doc-4").toLong())
//        org.junit.Assert.assertEquals(0L, doc.getLong("doc-4"))
//        org.junit.Assert.assertEquals(0.0f, doc.getFloat("doc-4"), 0.001f)
//        org.junit.Assert.assertEquals(0.0, doc.getDouble("doc-4"), 0.001)
//        org.junit.Assert.assertEquals(0L, doc.getNumber("doc-4"))
//        org.junit.Assert.assertNull(doc.getString("doc-4"))
//        org.junit.Assert.assertNull(doc.getDate("doc-4"))
//        org.junit.Assert.assertNull(doc.getBlob("doc-4"))
//        org.junit.Assert.assertNull(doc.getArray("doc-4"))
//        org.junit.Assert.assertNull(doc.getDictionary("doc-4"))
//
//        //#4 doc.setInt(Integer.MIN_VALUE);
//        org.junit.Assert.assertEquals(
//            java.lang.Long.valueOf(Int.MIN_VALUE.toLong()),
//            doc.getValue("doc-5")
//        )
//        org.junit.Assert.assertTrue(doc.getBoolean("doc-5"))
//        org.junit.Assert.assertEquals(Int.MIN_VALUE.toLong(), doc.getInt("doc-5").toLong())
//        org.junit.Assert.assertEquals(Int.MIN_VALUE.toLong(), doc.getLong("doc-5"))
//        org.junit.Assert.assertEquals(Int.MIN_VALUE.toFloat(), doc.getFloat("doc-5"), 0.001f)
//        org.junit.Assert.assertEquals(Int.MIN_VALUE.toDouble(), doc.getDouble("doc-5"), 0.001)
//        org.junit.Assert.assertEquals(
//            java.lang.Long.valueOf(Int.MIN_VALUE.toLong()),
//            doc.getNumber("doc-5")
//        )
//        org.junit.Assert.assertNull(doc.getString("doc-5"))
//        org.junit.Assert.assertNull(doc.getDate("doc-5"))
//        org.junit.Assert.assertNull(doc.getBlob("doc-5"))
//        org.junit.Assert.assertNull(doc.getArray("doc-5"))
//        org.junit.Assert.assertNull(doc.getDictionary("doc-5"))
//
//        //#5 doc.setInt(Integer.MAX_VALUE);
//        org.junit.Assert.assertEquals(
//            java.lang.Long.valueOf(Int.MAX_VALUE.toLong()),
//            doc.getValue("doc-6")
//        )
//        org.junit.Assert.assertTrue(doc.getBoolean("doc-6"))
//        org.junit.Assert.assertEquals(Int.MAX_VALUE.toLong(), doc.getInt("doc-6").toLong())
//        org.junit.Assert.assertEquals(Int.MAX_VALUE.toLong(), doc.getLong("doc-6"))
//        org.junit.Assert.assertEquals(Int.MAX_VALUE.toFloat(), doc.getFloat("doc-6"), 100.0f)
//        org.junit.Assert.assertEquals(Int.MAX_VALUE.toDouble(), doc.getDouble("doc-6"), 100.0)
//        org.junit.Assert.assertEquals(
//            java.lang.Long.valueOf(Int.MAX_VALUE.toLong()),
//            doc.getNumber("doc-6")
//        )
//        org.junit.Assert.assertNull(doc.getString("doc-6"))
//        org.junit.Assert.assertNull(doc.getDate("doc-6"))
//        org.junit.Assert.assertNull(doc.getBlob("doc-6"))
//        org.junit.Assert.assertNull(doc.getArray("doc-6"))
//        org.junit.Assert.assertNull(doc.getDictionary("doc-6"))
//
//        //#6 doc.setLong(0L);
//        org.junit.Assert.assertEquals(java.lang.Long.valueOf(0L), doc.getValue("doc-7"))
//        org.junit.Assert.assertFalse(doc.getBoolean("doc-7"))
//        org.junit.Assert.assertEquals(0, doc.getInt("doc-7").toLong())
//        org.junit.Assert.assertEquals(0L, doc.getLong("doc-7"))
//        org.junit.Assert.assertEquals(0.0f, doc.getFloat("doc-7"), 0.001f)
//        org.junit.Assert.assertEquals(0.0, doc.getDouble("doc-7"), 0.001)
//        org.junit.Assert.assertEquals(java.lang.Long.valueOf(0L), doc.getNumber("doc-7"))
//        org.junit.Assert.assertNull(doc.getString("doc-7"))
//        org.junit.Assert.assertNull(doc.getDate("doc-7"))
//        org.junit.Assert.assertNull(doc.getBlob("doc-7"))
//        org.junit.Assert.assertNull(doc.getArray("doc-7"))
//        org.junit.Assert.assertNull(doc.getDictionary("doc-7"))
//
//        //#7 doc.setLong(Long.MIN_VALUE);
//        org.junit.Assert.assertEquals(java.lang.Long.valueOf(Long.MIN_VALUE), doc.getValue("doc-8"))
//        // !!! Value differs for Documents and Results: assertTrue(doc.getBoolean("doc-8"));
//        org.junit.Assert.assertEquals(
//            java.lang.Long.valueOf(Long.MIN_VALUE).toInt().toLong(),
//            doc.getInt("doc-8").toLong()
//        )
//        org.junit.Assert.assertEquals(Long.MIN_VALUE, doc.getLong("doc-8"))
//        org.junit.Assert.assertEquals(Long.MIN_VALUE.toFloat(), doc.getFloat("doc-8"), 0.001f)
//        org.junit.Assert.assertEquals(Long.MIN_VALUE.toDouble(), doc.getDouble("doc-8"), 0.001)
//        org.junit.Assert.assertEquals(
//            java.lang.Long.valueOf(Long.MIN_VALUE),
//            doc.getNumber("doc-8")
//        )
//        org.junit.Assert.assertNull(doc.getString("doc-8"))
//        org.junit.Assert.assertNull(doc.getDate("doc-8"))
//        org.junit.Assert.assertNull(doc.getBlob("doc-8"))
//        org.junit.Assert.assertNull(doc.getArray("doc-8"))
//        org.junit.Assert.assertNull(doc.getDictionary("doc-8"))
//
//        //#8 doc.setLong(Long.MAX_VALUE);
//        org.junit.Assert.assertEquals(java.lang.Long.valueOf(Long.MAX_VALUE), doc.getValue("doc-9"))
//        org.junit.Assert.assertTrue(doc.getBoolean("doc-9"))
//        org.junit.Assert.assertEquals(
//            java.lang.Long.valueOf(Long.MAX_VALUE).toInt().toLong(),
//            doc.getInt("doc-9").toLong()
//        )
//        org.junit.Assert.assertEquals(Long.MAX_VALUE, doc.getLong("doc-9"))
//        org.junit.Assert.assertEquals(Long.MAX_VALUE.toFloat(), doc.getFloat("doc-9"), 100.0f)
//        org.junit.Assert.assertEquals(Long.MAX_VALUE.toDouble(), doc.getDouble("doc-9"), 100.0)
//        org.junit.Assert.assertEquals(
//            java.lang.Long.valueOf(Long.MAX_VALUE),
//            doc.getNumber("doc-9")
//        )
//        org.junit.Assert.assertNull(doc.getString("doc-9"))
//        org.junit.Assert.assertNull(doc.getDate("doc-9"))
//        org.junit.Assert.assertNull(doc.getBlob("doc-9"))
//        org.junit.Assert.assertNull(doc.getArray("doc-9"))
//        org.junit.Assert.assertNull(doc.getDictionary("doc-9"))
//
//        //#9 doc.setFloat(0.0F);
//        org.junit.Assert.assertEquals(java.lang.Float.valueOf(0.0f), doc.getValue("doc-10"))
//        org.junit.Assert.assertFalse(doc.getBoolean("doc-10"))
//        org.junit.Assert.assertEquals(0, doc.getInt("doc-10").toLong())
//        org.junit.Assert.assertEquals(0L, doc.getLong("doc-10"))
//        org.junit.Assert.assertEquals(0.0f, doc.getFloat("doc-10"), 0.001f)
//        org.junit.Assert.assertEquals(0.0, doc.getDouble("doc-10"), 0.001)
//        org.junit.Assert.assertEquals(java.lang.Float.valueOf(0.0f), doc.getNumber("doc-10"))
//        org.junit.Assert.assertNull(doc.getString("doc-10"))
//        org.junit.Assert.assertNull(doc.getDate("doc-10"))
//        org.junit.Assert.assertNull(doc.getBlob("doc-10"))
//        org.junit.Assert.assertNull(doc.getArray("doc-10"))
//        org.junit.Assert.assertNull(doc.getDictionary("doc-10"))
//
//        //#10 doc.setFloat(Float.MIN_VALUE);
//        org.junit.Assert.assertEquals(
//            java.lang.Float.valueOf(Float.MIN_VALUE),
//            demoteToFloat(doc.getValue("doc-11"))
//        )
//        org.junit.Assert.assertFalse(doc.getBoolean("doc-11"))
//        org.junit.Assert.assertEquals(
//            java.lang.Float.valueOf(Float.MIN_VALUE).toInt().toFloat(),
//            doc.getInt("doc-11").toFloat(),
//            0.001f
//        )
//        org.junit.Assert.assertEquals(
//            java.lang.Float.valueOf(Float.MIN_VALUE).toLong().toFloat(),
//            doc.getLong("doc-11").toFloat(),
//            0.001f
//        )
//        org.junit.Assert.assertEquals(Float.MIN_VALUE, doc.getFloat("doc-11"), 0.001f)
//        org.junit.Assert.assertEquals(Float.MIN_VALUE.toDouble(), doc.getDouble("doc-11"), 0.001)
//        org.junit.Assert.assertEquals(
//            java.lang.Float.valueOf(Float.MIN_VALUE),
//            demoteToFloat(doc.getValue("doc-11"))
//        )
//        org.junit.Assert.assertNull(doc.getString("doc-11"))
//        org.junit.Assert.assertNull(doc.getDate("doc-11"))
//        org.junit.Assert.assertNull(doc.getBlob("doc-11"))
//        org.junit.Assert.assertNull(doc.getArray("doc-11"))
//        org.junit.Assert.assertNull(doc.getDictionary("doc-11"))
//
//        //#11 doc.setFloat(Float.MAX_VALUE);
//        org.junit.Assert.assertEquals(
//            java.lang.Float.valueOf(Float.MAX_VALUE),
//            demoteToFloat(doc.getValue("doc-12"))
//        )
//        org.junit.Assert.assertTrue(doc.getBoolean("doc-12"))
//        // !!! Fails: assertEquals(Float.valueOf(Float.MAX_VALUE).intValue(), doc.getInt("doc-12"));
//        // !!! Fails: assertEquals(Float.valueOf(Float.MAX_VALUE).longValue(), doc.getLong("doc-12"));
//        org.junit.Assert.assertEquals(
//            Float.MAX_VALUE.toDouble(),
//            doc.getFloat("doc-12").toDouble(),
//            1.0E32
//        )
//        org.junit.Assert.assertEquals(Float.MAX_VALUE.toDouble(), doc.getDouble("doc-12"), 1.0E32)
//        org.junit.Assert.assertEquals(
//            java.lang.Float.valueOf(Float.MAX_VALUE),
//            demoteToFloat(doc.getNumber("doc-12"))
//        )
//        org.junit.Assert.assertNull(doc.getString("doc-12"))
//        org.junit.Assert.assertNull(doc.getDate("doc-12"))
//        org.junit.Assert.assertNull(doc.getBlob("doc-12"))
//        org.junit.Assert.assertNull(doc.getArray("doc-12"))
//        org.junit.Assert.assertNull(doc.getDictionary("doc-12"))
//
//        //#12 doc.setDouble(0.0);
//        org.junit.Assert.assertEquals(java.lang.Float.valueOf(0f), doc.getValue("doc-13"))
//        org.junit.Assert.assertFalse(doc.getBoolean("doc-13"))
//        org.junit.Assert.assertEquals(0, doc.getInt("doc-13").toLong())
//        org.junit.Assert.assertEquals(0L, doc.getLong("doc-13"))
//        org.junit.Assert.assertEquals(0.0f, doc.getFloat("doc-13"), 0.001f)
//        org.junit.Assert.assertEquals(0.0, doc.getDouble("doc-13"), 0.001)
//        org.junit.Assert.assertEquals(java.lang.Float.valueOf(0f), doc.getNumber("doc-13"))
//        org.junit.Assert.assertNull(doc.getString("doc-13"))
//        org.junit.Assert.assertNull(doc.getDate("doc-13"))
//        org.junit.Assert.assertNull(doc.getBlob("doc-13"))
//        org.junit.Assert.assertNull(doc.getArray("doc-13"))
//        org.junit.Assert.assertNull(doc.getDictionary("doc-13"))
//
//        //#13 doc.setDouble(Double.MIN_VALUE);
//        org.junit.Assert.assertEquals(
//            java.lang.Double.valueOf(Double.MIN_VALUE),
//            doc.getValue("doc-14")
//        )
//        org.junit.Assert.assertFalse(doc.getBoolean("doc-14"))
//        org.junit.Assert.assertEquals(
//            java.lang.Double.valueOf(Double.MIN_VALUE).toInt().toLong(),
//            doc.getInt("doc-14").toLong()
//        )
//        org.junit.Assert.assertEquals(
//            java.lang.Double.valueOf(Double.MIN_VALUE).toLong(),
//            doc.getLong("doc-14")
//        )
//        org.junit.Assert.assertEquals(
//            java.lang.Double.valueOf(Double.MIN_VALUE).toFloat(),
//            doc.getFloat("doc-14"),
//            0.001f
//        )
//        org.junit.Assert.assertEquals(Double.MIN_VALUE, doc.getDouble("doc-14"), 0.001)
//        org.junit.Assert.assertEquals(
//            java.lang.Double.valueOf(Double.MIN_VALUE),
//            doc.getNumber("doc-14")
//        )
//        org.junit.Assert.assertNull(doc.getString("doc-14"))
//        org.junit.Assert.assertNull(doc.getDate("doc-14"))
//        org.junit.Assert.assertNull(doc.getBlob("doc-14"))
//        org.junit.Assert.assertNull(doc.getArray("doc-14"))
//        org.junit.Assert.assertNull(doc.getDictionary("doc-14"))
//
//        //#14 doc.setDouble(Double.MAX_VALUE);
//        org.junit.Assert.assertEquals(
//            java.lang.Double.valueOf(Double.MAX_VALUE),
//            doc.getValue("doc-15")
//        )
//        // !!! Fails: assertEquals(Double.valueOf(Double.MAX_VALUE).intValue(), doc.getInt("doc-15"));
//        // !!! Fails: assertEquals(Double.valueOf(Double.MAX_VALUE).longValue(), doc.getLong("doc-15"));
//        org.junit.Assert.assertEquals(
//            java.lang.Double.valueOf(Double.MAX_VALUE).toFloat(),
//            doc.getFloat("doc-15"),
//            100.0f
//        )
//        org.junit.Assert.assertEquals(Double.MAX_VALUE, doc.getDouble("doc-15"), 100.0)
//        org.junit.Assert.assertEquals(
//            java.lang.Double.valueOf(Double.MAX_VALUE),
//            doc.getNumber("doc-15")
//        )
//        org.junit.Assert.assertNull(doc.getString("doc-15"))
//        org.junit.Assert.assertNull(doc.getDate("doc-15"))
//        org.junit.Assert.assertNull(doc.getBlob("doc-15"))
//        org.junit.Assert.assertNull(doc.getArray("doc-15"))
//        org.junit.Assert.assertNull(doc.getDictionary("doc-15"))
//
//        //#15 doc.setNumber(null);
//        org.junit.Assert.assertNull(doc.getValue("doc-16"))
//        org.junit.Assert.assertFalse(doc.getBoolean("doc-16"))
//        org.junit.Assert.assertEquals(0, doc.getInt("doc-16").toLong())
//        org.junit.Assert.assertEquals(0L, doc.getLong("doc-16"))
//        org.junit.Assert.assertEquals(0.0f, doc.getFloat("doc-16"), 0.001f)
//        org.junit.Assert.assertEquals(0.0, doc.getDouble("doc-16"), 0.001)
//        org.junit.Assert.assertNull(doc.getNumber("doc-16"))
//        org.junit.Assert.assertNull(doc.getString("doc-16"))
//        org.junit.Assert.assertNull(doc.getDate("doc-16"))
//        org.junit.Assert.assertNull(doc.getBlob("doc-16"))
//        org.junit.Assert.assertNull(doc.getArray("doc-16"))
//        org.junit.Assert.assertNull(doc.getDictionary("doc-16"))
//
//        //#16 doc.setNumber(0);
//        org.junit.Assert.assertEquals(0L, doc.getValue("doc-17"))
//        org.junit.Assert.assertFalse(doc.getBoolean("doc-17"))
//        org.junit.Assert.assertEquals(0, doc.getInt("doc-17").toLong())
//        org.junit.Assert.assertEquals(0L, doc.getLong("doc-17"))
//        org.junit.Assert.assertEquals(0.0f, doc.getFloat("doc-17"), 0.001f)
//        org.junit.Assert.assertEquals(0.0, doc.getDouble("doc-17"), 0.001)
//        org.junit.Assert.assertEquals(0L, doc.getNumber("doc-17"))
//        org.junit.Assert.assertNull(doc.getString("doc-17"))
//        org.junit.Assert.assertNull(doc.getDate("doc-17"))
//        org.junit.Assert.assertNull(doc.getBlob("doc-17"))
//        org.junit.Assert.assertNull(doc.getArray("doc-17"))
//        org.junit.Assert.assertNull(doc.getDictionary("doc-17"))
//
//        //#17 doc.setNumber(Float.MIN_VALUE);
//        org.junit.Assert.assertEquals(
//            java.lang.Float.valueOf(Float.MIN_VALUE),
//            demoteToFloat(doc.getValue("doc-18"))
//        )
//        org.junit.Assert.assertFalse(doc.getBoolean("doc-18"))
//        org.junit.Assert.assertEquals(
//            java.lang.Float.valueOf(Float.MIN_VALUE).toInt().toLong(),
//            doc.getInt("doc-18").toLong()
//        )
//        org.junit.Assert.assertEquals(
//            java.lang.Float.valueOf(Float.MIN_VALUE).toLong(),
//            doc.getLong("doc-18")
//        )
//        org.junit.Assert.assertEquals(Float.MIN_VALUE, doc.getFloat("doc-18"), 0.001f)
//        org.junit.Assert.assertEquals(Float.MIN_VALUE.toDouble(), doc.getDouble("doc-18"), 0.001)
//        org.junit.Assert.assertEquals(
//            java.lang.Float.valueOf(Float.MIN_VALUE),
//            demoteToFloat(doc.getNumber("doc-18"))
//        )
//        org.junit.Assert.assertNull(doc.getString("doc-18"))
//        org.junit.Assert.assertNull(doc.getDate("doc-18"))
//        org.junit.Assert.assertNull(doc.getBlob("doc-18"))
//        org.junit.Assert.assertNull(doc.getArray("doc-18"))
//        org.junit.Assert.assertNull(doc.getDictionary("doc-18"))
//
//        //#18 doc.setNumber(Long.MIN_VALUE);
//        org.junit.Assert.assertEquals(
//            java.lang.Long.valueOf(Long.MIN_VALUE),
//            doc.getValue("doc-19")
//        )
//        // !!! Value differs for Documents and Results: assertTrue(doc.getBoolean("doc-19"));
//        org.junit.Assert.assertEquals(
//            java.lang.Long.valueOf(Long.MIN_VALUE).toInt().toLong(),
//            doc.getInt("doc-19").toLong()
//        )
//        org.junit.Assert.assertEquals(Long.MIN_VALUE, doc.getLong("doc-19"))
//        org.junit.Assert.assertEquals(Long.MIN_VALUE.toFloat(), doc.getFloat("doc-19"), 0.001f)
//        org.junit.Assert.assertEquals(Long.MIN_VALUE.toDouble(), doc.getDouble("doc-19"), 0.001)
//        org.junit.Assert.assertEquals(
//            java.lang.Long.valueOf(Long.MIN_VALUE),
//            doc.getNumber("doc-19")
//        )
//        org.junit.Assert.assertNull(doc.getString("doc-19"))
//        org.junit.Assert.assertNull(doc.getDate("doc-19"))
//        org.junit.Assert.assertNull(doc.getBlob("doc-19"))
//        org.junit.Assert.assertNull(doc.getArray("doc-19"))
//        org.junit.Assert.assertNull(doc.getDictionary("doc-19"))
//
//        //#19 doc.setString(null);
//        org.junit.Assert.assertNull(doc.getValue("doc-20"))
//        org.junit.Assert.assertFalse(doc.getBoolean("doc-20"))
//        org.junit.Assert.assertEquals(0, doc.getInt("doc-20").toLong())
//        org.junit.Assert.assertEquals(0L, doc.getLong("doc-20"))
//        org.junit.Assert.assertEquals(0.0f, doc.getFloat("doc-20"), 0.001f)
//        org.junit.Assert.assertEquals(0.0, doc.getDouble("doc-20"), 0.001)
//        org.junit.Assert.assertNull(doc.getNumber("doc-20"))
//        org.junit.Assert.assertNull(doc.getString("doc-20"))
//        org.junit.Assert.assertNull(doc.getDate("doc-20"))
//        org.junit.Assert.assertNull(doc.getBlob("doc-20"))
//        org.junit.Assert.assertNull(doc.getArray("doc-20"))
//        org.junit.Assert.assertNull(doc.getDictionary("doc-20"))
//
//        //#20 doc.setString("Quatro");
//        org.junit.Assert.assertEquals("Jett", doc.getValue("doc-21"))
//        org.junit.Assert.assertTrue(doc.getBoolean("doc-21"))
//        org.junit.Assert.assertEquals(0, doc.getInt("doc-21").toLong())
//        org.junit.Assert.assertEquals(0, doc.getLong("doc-21"))
//        org.junit.Assert.assertEquals(0.0f, doc.getFloat("doc-21"), 0.001f)
//        org.junit.Assert.assertEquals(0.0, doc.getDouble("doc-21"), 0.001)
//        org.junit.Assert.assertNull(doc.getNumber("doc-21"))
//        org.junit.Assert.assertEquals("Jett", doc.getString("doc-21"))
//        org.junit.Assert.assertNull(doc.getDate("doc-21"))
//        org.junit.Assert.assertNull(doc.getBlob("doc-21"))
//        org.junit.Assert.assertNull(doc.getArray("doc-21"))
//        org.junit.Assert.assertNull(doc.getDictionary("doc-21"))
//
//        //#21 doc.setDate(null);
//        org.junit.Assert.assertNull(doc.getValue("doc-22"))
//        org.junit.Assert.assertFalse(doc.getBoolean("doc-22"))
//        org.junit.Assert.assertEquals(0, doc.getInt("doc-22").toLong())
//        org.junit.Assert.assertEquals(0L, doc.getLong("doc-22"))
//        org.junit.Assert.assertEquals(0.0f, doc.getFloat("doc-22"), 0.001f)
//        org.junit.Assert.assertEquals(0.0, doc.getDouble("doc-22"), 0.001)
//        org.junit.Assert.assertNull(doc.getNumber("doc-22"))
//        org.junit.Assert.assertNull(doc.getString("doc-22"))
//        org.junit.Assert.assertNull(doc.getDate("doc-22"))
//        org.junit.Assert.assertNull(doc.getBlob("doc-22"))
//        org.junit.Assert.assertNull(doc.getArray("doc-22"))
//        org.junit.Assert.assertNull(doc.getDictionary("doc-22"))
//
//        //#22 doc.setDate(JSONUtils.toDate(TEST_DATE));
//        org.junit.Assert.assertEquals(BaseDbTest.Companion.TEST_DATE, doc.getValue("doc-23"))
//        org.junit.Assert.assertTrue(doc.getBoolean("doc-23"))
//        org.junit.Assert.assertEquals(0, doc.getInt("doc-23").toLong())
//        org.junit.Assert.assertEquals(0L, doc.getLong("doc-23"))
//        org.junit.Assert.assertEquals(0.0f, doc.getFloat("doc-23"), 0.001f)
//        org.junit.Assert.assertEquals(0.0, doc.getDouble("doc-23"), 0.001)
//        org.junit.Assert.assertNull(doc.getNumber("doc-23"))
//        org.junit.Assert.assertEquals(BaseDbTest.Companion.TEST_DATE, doc.getString("doc-23"))
//        org.junit.Assert.assertEquals(
//            JSONUtils.toDate(BaseDbTest.Companion.TEST_DATE),
//            doc.getDate("doc-23")
//        )
//        org.junit.Assert.assertNull(doc.getBlob("doc-23"))
//        org.junit.Assert.assertNull(doc.getArray("doc-23"))
//        org.junit.Assert.assertNull(doc.getDictionary("doc-23"))
//
//        //#23 doc.setArray(null);
//        org.junit.Assert.assertNull(doc.getValue("doc-24"))
//        org.junit.Assert.assertFalse(doc.getBoolean("doc-24"))
//        org.junit.Assert.assertEquals(0, doc.getInt("doc-24").toLong())
//        org.junit.Assert.assertEquals(0L, doc.getLong("doc-24"))
//        org.junit.Assert.assertEquals(0.0f, doc.getFloat("doc-24"), 0.001f)
//        org.junit.Assert.assertEquals(0.0, doc.getDouble("doc-24"), 0.001)
//        org.junit.Assert.assertNull(doc.getNumber("doc-24"))
//        org.junit.Assert.assertNull(doc.getString("doc-24"))
//        org.junit.Assert.assertNull(doc.getDate("doc-24"))
//        org.junit.Assert.assertNull(doc.getBlob("doc-24"))
//        org.junit.Assert.assertNull(doc.getArray("doc-24"))
//        org.junit.Assert.assertNull(doc.getDictionary("doc-24"))
//
//        //#24 doc.setDictionary(null);
//        verifyArray(doc.getArray("doc-25"))
//
//        //#25 doc.setDictionary(null);
//        org.junit.Assert.assertNull(doc.getValue("doc-26"))
//        org.junit.Assert.assertFalse(doc.getBoolean("doc-26"))
//        org.junit.Assert.assertEquals(0, doc.getInt("doc-26").toLong())
//        org.junit.Assert.assertEquals(0L, doc.getLong("doc-26"))
//        org.junit.Assert.assertEquals(0.0f, doc.getFloat("doc-26"), 0.001f)
//        org.junit.Assert.assertEquals(0.0, doc.getDouble("doc-26"), 0.001)
//        org.junit.Assert.assertNull(doc.getNumber("doc-26"))
//        org.junit.Assert.assertNull(doc.getString("doc-26"))
//        org.junit.Assert.assertNull(doc.getDate("doc-26"))
//        org.junit.Assert.assertNull(doc.getBlob("doc-26"))
//        org.junit.Assert.assertNull(doc.getArray("doc-26"))
//        org.junit.Assert.assertNull(doc.getDictionary("doc-26"))
//
//        //#26 doc.setDictionary(simpleDict);
//        verifyDict(doc.getDictionary("doc-27"))
//
//        //#27 doc.setDictionary(null);
//        org.junit.Assert.assertNull(doc.getValue("doc-28"))
//        org.junit.Assert.assertFalse(doc.getBoolean("doc-28"))
//        org.junit.Assert.assertEquals(0, doc.getInt("doc-28").toLong())
//        org.junit.Assert.assertEquals(0L, doc.getLong("doc-28"))
//        org.junit.Assert.assertEquals(0.0f, doc.getFloat("doc-28"), 0.001f)
//        org.junit.Assert.assertEquals(0.0, doc.getDouble("doc-28"), 0.001)
//        org.junit.Assert.assertNull(doc.getNumber("doc-28"))
//        org.junit.Assert.assertNull(doc.getString("doc-28"))
//        org.junit.Assert.assertNull(doc.getDate("doc-28"))
//        org.junit.Assert.assertNull(doc.getBlob("doc-28"))
//        org.junit.Assert.assertNull(doc.getArray("doc-28"))
//        org.junit.Assert.assertNull(doc.getDictionary("doc-28"))
//        verifyBlob(doc.getBlob("doc-29"))
//    }
//
//    @Throws(CouchbaseLiteException::class)
//    protected fun reopenBaseTestDb() {
//        baseTestDb = reopenDb(baseTestDb)
//    }
//
//    @Throws(CouchbaseLiteException::class)
//    protected fun recreateBastTestDb() {
//        baseTestDb = recreateDb(baseTestDb)
//    }
//
//    // Some JSON encoding will promote a Float to a Double.
//    protected fun demoteToFloat(`val`: Any?): Float {
//        if (`val` is Float) {
//            return `val`
//        }
//        if (`val` is Double) {
//            return `val`.toFloat()
//        }
//        throw java.lang.IllegalArgumentException("expected a floating point value")
//    }
//
//    companion object {
//        const val TEST_DATE = "2019-02-21T05:37:22.014Z"
//        const val BLOB_CONTENT = "Knox on fox in socks in box. Socks on Knox and Knox in box."
//    }
//}