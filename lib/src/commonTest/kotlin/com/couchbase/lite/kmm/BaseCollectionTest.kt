//// TODO:
//package com.couchbase.lite.kmm
//
//import com.couchbase.lite.kmm.internal.utils.Report
//import java.util.*
//import kotlin.test.AfterTest
//import kotlin.test.BeforeTest
//import kotlin.test.assertEquals
//import kotlin.test.assertNotNull
//
//open class BaseCollectionTest : BaseDbTest() {
//
//    private var testScope: Scope? = null
//    private var testCollection: Collection? = null
//
//    protected val Scope.collectionCount
//        get() = this.collections.size
//
//    @BeforeTest
//    fun setUpBaseCollectionTest() {
//        testScope = baseTestDb.defaultScope
//        testCollection = testScope!!.getCollection(Collection.DEFAULT_NAME)
//        Report.log(LogLevel.INFO, "Created base test Collection: $testCollection")
//    }
//
//    @AfterTest
//    fun tearDownBaseCollectionTest() {
//        val collectionName = if (testCollection == null) Collection.DEFAULT_NAME else testCollection!!.name
//        // don't delete the default collection
//        if (Collection.DEFAULT_NAME != collectionName) {
//            baseTestDb.deleteCollection(collectionName)
//            Report.log(LogLevel.INFO, "Deleted testCollection: $testCollection")
//        }
//    }
//
//    @Throws(CouchbaseLiteException::class)
//    protected fun createSingleDocInCollectionWithId(docID: String?): Document {
//        val n = testCollection!!.count
//        val doc = MutableDocument(docID)
//        doc.setValue("key", 1)
//        val savedDoc = saveDocInBaseCollectionTest(doc)
//        assertEquals(n + 1, testCollection!!.count)
//        assertEquals(1, savedDoc.sequence)
//        return savedDoc
//    }
//
//    @Throws(CouchbaseLiteException::class)
//    protected fun saveDocInBaseCollectionTest(doc: MutableDocument): Document {
//        testCollection!!.save(doc)
//        val savedDoc = testCollection!!.getDocument(doc.id)
//        assertNotNull(savedDoc)
//        assertEquals(doc.id, savedDoc!!.id)
//        return savedDoc
//    }
//}
