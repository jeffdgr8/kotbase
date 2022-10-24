package com.couchbase.lite.kmp

import com.couchbase.lite.kmp.internal.utils.FileUtils
import com.couchbase.lite.kmp.internal.utils.TestUtils
import com.couchbase.lite.kmp.internal.utils.paddedString
import com.couchbase.lite.kmp.internal.utils.getParentDir
import com.udobny.kmp.test.IgnoreNative
import com.udobny.kmp.use
import kotlin.test.*

// The rules in this test are:
// baseTestDb is managed by the superclass
// If a test opens a new database it guarantee that it is deleted.
// If a test opens a copy of the baseTestDb, it must close (but NOT delete)
class DatabaseTest : BaseDbTest() {

    //---------------------------------------------
    //  Get Document
    //---------------------------------------------

    @Test
    fun testGetNonExistingDocWithID() {
        assertNull(baseTestDb.getDocument("non-exist"))
    }

    @Test
    fun testGetExistingDocWithID() {
        val docID = "doc1"
        createSingleDocInBaseTestDb(docID)
        verifyGetDocument(docID)
    }

    @Test
    fun testGetExistingDocWithIDFromDifferentDBInstance() {
        // store doc
        val docID = "doc1"
        createSingleDocInBaseTestDb(docID)

        // open db with same db name and default option
        val otherDb = duplicateBaseTestDb()
        try {
            assertNotSame(baseTestDb, otherDb)

            // get doc from other DB.
            assertEquals(1, otherDb.count)

            verifyGetDocument(otherDb, docID)
            verifyGetDocument(otherDb, docID)
        } finally {
            closeDb(otherDb)
        }
    }

    @Test
    fun testGetExistingDocWithIDInBatch() {
        val n = 10

        // Save 10 docs:
        createDocsInBaseTestDb(n)

        baseTestDb.inBatch { verifyDocuments(n) }
    }

    @Test
    fun testGetDocFromClosedDB() {
        assertFailsWith<IllegalStateException> {
            // Store doc:
            createSingleDocInBaseTestDb("doc1")

            // Close db:
            baseTestDb.close()

            // should fail
            baseTestDb.getDocument("doc1")
        }
    }

    @Test
    fun testGetDocFromDeletedDB() {
        assertFailsWith<IllegalStateException> {
            // Store doc:
            createSingleDocInBaseTestDb("doc1")

            // Delete db:
            baseTestDb.delete()

            // should fail
            baseTestDb.getDocument("doc1")
        }
    }

    //---------------------------------------------
    //  Save Document
    //---------------------------------------------

    // base test method
    private fun testSaveNewDocWithID(docID: String) {
        // store doc
        createSingleDocInBaseTestDb(docID)

        assertEquals(1, baseTestDb.count)

        // validate document by getDocument
        verifyGetDocument(docID)
    }

    @Test
    fun testSaveNewDocWithID() {
        testSaveNewDocWithID("doc1")
    }

    @Test
    fun testSaveNewDocWithSpecialCharactersDocID() {
        testSaveNewDocWithID("`~@#$%^&*()_+{}|\\\\][=-/.,<>?\\\":;'")
    }

    @Test
    fun testSaveAndGetMultipleDocs() {
        val nDocs = 10 //1000
        for (i in 0 until nDocs) {
            val doc = MutableDocument("doc_${i.paddedString(3)}")
            doc.setValue("key", i)
            saveDocInBaseTestDb(doc)
        }
        assertEquals(nDocs.toLong(), baseTestDb.count)
        verifyDocuments(nDocs)
    }

    @Test
    fun testSaveDoc() {
        // store doc
        val docID = "doc1"
        val doc = createSingleDocInBaseTestDb(docID).toMutable()

        // update doc
        doc.setValue("key", 2)
        saveDocInBaseTestDb(doc)

        assertEquals(1, baseTestDb.count)

        // validate document by getDocument
        verifyGetDocument(docID, 2)
    }

    @Test
    fun testSaveDocInDifferentDBInstance() {
        // Store doc
        val docID = "doc1"
        val doc = createSingleDocInBaseTestDb(docID).toMutable()

        // Create db with default
        val otherDb: Database = duplicateBaseTestDb()
        try {
            assertNotSame(otherDb, baseTestDb)
            assertEquals(1, otherDb.count)

            // Update doc & store it into different instance
            doc.setValue("key", 2)
            TestUtils.assertThrowsCBL(CBLError.Domain.CBLITE, CBLError.Code.INVALID_PARAMETER) {
                otherDb.save(doc)
            }
        } finally {
            closeDb(otherDb)
        }
    }

    @Test
    fun testSaveDocInDifferentDB() {
        // Store doc
        val docID = "doc1"
        val doc = createSingleDocInBaseTestDb(docID).toMutable()

        // Create db with default
        val otherDb = openDatabase()
        try {
            assertNotSame(otherDb, baseTestDb)
            assertEquals(0, otherDb.count)

            // Update doc & store it into different instance
            doc.setValue("key", 2)
            TestUtils.assertThrowsCBL(CBLError.Domain.CBLITE, CBLError.Code.INVALID_PARAMETER) {
                otherDb.save(doc)
            }
        } finally {
            // delete otherDb
            deleteDb(otherDb)
        }
    }


    @Test
    fun testSaveSameDocTwice() {
        val docID = "doc1"
        val doc = createSingleDocInBaseTestDb(docID).toMutable()
        assertEquals(docID, saveDocInBaseTestDb(doc).id)
        assertEquals(1, baseTestDb.count)
    }

    @Test
    fun testSaveInBatch() {
        val nDocs = 10

        baseTestDb.inBatch { createDocsInBaseTestDb(nDocs) }
        assertEquals(nDocs.toLong(), baseTestDb.count)
        verifyDocuments(nDocs)
    }

    @Test
    fun testSaveDocToClosedDB() {
        assertFailsWith<IllegalStateException> {
            baseTestDb.close()

            val doc = MutableDocument("doc1")
            doc.setValue("key", 1)

            saveDocInBaseTestDb(doc)
        }
    }

    @Test
    fun testSaveDocToDeletedDB() {
        assertFailsWith<IllegalStateException> {
            // Delete db:
            baseTestDb.delete()

            val doc = MutableDocument("doc1")
            doc.setValue("key", 1)

            saveDocInBaseTestDb(doc)
        }
    }

    //---------------------------------------------
    //  Delete Document
    //---------------------------------------------

    @Test
    fun testDeletePreSaveDoc() {
        val doc = MutableDocument("doc1")
        doc.setValue("key", 1)
        TestUtils.assertThrowsCBL(CBLError.Domain.CBLITE, CBLError.Code.NOT_FOUND) {
            baseTestDb.delete(doc)
        }
    }

    @Test
    fun testDeleteDoc() {
        val docID = "doc1"
        val doc = createSingleDocInBaseTestDb(docID)
        assertEquals(1, baseTestDb.count)
        baseTestDb.delete(doc)
        assertEquals(0, baseTestDb.count)
        assertNull(baseTestDb.getDocument(docID))
    }

    @Test
    fun testDeleteDocInDifferentDBInstance() {
        // Store doc:
        val docID = "doc1"
        val doc = createSingleDocInBaseTestDb(docID)

        // Create db with same name:
        // Create db with default
        val otherDb: Database = duplicateBaseTestDb()
        try {
            assertNotSame(otherDb, baseTestDb)
            assertEquals(1, otherDb.count)

            // Delete from the different db instance:
            TestUtils.assertThrowsCBL(CBLError.Domain.CBLITE, CBLError.Code.INVALID_PARAMETER) {
                otherDb.delete(doc)
            }
        } finally {
            closeDb(otherDb)
        }
    }

    @Test
    fun testDeleteDocInDifferentDB() {
        // Store doc
        val docID = "doc1"
        val doc = createSingleDocInBaseTestDb(docID)

        // Create db with default
        val otherDb: Database = openDatabase()
        try {
            assertNotSame(otherDb, baseTestDb)

            // Delete from the different db:
            TestUtils.assertThrowsCBL(CBLError.Domain.CBLITE, CBLError.Code.INVALID_PARAMETER) {
                otherDb.delete(doc)
            }
        } finally {
            deleteDb(otherDb)
        }
    }

    @Test
    fun testDeleteDocInBatch() {
        val nDocs = 10

        // Save 10 docs:
        createDocsInBaseTestDb(nDocs)
        baseTestDb.inBatch {
            for (i in 0 until nDocs) {
                val docID = "doc_${i.paddedString(3)}"
                val doc = baseTestDb.getDocument(docID)!!
                baseTestDb.delete(doc)
                assertNull(baseTestDb.getDocument(docID))
                assertEquals(9L - i, baseTestDb.count)
            }
        }
        assertEquals(0, baseTestDb.count)
    }

    @Test
    fun testDeleteDocOnClosedDB() {
        assertFailsWith<IllegalStateException> {
            // Store doc:
            val doc = createSingleDocInBaseTestDb("doc1")

            // Close db:
            baseTestDb.close()

            // Delete doc from db:
            baseTestDb.delete(doc)
        }
    }

    @Test
    fun testDeleteDocOnDeletedDB() {
        assertFailsWith<IllegalStateException> {
            // Store doc:
            val doc = createSingleDocInBaseTestDb("doc1")
            baseTestDb.delete()

            // Delete doc from db:
            baseTestDb.delete(doc)
        }
    }

    //---------------------------------------------
    //  Purge Document
    //---------------------------------------------

    @Test
    fun testPurgePreSaveDoc() {
        val doc = MutableDocument("doc1")
        assertEquals(0, baseTestDb.count)
        TestUtils.assertThrowsCBL(CBLError.Domain.CBLITE, CBLError.Code.NOT_FOUND) {
            baseTestDb.purge(doc)
        }
        assertEquals(0, baseTestDb.count)
    }

    @Test
    fun testPurgeDoc() {
        val docID = "doc1"
        val doc = createSingleDocInBaseTestDb(docID)

        // Purge Doc
        purgeDocAndVerify(doc)
        assertEquals(0, baseTestDb.count)
    }

    @Test
    fun testPurgeDocInDifferentDBInstance() {
        // Store doc:
        val docID = "doc1"
        val doc = createSingleDocInBaseTestDb(docID)

        // Create db with default:
        val otherDb: Database = duplicateBaseTestDb()
        try {
            assertNotSame(otherDb, baseTestDb)
            assertEquals(1, otherDb.count)

            // purge document against other db instance:
            TestUtils.assertThrowsCBL(CBLError.Domain.CBLITE, CBLError.Code.INVALID_PARAMETER) {
                otherDb.purge(doc)
            }
        } finally {
            closeDb(otherDb)
        }
    }

    @Test
    fun testPurgeDocInDifferentDB() {
        // Store doc:
        val docID = "doc1"
        val doc = createSingleDocInBaseTestDb(docID)

        // Create db with default:
        val otherDb: Database = openDatabase()
        try {
            assertNotSame(otherDb, baseTestDb)
            assertEquals(0, otherDb.count)

            // Purge document against other db:
            TestUtils.assertThrowsCBL(CBLError.Domain.CBLITE, CBLError.Code.INVALID_PARAMETER) {
                otherDb.purge(doc)
            }
        } finally {
            deleteDb(otherDb)
        }
    }

    @Test
    fun testPurgeSameDocTwice() {
        // Store doc:
        val docID = "doc1"
        val doc = createSingleDocInBaseTestDb(docID)

        // Get the document for the second purge:
        val doc1 = baseTestDb.getDocument(docID)!!

        // Purge the document first time:
        purgeDocAndVerify(doc)
        assertEquals(0, baseTestDb.count)

        // Purge the document second time:
        purgeDocAndVerify(doc1)
        assertEquals(0, baseTestDb.count)
    }

    @Test
    fun testPurgeDocInBatch() {
        val nDocs = 10
        // Save 10 docs:
        createDocsInBaseTestDb(nDocs)

        baseTestDb.inBatch {
            for (i in 0 until nDocs) {
                val docID = "doc_${i.paddedString(3)}"
                val doc = baseTestDb.getDocument(docID)!!
                purgeDocAndVerify(doc)
                assertEquals(9L - i, baseTestDb.count)
            }
        }

        assertEquals(0, baseTestDb.count)
    }

    @Test
    fun testPurgeDocOnClosedDB() {
        assertFailsWith<IllegalStateException> {
            // Store doc:
            val doc = createSingleDocInBaseTestDb("doc1")

            // Close db:
            baseTestDb.close()

            // Purge doc:
            baseTestDb.purge(doc)
        }
    }

    @Test
    fun testPurgeDocOnDeletedDB() {
        assertFailsWith<IllegalStateException> {
            // Store doc:
            val doc = createSingleDocInBaseTestDb("doc1")

            // Close db:
            baseTestDb.close()

            // Purge doc:
            baseTestDb.purge(doc)
        }
    }

    //---------------------------------------------
    //  Close Database
    //---------------------------------------------

    @Test
    fun testClose() {
        baseTestDb.close()
    }

    @Test
    fun testCloseTwice() {
        baseTestDb.close()
        baseTestDb.close()
    }

    @Test
    fun testCloseThenAccessDoc() {
        // Store doc:
        val docID = "doc1"
        val mDoc = MutableDocument(docID)
        mDoc.setInt("key", 1)

        val mDict = MutableDictionary() // nested dictionary
        mDict.setString("hello", "world")
        mDoc.setDictionary("dict", mDict)
        val doc = saveDocInBaseTestDb(mDoc)

        // Close db:
        baseTestDb.close()

        // Content should be accessible & modifiable without error:
        assertEquals(docID, doc.id)
        assertEquals(1, (doc.getValue("key") as Number).toInt())

        val dict = doc.getDictionary("dict")
        assertNotNull(dict)
        assertEquals("world", dict.getString("hello"))

        val updateDoc = doc.toMutable()
        updateDoc.setValue("key", 2)
        updateDoc.setValue("key1", "value")
        assertEquals(2, updateDoc.getInt("key"))
        assertEquals("value", updateDoc.getString("key1"))
    }

    @Test
    fun testCloseThenAccessBlob() {
        assertFailsWith<IllegalStateException> {
            // Store doc with blob:
            val mDoc = createSingleDocInBaseTestDb("doc1").toMutable()
            mDoc.setValue("blob", Blob("text/plain", BLOB_CONTENT.encodeToByteArray()))
            val doc = saveDocInBaseTestDb(mDoc)

            // Close db:
            baseTestDb.close()

            // content should be accessible & modifiable without error
            assertTrue(doc.getValue("blob") is Blob)
            val blob = doc.getBlob("blob")!!
            assertEquals(BLOB_CONTENT.length.toLong(), blob.length)

            // trying to get the content, however, should fail
            blob.content
        }
    }

    @Test
    fun testCloseThenGetDatabaseName() {
        val dbName = baseTestDb.name
        baseTestDb.close()
        assertEquals(dbName, baseTestDb.name)
    }

    @Test
    fun testCloseThenGetDatabasePath() {
        baseTestDb.close()
        assertNull(baseTestDb.path)
    }

    @Test
    fun testCloseThenCallInBatch() {
        assertFailsWith<IllegalStateException> {
            baseTestDb.close()
            baseTestDb.inBatch { fail() }
        }
    }

    @Test
    fun testCloseInInBatch() {
        baseTestDb.inBatch {
            // delete db
            TestUtils.assertThrowsCBL(
                CBLError.Domain.CBLITE,
                CBLError.Code.TRANSACTION_NOT_CLOSED
            ) { baseTestDb.close() }
        }
    }

    @Test
    fun testCloseThenDeleteDatabase() {
        assertFailsWith<IllegalStateException> {
            baseTestDb.close()
            baseTestDb.delete()
        }
    }

    //---------------------------------------------
    //  Delete Database
    //---------------------------------------------

    @Test
    fun testDelete() {
        baseTestDb.delete()
    }

    @Test
    fun testDeleteTwice() {
        assertFailsWith<IllegalStateException> {
            // delete db twice
            val path = baseTestDb.path!!
            assertTrue(FileUtils.dirExists(path))

            baseTestDb.delete()
            assertFalse(FileUtils.dirExists(path))

            // second delete should fail
            baseTestDb.delete()
        }
    }

    @Test
    fun testDeleteThenAccessDoc() {
        // Store doc:
        val docID = "doc1"
        val doc = createSingleDocInBaseTestDb(docID).toMutable()

        // Delete db:
        baseTestDb.delete()

        // Content should be accessible & modifiable without error:
        assertEquals(docID, doc.id)
        assertEquals(1, (doc.getValue("key") as Number).toInt())
        doc.setValue("key", 2)
        doc.setValue("key1", "value")
    }

    // CBLDatabase_GetBlob() returns null after database is deleted for native C
    @IgnoreNative
    @Test
    fun testDeleteThenAccessBlob() {
        // Store doc with blob:
        val docID = "doc1"
        val doc = createSingleDocInBaseTestDb(docID).toMutable()
        doc.setValue("blob", Blob("text/plain", BLOB_CONTENT.encodeToByteArray()))
        saveDocInBaseTestDb(doc)

        // Delete db:
        baseTestDb.delete()

        // content should be accessible & modifiable without error
        val obj = doc.getValue("blob")
        assertNotNull(obj)

        assertTrue(obj is Blob)
        val blob: Blob = obj
        assertEquals(BLOB_CONTENT.length.toLong(), blob.length)

        // NOTE content still exists in memory for this case.
        // (except for native C, this is where test fails)
        assertNotNull(blob.content)
    }

    @Test
    fun testDeleteThenGetDatabaseName() {
        val dbName = baseTestDb.name

        // delete db
        baseTestDb.delete()

        assertEquals(dbName, baseTestDb.name)
    }

    @Test
    fun testDeleteThenGetDatabasePath() {
        // delete db
        baseTestDb.delete()
        assertNull(baseTestDb.path)
    }

    @Test
    fun testDeleteThenCallInBatch() {
        assertFailsWith<IllegalStateException> {
            baseTestDb.delete()
            baseTestDb.inBatch { fail() }
        }
    }

    @Test
    fun testDeleteInInBatch() {
        baseTestDb.inBatch {
            // delete db
            TestUtils.assertThrowsCBL(
                CBLError.Domain.CBLITE,
                CBLError.Code.TRANSACTION_NOT_CLOSED
            ) { baseTestDb.close() }
        }
    }

    @Test
    fun testDeleteDBOpenedByOtherInstance() {
        val otherDb: Database = duplicateBaseTestDb(0)
        try {
            assertNotSame(baseTestDb, otherDb)
            // delete db
            TestUtils.assertThrowsCBL(CBLError.Domain.CBLITE, CBLError.Code.BUSY) {
                baseTestDb.delete()
            }
        } finally {
            closeDb(otherDb)
        }
    }

    //---------------------------------------------
    //  Delete Database (static)
    //---------------------------------------------

    @Test
    fun testDeleteWithDefaultDirDB() {
        val dbName = baseTestDb.name

        val path = baseTestDb.path

        assertNotNull(path)
        assertTrue(FileUtils.dirExists(path))

        // close db before delete
        baseTestDb.close()

        Database.delete(dbName, null)

        assertFalse(FileUtils.dirExists(path))
    }

    @Test
    fun testDeleteOpenDbWithDefaultDir() {
        val path = baseTestDb.path
        assertNotNull(path)
        assertTrue(FileUtils.dirExists(path))

        TestUtils.assertThrowsCBL(CBLError.Domain.CBLITE, CBLError.Code.BUSY) {
            Database.delete(baseTestDb.name, null)
        }
    }

    @Test
    fun testStaticDeleteDb() {
        val dbDirPath = getScratchDirectoryPath(getUniqueName("static-delete-dir"))

        // create db in a custom directory
        val db = createDb("static_del_db", DatabaseConfiguration().setDirectory(dbDirPath))
        try {
            val dbName = db.name

            val dbPath = db.path!!
            assertTrue(FileUtils.dirExists(dbPath))

            // close db before delete
            db.close()

            Database.delete(dbName, dbDirPath)

            assertFalse(FileUtils.dirExists(dbPath))
        } finally {
            deleteDb(db)
        }
    }

    @Test
    fun testDeleteOpeningDBByStaticMethod() {
        val db = duplicateBaseTestDb()

        val dbName = db.name
        val dbDir = FileUtils.getParentDir(db.path!!)
        try {
            TestUtils.assertThrowsCBL(CBLError.Domain.CBLITE, CBLError.Code.BUSY) {
                Database.delete(dbName, dbDir)
            }
        } finally {
            closeDb(db)
        }
    }

    @Test
    fun testDeleteNonExistingDBWithDefaultDir() {
        assertFailsWith<CouchbaseLiteException> {
            Database.delete("notexistdb", baseTestDb.path)
        }
    }

    @Test
    fun testDeleteNonExistingDB() {
        TestUtils.assertThrowsCBL(CBLError.Domain.CBLITE, CBLError.Code.NOT_FOUND) {
            Database.delete(baseTestDb.name, getScratchDirectoryPath("nowhere"))
        }
    }

    //---------------------------------------------
    //  Database Existing
    //---------------------------------------------

    @Test
    fun testDatabaseExistsWithDir() {
        val dirName = getUniqueName("test-exists-dir")

        val dbDir = getScratchDirectoryPath(dirName)

        assertFalse(Database.exists(dirName, dbDir))

        // create db with custom directory
        val db = Database(dirName, DatabaseConfiguration().setDirectory(dbDir))
        try {
            assertTrue(Database.exists(dirName, dbDir))

            val dbPath = db.path!!

            db.close()
            assertTrue(Database.exists(dirName, dbDir))

            Database.delete(dirName, dbDir)
            assertFalse(Database.exists(dirName, dbDir))

            assertFalse(FileUtils.dirExists(dbPath))
        } finally {
            deleteDb(db)
        }
    }

    @Test
    fun testDatabaseExistsAgainstNonExistDBWithDefaultDir() {
        assertFalse(Database.exists("notexistdb", baseTestDb.path!!))
    }

    @Test
    fun testDatabaseExistsAgainstNonExistDB() {
        assertFalse(Database.exists(baseTestDb.name, getScratchDirectoryPath("nowhere")))
    }

    @Test
    fun testCompact() {
        val nDocs = 20
        val nUpdates = 25

        val docIDs: List<String> = createDocsInBaseTestDb(nDocs)

        // Update each doc 25 times:
        baseTestDb.inBatch {
            for (docID in docIDs) {
                var savedDoc = baseTestDb.getDocument(docID)!!
                for (i in 0 until nUpdates) {
                    val doc = savedDoc.toMutable()
                    doc.setValue("number", i)
                    savedDoc = saveDocInBaseTestDb(doc)
                }
            }
        }

        // Add each doc with a blob object:
        for (docID in docIDs) {
            val doc = baseTestDb.getDocument(docID)!!.toMutable()
            doc.setValue("blob", Blob("text/plain", doc.id.encodeToByteArray()))
            saveDocInBaseTestDb(doc)
        }

        assertEquals(nDocs.toLong(), baseTestDb.count)

        val attsDir = "${baseTestDb.path}/Attachments"
        assertTrue(FileUtils.dirExists(attsDir))
        assertEquals(nDocs, FileUtils.listFiles(attsDir).size)

        // Compact:
        assertTrue(baseTestDb.performMaintenance(MaintenanceType.COMPACT))
        assertEquals(nDocs, FileUtils.listFiles(attsDir).size)

        // Delete all docs:
        for (docID in docIDs) {
            val savedDoc = baseTestDb.getDocument(docID)!!
            baseTestDb.delete(savedDoc)
            assertNull(baseTestDb.getDocument(docID))
        }

        // Compact:
        assertTrue(baseTestDb.performMaintenance(MaintenanceType.COMPACT))
        assertEquals(0, FileUtils.listFiles(attsDir).size)
    }

    // REF: https://github.com/couchbase/couchbase-lite-android/issues/1231
    @Test
    fun testOverwriteDocWithNewDocInstance() {
        val mDoc1 = MutableDocument("abc")
        mDoc1.setValue("someKey", "someVar")
        val doc1 = saveDocInBaseTestDb(mDoc1)

        // This cause conflict, DefaultConflictResolver should be applied.
        val mDoc2 = MutableDocument("abc")
        mDoc2.setValue("someKey", "newVar")
        val doc2 = saveDocInBaseTestDb(mDoc2)

        // NOTE: Both doc1 and doc2 are generation 1. Higher revision one should win
        assertEquals(1, baseTestDb.count)
        val doc = baseTestDb.getDocument("abc")
        assertNotNull(doc)
        // NOTE doc1 -> theirs, doc2 -> mine
        if (doc2.revisionID!! > doc1.revisionID!!) {
            // mine -> doc 2 win
            assertEquals("newVar", doc.getString("someKey"))
        } else {
            // their -> doc 1 win
            assertEquals("someVar", doc.getString("someKey"))
        }
    }

    @Test
    fun testCopy() {
        val nDocs = 10
        for (i in 0 until nDocs) {
            val docID = "doc_$i"
            val doc = MutableDocument(docID)
            doc.setValue("name", docID)
            doc.setValue("data", Blob("text/plain", docID.encodeToByteArray()))
            saveDocInBaseTestDb(doc)
        }

        val config = baseTestDb.config

        val dbName = getUniqueName("test_copy_db")

        // Copy:
        Database.copy(baseTestDb.path!!, dbName, config)

        // Verify:
        assertTrue(Database.exists(dbName, config.directory))

        val newDb = Database(dbName, config)
        try {
            assertNotNull(newDb)
            assertEquals(nDocs.toLong(), newDb.count)

            QueryBuilder.select(SelectResult.expression(Meta.id))
                .from(DataSource.database(newDb))
                .execute().use { rs ->
                    for (r in rs) {
                        val docID = r.getString(0)
                        assertNotNull(docID)

                        val doc = newDb.getDocument(docID)
                        assertNotNull(doc)
                        assertEquals(docID, doc.getString("name"))

                        val blob = doc.getBlob("data")
                        assertNotNull(blob)

                        assertEquals(docID, blob.content?.decodeToString())
                    }
                }
        } finally {
            deleteDb(newDb)
        }
    }

    @Test
    fun testCreateIndex() {
        assertEquals(0, baseTestDb.getIndexes().size)

        baseTestDb.createIndex(
            "index1",
            IndexBuilder.valueIndex(
                ValueIndexItem.property("firstName"),
                ValueIndexItem.property("lastName")
            )
        )
        assertEquals(1, baseTestDb.getIndexes().size)

        // Create FTS index:
        baseTestDb.createIndex(
            "index2",
            IndexBuilder.fullTextIndex(FullTextIndexItem.property("detail"))
        )
        assertEquals(2, baseTestDb.getIndexes().size)

        baseTestDb.createIndex(
            "index3",
            IndexBuilder.fullTextIndex(FullTextIndexItem.property("es-detail"))
                .ignoreAccents(true)
                .setLanguage("es")
        )
        assertEquals(3, baseTestDb.getIndexes().size)

        // Create value index with expression() instead of property()
        baseTestDb.createIndex(
            "index4",
            IndexBuilder.valueIndex(
                ValueIndexItem.expression(Expression.property("firstName")),
                ValueIndexItem.expression(Expression.property("lastName"))
            )
        )
        assertEquals(4, baseTestDb.getIndexes().size)

        assertContents(baseTestDb.getIndexes(), "index1", "index2", "index3", "index4")
    }

    @Test
    fun testCreateIndexWithConfig() {
        assertEquals(0, baseTestDb.getIndexes().size)

        baseTestDb.createIndex("index1", ValueIndexConfiguration("firstName", "lastName"))
        assertEquals(1, baseTestDb.getIndexes().size)

        baseTestDb.createIndex(
            "index2",
            FullTextIndexConfiguration("detail").ignoreAccents(true).setLanguage("es")
        )
        assertEquals(2, baseTestDb.getIndexes().size)

        assertContents(baseTestDb.getIndexes(), "index1", "index2")
    }

    @Test
    fun testCreateSameIndexTwice() {
        // Create index with first name:
        val indexItem = ValueIndexItem.property("firstName")
        val index: Index = IndexBuilder.valueIndex(indexItem)
        baseTestDb.createIndex("myindex", index)

        // Call create index again:
        baseTestDb.createIndex("myindex", index)

        assertEquals(1, baseTestDb.getIndexes().size)
        assertContents(baseTestDb.getIndexes(), "myindex")
    }

    @Test
    fun testCreateSameNameIndexes() {
        val fNameItem = ValueIndexItem.property("firstName")
        val lNameItem = ValueIndexItem.property("lastName")
        val detailItem = FullTextIndexItem.property("detail")

        // Create value index with first name:
        val fNameIndex: Index = IndexBuilder.valueIndex(fNameItem)
        baseTestDb.createIndex("myindex", fNameIndex)

        // Create value index with last name:
        val lNameindex = IndexBuilder.valueIndex(lNameItem)
        baseTestDb.createIndex("myindex", lNameindex)

        // Check:
        assertEquals(1, baseTestDb.getIndexes().size)
        assertContents(baseTestDb.getIndexes(), "myindex")

        // Create FTS index:
        val detailIndex: Index = IndexBuilder.fullTextIndex(detailItem)
        baseTestDb.createIndex("myindex", detailIndex)

        // Check:
        assertEquals(1, baseTestDb.getIndexes().size)
        assertContents(baseTestDb.getIndexes(), "myindex")
    }

    @Test
    fun testDeleteIndex() {
        testCreateIndex()

        // Delete indexes:
        baseTestDb.deleteIndex("index4")
        assertEquals(3, baseTestDb.getIndexes().size)
        assertContents(baseTestDb.getIndexes(), "index1", "index2", "index3")

        baseTestDb.deleteIndex("index1")
        assertEquals(2, baseTestDb.getIndexes().size)
        assertContents(baseTestDb.getIndexes(), "index2", "index3")

        baseTestDb.deleteIndex("index2")
        assertEquals(1, baseTestDb.getIndexes().size)
        assertContents(baseTestDb.getIndexes(), "index3")

        baseTestDb.deleteIndex("index3")
        assertEquals(0, baseTestDb.getIndexes().size)
        assertTrue(baseTestDb.getIndexes().isEmpty())

        // Delete non existing index:
        baseTestDb.deleteIndex("dummy")

        // Delete deleted indexes:
        baseTestDb.deleteIndex("index1")
        baseTestDb.deleteIndex("index2")
        baseTestDb.deleteIndex("index3")
        baseTestDb.deleteIndex("index4")
    }

    @Test
    fun testRebuildIndex() {
        testCreateIndex()
        assertTrue(baseTestDb.performMaintenance(MaintenanceType.REINDEX))
    }

    // https://github.com/couchbase/couchbase-lite-android/issues/1416
    @Test
    fun testDeleteAndOpenDB() {
        val config = DatabaseConfiguration()

        var database1: Database? = null
        var database2: Database? = null
        try {
            // open a database
            database1 = createDb("del_open_db", config)
            val dbName = database1.name

            // delete it
            database1.delete()

            // open it again
            database2 = Database(dbName, config)

            // insert documents
            val db = database2
            database2.inBatch {
                // just create 100 documents
                for (i in 0 until 100) {
                    val doc = MutableDocument()

                    // each doc has 10 items
                    doc.setInt("index", i)
                    for (j in 0..9) {
                        doc.setInt("item_$j", j)
                    }

                    db.save(doc)
                }
            }

            // close db again
            database2.close()
        } finally {
            deleteDb(database1)
            deleteDb(database2)
        }
    }

    @Test
    fun testSaveAndUpdateMutableDoc() {
        val doc = MutableDocument("doc1")
        doc.setString("firstName", "Daniel")
        baseTestDb.save(doc)

        // Update:
        doc.setString("lastName", "Tiger")
        baseTestDb.save(doc)

        // Update:
        doc.setLong("age", 20L) // Int vs Long assertEquals can not ignore diff.
        baseTestDb.save(doc)
        assertEquals(3, doc.sequence)

        val expected = mapOf<String, Any?>(
            "firstName" to "Daniel",
            "lastName" to "Tiger",
            "age" to 20L
        )
        assertEquals(expected, doc.toMap())

        val savedDoc = baseTestDb.getDocument(doc.id)!!
        assertEquals(expected, savedDoc.toMap())
        assertEquals(3, savedDoc.sequence)
    }

    @Test
    fun testSaveDocWithConflict() {
        testSaveDocWithConflictUsingConcurrencyControl(ConcurrencyControl.LAST_WRITE_WINS)
        testSaveDocWithConflictUsingConcurrencyControl(ConcurrencyControl.FAIL_ON_CONFLICT)
    }

    @Test
    fun testDeleteDocWithConflict() {
        testDeleteDocWithConflictUsingConcurrencyControl(ConcurrencyControl.LAST_WRITE_WINS)
        testDeleteDocWithConflictUsingConcurrencyControl(ConcurrencyControl.FAIL_ON_CONFLICT)
    }

    @Test
    fun testSaveDocWithNoParentConflict() {
        testSaveDocWithNoParentConflictUsingConcurrencyControl(ConcurrencyControl.LAST_WRITE_WINS)
        testSaveDocWithNoParentConflictUsingConcurrencyControl(ConcurrencyControl.FAIL_ON_CONFLICT)
    }

    @Test
    fun testSaveDocWithDeletedConflict() {
        testSaveDocWithDeletedConflictUsingConcurrencyControl(ConcurrencyControl.LAST_WRITE_WINS)
        testSaveDocWithDeletedConflictUsingConcurrencyControl(ConcurrencyControl.FAIL_ON_CONFLICT)
    }

    @Test
    fun testDeleteAndUpdateDoc() {
        val doc = MutableDocument("doc1")
        doc.setString("firstName", "Daniel")
        doc.setString("lastName", "Tiger")
        baseTestDb.save(doc)

        baseTestDb.delete(doc)
        assertEquals(2, doc.sequence)
        assertNull(baseTestDb.getDocument(doc.id))

        doc.setString("firstName", "Scott")
        baseTestDb.save(doc)
        assertEquals(3, doc.sequence)

        val expected = mapOf<String, Any?>(
            "firstName" to "Scott",
            "lastName" to "Tiger"
        )
        assertEquals(expected, doc.toMap())

        val savedDoc = baseTestDb.getDocument(doc.id)
        assertNotNull(savedDoc)
        assertEquals(expected, savedDoc.toMap())
    }

    // TODO: doc1b sequence goes from 1 to 3 after deletion (expecting 2) for native C
    //  https://forums.couchbase.com/t/cbl-c-sdk-kotlin-multiplatform-feedback-questions/34649
    //  https://issues.couchbase.com/browse/CBL-3749
    @IgnoreNative
    @Test
    fun testDeleteAlreadyDeletedDoc() {
        val doc = MutableDocument("doc1")
        doc.setString("firstName", "Daniel")
        doc.setString("lastName", "Tiger")
        baseTestDb.save(doc)

        // Get two doc1 document objects (doc1a and doc1b):
        val doc1a = baseTestDb.getDocument("doc1")!!
        val doc1b = baseTestDb.getDocument("doc1")!!.toMutable()

        // Delete doc1a:
        baseTestDb.delete(doc1a)
        assertEquals(2, doc1a.sequence)
        assertNull(baseTestDb.getDocument(doc.id))

        // Delete doc1b:
        baseTestDb.delete(doc1b)
        assertEquals(2, doc1b.sequence)
        assertNull(baseTestDb.getDocument(doc.id))
    }

    @Test
    fun testDeleteNonExistingDoc() {
        val doc1a = createSingleDocInBaseTestDb("doc1")
        val doc1b = baseTestDb.getDocument("doc1")!!

        // purge doc
        baseTestDb.purge(doc1a)
        assertEquals(0, baseTestDb.count)
        assertNull(baseTestDb.getDocument(doc1a.id))

        TestUtils.assertThrowsCBL(CBLError.Domain.CBLITE, CBLError.Code.NOT_FOUND) {
            baseTestDb.delete(doc1a)
        }
        TestUtils.assertThrowsCBL(CBLError.Domain.CBLITE, CBLError.Code.NOT_FOUND) {
            baseTestDb.delete(doc1b)
        }

        assertEquals(0, baseTestDb.count)
        assertNull(baseTestDb.getDocument(doc1b.id))
    }

    // https://github.com/couchbase/couchbase-lite-android/issues/1652
    @Test
    fun testDeleteWithOldDocInstance() {
        // 1. save
        var mdoc = MutableDocument("doc")
        mdoc.setBoolean("updated", false)
        baseTestDb.save(mdoc)

        val doc = baseTestDb.getDocument("doc")!!

        // 2. update
        mdoc = doc.toMutable()
        mdoc.setBoolean("updated", true)
        baseTestDb.save(mdoc)

        // 3. delete by previously retrieved document
        baseTestDb.delete(doc)
        assertNull(baseTestDb.getDocument("doc"))
    }

    // The following four tests verify, explicitly, the code that
    // mitigates the 2.8.0 bug (CBL-1408)
    // There is one more test for this in DatabaseEncryptionTest
    @Test
    fun testReOpenExistingDb() {
        val dbName = getUniqueName("test_db")

        // verify that the db directory is no longer in the misguided 2.8.0 subdirectory
        val dbDirectory = DatabaseConfiguration().directory
        assertFalse(dbDirectory.endsWith(".couchbase"))

        var db: Database? = null
        try {
            db = Database(dbName)
            val mDoc = MutableDocument()
            mDoc.setString("foo", "bar")
            db.save(mDoc)
            db.close()

            db = Database(dbName)
            assertEquals(1L, db.count)
            val doc = db.getDocument(mDoc.id)!!
            assertEquals("bar", doc.getString("foo"))
        } finally {
            try {
                db?.delete()
            } catch (ignore: Exception) {
            }
        }
    }

    private fun duplicateBaseTestDb(count: Int): Database {
        val db = duplicateBaseTestDb()

        val actualCount = db.count
        if (count.toLong() != actualCount) {
            deleteDb(db)
            fail("Unexpected database count: $count <> $actualCount")
        }

        return db
    }

    // helper method to save n number of docs
    private fun createDocsInBaseTestDb(n: Int): List<String> {
        val docs = mutableListOf<String>()
        for (i in 0 until n) {
            val doc = MutableDocument("doc_${i.paddedString(3)}")
            doc.setValue("key", i)
            docs.add(saveDocInBaseTestDb(doc).id)
        }
        assertEquals(n.toLong(), baseTestDb.count)
        return docs
    }

    // helper method to verify n number of docs
    private fun verifyDocuments(n: Int) {
        for (i in 0 until n) {
            verifyGetDocument("doc_${i.paddedString(3)}", i)
        }
    }

    // helper methods to verify getDoc
    private fun verifyGetDocument(docID: String) {
        verifyGetDocument(docID, 1)
    }

    // helper methods to verify getDoc
    private fun verifyGetDocument(docID: String, value: Int) {
        verifyGetDocument(baseTestDb, docID, value)
    }

    // helper methods to verify getDoc
    private fun verifyGetDocument(db: Database, docID: String) {
        verifyGetDocument(db, docID, 1)
    }

    // helper methods to verify getDoc
    private fun verifyGetDocument(db: Database, docID: String, value: Int) {
        val doc = db.getDocument(docID)
        assertNotNull(doc)
        assertEquals(docID, doc.id)
        assertEquals(value, (doc.getValue("key") as Number).toInt())
    }

    // helper method to purge doc and verify doc.
    private fun purgeDocAndVerify(doc: Document) {
        val docID = doc.id
        baseTestDb.purge(doc)
        assertNull(baseTestDb.getDocument(docID))
    }

    private fun testSaveDocWithConflictUsingConcurrencyControl(cc: ConcurrencyControl) {
        val doc = MutableDocument("doc1")
        doc.setString("firstName", "Daniel")
        doc.setString("lastName", "Tiger")
        baseTestDb.save(doc)

        // Get two doc1 document objects (doc1a and doc1b):
        val doc1a = baseTestDb.getDocument("doc1")!!.toMutable()
        val doc1b = baseTestDb.getDocument("doc1")!!.toMutable()

        // Modify doc1a:
        doc1a.setString("firstName", "Scott")
        baseTestDb.save(doc1a)
        doc1a.setString("nickName", "Scotty")
        baseTestDb.save(doc1a)

        val expected = mapOf<String, Any?>(
            "firstName" to "Scott",
            "lastName" to "Tiger",
            "nickName" to "Scotty"
        )
        assertEquals(expected, doc1a.toMap())
        assertEquals(3, doc1a.sequence)

        // Modify doc1b, result to conflict when save:
        doc1b.setString("lastName", "Lion")
        if (cc === ConcurrencyControl.LAST_WRITE_WINS) {
            assertTrue(baseTestDb.save(doc1b, cc))
            val savedDoc = baseTestDb.getDocument(doc.id)!!
            assertEquals(doc1b.toMap(), savedDoc.toMap())
            assertEquals(4, savedDoc.sequence)
        } else {
            assertFalse(baseTestDb.save(doc1b, cc))
            val savedDoc = baseTestDb.getDocument(doc.id)!!
            assertEquals(expected, savedDoc.toMap())
            assertEquals(3, savedDoc.sequence)
        }

        recreateBastTestDb()
    }

    private fun testDeleteDocWithConflictUsingConcurrencyControl(cc: ConcurrencyControl) {
        val doc = MutableDocument("doc1")
        doc.setString("firstName", "Daniel")
        doc.setString("lastName", "Tiger")
        baseTestDb.save(doc)

        // Get two doc1 document objects (doc1a and doc1b):
        val doc1a = baseTestDb.getDocument("doc1")!!.toMutable()
        val doc1b = baseTestDb.getDocument("doc1")!!.toMutable()

        // Modify doc1a:
        doc1a.setString("firstName", "Scott")
        baseTestDb.save(doc1a)

        val expected = mapOf<String, Any?>(
            "firstName" to "Scott",
            "lastName" to "Tiger"
        )
        assertEquals(expected, doc1a.toMap())
        assertEquals(2, doc1a.sequence)

        // Modify doc1b and delete, result to conflict when delete:
        doc1b.setString("lastName", "Lion")
        if (cc === ConcurrencyControl.LAST_WRITE_WINS) {
            assertTrue(baseTestDb.delete(doc1b, cc))
            assertEquals(3, doc1b.sequence)
            assertNull(baseTestDb.getDocument(doc1b.id))
        } else {
            assertFalse(baseTestDb.delete(doc1b, cc))
            val savedDoc = baseTestDb.getDocument(doc.id)!!
            assertEquals(expected, savedDoc.toMap())
            assertEquals(2, savedDoc.sequence)
        }

        recreateBastTestDb()
    }

    private fun testSaveDocWithNoParentConflictUsingConcurrencyControl(cc: ConcurrencyControl) {
        val doc1a = MutableDocument("doc1")
        doc1a.setString("firstName", "Daniel")
        doc1a.setString("lastName", "Tiger")
        baseTestDb.save(doc1a)

        var savedDoc = baseTestDb.getDocument(doc1a.id)!!
        assertEquals(doc1a.toMap(), savedDoc.toMap())
        assertEquals(1, savedDoc.sequence)

        val doc1b = MutableDocument("doc1")
        doc1b.setString("firstName", "Scott")
        doc1b.setString("lastName", "Tiger")
        if (cc === ConcurrencyControl.LAST_WRITE_WINS) {
            assertTrue(baseTestDb.save(doc1b, cc))
            savedDoc = baseTestDb.getDocument(doc1b.id)!!
            assertEquals(doc1b.toMap(), savedDoc.toMap())
            assertEquals(2, savedDoc.sequence)
        } else {
            assertFalse(baseTestDb.save(doc1b, cc))
            savedDoc = baseTestDb.getDocument(doc1b.id)!!
            assertEquals(doc1a.toMap(), savedDoc.toMap())
            assertEquals(1, savedDoc.sequence)
        }

        recreateBastTestDb()
    }

    private fun testSaveDocWithDeletedConflictUsingConcurrencyControl(cc: ConcurrencyControl) {
        val doc = MutableDocument("doc1")
        doc.setString("firstName", "Daniel")
        doc.setString("lastName", "Tiger")
        baseTestDb.save(doc)

        // Get two doc1 document objects (doc1a and doc1b):
        val doc1a = baseTestDb.getDocument("doc1")!!
        val doc1b = baseTestDb.getDocument("doc1")!!.toMutable()

        // Delete doc1a:
        baseTestDb.delete(doc1a)
        assertEquals(2, doc1a.sequence)
        assertNull(baseTestDb.getDocument(doc.id))

        // Modify doc1b, result to conflict when save:
        doc1b.setString("lastName", "Lion")
        if (cc === ConcurrencyControl.LAST_WRITE_WINS) {
            assertTrue(baseTestDb.save(doc1b, cc))
            val savedDoc = baseTestDb.getDocument(doc.id)!!
            assertEquals(doc1b.toMap(), savedDoc.toMap())
            assertEquals(3, savedDoc.sequence)
        } else {
            assertFalse(baseTestDb.save(doc1b, cc))
            assertNull(baseTestDb.getDocument(doc.id))
        }

        recreateBastTestDb()
    }
}
