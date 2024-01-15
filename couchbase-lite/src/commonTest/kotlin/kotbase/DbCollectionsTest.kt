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

import kotlin.test.*

@OptIn(ExperimentalStdlibApi::class)
class DbCollectionsTest : BaseDbTest() {
    private val invalidChars = charArrayOf(
        '!', '@', '#', '$', '^', '&', '*', '(', ')', '+', '.', '<', '>', '?',
        '[', ']', '{', '}', '=', '“', '‘', '|', '\\', '/', '`', '~', ':', ';'
    )

    @Test
    fun testGetDefaultScope() {
        val scope = testDatabase.defaultScope
        assertNotNull(scope)
        assertTrue(testDatabase.scopes.contains(scope))
        assertEquals(Scope.DEFAULT_NAME, scope.name)
        assertEquals(1, scope.collectionCount)
        assertNotNull(scope.getCollection(Collection.DEFAULT_NAME))
    }

    @Test
    fun testGetDefaultCollection() {
        val col = testDatabase.defaultCollection
        assertNotNull(col)
        assertEquals(Collection.DEFAULT_NAME, col.name)
        assertEquals(col, testDatabase.getCollection(Collection.DEFAULT_NAME))
        assertTrue(testDatabase.getCollections().contains(col))
        assertNotNull(col.scope)
        assertEquals(Scope.DEFAULT_NAME, col.scope.name)
        assertEquals(0, col.count)
    }

    // Test that collections can be created and accessed from the default scope
    @Test
    fun testCreateCollectionInDefaultScope() {
        //name with valid characters
        testDatabase.createCollection("chintz")
        // collection names should be case sensitive
        testDatabase.createCollection("Chintz")
        testDatabase.createCollection("6hintz")
        testDatabase.createCollection("-Ch1ntz")

        val scope = testDatabase.defaultScope
        assertEquals(5, scope.collectionCount)
        assertNotNull(scope.getCollection("chintz"))
        assertNotNull(scope.getCollection("Chintz"))
        assertNotNull(scope.getCollection("6hintz"))
        assertNotNull(scope.getCollection("-Ch1ntz"))

        // collections exists when calling from database
        assertNotNull(testDatabase.getCollection("chintz"))
        assertNotNull(testDatabase.getCollection("Chintz"))
        assertNotNull(testDatabase.getCollection("6hintz"))
        assertNotNull(testDatabase.getCollection("-Ch1ntz"))
    }


    @Test
    fun testCollectionNameStartsWithIllegalChars1() {
        assertThrowsCBLException(CBLError.Domain.CBLITE, CBLError.Code.INVALID_PARAMETER) {
            testDatabase.createCollection("_notvalid")
        }
    }

    @Test
    fun testCollectionNameStartsWithIllegalChars2() {
        assertThrowsCBLException(CBLError.Domain.CBLITE, CBLError.Code.INVALID_PARAMETER) {
            testDatabase.createCollection("%notvalid")
        }
    }

    @Test
    fun testCollectionNameContainingIllegalChars() {
        for (c in invalidChars) {
            val colName = "notval" + c + "d"
            try {
                testDatabase.createCollection(colName)
                fail("Expect CBL Exception for collection : $colName")
            } catch (ignore: CouchbaseLiteException) {
            }
        }
    }

    @Test
    fun testCreateCollectionNameLength252() {
        val name =
            "fhndlbjgjyggvvnreutzuzyzszqiqmbqbegudyvdzvenpybjuayxssmipnpjysyfldhjmyyjmzxhegjjqwfrgzkwbiepqbvwbijcifvqamanpmiqydqpcqgubyputmrjiulrjxbayzpxqbxsaszkdxdobhreeqorlmfeoukbspfocymiucffsvioqmvqpqnpvdhpbnenkppfogruvdrrhiaalcfijifapsjqpjuwmlkkrxohvgxoqumkktipsqpsgrqidtcdeadnanxlhbivyvqkdxprsjybvuhjolkpaswlkgtiz"
        assertThrowsCBLException(CBLError.Domain.CBLITE, CBLError.Code.INVALID_PARAMETER) {
            testDatabase.createCollection(name)
        }
    }

    @Test
    fun testCreateCollectionInNamedScope() {
        testDatabase.createCollection("chintz", "micro")
        testDatabase.createCollection("chintz", "3icro")
        testDatabase.createCollection("chintz", "-micro")

        var scope: Scope? = testDatabase.defaultScope
        assertEquals(1, scope?.collectionCount)

        // get non-existing collection returns null
        assertNull(scope?.getCollection("chintz"))
        assertNull(testDatabase.getCollection("chintz"))

        scope = testDatabase.getScope("micro")
        assertEquals(1, scope?.collectionCount)
        assertNotNull(scope?.getCollection("chintz"))

        scope = testDatabase.getScope("3icro")
        assertEquals(1, scope?.collectionCount)
        assertNotNull(scope?.getCollection("chintz"))


        scope = testDatabase.getScope("-micro")
        assertEquals(1, scope?.collectionCount)
        assertNotNull(scope?.getCollection("chintz"))


        // collections exists when calling from database
        assertNotNull(testDatabase.getCollection("chintz", "micro"))
        assertNotNull(testDatabase.getCollection("chintz", "3icro"))
        assertNotNull(testDatabase.getCollection("chintz", "-micro"))
    }

    //Test that creating an existing collection returns an existing collection
    @Test
    fun testCreateAnExistingCollection() {
        //save doc in testCollection
        val doc = createDocInCollection()

        val col = testDatabase.createCollection(testCollection.name, testCollection.scope.name)

        // the copy collection has the same content as testCollection
        assertEquals(col, testCollection)
        assertNotNull(col.getDocument(doc.id))

        // updating the copy col also update the original one
        col.save(MutableDocument("doc2"))
        assertNotNull(testCollection.getDocument("doc2"))
    }


    @Test
    fun testScopeNameStartsWithIllegalChar1() {
        assertThrowsCBLException(CBLError.Domain.CBLITE, CBLError.Code.INVALID_PARAMETER) {
            testDatabase.createCollection("chintz", "_micro")
        }
    }

    @Test
    fun testScopeNameStartsWithIllegalChar2() {
        assertThrowsCBLException(CBLError.Domain.CBLITE, CBLError.Code.INVALID_PARAMETER) {
            testDatabase.createCollection("chintz", "%micro")
        }
    }

    @Test
    fun testScopeNameCaseSensitive() {
        testDatabase.createCollection("coll1", "scope1")
        val scope1 = testDatabase.getScope("scope1")

        testDatabase.createCollection("coll2", "Scope1")
        val scope2 = testDatabase.getScope("Scope1")

        assertNotNull(scope1)
        assertNotNull(scope2)
        assertEquals(scope1, testDatabase.getScope("scope1"))
        assertNotSame(scope1, scope2)
    }

    @Test
    fun testGetScopes() {
        val scopes = testDatabase.scopes
        assertEquals(2, scopes.size)

        var scope = scopes.first { it.name == Scope.DEFAULT_NAME }
        assertNotNull(scope.getCollection(Scope.DEFAULT_NAME))

        scope = scopes.first { it.name == testCollection.scope.name }
        assertNotNull(scope.getCollection(testCollection.name))
    }

    @Test
    fun testDeleteCollectionFromNamedScope() {
        var scopes = testDatabase.scopes
        assertEquals(2, scopes.size)

        testDatabase.deleteCollection(testCollection.name, testCollection.scope.name)

        scopes = testDatabase.scopes
        assertEquals(1, scopes.size)

        val recreateCol = testDatabase.createCollection(testCollection.name, testCollection.scope.name)
        assertNotNull(recreateCol)
    }

    @Test
    fun testDeleteDefaultCollection() {
        val scopes = testDatabase.scopes

        // scopes should have a default scope and a non default test scope created in BaseDbTest
        assertEquals(2, scopes.size)

        val scope = testDatabase.defaultScope
        assertEquals(1, scope.collectionCount)

        assertThrowsCBLException(CBLError.Domain.CBLITE, CBLError.Code.INVALID_PARAMETER) {
            testDatabase.deleteCollection(Collection.DEFAULT_NAME, Scope.DEFAULT_NAME)
        }

        // Creating the default collection just returns it
        assertEquals(
            testDatabase.defaultScope.getCollection(Collection.DEFAULT_NAME),
            testDatabase.createCollection(Collection.DEFAULT_NAME, Scope.DEFAULT_NAME))
    }

    // When deleting all collections in non-default scope, the scope will be deleted
    @Test
    fun testDeleteAllCollectionsInNamedScope() {
        testDatabase.deleteCollection(testCollection.name, testCollection.scope.name)
        assertNull(testDatabase.getScope(testCollection.name))
        assertEquals(setOf(testDatabase.defaultScope), testDatabase.scopes)
    }

    @Test
    fun testScopeNameContainingIllegalChars() {
        for (c in invalidChars) {
            val scopeName = "notval${c}d"
            try {
                testDatabase.createCollection("col", scopeName)
                fail("Expect CBL Exception for scope : $scopeName")
            } catch (ignore: CouchbaseLiteException) {
            }
        }
    }

    @Test
    fun testCreateScopeNameLength252() {
        val name =
            "fhndlbjgjyggvvnreutzuzyzszqiqmbqbegudyvdzvenpybjuayxssmipnpjysyfldhjmyyjmzxhegjjqwfrgzkwbiepqbvwbijcifvqamanpmiqydqpcqgubyputmrjiulrjxbayzpxqbxsaszkdxdobhreeqorlmfeoukbspfocymiucffsvioqmvqpqnpvdhpbnenkppfogruvdrrhiaalcfijifapsjqpjuwmlkkrxohvgxoqumkktipsqpsgrqidtcdeadnanxlhbivyvqkdxprsjybvuhjolkpaswlkgtiz"
        assertThrowsCBLException(CBLError.Domain.CBLITE, CBLError.Code.INVALID_PARAMETER) {
            testDatabase.createCollection("col", name)
        }
    }

    /**
     * Collections and Cross Database instance
     */

    @Test
    fun testCreateThenGetCollectionFromDifferentDatabaseInstance() {
        duplicateDb(testDatabase).use { otherDb ->
            testDatabase.createCollection("testColl")
            val collection = otherDb.getCollection("testColl")
            assertNotNull(collection)

            //delete coll from a db
            testDatabase.deleteCollection("testColl")
            assertNull(testDatabase.getCollection("testColl"))
            assertNull(otherDb.getCollection("testColl"))

            //recreate collection
            testDatabase.createCollection("testColl")
            val collectionRecreated = otherDb.getCollection("testColl")
            assertNotSame(collectionRecreated, collection)
        }
    }

    @Test
    fun testCreateCollectionFromDifferentDatabase() {
        //open a new db
        val newDB = createDb("different_db")
        try {
            assertNull(newDB.getCollection(testCollection.name, testCollection.scope.name))
        } finally {
            eraseDb(newDB)
        }
    }

    /* Use APIs on Collection when collection is deleted */
    @Test
    fun testGetScopeFromDeletedCollection() {
        val scopeName = testCollection.scope.name
        testDatabase.deleteCollection(testCollection.name, scopeName)
        assertEquals(scopeName, testCollection.scope.name)
    }

    @Test
    fun testGetColNameFromDeletedCollection() {
        val collectionName = testCollection.name
        testDatabase.deleteCollection(collectionName, testCollection.scope.name)
        assertEquals(collectionName, testCollection.name)
    }

    // Test get scope from a collection that is deleted from a different database instance
    @Test
    fun testGetScopeAndNameFromCollectionFromDifferentDBInstance() {
        val collectionName = testCollection.name
        val otherDb = duplicateDb(testDatabase)
        otherDb.use {
            val collection = otherDb.getCollection(collectionName, testCollection.scope.name)
            assertNotNull(collection)

            otherDb.deleteCollection(testCollection.name, testCollection.scope.name)
            assertNull(otherDb.getCollection(testCollection.name, testCollection.scope.name))

            //get from original collection
            assertNotNull(testCollection.scope)
            assertEquals(collectionName, testCollection.name)
        }
    }

    // Test getting scope, and collection name from a collection when database is closed returns the scope and name
    @Test
    fun testGetScopeAndCollectionNameFromAClosedDatabase() {
        val collectionName = testCollection.name
        testDatabase.close()
        assertNotNull(testCollection.scope)
        assertEquals(collectionName, testCollection.name)
    }

    // Test getting scope, and collection name from a collection when database is deleted returns the scope and name
    @Test
    fun testGetScopeAndCollectionNameFromADeletedDatabase() {
        val collectionName = testCollection.name
        testDatabase.delete()
        assertNotNull(testCollection.scope)
        assertEquals(collectionName, testCollection.name)
    }
}

