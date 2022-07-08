//// TODO:
//package com.couchbase.lite.kmm
//
//import kotlin.test.*
//
//class DbCollectionsTest : BaseCollectionTest() {
//
//    @Test
//    fun testGetDefaultScope() {
//        val scope = baseTestDb.defaultScope
//        assertEquals(Scope.DEFAULT_NAME, scope.name)
//        assertEquals(1, scope.collectionCount)
//        assertNotNull(scope.getCollection(Collection.DEFAULT_NAME))
//    }
//
//    @Ignore("CouchbaseLiteException: duplicate column when creating collection Chintz")
//    @Test
//    //create valid collections
//    fun testCreateCollectionInDefaultScope() {
//        //name with valid characters
//        baseTestDb.createCollection("chintz")
//        baseTestDb.createCollection("Chintz") //collection is case sensitive, this collection should be created independently from chintz
//        baseTestDb.createCollection("6hintz")
//        baseTestDb.createCollection("-Ch1ntz")
//
//        val scope = baseTestDb.defaultScope
//        assertEquals(5, scope.collectionCount)
//        assertNotNull(scope.getCollection("chintz"))
//        assertNotNull(scope.getCollection("Chintz"))
//        assertNotNull(scope.getCollection("6hintz"))
//        assertNotNull(scope.getCollection("-Ch1ntz"))
//    }
//
//
//    @Test(expected = CouchbaseLiteException::class)
//    fun testCollectionNameStartWithIllegalChars1() {
//        baseTestDb.createCollection("_notvalid")
//    }
//
//    @Test(expected = CouchbaseLiteException::class)
//    fun tesCollectionNameStartWithIllegalChars2() {
//        baseTestDb.createCollection("%notvalid")
//    }
//
//    @Test(expected = CouchbaseLiteException::class)
//    fun testCollectionNameContainingIllegalChars(){
//        baseTestDb.createCollection("notval!d")
//    }
//
//    @Test
//    fun testCreateCollectionInNamedScope() {
//        baseTestDb.createCollection("chintz", "micro")
//        baseTestDb.createCollection("chintz", "3icro")
//        baseTestDb.createCollection("chintz", "-micro")
//
//        var scope: Scope? = baseTestDb.defaultScope
//        assertEquals(1, scope?.collectionCount)
//        assertNull(scope?.getCollection("chintz"))
//
//        scope = baseTestDb.getScope("micro")
//        assertEquals(1, scope?.collectionCount)
//        assertNotNull(scope?.getCollection("chintz"))
//
//        scope = baseTestDb.getScope("3icro")
//        assertEquals(1, scope?.collectionCount)
//        assertNotNull(scope?.getCollection("chintz"))
//
//
//        scope = baseTestDb.getScope("-micro")
//        assertEquals(1, scope?.collectionCount)
//        assertNotNull(scope?.getCollection("chintz"))
//
//    }
//
//    @Test(expected = CouchbaseLiteException::class)
//    fun testScopeNameWithIllegalChar1(){
//        baseTestDb.createCollection("chintz", "_micro")
//    }
//
//    @Test(expected = CouchbaseLiteException::class)
//    fun testScopeNameWithIllegalChar2(){
//        baseTestDb.createCollection("chintz", "%micro")
//    }
//
//    @Test
//    fun testScopeNameCaseSensitive(){
//        baseTestDb.createCollection("coll1", "scope1")
//        val scope1 = baseTestDb.getScope("scope1")
//
//        baseTestDb.createCollection("coll2", "Scope1")
//        val scope2 = baseTestDb.getScope("Scope1")
//
//        assertNotNull(scope1)
//        assertNotNull(scope2)
//        assertEquals(scope1, baseTestDb.getScope("scope1"))
//        assertNotSame(scope1, scope2)
//    }
//
//    @Test
//    fun testGetScopes() {
//        baseTestDb.createCollection("pezDispenser", "tele")
//
//        val scopes = baseTestDb.scopes
//        assertEquals(2, scopes.size)
//
//        var scope = scopes.first { it.name == Scope.DEFAULT_NAME }
//        assertNotNull(scope.getCollection(Scope.DEFAULT_NAME))
//
//        scope = scopes.first { it.name == "tele" }
//        assertNotNull(scope.getCollection("pezDispenser"))
//    }
//
//    @Test
//    fun testDeleteCollectionFromNamedScope() {
//        baseTestDb.createCollection("pezDispenser", "tele")
//
//        var scopes = baseTestDb.scopes
//        assertEquals(2, scopes.size)
//
//        baseTestDb.deleteCollection("pezDispenser", "tele")
//
//        scopes = baseTestDb.scopes
//        assertEquals(1, scopes.size)
//    }
//
//
//// !!! TESTS BELOW NEED TO BE MOVED TO CollectionTest
//
//    @Ignore("CBL-3257: getScopeNames does not return default scope when it is empty")
//    @Test
//    fun testDeleteDefaultCollection() {
//        var scopes = baseTestDb.scopes
//        assertEquals(1, scopes.size)
//
//        var scope = baseTestDb.defaultScope
//        assertEquals(1, scope.collectionCount)
//
//        baseTestDb.deleteCollection(Collection.DEFAULT_NAME)
//
//        // The default collection should not go away when it is empty
//        scopes = baseTestDb.scopes
//        assertEquals(1, scopes.size)
//
//        scope = baseTestDb.defaultScope
//        assertEquals(0, scope.collectionCount)
//    }
//
//    @Test(expected = IllegalStateException::class)
//    fun testPurgeDocOnDeletedDB() {
//        // Store doc:
//        val doc = createSingleDocInBaseTestDb("doc1")
//
//        // Close db:
//        baseTestDb.close()
//
//        // Purge doc:
//        baseTestDb.purge(doc)
//    }
//
//    @Test
//    fun testSaveDocWithConflictLastWriteWins() {
//        val doc = MutableDocument("doc1")
//        doc.setString("firstName", "Daniel")
//        doc.setString("lastName", "Tiger")
//        baseTestDb.save(doc)
//
//        // Get two doc1 document objects (doc1a and doc1b):
//        val doc1a = baseTestDb.getDocument("doc1")!!.toMutable()
//        val doc1b = baseTestDb.getDocument("doc1")!!.toMutable()
//
//        // Modify doc1a:
//        doc1a.setString("firstName", "Scott")
//        baseTestDb.save(doc1a)
//        doc1a.setString("nickName", "Scotty")
//        baseTestDb.save(doc1a)
//
//        val expected: MutableMap<String, Any> = HashMap()
//        expected["firstName"] = "Scott"
//        expected["lastName"] = "Tiger"
//        expected["nickName"] = "Scotty"
//        assertEquals(expected, doc1a.toMap())
//        assertEquals(3, doc1a.sequence)
//
//        // Modify doc1b, result to conflict when save:
//        doc1b.setString("lastName", "Lion")
//        Assert.assertTrue(baseTestDb.save(doc1b, ConcurrencyControl.LAST_WRITE_WINS))
//        val savedDoc = baseTestDb.getDocument(doc.id)
//        assertEquals(doc1b.toMap(), savedDoc!!.toMap())
//        assertEquals(4, savedDoc.sequence)
//
//        recreateBastTestDb()
//    }
//
//    @Test
//    fun testSaveDocWithConflictFailOnConflict() {
//        val doc = MutableDocument("doc1")
//        doc.setString("firstName", "Daniel")
//        doc.setString("lastName", "Tiger")
//        baseTestDb.save(doc)
//
//        // Get two doc1 document objects (doc1a and doc1b):
//        val doc1a = baseTestDb.getDocument("doc1")!!.toMutable()
//        val doc1b = baseTestDb.getDocument("doc1")!!.toMutable()
//
//        // Modify doc1a:
//        doc1a.setString("firstName", "Scott")
//        baseTestDb.save(doc1a)
//        doc1a.setString("nickName", "Scotty")
//        baseTestDb.save(doc1a)
//
//        val expected: MutableMap<String, Any> = HashMap()
//        expected["firstName"] = "Scott"
//        expected["lastName"] = "Tiger"
//        expected["nickName"] = "Scotty"
//        assertEquals(expected, doc1a.toMap())
//        assertEquals(3, doc1a.sequence)
//
//        // Modify doc1b, result to conflict when save:
//        doc1b.setString("lastName", "Lion")
//        Assert.assertFalse(baseTestDb.save(doc1b, ConcurrencyControl.FAIL_ON_CONFLICT))
//        val savedDoc = baseTestDb.getDocument(doc.id)
//        assertEquals(expected, savedDoc!!.toMap())
//        assertEquals(3, savedDoc.sequence)
//
//        recreateBastTestDb()
//    }
//
//    @Test
//    fun testDeleteDocWithConflictLastWriteWins() {
//        val doc = MutableDocument("doc1")
//        doc.setString("firstName", "Daniel")
//        doc.setString("lastName", "Tiger")
//        baseTestDb.save(doc)
//
//        // Get two doc1 document objects (doc1a and doc1b):
//        val doc1a = baseTestDb.getDocument("doc1")!!.toMutable()
//        val doc1b = baseTestDb.getDocument("doc1")!!.toMutable()
//
//        // Modify doc1a:
//        doc1a.setString("firstName", "Scott")
//        baseTestDb.save(doc1a)
//
//        val expected: MutableMap<String, Any> = HashMap()
//        expected["firstName"] = "Scott"
//        expected["lastName"] = "Tiger"
//        assertEquals(expected, doc1a.toMap())
//        assertEquals(2, doc1a.sequence)
//
//        // Modify doc1b and delete, result to conflict when delete:
//        doc1b.setString("lastName", "Lion")
//        Assert.assertTrue(baseTestDb.delete(doc1b, ConcurrencyControl.LAST_WRITE_WINS))
//        assertEquals(3, doc1b.sequence)
//        assertNull(baseTestDb.getDocument(doc1b.id))
//
//        recreateBastTestDb()
//    }
//
//    @Test
//    fun testDeleteDocWithConflictFailOnConflict() {
//        val doc = MutableDocument("doc1")
//        doc.setString("firstName", "Daniel")
//        doc.setString("lastName", "Tiger")
//        baseTestDb.save(doc)
//
//        // Get two doc1 document objects (doc1a and doc1b):
//        val doc1a = baseTestDb.getDocument("doc1")!!.toMutable()
//        val doc1b = baseTestDb.getDocument("doc1")!!.toMutable()
//
//        // Modify doc1a:
//        doc1a.setString("firstName", "Scott")
//        baseTestDb.save(doc1a)
//
//        val expected: MutableMap<String, Any> = HashMap()
//        expected["firstName"] = "Scott"
//        expected["lastName"] = "Tiger"
//        assertEquals(expected, doc1a.toMap())
//        assertEquals(2, doc1a.sequence)
//
//        // Modify doc1b and delete, result to conflict when delete:
//        Assert.assertFalse(baseTestDb.delete(doc1b, ConcurrencyControl.FAIL_ON_CONFLICT))
//        val savedDoc = baseTestDb.getDocument(doc.id)
//        assertEquals(expected, savedDoc!!.toMap())
//        assertEquals(2, savedDoc.sequence)
//
//        recreateBastTestDb()
//    }
//
//    @Test
//    fun testSaveDocWithNoParentConflictLastWriteWins() {
//        val doc1a = MutableDocument("doc1")
//        doc1a.setString("firstName", "Daniel")
//        doc1a.setString("lastName", "Tiger")
//        baseTestDb.save(doc1a)
//
//        var savedDoc = baseTestDb.getDocument(doc1a.id)
//        assertEquals(doc1a.toMap(), savedDoc!!.toMap())
//        assertEquals(1, savedDoc.sequence)
//
//        val doc1b = MutableDocument("doc1")
//        doc1b.setString("firstName", "Scott")
//        doc1b.setString("lastName", "Tiger")
//
//        assertTrue(baseTestDb.save(doc1b, ConcurrencyControl.LAST_WRITE_WINS))
//        savedDoc = baseTestDb.getDocument(doc1b.id)
//        assertEquals(doc1b.toMap(), savedDoc!!.toMap())
//        assertEquals(2, savedDoc.sequence)
//
//        recreateBastTestDb()
//    }
//
//    @Test
//    fun testSaveDocWithNoParentConflictFailOnConflict() {
//        val doc1a = MutableDocument("doc1")
//        doc1a.setString("firstName", "Daniel")
//        doc1a.setString("lastName", "Tiger")
//        baseTestDb.save(doc1a)
//
//        var savedDoc = baseTestDb.getDocument(doc1a.id)
//        assertEquals(doc1a.toMap(), savedDoc!!.toMap())
//        assertEquals(1, savedDoc.sequence)
//
//        val doc1b = MutableDocument("doc1")
//        doc1b.setString("firstName", "Scott")
//        doc1b.setString("lastName", "Tiger")
//
//        assertFalse(baseTestDb.save(doc1b, ConcurrencyControl.FAIL_ON_CONFLICT))
//        savedDoc = baseTestDb.getDocument(doc1b.id)
//        assertEquals(doc1a.toMap(), savedDoc!!.toMap())
//        assertEquals(1, savedDoc.sequence)
//
//        recreateBastTestDb()
//    }
//
//    @Test
//    fun testSaveDocWithDeletedConflictLastWriteWins() {
//        val doc = MutableDocument("doc1")
//        doc.setString("firstName", "Daniel")
//        doc.setString("lastName", "Tiger")
//        baseTestDb.save(doc)
//
//        // Get two doc1 document objects (doc1a and doc1b):
//        val doc1a = baseTestDb.getDocument("doc1")
//        val doc1b = baseTestDb.getDocument("doc1")!!.toMutable()
//
//        // Delete doc1a:
//        baseTestDb.delete(doc1a!!)
//        assertEquals(2, doc1a.sequence)
//        assertNull(baseTestDb.getDocument(doc.id))
//
//        // Modify doc1b, result to conflict when save:
//        doc1b.setString("lastName", "Lion")
//        assertTrue(baseTestDb.save(doc1b, ConcurrencyControl.LAST_WRITE_WINS))
//        val savedDoc = baseTestDb.getDocument(doc.id)
//        assertEquals(doc1b.toMap(), savedDoc!!.toMap())
//        assertEquals(3, savedDoc.sequence)
//
//        recreateBastTestDb()
//    }
//
//    @Test
//    fun testSaveDocWithDeletedConflictFailOnConflict() {
//        val doc = MutableDocument("doc1")
//        doc.setString("firstName", "Daniel")
//        doc.setString("lastName", "Tiger")
//        baseTestDb.save(doc)
//
//        // Get two doc1 document objects (doc1a and doc1b):
//        val doc1a = baseTestDb.getDocument("doc1")
//        val doc1b = baseTestDb.getDocument("doc1")!!.toMutable()
//
//        // Delete doc1a:
//        baseTestDb.delete(doc1a!!)
//        assertEquals(2, doc1a.sequence)
//        assertNull(baseTestDb.getDocument(doc.id))
//
//        // Modify doc1b, result to conflict when save:
//        doc1b.setString("lastName", "Lion")
//        assertFalse(baseTestDb.save(doc1b, ConcurrencyControl.FAIL_ON_CONFLICT))
//        assertNull(baseTestDb.getDocument(doc.id))
//
//        recreateBastTestDb()
//    }
//
//    /**
//     * Collections and Cross Database instance
//     */
//
//    @Test
//    fun testCreateThenGetCollectionFromDifferentDatabaseInstance(){
//        val otherDb = duplicateDb(baseTestDb)
//        try {
//            baseTestDb.createCollection("testColl")
//            val collection = otherDb.getCollection("testColl")
//            assertNotNull(collection)
//
//            //delete coll from a db
//            baseTestDb.deleteCollection("testColl")
//            assertNull(baseTestDb.getCollection("testColl"))
//            assertNull(otherDb.getCollection("testColl"))
//
//            //recreate collection
//            baseTestDb.createCollection("testColl")
//            val collectionRecreated = otherDb.getCollection("testColl")
//            assertNotSame(collectionRecreated, collection)
//        } finally {
//            closeDb(otherDb);
//        }
//    }
//}
