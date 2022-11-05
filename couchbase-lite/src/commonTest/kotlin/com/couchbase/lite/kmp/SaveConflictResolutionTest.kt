package com.couchbase.lite.kmp

import com.couchbase.lite.generation
import com.couchbase.lite.getC4Document
import kotlin.test.*

// !!! WILL LIKELY CHANGE WITH COLLECTION SAVVY REPLICATION

private const val docID = "doc1"

// These tests are largely translations of Jay Vavachan's Obj-C tests
class SaveConflictResolutionTest : BaseDbTest() {

    /**
     * 1. Test conflict handler that just returns true without modifying the document.
     * 2. Test conflict handler that modifying the document and returns true.
     * 3. Make sure that the document is saved correctly for both scenarios.
     */
    @Test
    fun testConflictHandler() {
        val doc = MutableDocument(docID)
        doc.setString("location", "Olympia")
        saveDocInBaseTestDb(doc)

        assertEquals(1, baseTestDb.getNonNullDoc(docID).generation)

        val doc1a = baseTestDb.getNonNullDoc(docID).toMutable()
        val doc1b = baseTestDb.getNonNullDoc(docID).toMutable()

        doc1a.setString("artist", "Sheep Jones")
        baseTestDb.save(doc1a)

        assertEquals(2, baseTestDb.getNonNullDoc(docID).generation)

        doc1b.setString("artist", "Holly Sears")

        var succeeded = baseTestDb.save(doc1b) { cur: MutableDocument, old: Document? ->
            assertEquals(doc1b, cur)
            assertEquals(doc1a, old)
            assertEquals(2L, cur.generation)
            assertEquals(2L, old?.generation)
            true
        }
        assertTrue(succeeded)

        val newDoc = baseTestDb.getNonNullDoc(docID)
        assertEquals(doc1b, newDoc)
        assertEquals(3L, newDoc.generation)

        val doc1c = baseTestDb.getNonNullDoc(docID).toMutable()
        val doc1d = baseTestDb.getNonNullDoc(docID).toMutable()

        doc1c.setString("artist", "Marjorie Morgan")
        baseTestDb.save(doc1c)

        assertEquals(4L, baseTestDb.getNonNullDoc(docID).generation)

        doc1d.setString("artist", "G. Charnelet-Vasselon")

        succeeded = baseTestDb.save(doc1d) { cur: MutableDocument, old: Document? ->
            assertEquals(doc1d, cur)
            assertEquals(doc1c, old)
            assertEquals(4L, cur.generation)
            assertEquals(4L, old?.generation)
            cur.setString("artist", "Sheep Jones")
            true
        }
        assertTrue(succeeded)

        val curDoc = baseTestDb.getNonNullDoc(docID)
        assertEquals("Olympia", curDoc.getString("location"))
        assertEquals("Sheep Jones", curDoc.getString("artist"))
        assertEquals(5L, curDoc.generation)
    }

    /**
     * 1. Test conflict handler that return false.
     * 2. Make sure that the save method return false as well and the original document has no change.
     */
    @Test
    fun testCancelConflictHandler() {
        val doc = MutableDocument(docID)
        doc.setString("location", "Olympia")
        saveDocInBaseTestDb(doc)

        assertEquals(1, baseTestDb.getNonNullDoc(docID).generation)

        val doc1a = baseTestDb.getNonNullDoc(docID).toMutable()
        val doc1b = baseTestDb.getNonNullDoc(docID).toMutable()

        doc1a.setString("artist", "Sheep Jones")
        baseTestDb.save(doc1a)

        assertEquals(2, baseTestDb.getNonNullDoc(docID).generation)

        doc1b.setString("artist", "Holly Sears")

        var succeeded = false
        try {
            @Suppress("UNUSED_VALUE")
            succeeded = baseTestDb.save(doc1b) { cur: MutableDocument, old: Document? ->
                assertEquals(doc1b, cur)
                assertEquals(doc1a, old)
                false
            }
            fail("save should not succeed!")
        } catch (err: CouchbaseLiteException) {
            assertEquals(CBLError.Code.CONFLICT, err.getCode())
        }
        assertFalse(succeeded)

        val curDoc = baseTestDb.getNonNullDoc(docID)
        assertEquals(curDoc, doc1a)

        // make sure no update to revision and generation
        assertEquals(doc1a.revisionID, curDoc.revisionID)
        assertEquals(2, curDoc.generation)

        val doc1c = baseTestDb.getNonNullDoc(docID).toMutable()
        val doc1d = baseTestDb.getNonNullDoc(docID).toMutable()

        doc1c.setString("artist", "Marjorie Morgan")
        baseTestDb.save(doc1c)

        assertEquals(3, baseTestDb.getNonNullDoc(docID).generation)

        doc1d.setString("artist", "G. Charnelet-Vasselon")

        try {
            @Suppress("UNUSED_VALUE")
            succeeded = baseTestDb.save(doc1d) { cur, _ ->
                cur.setString("artist", "Holly Sears")
                false
            }
            fail("save should not succeed!")
        } catch (err: CouchbaseLiteException) {
            assertEquals(CBLError.Code.CONFLICT, err.getCode())
        }
        assertFalse(succeeded)

        // make sure no update to revision and generation
        val newDoc = baseTestDb.getNonNullDoc(docID)
        assertEquals(newDoc, doc1c)
        assertEquals(doc1c.revisionID, newDoc.revisionID)
        assertEquals(3, newDoc.generation)
    }

    /**
     * 1. Test conflict handler that has an old doc as a deleted doc.
     * 2. Make sure that the old doc is null.
     * 3. Make sure that if returning true, the doc is saved correctly.
     *    If returning false, the document should be deleted as no change.
     */
    @Test
    fun testConflictHandlerWithDeletedOldDoc1() {
        createSingleDocInBaseTestDb(docID)

        assertEquals(1, baseTestDb.getNonNullDoc(docID).generation)

        val doc1a = baseTestDb.getNonNullDoc(docID)
        val doc1b = baseTestDb.getNonNullDoc(docID).toMutable()

        baseTestDb.delete(doc1a, ConcurrencyControl.LAST_WRITE_WINS)

        doc1b.setString("location", "Olympia")

        val succeeded = baseTestDb.save(doc1b) { cur: MutableDocument, old: Document? ->
            assertNotNull(cur)
            assertNull(old)
            true
        }
        assertTrue(succeeded)

        assertEquals(doc1b, baseTestDb.getNonNullDoc(docID))
    }

    /**
     * 1. Test conflict handler that has an old doc as a deleted doc.
     * 2. Make sure that the old doc is null.
     * 3. Make sure that if returning true, the doc is saved correctly.
     *    If returning false, the document should be deleted as no change.
     */
    @Test
    fun testConflictHandlerWithDeletedOldDoc2() {
        createSingleDocInBaseTestDb(docID)

        assertEquals(1, baseTestDb.getNonNullDoc(docID).generation)

        val doc1a = baseTestDb.getNonNullDoc(docID).toMutable()
        val doc1b = baseTestDb.getNonNullDoc(docID).toMutable()

        baseTestDb.delete(doc1a, ConcurrencyControl.LAST_WRITE_WINS)

        doc1b.setString("location", "Olympia")

        var succeeded = false
        try {
            @Suppress("UNUSED_VALUE")
            succeeded = baseTestDb.save(doc1b) { cur: MutableDocument, old: Document? ->
                assertNull(old)
                assertNotNull(cur)
                false
            }
            fail("save should not succeed!")
        } catch (err: CouchbaseLiteException) {
            assertEquals(CBLError.Code.CONFLICT, err.getCode())
        }
        assertFalse(succeeded)

        assertNull(baseTestDb.getDocument(docID))

        // TODO: 3.1 API
        //val c4doc = baseTestDb.getDefaultCollection()?.getC4Document(docID)
        val c4doc = baseTestDb.getC4Document(docID)
        assertNotNull(c4doc)
        assertTrue(c4doc.isRevDeleted())
    }

    /**
     * 1. Test that an exception thrown from the conflict handler is captured and rethrown to the save method correctly.
     */
    @Test
    fun testConflictHandlerThrowsException() {
        val doc = MutableDocument(docID)
        doc.setString("location", "Olympia")
        saveDocInBaseTestDb(doc)

        assertEquals(1L, baseTestDb.getNonNullDoc(docID).generation)

        val doc1a = baseTestDb.getNonNullDoc(docID).toMutable()
        val doc1b = baseTestDb.getNonNullDoc(docID).toMutable()

        doc1a.setString("artist", "Sheep Jones")
        baseTestDb.save(doc1a)

        assertEquals(2L, baseTestDb.getNonNullDoc(docID).generation)

        doc1b.setString("artist", "Holly Sears")

        var succeeded = false
        try {
            @Suppress("UNUSED_VALUE")
            succeeded = baseTestDb.save(doc1b) { _: MutableDocument, _: Document? ->
                throw IllegalStateException("freak out!")
            }
            fail("save should not succeed!")
        } catch (err: CouchbaseLiteException) {
            assertEquals(CBLError.Code.CONFLICT, err.getCode())
            assertEquals("freak out!", err.cause?.message)
        }
        assertFalse(succeeded)

        assertEquals(doc1a, baseTestDb.getNonNullDoc(docID))
        assertEquals(2L, baseTestDb.getNonNullDoc(docID).generation)
    }

    /**
     * 1. Test conflict handler that just returns true with modifying the document.
     *    It's possible that the conflict might happen again after trying to save the resolved document to the database.
     * 2. We could simulate this situation by update the local document before returning a resolved doc
     *    and make sure that the conflict resolver is called again.
     * 3. Make sure that the document is saved correctly with updated information, called twice.
     */
    @Test
    fun testCancelConflictHandlerCalledTwice() {
        val doc = MutableDocument(docID)
        doc.setString("location", "Olympia")
        saveDocInBaseTestDb(doc)
        assertEquals(1, baseTestDb.getNonNullDoc(docID).generation)

        val doc1a = baseTestDb.getNonNullDoc(docID).toMutable()
        val doc1b = baseTestDb.getNonNullDoc(docID).toMutable()

        doc1a.setString("artist", "Sheep Jones")
        baseTestDb.save(doc1a)
        assertEquals(2, baseTestDb.getNonNullDoc(docID).generation)

        doc1b.setString("artist", "Holly Sears")

        var count = 0
        val succeeded = baseTestDb.save(doc1b) { cur: MutableDocument, old: Document? ->
            count++
            val doc1c = baseTestDb.getNonNullDoc(docID).toMutable()
            if (!doc1c.getBoolean("second update")) {
                assertEquals(2L, cur.generation)
                assertEquals(2L, old?.generation)
                doc1c.setBoolean("second update", true)
                assertEquals(3L, saveDocInBaseTestDb(doc1c).generation)
            }

            val data = old?.toMap()?.toMutableMap() ?: mutableMapOf()
            for (key in cur.keys) {
                data[key] = cur.getValue(key)
            }
            cur.setData(data)
            cur.setString("edit", "local")
            true
        }
        assertTrue(succeeded)

        assertEquals(2, count)

        val newDoc = baseTestDb.getNonNullDoc(docID)
        assertEquals(4, newDoc.generation)
        assertEquals(newDoc.getString("location"), "Olympia")
        assertEquals(newDoc.getString("artist"), "Holly Sears")
        assertEquals(newDoc.getString("edit"), "local")
    }

    /**
     * 1. Get and make some changes to doc1a.
     * 2. Purge doc1b.
     * 3. Save doc1a, which should return false with error NotFound.
     */
    @Test
    fun testConflictHandlerWhenDocumentIsPurged() {
        val doc = MutableDocument(docID)
        doc.setString("location", "Olympia")
        saveDocInBaseTestDb(doc)

        assertEquals(1L, baseTestDb.getNonNullDoc(docID).generation)

        val doc1a = baseTestDb.getNonNullDoc(docID).toMutable()

        baseTestDb.purge(docID)

        doc1a.setString("artist", "Sheep Jones")

        var succeeded = false
        try {
            @Suppress("UNUSED_VALUE")
            succeeded = baseTestDb.save(doc1a) { _: MutableDocument, _: Document? -> true }
            fail("save should not succeed!")
        } catch (err: CouchbaseLiteException) {
            assertEquals(CBLError.Code.NOT_FOUND, err.getCode())
        }
        assertFalse(succeeded)
    }

    private fun Database.getNonNullDoc(id: String) =
        this.getDocument(id) ?: throw IllegalStateException("document $id is null")
}
