package kotbase

import kotbase.internal.utils.PlatformUtils
import kotbase.internal.utils.Report
import kotbase.internal.utils.paddedString
import kotlinx.atomicfu.atomic
import kotlinx.datetime.Clock
import kotlin.test.*
import kotlin.time.Duration.Companion.milliseconds

private const val ITERATIONS = 2000

private fun interface Verifier {
    fun verify(n: Int, result: Result?)
}

// Timings were chosen to allow a Nexus 6 running Android 7.0 to pass.
class LoadTest : BaseDbTest() {

    @Test
    fun testAddRevisions() {
        timeTest("testAddRevisions", 35 * 1000L) {
            addRevisions(1000, false)
            addRevisions(1000, true)
        }
    }

    @Test
    fun testCreate() {
        timeTest("testCreate", 10 * 1000L) {
            createAndSaveDocument("Create", ITERATIONS)
            verifyByTag("Create", ITERATIONS)
            assertEquals(ITERATIONS.toLong(), baseTestDb.count)
        }
    }

    // This test reliably drove a bug that caused C4NativePeer
    // to finalize what appears to have been an incompletely initialize
    // instance of C4Document.  It is, otherwise, not relevant.
    @Ignore // Same test as testCreate
    @Test
    fun testCreateMany() {
        timeTest("testCreateMany", 35 * 1000L) {
            for (i in 0..3) {
                createAndSaveDocument("Create", ITERATIONS)
                verifyByTag("Create", ITERATIONS)
                assertEquals(ITERATIONS.toLong(), baseTestDb.count)
            }
        }
    }

    @Test
    fun testDelete() {
        timeTest("testDelete", 20 * 1000L) {
            // create & delete doc ITERATIONS times
            for (i in 0 until ITERATIONS) {
                val docID = "doc-${i.paddedString(10)}"
                createAndSaveDocument(docID, "Delete")
                assertEquals(1, baseTestDb.count)
                val doc = baseTestDb.getDocument(docID)
                assertNotNull(doc)
                assertEquals("Delete", doc.getString("tag"))
                baseTestDb.delete(doc)
                assertEquals(0, baseTestDb.count)
            }
        }
    }

    @Test
    fun testRead() {
        timeTest("testRead", 5 * 1000L) {
            // create 1 doc
            createAndSaveDocument("doc1", "Read")
            // read the doc n times
            for (i in 0 until ITERATIONS) {
                val doc = baseTestDb.getDocument("doc1")
                assertNotNull(doc)
                assertEquals("doc1", doc.id)
                assertEquals("Read", doc.getString("tag"))
            }
        }
    }

    // https://github.com/couchbase/couchbase-lite-android/issues/1447
    @Test
    fun testSaveManyDocs() {
        timeTest("testSaveManyDocs", 20 * 1000L) {
            // Without Batch
            for (i in 0 until ITERATIONS) {
                val doc = MutableDocument("doc-${i.paddedString(5)}")
                for (j in 0 until 100) {
                    doc.setInt(j.toString(), j); }
                try {
                    baseTestDb.save(doc)
                } catch (e: CouchbaseLiteException) {
                    Report.log(LogLevel.ERROR, "Failed to save", e)
                }
            }
            assertEquals(ITERATIONS.toLong(), baseTestDb.count)
        }
    }

    @Test
    fun testUpdate() {
        timeTest("testUpdate", 25 * 1000L) {
            // create doc
            createAndSaveDocument("doc1", "Create")
            var doc = baseTestDb.getDocument("doc1")
            assertNotNull(doc)
            assertEquals("doc1", doc.id)
            assertEquals("Create", doc.getString("tag"))

            // update doc n times
            updateDoc(doc, ITERATIONS, "Update")

            // check document
            doc = baseTestDb.getDocument("doc1")
            assertNotNull(doc)
            assertEquals("doc1", doc.id)
            assertEquals("Update", doc.getString("tag"))
            assertEquals(ITERATIONS, doc.getInt("update"))
            val street = "$ITERATIONS street."
            val phone = "650-000-${ITERATIONS.paddedString(4)}"
            assertEquals(street, doc.getDictionary("address")!!.getString("street"))
            assertEquals(phone, doc.getArray("phones")!!.getString(0))
        }
    }

    // https://github.com/couchbase/couchbase-lite-android/issues/1610
    @Test
    fun testUpdate2() {
        timeTest("testUpdate2", 25 * 1000L) {
            val mDoc = MutableDocument("doc1")
            val map = mutableMapOf<String, Any?>()
            map["ID"] = "doc1"
            mDoc.setValue("map", map)
            saveDocInBaseTestDb(mDoc)
            for (i in 0..1999) {
                map["index"] = i
                assertTrue(updateMap(map, i, i.toLong()))
            }
        }
    }

    /// Utility methods
    private fun updateMap(map: Map<String, *>, i: Int, l: Long): Boolean {
        val doc = baseTestDb.getDocument(map["ID"].toString()) ?: return false
        val newDoc = doc.toMutable()
        newDoc.setValue("map", map)
        newDoc.setInt("int", i)
        newDoc.setLong("long", l)
        try {
            baseTestDb.save(newDoc)
        } catch (e: CouchbaseLiteException) {
            Report.log(LogLevel.ERROR, "DB is not responding", e)
            return false
        }
        return true
    }

    private fun addRevisions(revisions: Int, retrieveNewDoc: Boolean) {
        baseTestDb.inBatch {
            val mDoc = MutableDocument("doc")
            if (retrieveNewDoc) {
                updateDocWithGetDocument(mDoc, revisions)
            } else {
                updateDoc(mDoc, revisions)
            }
        }
        val doc = baseTestDb.getDocument("doc")
        assertEquals(revisions - 1, doc!!.getInt("count")) // start from 0.
    }

    private fun updateDoc(doc: MutableDocument, revisions: Int) {
        for (i in 0 until revisions) {
            doc.setValue("count", i)
            baseTestDb.save(doc)
        }
    }

    private fun updateDocWithGetDocument(document: MutableDocument, revisions: Int) {
        var doc = document
        for (i in 0 until revisions) {
            doc.setValue("count", i)
            baseTestDb.save(doc)
            doc = baseTestDb.getDocument("doc")!!.toMutable()
        }
    }

    private fun createDocumentWithTag(id: String?, tag: String): MutableDocument {
        val doc = id?.let { MutableDocument(it) } ?: MutableDocument()

        // Tag
        doc.setValue("tag", tag)

        // String
        doc.setValue("firstName", "Daniel")
        doc.setValue("lastName", "Tiger")

        // Dictionary:
        val address = MutableDictionary()
        address.setValue("street", "1 Main street")
        address.setValue("city", "Mountain View")
        address.setValue("state", "CA")
        doc.setValue("address", address)

        // Array:
        val phones = MutableArray()
        phones.addValue("650-123-0001")
        phones.addValue("650-123-0002")
        doc.setValue("phones", phones)

        // Date:
        doc.setValue("updated", Clock.System.now())
        return doc
    }

    private fun createAndSaveDocument(id: String, tag: String) {
        val doc = createDocumentWithTag(id, tag)
        baseTestDb.save(doc)
    }

    private fun createAndSaveDocument(tag: String, nDocs: Int) {
        for (i in 0 until nDocs) {
            val docID = "doc-${i.paddedString(10)}"
            createAndSaveDocument(docID, tag)
        }
    }

    private fun updateDoc(document: Document?, rounds: Int, tag: String) {
        var doc = document
        for (i in 1..rounds) {
            val mDoc = doc!!.toMutable()
            mDoc.setValue("update", i)
            mDoc.setValue("tag", tag)
            val address = mDoc.getDictionary("address")
            assertNotNull(address)
            val street = "$i street."
            address.setValue("street", street)
            mDoc.setDictionary("address", address)
            val phones = mDoc.getArray("phones")
            assertNotNull(phones)
            assertEquals(2, phones.count)
            val phone = "650-000-${i.paddedString(4)}"
            phones.setValue(0, phone)
            mDoc.setArray("phones", phones)
            mDoc.setValue("updated", Clock.System.now())
            doc = saveDocInBaseTestDb(mDoc)
        }
    }

    private fun verifyByTag(tag: String, verifier: Verifier) {
        var n = 0
        QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(baseTestDb))
            .where(Expression.property("tag").equalTo(Expression.string(tag)))
            .execute().use { rs ->
                for (row in rs) {
                    verifier.verify(++n, row)
                }
            }
    }

    private fun verifyByTag(tag: String, nRows: Int) {
        val count = atomic(0)
        verifyByTag(tag) { _, _ -> count.incrementAndGet() }
        assertEquals(nRows, count.value)
    }

    private fun timeTest(testName: String, maxTimeMs: Long, test: () -> Unit) {
        val t0 = Clock.System.now()
        test()
        val elapsedTime = Clock.System.now() - t0
        Report.log("Test $testName time: $elapsedTime")
        assertTrue(elapsedTime < maxTimeMs.milliseconds)
    }
}
