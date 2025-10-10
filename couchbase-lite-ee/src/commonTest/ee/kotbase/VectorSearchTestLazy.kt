/*
 * Copyright 2025 Jeff Lockhart
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

import kotbase.ext.toStringMillis
import kotbase.internal.utils.PlatformUtils
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

// VectorSearchTest+Lazy.swift
// VectorSearchTest+Lazy.m

/**
 * Test Spec :
 * https://github.com/couchbaselabs/couchbase-lite-api/blob/master/spec/tests/T0002-Lazy-Vector-Index.md
 *
 * Vesion: 2.0.1
 */
class VectorSearchTestLazy : BaseVectorSearchTest() {
    /// Override the default VectorSearch Expression
    override fun wordsQueryDefaultExpression(): String =
        "word"

    fun wordsIndex(): QueryIndex {
        val index = wordsCollection.getIndex(wordsIndexName)
        assertNotNull(index)
        return index
    }

    fun lazyConfig(config: VectorIndexConfiguration): VectorIndexConfiguration {
        config.isLazy = true
        return config
    }

    fun vector(word: String): List<Float>? {
        val model = WordEmbeddingModel(wordDB)
        model.getWordVector(word, wordsCollectionName)?.let { vector ->
            @Suppress("UNCHECKED_CAST")
            return vector.toList() as List<Float>
        }
        model.getWordVector(word, extWordsCollectionName)?.let { vector ->
            @Suppress("UNCHECKED_CAST")
            return vector.toList() as List<Float>
        }
        return null
    }

    /**
     * 1. TestIsLazyDefaultValue
     *
     * Description
     * Test that isLazy property is false by default.
     *
     * Steps
     * 1. Create a VectorIndexConfiguration object.
     *     - expression: “vector”
     *     - dimensions: 300
     *     - centroids : 20
     * 2. Check that isLazy returns false.
     */
    @Test
    fun testIsLazyDefaultValue() {
        val config = VectorIndexConfiguration(expression = "vector", dimensions = 300, centroids = 20)
        assertFalse(config.isLazy)
    }

    /**
     * 2. TestIsLazyAccessor
     *
     * Description
     * Test that isLazy getter/setter of the VectorIndexConfiguration work as expected.
     *
     * Steps
     * 1. Create a VectorIndexConfiguration object.
     *    - expression: word
     *    - dimensions: 300
     *    - centroids : 20
     * 2. Set isLazy to true
     * 3. Check that isLazy returns true.
     */
    @Test
    fun testIsLazyAccessor() {
        val config = VectorIndexConfiguration(expression = "vector", dimensions = 300, centroids = 20)
        config.isLazy = true
        assertTrue(config.isLazy)
    }

    /**
     * 3. TestGetNonExistingIndex
     *
     * Description
     * Test that getting non-existing index object by name returning null.
     *
     * Steps
     * 1. Get the default collection from a test database.
     * 2. Get a QueryIndex object from the default collection with the name as
     *    "nonexistingindex".
     * 3. Check that the result is null.
     */
    @Test
    fun testGetNonExistingIndex() {
        val collection = testCollection
        val index = collection.getIndex("nonexistingindex")
        assertNull(index)
    }

    /**
     * 4. TestGetExistingNonVectorIndex
     *
     * Description
     * Test that getting non-existing index object by name returning an index object correctly.
     *
     * Steps
     * 1. Get the default collection from a test database.
     * 2. Create a value index named "value_index" in the default collection
     *   with the expression as "value".
     * 3. Get a QueryIndex object from the default collection with the name as
     *   "value_index".
     * 4. Check that the result is not null.
     * 5. Check that the QueryIndex's name is "value_index".
     * 6. Check that the QueryIndex's collection is the same instance that
     *   is used for getting the QueryIndex object.
     */
    @Test
    fun testGetExistingNonVectorIndex() {
        val item = ValueIndexItem.property("value")
        val vIndex = IndexBuilder.valueIndex(item)
        testCollection.createIndex("value_index", vIndex)
        val qIndex = testCollection.getIndex("value_index")
        assertNotNull(qIndex)
        assertEquals("value_index", qIndex.name)
        assertEquals(testCollection, qIndex.collection)
    }

    /**
     * 5. TestGetExistingVectorIndex
     *
     * Description
     * Test that getting an existing index object by name returning an index object correctly.
     *
     * Steps
     * 1. Copy database words_db.
     * 2. Create a vector index named "words_index" in the _default.words collection.
     *     - expression: "word"
     *     - dimensions: 300
     *     - centroids : 8
     * 3. Get a QueryIndex object from the words collection with the name as
     *    "words_index".
     * 4. Check that the result is not null.
     * 5. Check that the QueryIndex's name is "words_index".
     * 6. Check that the QueryIndex's collection is the same instance that is used for
     *   getting the index.
     */
    @Test
    fun testGetExistingVectorIndex() {
        val config = VectorIndexConfiguration(expression = "vector", dimensions = 300, centroids = 8)
        createWordsIndex(config)
        val index = wordsIndex()
        assertEquals(wordsIndexName, index.name)
        assertSame(wordsCollection, index.collection)
    }

    /**
     * 8. TestLazyVectorIndexNotAutoUpdatedChangedDocs
     *
     * Description
     * Test that the lazy index is lazy. The index will not be automatically
     * updated when the documents are created or updated.
     *
     * Steps
     * 1. Copy database words_db.
     * 2. Create a vector index named "words_index" in the _default.words collection.
     *     - expression: "word"
     *     - dimensions: 300
     *     - centroids : 8
     *     - isLazy : true
     * 3. Create an SQL++ query:
     *     - SELECT word
     *       FROM _default.words
     *       ORDER BY APPROX_VECTOR_DISTANCE(word, $dinnerVector)
     *       LIMIT 10
     * 4. Execute the query and check that 0 results are returned.
     * 5. Update the documents:
     *     - Create _default.words.word301 with the content from _default.extwords.word1
     *     - Update _default.words.word1 with the content from _default.extwords.word3
     * 6. Execute the same query and check that 0 results are returned.
     */
    @Test
    fun testLazyVectorIndexNotAutoUpdatedChangedDocs() {
        val config = VectorIndexConfiguration(expression = "word", dimensions = 300, centroids = 8)
        createWordsIndex(lazyConfig(config))

        executeWordsQuery(limit = 10, checkTraining = false).use { rs ->
            assertEquals(0, rs.allResults().size)
        }

        // Update docs:
        val extWord1 = extWordsCollection.getDocument("word1")!!
        val word301 = MutableDocument("word301", extWord1.toMap())
        wordsCollection.save(word301)

        val extWord3 = extWordsCollection.getDocument("word3")!!
        val word1 = wordsCollection.getDocument("word1")!!.toMutable()
        word1.setData(extWord3.toMap())
        wordsCollection.save(word1)

        executeWordsQuery(limit = 10, checkTraining = false).use { rs ->
            assertEquals(0, rs.allResults().size)
        }
    }

    /**
     * 9. TestLazyVectorIndexAutoUpdateDeletedDocs
     *
     * Description
     * Test that when the lazy vector index automatically update when documents are
     * deleted.
     *
     * Steps
     * 1. Copy database words_db.
     * 2. Create a vector index named "words_index" in the _default.words collection.
     *     - expression: "word"
     *     - dimensions: 300
     *     - centroids : 8
     *     - isLazy : true
     * 3. Call beginUpdate() with limit 1 to get an IndexUpdater object.
     * 4. Check that the IndexUpdater is not null and IndexUpdater.count = 1.
     * 5. With the IndexUpdater object:
     *    - Get the word string from the IndexUpdater.
     *    - Query the vector by word from the _default.words collection.
     *    - Convert the vector result which is an array object to a platform's float array.
     *    - Call setVector() with the platform's float array at the index.
     *    - Call finish()
     * 6. Create an SQL++ query:
     *    - SELECT word
     *      FROM _default.words
     *      ORDER BY APPROX_VECTOR_DISTANCE(word, $dinnerVector)
     *      LIMIT 300
     * 7. Execute the query and check that 1 results are returned.
     * 8. Check that the word gotten from the query result is the same as the word in Step 5.
     * 9. Delete _default.words.word1 doc.
     * 10. Execute the same query as Step again and check that 0 results are returned.
     */
    @Test
    fun testLazyVectorIndexAutoUpdateDeletedDocs() {
        val config = VectorIndexConfiguration(expression = "word", dimensions = 300, centroids = 8)
        createWordsIndex(lazyConfig(config))

        val index = wordsIndex()
        val updater = index.beginUpdate(limit = 1)
        assertNotNull(updater)
        assertEquals(1, updater.count)

        // Update Index:
        val word = updater[0].string!!
        val vector = vector(word)
        updater.setVector(vector, 0)
        updater.finish()

        // Query:
        executeWordsQuery(limit = 300, checkTraining = false).use { rs ->
            assertEquals(1, rs.allResults().size)
        }

        // Delete doc and requery:
        wordsCollection.delete(wordsCollection.getDocument("word1")!!)
        executeWordsQuery(limit = 300, checkTraining = false).use { rs ->
            assertEquals(0, rs.allResults().size)
        }
    }

    /**
     * 10. TestLazyVectorIndexAutoUpdatePurgedDocs
     *
     * Description
     * Test that when the lazy vector index automatically update when documents are
     * purged.
     *
     * Steps
     * 1. Copy database words_db.
     * 2. Create a vector index named "words_index" in the _default.words collection.
     *     - expression: "word"
     *     - dimensions: 300
     *     - centroids : 8
     *     - isLazy : true
     * 3. Call beginUpdate() with limit 1 to get an IndexUpdater object.
     * 4. Check that the IndexUpdater is not null and IndexUpdater.count = 1.
     * 5. With the IndexUpdater object:
     *    - Get the word string from the IndexUpdater.
     *    - Query the vector by word from the _default.words collection.
     *    - Convert the vector result which is an array object to a platform's float array.
     *    - Call setVector() with the platform's float array at the index.
     * 6. With the IndexUpdater object, call finish()
     * 7. Create an SQL++ query:
     *    - SELECT word
     *      FROM _default.words
     *      ORDER BY APPROX_VECTOR_DISTANCE(word, $dinnerVector)
     *      LIMIT 300
     * 8. Execute the query and check that 1 results are returned.
     * 9. Check that the word gotten from the query result is the same as the word in Step 5.
     * 10. Purge _default.words.word1 doc.
     * 11. Execute the same query as Step again and check that 0 results are returned.
     */
    @Test
    fun testLazyVectorIndexAutoUpdatePurgedDocs() {
        val config = VectorIndexConfiguration(expression = "word", dimensions = 300, centroids = 8)
        createWordsIndex(lazyConfig(config))

        val index = wordsIndex()
        val updater = index.beginUpdate(limit = 1)
        assertNotNull(updater)
        assertEquals(1, updater.count)

        // Update Index:
        val word = updater[0].string!!
        val vector = vector(word)
        updater.setVector(vector, 0)
        updater.finish()

        // Query:
        executeWordsQuery(limit = 300, checkTraining = false).use { rs ->
            assertEquals(1, rs.allResults().size)
        }

        // Delete doc and requery:
        wordsCollection.purge("word1")
        executeWordsQuery(limit = 300, checkTraining = false).use { rs ->
            assertEquals(0, rs.allResults().size)
        }
    }

    /**
     * 11. TestIndexUpdaterBeginUpdateOnNonVectorIndex
     *
     * Description
     * Test that a CouchbaseLiteException is thrown when calling beginUpdate on
     * a non vector index.
     *
     * Steps
     * 1. Get the default collection from a test database.
     * 2. Create a value index named "value_index" in the default collection with the
     *   expression as "value".
     * 3. Get a QueryIndex object from the default collection with the name as
     *   "value_index".
     * 4. Call beginUpdate() with limit 10 on the QueryIndex object.
     * 5. Check that a CouchbaseLiteException with the code Unsupported is thrown.
     */
    @Test
    fun testIndexUpdaterBeginUpdateOnNonVectorIndex() {
        val item = ValueIndexItem.property("value")
        val vIndex = IndexBuilder.valueIndex(item)
        testCollection.createIndex("value_index", vIndex)

        val qIndex = testCollection.getIndex("value_index")!!

        assertThrowsCBLException(CBLError.Domain.CBLITE, CBLError.Code.UNSUPPORTED) {
            qIndex.beginUpdate(limit = 10)
        }
    }

    /**
     * 12. TestIndexUpdaterBeginUpdateOnNonLazyVectorIndex
     *
     * Description
     * Test that a CouchbaseLiteException is thrown when calling beginUpdate
     * on a non lazy vector index.
     *
     * Steps
     * 1. Copy database words_db.
     * 2. Create a vector index named "words_index" in the _default.words collecti
     *     - expression: "word"
     *     - dimensions: 300
     *     - centroids : 8
     * 3. Get a QueryIndex object from the words collection with the name as
     *    "words_index".
     * 4. Call beginUpdate() with limit 10 on the QueryIndex object.
     * 5. Check that a CouchbaseLiteException with the code Unsupported is thrown.
     */
    @Test
    fun testIndexUpdaterBeginUpdateOnNonLazyVectorIndex() {
        val config = VectorIndexConfiguration(expression = "vector", dimensions = 300, centroids = 8)
        createWordsIndex(config)

        val index = wordsIndex()

        assertThrowsCBLException(CBLError.Domain.CBLITE, CBLError.Code.UNSUPPORTED) {
            index.beginUpdate(limit = 10)
        }
    }

    /**
     * 13. TestIndexUpdaterBeginUpdateWithZeroLimit
     *
     * Description
     * Test that an InvalidArgument exception is returned when calling beginUpdate
     * with zero limit.
     *
     * Steps
     * 1. Copy database words_db.
     * 2. Create a vector index named "words_index" in the _default.words collection.
     *     - expression: "word"
     *     - dimensions: 300
     *     - centroids : 8
     *     - isLazy : true
     * 3. Get a QueryIndex object from the words collection with the name as
     *    "words_index".
     * 4. Call beginUpdate() with limit 0 on the QueryIndex object.
     * 5. Check that an InvalidArgumentException is thrown.
     */
    @Test
    fun testIndexUpdaterBeginUpdateWithZeroLimit() {
        val config = VectorIndexConfiguration(expression = "vector", dimensions = 300, centroids = 8)
        createWordsIndex(config)

        val index = wordsIndex()

        assertFailsWith<IllegalArgumentException> {
            index.beginUpdate(limit = 0)
        }
    }

    /**
     * 14. TestIndexUpdaterBeginUpdateOnLazyVectorIndex
     *
     * Description
     * Test that calling beginUpdate on a lazy vector index returns an IndexUpdater.
     *
     * Steps
     * 1. Copy database words_db.
     * 2. Create a vector index named "words_index" in the _default.words collection.
     *     - expression: "word"
     *     - dimensions: 300
     *     - centroids : 8
     *     - isLazy : true
     * 3. Get a QueryIndex object from the words with the name as "words_index".
     * 4. Call beginUpdate() with limit 10 on the QueryIndex object.
     * 5. Check that the returned IndexUpdater is not null.
     * 6. Check that the IndexUpdater.count is 10.
     */
    @Test
    fun testIndexUpdaterBeginUpdateOnLazyVectorIndex() {
        val config = VectorIndexConfiguration(expression = "vector", dimensions = 300, centroids = 8)
        createWordsIndex(lazyConfig(config))

        val index = wordsIndex()
        val updater = index.beginUpdate(limit = 10)
        assertNotNull(updater)
        assertEquals(10, updater.count)
    }

    /**
     * 15. TestIndexUpdaterGettingValues
     *
     * Description
     * Test all type getters and toArary() from the Array interface. The test
     * may be divided this test into multiple tests per type getter as appropriate.
     *
     * Steps
     * 1. Get the default collection from a test database.
     * 2. Create the followings documents:
     *     - doc-0 : { "value": "a string" }
     *     - doc-1 : { "value": 100 }
     *     - doc-2 : { "value": 20.8 }
     *     - doc-3 : { "value": true }
     *     - doc-4 : { "value": false }
     *     - doc-5 : { "value": Date("2024-05-10T00:00:00.000Z") }
     *     - doc-6 : { "value": Blob(Data("I'm Bob")) }
     *     - doc-7 : { "value": {"name": "Bob"} }
     *     - doc-8 : { "value": ["one", "two", "three"] }
     *     - doc-9 : { "value": null }
     * 3. Create a vector index named "vector_index" in the default collection.
     *     - expression: "value"
     *     - dimensions: 300
     *     - centroids : 8
     *     - isLazy : true
     * 4. Get a QueryIndex object from the default collection with the name as
     *    "vector_index".
     * 5. Call beginUpdate() with limit 10 to get an IndexUpdater object.
     * 6. Check that the IndexUpdater.count is 10.
     * 7. Get string value from each index and check the followings:
     *     - getString(0) : value == "a string"
     *     - getString(1) : value == null
     *     - getString(2) : value == null
     *     - getString(3) : value == null
     *     - getString(4) : value == null
     *     - getString(5) : value == "2024-05-10T00:00:00.000Z"
     *     - getString(6) : value == null
     *     - getString(7) : value == null
     *     - getString(8) : value == null
     *     - getString(9) : value == null
     * 8. Get integer value from each index and check the followings:
     *     - getInt(0) : value == 0
     *     - getInt(1) : value == 100
     *     - getInt(2) : value == 20
     *     - getInt(3) : value == 1
     *     - getInt(4) : value == 0
     *     - getInt(5) : value == 0
     *     - getInt(6) : value == 0
     *     - getInt(7) : value == 0
     *     - getInt(8) : value == 0
     *     - getInt(9) : value == 0
     * 9. Get float value from each index and check the followings:
     *     - getFloat(0) : value == 0.0
     *     - getFloat(1) : value == 100.0
     *     - getFloat(2) : value == 20.8
     *     - getFloat(3) : value == 1.0
     *     - getFloat(4) : value == 0.0
     *     - getFloat(5) : value == 0.0
     *     - getFloat(6) : value == 0.0
     *     - getFloat(7) : value == 0.0
     *     - getFloat(8) : value == 0.0
     *     - getFloat(9) : value == 0.0
     * 10. Get double value from each index and check the followings:
     *     - getDouble(0) : value == 0.0
     *     - getDouble(1) : value == 100.0
     *     - getDouble(2) : value == 20.8
     *     - getDouble(3) : value == 1.0
     *     - getDouble(4) : value == 0.0
     *     - getDouble(5) : value == 0.0
     *     - getDouble(6) : value == 0.0
     *     - getDouble(7) : value == 0.0
     *     - getDouble(8) : value == 0.0
     *     - getDouble(9) : value == 0.0
     * 11. Get boolean value from each index and check the followings:
     *     - getBoolean(0) : value == true
     *     - getBoolean(1) : value == true
     *     - getBoolean(2) : value == true
     *     - getBoolean(3) : value == true
     *     - getBoolean(4) : value == false
     *     - getBoolean(5) : value == true
     *     - getBoolean(6) : value == true
     *     - getBoolean(7) : value == true
     *     - getBoolean(8) : value == true
     *     - getBoolean(9) : value == false
     * 12. Get date value from each index and check the followings:
     *     - getDate(0) : value == null
     *     - getDate(1) : value == null
     *     - getDate(2) : value == null
     *     - getDate(3) : value == null
     *     - getDate(4) : value == null
     *     - getDate(5) : value == Date("2024-05-10T00:00:00.000Z")
     *     - getDate(6) : value == null
     *     - getDate(7) : value == null
     *     - getDate(8) : value == null
     *     - getDate(9) : value == null
     * 13. Get blob value from each index and check the followings:
     *     - getBlob(0) : value == null
     *     - getBlob(1) : value == null
     *     - getBlob(2) : value == null
     *     - getBlob(3) : value == null
     *     - getBlob(4) : value == null
     *     - getBlob(5) : value == null
     *     - getBlob(6) : value == Blob(Data("I'm Bob"))
     *     - getBlob(7) : value == null
     *     - getBlob(8) : value == null
     *     - getBlob(9) : value == null
     * 14. Get dictionary object from each index and check the followings:
     *     - getDictionary(0) : value == null
     *     - getDictionary(1) : value == null
     *     - getDictionary(2) : value == null
     *     - getDictionary(3) : value == null
     *     - getDictionary(4) : value == null
     *     - getDictionary(5) : value == null
     *     - getDictionary(6) : value == null
     *     - getDictionary(7) : value == Dictionary({"name": "Bob"})
     *     - getDictionary(8) : value == null
     *     - getDictionary(9) : value == null
     * 15. Get array object from each index and check the followings:
     *     - getArray(0) : value == null
     *     - getArray(1) : value == null
     *     - getArray(2) : value == null
     *     - getArray(3) : value == null
     *     - getArray(4) : value == null
     *     - getArray(5) : value == null
     *     - getArray(6) : value == null
     *     - getArray(7) : value == null
     *     - getArray(8) : value == Array(["one", "two", "three"])
     *     - getArray(9) : value == null
     * 16. Get value from each index and check the followings:
     *     - getValue(0) : value == "a string"
     *     - getValue(1) : value == PlatformNumber(100)
     *     - getValue(2) : value == PlatformNumber(20.8)
     *     - getValue(3) : value == PlatformBoolean(true)
     *     - getValue(4) : value == PlatformBoolean(false)
     *     - getValue(5) : value == "2024-05-10T00:00:00.000Z"
     *     - getValue(6) : value == Blob(Data("I'm Bob"))
     *     - getValue(7) : value == PlatformDict({"name": "Bob"})
     *     - getValue(8) : value == PlatformArray(["one", "two", "three"])
     *     - getValue(9) : value == null
     * 17. Get IndexUpdater values as a platform array by calling toArray() and check
     *     that the array contains all values as expected.
     */
    @Test
    fun testIndexUpdaterGettingValues() {
        val collection = testCollection

        val doc0 = MutableDocument(mapOf("value" to "a string"))
        collection.save(doc0)

        val doc1 = MutableDocument(mapOf("value" to 100))
        collection.save(doc1)

        val doc2 = MutableDocument(mapOf("value" to 20.8))
        collection.save(doc2)

        //val doc3 = MutableDocument(mapOf("value" to true))
        val doc3 = MutableDocument()
        doc3.setBoolean("value", true)
        collection.save(doc3)

        //val doc4 = MutableDocument(mapOf("value" to false))
        val doc4 = MutableDocument()
        doc4.setBoolean("value", false)
        collection.save(doc4)

        val date = Instant.parse("2024-05-10T00:00:00.000Z")
        val doc5 = MutableDocument(mapOf("value" to date))
        collection.save(doc5)

        val content = "I'm Bob".encodeToByteArray()
        val blob = Blob("text/plain", content)
        val doc6 = MutableDocument(mapOf("value" to blob))
        collection.save(doc6)

        val doc7 = MutableDocument(mapOf("value" to mapOf("name" to "Bob")))
        collection.save(doc7)

        val doc8 = MutableDocument(mapOf("value" to listOf("one", "two", "three")))
        collection.save(doc8)

        val doc9 = MutableDocument(mapOf("value" to null))
        collection.save(doc9)

        val config = VectorIndexConfiguration(expression = "value", dimensions = 300, centroids = 8)
        createVectorIndex(collection, "vector_index", lazyConfig(config))

        val index = collection.getIndex("vector_index")
        assertNotNull(index)

        val updater = index.beginUpdate(limit = 10)
        assertNotNull(updater)
        assertEquals(10, updater.count)

        // String:
        assertEquals("a string", updater.getString(0))
        assertNull(updater.getString(1))
        assertNull(updater.getString(2))
        assertNull(updater.getString(3))
        assertNull(updater.getString(4))
        assertEquals("2024-05-10T00:00:00.000Z", updater.getString(5))
        assertNull(updater.getString(6))
        assertNull(updater.getString(7))
        assertNull(updater.getString(8))
        assertNull(updater.getString(9))

        // Int:
        assertEquals(0, updater.getInt(0))
        assertEquals(100, updater.getInt(1))
        assertEquals(20, updater.getInt(2))
        assertEquals(1, updater.getInt(3))
        assertEquals(0, updater.getInt(4))
        assertEquals(0, updater.getInt(5))
        assertEquals(0, updater.getInt(6))
        assertEquals(0, updater.getInt(7))
        assertEquals(0, updater.getInt(8))
        assertEquals(0, updater.getInt(9))

        // Int64:
        assertEquals(0L, updater.getLong(0))
        assertEquals(100L, updater.getLong(1))
        assertEquals(20L, updater.getLong(2))
        assertEquals(1L, updater.getLong(3))
        assertEquals(0L, updater.getLong(4))
        assertEquals(0L, updater.getLong(5))
        assertEquals(0L, updater.getLong(6))
        assertEquals(0L, updater.getLong(7))
        assertEquals(0L, updater.getLong(8))
        assertEquals(0L, updater.getLong(9))

        // Float:
        assertEquals(0.0F, updater.getFloat(0))
        assertEquals(100.0F, updater.getFloat(1))
        assertEquals(20.8F, updater.getFloat(2))
        assertEquals(1.0F, updater.getFloat(3))
        assertEquals(0F, updater.getFloat(4))
        assertEquals(0F, updater.getFloat(5))
        assertEquals(0F, updater.getFloat(6))
        assertEquals(0F, updater.getFloat(7))
        assertEquals(0F, updater.getFloat(8))
        assertEquals(0F, updater.getFloat(9))

        // Double:
        assertEquals(0.0, updater.getDouble(0))
        assertEquals(100.0, updater.getDouble(1))
        assertEquals(20.8, updater.getDouble(2))
        assertEquals(1.0, updater.getDouble(3))
        assertEquals(0.0, updater.getDouble(4))
        assertEquals(0.0, updater.getDouble(5))
        assertEquals(0.0, updater.getDouble(6))
        assertEquals(0.0, updater.getDouble(7))
        assertEquals(0.0, updater.getDouble(8))
        assertEquals(0.0, updater.getDouble(9))

        // Boolean:
        assertEquals(true, updater.getBoolean(0))
        assertEquals(true, updater.getBoolean(1))
        assertEquals(true, updater.getBoolean(2))
        assertEquals(true, updater.getBoolean(3))
        assertEquals(false, updater.getBoolean(4))
        assertEquals(true, updater.getBoolean(5))
        assertEquals(true, updater.getBoolean(6))
        assertEquals(true, updater.getBoolean(7))
        assertEquals(true, updater.getBoolean(8))
        assertEquals(false, updater.getBoolean(9))

        // Date:
        assertNull(updater.getDate(0))
        assertNull(updater.getDate(1))
        assertNull(updater.getDate(2))
        assertNull(updater.getDate(3))
        assertNull(updater.getDate(4))
        val getDate = updater.getDate(5)
        assertNotNull(getDate)
        assertEquals("2024-05-10T00:00:00.000Z", getDate.toStringMillis())
        assertNull(updater.getDate(6))
        assertNull(updater.getDate(7))
        assertNull(updater.getDate(8))
        assertNull(updater.getDate(9))

        // Blob:
        assertNull(updater.getBlob(0))
        assertNull(updater.getBlob(1))
        assertNull(updater.getBlob(2))
        assertNull(updater.getBlob(3))
        assertNull(updater.getBlob(4))
        assertNull(updater.getBlob(5))
        val getBlob = updater.getBlob(6)
        assertNotNull(getBlob)
        assertContentEquals(content, getBlob.content)
        assertNull(updater.getBlob(7))
        assertNull(updater.getBlob(8))
        assertNull(updater.getBlob(9))

        // Dict:
        assertNull(updater.getDictionary(0))
        assertNull(updater.getDictionary(1))
        assertNull(updater.getDictionary(2))
        assertNull(updater.getDictionary(3))
        assertNull(updater.getDictionary(4))
        assertNull(updater.getDictionary(5))
        assertNull(updater.getDictionary(6))
        val dict = updater.getDictionary(7)
        assertNotNull(dict)
        assertTrue(dict == doc7.getDictionary("value"))
        assertNull(updater.getDictionary(8))
        assertNull(updater.getDictionary(9))

        // Array:
        assertNull(updater.getArray(0))
        assertNull(updater.getArray(1))
        assertNull(updater.getArray(2))
        assertNull(updater.getArray(3))
        assertNull(updater.getArray(4))
        assertNull(updater.getArray(5))
        assertNull(updater.getArray(6))
        assertNull(updater.getArray(7))
        val array = updater.getArray(8)
        assertNotNull(array)
        assertTrue(array == doc8.getArray("value"))
        assertNull(updater.getArray(9))

        // value:
        assertEquals("a string", updater.getValue(0))
        assertEquals(100L, updater.getValue(1))
        assertEquals(20.8, updater.getValue(2))
        assertEquals(true, updater.getValue(3))
        assertEquals(false, updater.getValue(4))
        assertEquals("2024-05-10T00:00:00.000Z", updater.getValue(5))
        assertEquals(blob, updater.getValue(6))
        assertEquals(doc7.getDictionary("value"), updater.getValue(7))
        assertEquals(doc8.getArray("value"), updater.getValue(8))
        assertEquals(null, updater.getValue(9))

        // toArray
        val values = updater.toList()
        assertEquals("a string", values[0])
        assertEquals(100L, values[1])
        assertEquals(20.8, values[2])
        assertEquals(true, values[3])
        assertEquals(false, values[4])
        assertEquals("2024-05-10T00:00:00.000Z", values[5])
        assertEquals(blob, values[6])
        assertEquals(doc7.getDictionary("value")!!.toMap(), values[7])
        assertEquals(doc8.getArray("value")!!.toList(), values[8])
        assertEquals(null, values[9])

        // toJSON
        assertTrue(updater.toJSON().isNotEmpty())
    }

    /**
     * 16. TestIndexUpdaterArrayIterator
     *
     * Description
     * Test that iterating the index updater using the platform array iterator
     * interface works as expected.
     *
     * Steps
     * 1. Copy database words_db.
     * 2. Create a vector index named "words_index" in the _default.words collection.
     *     - expression: "word"
     *     - dimensions: 300
     *     - centroids : 8
     *     - isLazy : true
     * 3. Get a QueryIndex object from the words with the name as "words_index".
     * 4. Call beginUpdate() with limit 10 to get an IndexUpdater object.
     * 5. Check that the IndexUpdater.count is 10.
     * 6. Iterate using the platfrom array iterator.
     * 7. For each iteration, check that the value is the same as the value getting
     *    from getValue(index).
     * 8. Check that there were 10 iteration calls.
     */
    @Test
    fun testIndexUpdaterArrayIterator() {
        val config = VectorIndexConfiguration(expression = "word", dimensions = 300, centroids = 8)
        createWordsIndex(lazyConfig(config))

        val index = wordsIndex()
        val updater = index.beginUpdate(limit = 10)!!
        assertEquals(10, updater.count)
        var i = 0
        for (value in updater) {
            assertEquals(updater[i].value, value)
            i += 1
        }
        assertEquals(10, i)
    }

    /**
     * 17. TestIndexUpdaterSetFloatArrayVectors
     *
     * Description
     * Test that setting float array vectors works as expected.
     *
     * Steps
     * 1. Copy database words_db.
     * 2. Create a vector index named "words_index" in the _default.words collection.
     *     - expression: "word"
     *     - dimensions: 300
     *     - centroids : 8
     *     - isLazy : true
     * 3. Get a QueryIndex object from the words with the name as "words_index".
     * 4. Call beginUpdate() with limit 10 to get an IndexUpdater object.
     * 5. With the IndexUpdater object, for each index from 0 to 9.
     *     - Get the word string from the IndexUpdater and store the word string in a set for verifying
     *        the vector search result.
     *     - Query the vector by word from the _default.words collection.
     *     - Convert the vector result which is an array object to a platform's float array.
     *     - Call setVector() with the platform's float array at the index.
     * 6. With the IndexUpdater object, call finish()
     * 7. Execute a vector search query.
     *     - SELECT word
     *       FROM _default.words
     *       ORDER BY APPROX_VECTOR_DISTANCE(word, $dinnerVector)
     *       LIMIT 300
     * 8. Check that there are 10 words returned.
     * 9. Check that the word is in the word set from the step 5.
     */
    @Test
    fun testIndexUpdaterSetFloatArrayVectors() {
        val config = VectorIndexConfiguration(expression = "word", dimensions = 300, centroids = 8)
        createWordsIndex(lazyConfig(config))

        val index = wordsIndex()
        val updater = index.beginUpdate(limit = 10)!!
        assertEquals(10, updater.count)

        val words = mutableListOf<String>()
        for (i in 0..<updater.count) {
            val word = updater.getString(i)!!
            val vector = vector(word)!!
            updater.setVector(vector, i)
            words.add(word)
        }
        updater.finish()

        val rs = executeWordsQuery(limit = 300, checkTraining = false)
        val resultWords = toDocIDWordMap(rs).values
        assertEquals(10, resultWords.size)
        for (word in resultWords) {
            assertContains(words, word)
        }
    }

    /**
     * 20. TestIndexUpdaterSetInvalidVectorDimensions
     *
     * Description
     * Test thta the vector with the invalid dimenions different from the dimensions
     * set to the configuration will not be included in the index.
     *
     * Steps
     * 1. Copy database words_db.
     * 2. Create a vector index named "words_index" in the _default.words collection.
     *     - expression: "word"
     *     - dimensions: 300
     *     - centroids : 8
     *     - isLazy : true
     * 3. Get a QueryIndex object from the words with the name as "words_index".
     * 4. Call beginUpdate() with limit 1 to get an IndexUpdater object.
     * 5. With the IndexUpdater object, call setVector() with a float array as [1.0]
     * 6. Check that the setVector throws CouchbaseLiteException with the InvalidParameter error.
     */
    @Test
    fun testIndexUpdaterSetInvalidVectorDimensions() {
        val config = VectorIndexConfiguration(expression = "word", dimensions = 300, centroids = 8)
        createWordsIndex(lazyConfig(config))

        val index = wordsIndex()
        val updater = index.beginUpdate(limit = 1)
        assertNotNull(updater)
        assertEquals(1, updater.count)

        assertThrowsCBLException(CBLError.Domain.CBLITE, CBLError.Code.INVALID_PARAMETER) {
            updater.setVector(listOf(1.0F), 0)
        }
    }

    /**
     * 21. TestIndexUpdaterSkipVectors
     *
     * Description
     * Test that skipping vectors works as expected.
     *
     * Steps
     * 1. Copy database words_db.
     * 2. Create a vector index named "words_index" in the _default.words collection.
     *     - expression: "word"
     *     - dimensions: 300
     *     - centroids : 8
     *     - isLazy : true
     * 3. Get a QueryIndex object from the words with the name as "words_index".
     * 4. Call beginUpdate() with limit 10 to get an IndexUpdater object.
     * 5. With the IndexUpdater object, for each index from 0 - 9.
     *     - Get the word string from the IndexUpdater.
     *     - If index % 2 == 0,
     *         - Store the word string in a skipped word set for verifying the skipped words later.
     *         - Call skipVector at the index.
     *     - If index % 2 != 0,
     *         - Query the vector by word from the _default.words collection.
     *         - Convert the vector result which is an array object to a platform's float array.
     *         - Call setVector() with the platform's float array at the index.
     * 6. With the IndexUpdater object, call finish()
     * 7. Call beginUpdate with limit 10 to get an IndexUpdater object.
     * 8. With the IndexUpdater object, for each index
     *     - Get the word string from the dictionary for the key named "word".
     *     - Check if the word is in the skipped word set from the Step 5. If the word
     *        is in the skipped word set, remove the word from the skipped word set.
     *     - Query the vector by word from the _default.words collection.
     *         - Convert the vector result which is an array object to a platform's float array.
     *         - Call setVector() with the platform's float array at the index
     * 9. With the IndexUpdater object, call finish()
     * 10. Repeat Step 7, until the returned IndexUpdater is null or the skipped word set
     *      has zero words in it.
     * 11. Verify that the skipped word set has zero words in it.
     */
    @Test
    fun testIndexUpdaterSkipVectors() {
        val config = VectorIndexConfiguration(expression = "word", dimensions = 300, centroids = 8)
        createWordsIndex(lazyConfig(config))

        val index = wordsIndex()
        var updater = index.beginUpdate(limit = 10)!!
        assertEquals(10, updater.count)

        val skipWords = mutableListOf<String>()
        for (i in 0..<updater.count) {
            val word = updater.getString(i)!!
            if (i % 2 == 0) {
                updater.skipVector(i)
                skipWords.add(word)
            } else {
                val vector = vector(word)!!
                updater.setVector(vector, i)
            }
        }
        updater.finish()

        updater = index.beginUpdate(limit = 10)!!
        for (i in 0..<updater.count) {
            val word = updater.getString(i)!!
            val vector = vector(word)!!
            updater.setVector(vector, i)

            skipWords.remove(word)
        }
        updater.finish()
        assertEquals(0, skipWords.size)
    }

    /**
     * 22. TestIndexUpdaterFinishWithIncompletedUpdate
     *
     * Description
     * Test that a CouchbaseLiteException is thrown when calling finish() on
     * an IndexUpdater that has incomplete updated.
     *
     * Steps
     * 1. Copy database words_db.
     * 2. Create a vector index named "words_index" in the _default.words collection.
     *     - expression: "word"
     *     - dimensions: 300
     *     - centroids : 8
     *     - isLazy : true
     * 3. Get a QueryIndex object from the words with the name as "words_index".
     * 4. Call beginUpdate() with limit 2 to get an IndexUpdater object.
     * 5. With the IndexUpdater object, call finish().
     * 6. Check that a CouchbaseLiteException with code UnsupportedOperation is thrown.
     * 7. For the index 0,
     *     - Get the word string from the IndexUpdater.
     *     - Query the vector by word from the _default.words collection.
     *     - Convert the vector result which is an array object to a platform's float array.
     *     - Call setVector() with the platform's float array at the index.
     * 8. With the IndexUpdater object, call finish().
     * 9. Check that a CouchbaseLiteException with code UnsupportedOperation is thrown.
     */
    @Test
    fun testIndexUpdaterFinishWithIncompletedUpdate() {
        val config = VectorIndexConfiguration(expression = "word", dimensions = 300, centroids = 10)
        createWordsIndex(lazyConfig(config))

        val index = wordsIndex()
        val updater = index.beginUpdate(limit = 2)
        assertNotNull(updater)
        assertEquals(2, updater.count)

        assertThrowsCBLException(CBLError.Domain.CBLITE, CBLError.Code.UNSUPPORTED) {
            updater.finish()
        }

        val word = updater.getString(0)!!
        val vector = vector(word)
        updater.setVector(vector, 0)
        assertThrowsCBLException(CBLError.Domain.CBLITE, CBLError.Code.UNSUPPORTED) {
            updater.finish()
        }
    }

    /**
     * 23. TestIndexUpdaterCaughtUp
     *
     * Description
     * Test that when the lazy vector index is caught up, calling beginUpdate() to
     * get an IndexUpdater will return null.
     *
     * Steps
     * 1. Copy database words_db.
     * 2. Create a vector index named "words_index" in the _default.words collection.
     *     - expression: "word"
     *     - dimensions: 300
     *     - centroids : 8
     *     - isLazy : true
     * 3. Call beginUpdate() with limit 100 to get an IndexUpdater object.
     *     - Get the word string from the IndexUpdater.
     *     - Query the vector by word from the _default.words collection.
     *     - Convert the vector result which is an array object to a platform's float array.
     *     - Call setVector() with the platform's float array at the index.
     * 4. Repeat Step 3 two more times.
     * 5. Call beginUpdate() with limit 100 to get an IndexUpdater object.
     * 6. Check that the returned IndexUpdater is null.
     */
    @Test
    fun testIndexUpdaterCaughtUp() {
        val config = VectorIndexConfiguration(expression = "word", dimensions = 300, centroids = 10)
        createWordsIndex(lazyConfig(config))

        val index = wordsIndex()

        for (i in 0..<3) {
            val updater = index.beginUpdate(limit = 100)
            assertNotNull(updater)

            for (j in 0..<updater.count) {
                val word = updater.getString(j)!!
                val vector = vector(word)
                updater.setVector(vector, j)
            }
            updater.finish()
        }

        index.beginUpdate(limit = 100)
    }

    /**
     * 24. TestNonFinishedIndexUpdaterNotUpdateIndex
     *
     * Description
     * Test that the index updater can be released without calling finish(),
     * and the released non-finished index updater doesn't update the index.
     *
     * Steps
     * 1. Copy database words_db.
     * 2. Create a vector index named "words_index" in the _default.words collection.
     *     - expression: "word"
     *     - dimensions: 300
     *     - centroids : 8
     *     - isLazy : true
     * 3. Get a QueryIndex object from the words with the name as "words_index".
     * 4. Call beginUpdate() with limit 10 to get an IndexUpdater object.
     * 5. With the IndexUpdater object, for each index from 0 - 9.
     *     - Get the word string from the IndexUpdater.
     *     - Query the vector by word from the _default.words collection.
     *     - Convert the vector result which is an array object to a platform's float array.
     *     - Call setVector() with the platform's float array at the index.
     * 6. Release or close the index updater object.
     * 7. Execute a vector search query.
     *     - SELECT word
     *       FROM _default.words
     *       ORDER BY APPROX_VECTOR_DISTANCE(word, $dinnerVector)
     *       LIMIT 300
     * 8. Check that there are 0 words returned.
     */
    @Test
    fun testNonFinishedIndexUpdaterNotUpdateIndex() {
        val config = VectorIndexConfiguration(expression = "word", dimensions = 300, centroids = 8)
        createWordsIndex(lazyConfig(config))

        val index = wordsIndex()
        var updater = index.beginUpdate(limit = 10)
        assertNotNull(updater)
        assertEquals(10, updater.count)

        // Update index:
        for (i in 0..<updater.count) {
            val word = updater.getString(i)!!
            val vector = vector(word)
            updater.setVector(vector, i)
        }

        // "Release" CBLIndexUpdater
        updater = null
        PlatformUtils.gc()
        executeWordsQuery(limit = 300, checkTraining = false).use { rs ->
            assertEquals(0, rs.allResults().size)
        }
    }

    /**
     * 25. TestIndexUpdaterIndexOutOfBounds
     *
     * Description
     * Test that when using getter, setter, and skip function with the index that
     * is out of bounds, an IndexOutOfBounds or InvalidArgument exception
     * is throws.
     *
     * Steps
     * 1. Get the default collection from a test database.
     * 2. Create the followings documents:
     *     - doc-0 : { "value": "a string" }
     * 3. Create a vector index named "vector_index" in the default collection.
     *     - expression: "value"
     *     - dimensions: 3
     *     - centroids : 8
     *     - isLazy : true
     * 4. Get a QueryIndex object from the default collection with the name as
     *    "vector_index".
     * 5. Call beginUpdate() with limit 10 to get an IndexUpdater object.
     * 6. Check that the IndexUpdater.count is 1.
     * 7. Call each getter function with index = -1 and check that
     *    an IndexOutOfBounds or InvalidArgument exception is thrown.
     * 8. Call each getter function with index = 1 and check that
     *    an IndexOutOfBounds or InvalidArgument exception is thrown.
     * 9. Call setVector() function with a vector = [1.0, 2.0, 3.0] and index = -1 and check that
     *    an IndexOutOfBounds or InvalidArgument exception is thrown.
     * 10. Call setVector() function with a vector = [1.0, 2.0, 3.0] and index = 1 and check that
     *    an IndexOutOfBounds or InvalidArgument exception is thrown.
     * 9. Call skipVector() function with index = -1 and check that
     *    an IndexOutOfBounds or InvalidArgument exception is thrown.
     * 10. Call skipVector() function with index = 1 and check that
     *    an IndexOutOfBounds or InvalidArgument exception is thrown.
     */
    @Test
    fun testIndexUpdaterIndexOutOfBounds() {
        val collection = testCollection

        val doc0 = MutableDocument(mapOf("value" to "a string"))
        collection.save(doc0)

        val config = VectorIndexConfiguration(expression = "value", dimensions = 300, centroids = 8)
        createVectorIndex(collection, "vector_index", lazyConfig(config))

        val index = collection.getIndex("vector_index")!!
        val updater = index.beginUpdate(limit = 10)!!
        assertEquals(1, updater.count)

        assertFailsWith<IndexOutOfBoundsException> {
            updater.getString(1)
        }

        assertFailsWith<IndexOutOfBoundsException> {
            updater.getInt(1)
        }

        assertFailsWith<IndexOutOfBoundsException> {
            updater.getLong(1)
        }

        assertFailsWith<IndexOutOfBoundsException> {
            updater.getFloat(1)
        }

        assertFailsWith<IndexOutOfBoundsException> {
            updater.getDouble(1)
        }

        assertFailsWith<IndexOutOfBoundsException> {
            updater.getBoolean(1)
        }

        assertFailsWith<IndexOutOfBoundsException> {
            updater.getDate(1)
        }

        assertFailsWith<IndexOutOfBoundsException> {
            updater.getBlob(1)
        }

        assertFailsWith<IndexOutOfBoundsException> {
            updater.getDictionary(1)
        }

        assertFailsWith<IndexOutOfBoundsException> {
            updater.getArray(1)
        }

        assertFailsWith<IndexOutOfBoundsException> {
            updater.getValue(1)
        }

        assertFailsWith<IndexOutOfBoundsException> {
            updater.setVector(listOf(1.0F, 2.0F, 3.0F), 1)
        }

        assertFailsWith<IndexOutOfBoundsException> {
            updater.skipVector(1)
        }
    }

    /**
     * 26. TestIndexUpdaterCallFinishTwice + 27. TestIndexUpdaterUseAfterFinished
     *
     * Description
     * Test that when calling IndexUpdater's finish() after it was finished,
     * a CuchbaseLiteException is thrown.
     *
     * Steps
     * 1. Copy database words_db.
     * 2. Create a vector index named "words_index" in the _default.words collection.
     *     - expression: "word"
     *     - dimensions: 300
     *     - centroids : 8
     *     - isLazy : true
     * 3. Call beginUpdate() with limit 1 to get an IndexUpdater object.
     *     - Get the word string from the IndexUpdater.
     *     - Query the vector by word from the _default.words collection.
     *     - Convert the vector result which is an array object to a platform's float array.
     *     - Call setVector() with the platform's float array at the index.
     * 4. Call finish() and check that the finish() is successfully called.
     * 5. Call finish() again and check that it throws exception.
     * 6. Count, getValue, setVector, skipVector throw exception.
     */
    @Test
    fun testIndexUpdaterUseAfterFinished() {
        val config = VectorIndexConfiguration(expression = "word", dimensions = 300, centroids = 8)
        createWordsIndex(lazyConfig(config))

        val index = wordsIndex()
        val updater = index.beginUpdate(limit = 1)
        assertNotNull(updater)
        assertEquals(1, updater.count)

        val word = updater.getString(0)!!
        val vector = vector(word)
        updater.setVector(vector, 0)
        updater.finish()

        assertFailsWith<CouchbaseLiteError> {
            updater.finish()
        }

        assertFailsWith<CouchbaseLiteError> {
            updater.count
        }

        assertFailsWith<CouchbaseLiteError> {
            updater.getValue(0)
        }

        assertFailsWith<CouchbaseLiteError> {
            updater.setVector(vector, 0)
        }

        assertFailsWith<CouchbaseLiteError> {
            updater.skipVector(0)
        }
    }
}
