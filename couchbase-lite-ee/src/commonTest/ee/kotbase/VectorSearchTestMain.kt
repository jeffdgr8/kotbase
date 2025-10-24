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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Test Spec:
 * https://github.com/couchbaselabs/couchbase-lite-api/blob/master/spec/tests/T0001-Vector-Search.md
 *
 * Version: 2.1.0
 */
class VectorSearchTestMain : BaseVectorSearchTest() {

    /**
     * 1. TestVectorIndexConfigurationDefaultValue
     * Description
     *     Test that the VectorIndexConfiguration has all default values returned as expected.
     * Steps
     *     1. Create a VectorIndexConfiguration object.
     *         - expression: "vector"
     *         - dimensions: 300
     *         - centroids: 20
     *     2. Get and check the following property values:
     *         - encoding: 8-Bit Scalar Quantizer Encoding
     *         - metric: Euclidean Distance
     *         - minTrainingSize: 0
     *         - maxTrainingSize: 0
     *     3. To check the encoding type, platform code will have to expose some internal
     *        property to the tests for verification.
     */
    @Test
    fun testVectorIndexConfigurationDefaultValue() {
        val config = VectorIndexConfiguration(expression = "vector", dimensions = 300, centroids = 20)
        assertEquals(VectorEncoding.scalarQuantizer(Defaults.VectorIndex.ENCODING), config.encoding)
        assertEquals(Defaults.VectorIndex.DISTANCE_METRIC, config.metric)
        assertEquals(0, config.minTrainingSize)
        assertEquals(0, config.maxTrainingSize)
        assertEquals(0, config.numProbes)
    }

    /**
     * 2. TestVectorIndexConfigurationSettersAndGetters
     * Description
     *     Test that all getters and setters of the VectorIndexConfiguration work as expected.
     * Steps
     *     1. Create a VectorIndexConfiguration object with the following properties.
     *         - expression: "vector"
     *         - dimensions: 300
     *         - centroids: 20
     *         - encoding: None
     *         - metric: Cosine Distance
     *         - minTrainingSize: 100
     *         - maxTrainingSize: 200
     *     2. Get and check the following properties.
     *         - expression: "vector"
     *         - expressions: ["vector"]
     *         - dimensions: 300
     *         - centroids: 20
     *         - encoding: None
     *         - metric: Cosine
     *         - minTrainingSize: 100
     *         - maxTrainingSize: 200
     */
    @Test
    fun testVectorIndexConfigurationSettersAndGetters() {
        val config = VectorIndexConfiguration(expression = "vector", dimensions = 300, centroids = 20)
        config.encoding = VectorEncoding.none()
        config.metric = VectorIndexConfiguration.DistanceMetric.COSINE
        config.minTrainingSize = 100
        config.maxTrainingSize = 200

        assertEquals("vector", config.expression)
        assertEquals(300, config.dimensions)
        assertEquals(20, config.centroids)
        assertEquals(VectorEncoding.none(), config.encoding)
        assertEquals(VectorIndexConfiguration.DistanceMetric.COSINE, config.metric)
        assertEquals(100, config.minTrainingSize)
        assertEquals(200, config.maxTrainingSize)
    }

    /**
     * 3. TestDimensionsValidation
     * Description
     *     Test that the dimensions are validated correctly. The invalid argument exception
     *     should be thrown when creating vector index configuration objects with invalid
     *     dimensions.
     * Steps
     *     1. Create a VectorIndexConfiguration object.
     *         - expression: "vector"
     *         - dimensions: 2 and 4096
     *         - centroids: 8
     *     2. Check that the config can be created without an error thrown.
     *     3. Use the config to create the index and check that the index
     *       can be created successfully.
     *     4. Change the dimensions to 1 and 4097.
     *     5. Check that an invalid argument exception is thrown.
     */
    @Test
    fun testDimensionsValidation() {
        val config1 = VectorIndexConfiguration(expression = "vector", dimensions = 2, centroids = 8)
        wordsCollection.createIndex("words_index_1", config1)

        val config2 = VectorIndexConfiguration(expression = "vector", dimensions = 4096, centroids = 8)
        wordsCollection.createIndex("words_index_2", config2)

        assertFailsWith<IllegalArgumentException> {
            VectorIndexConfiguration(expression = "vector", dimensions = 1, centroids = 8)
        }

        assertFailsWith<IllegalArgumentException> {
            VectorIndexConfiguration(expression = "vector", dimensions = 4097, centroids = 8)
        }
    }

    /**
     * 4. TestCentroidsValidation
     * Description
     *     Test that the centroids value is validated correctly. The invalid argument
     *     exception should be thrown when creating vector index configuration objects with
     *     invalid centroids..
     * Steps
     *     1. Create a VectorIndexConfiguration object.
     *         - expression: "vector"
     *         - dimensions: 300
     *         - centroids: 1 and 64000
     *     2. Check that the config can be created without an error thrown.
     *     3. Use the config to create the index and check that the index
     *        can be created successfully.
     *     4. Change the centroids to 0 and 64001.
     *     5. Check that an invalid argument exception is thrown.
     */
    @Test
    fun testCentroidsValidation() {
        val config1 = VectorIndexConfiguration(expression = "vector", dimensions = 300, centroids = 1)
        wordsCollection.createIndex("words_index_1", config1)

        val config2 = VectorIndexConfiguration(expression = "vector", dimensions = 300, centroids = 64000)
        wordsCollection.createIndex("words_index_2", config2)

        assertFailsWith<IllegalArgumentException> {
            VectorIndexConfiguration(expression = "vector", dimensions = 300, centroids = 0)
        }

        assertFailsWith<IllegalArgumentException> {
            VectorIndexConfiguration(expression = "vector", dimensions = 300, centroids = 64001)
        }
    }

    /**
     * 5. TestCreateVectorIndex
     * Description
     *     Using the default configuration, test that the vector index can be created from
     *     the embedded vectors in the documents. The test also verifies that the created
     *     index can be used in the query.
     * Steps
     *     1. Copy database words_db.
     *     2. Register a custom logger to capture the INFO log.
     *     3. Create a vector index named "words_index" in _default.words collection.
     *         - expression: "vector"
     *         - dimensions: 300
     *         - centroids: 8
     *     4. Check that the index is created without an error returned.
     *     5. Get index names from the _default.words collection and check that the index
     *       names contains “words_index”.
     *     6. Create an SQL++ query:
     *         - SELECT meta().id, word
     *           FROM _default.words
     *           ORDER BY APPROX_VECTOR_DISTANCE(vector, $dinerVector)
     *           LIMIT 20
     *     7. Check the explain() result of the query to ensure that the "words_index" is used.
     *     8. Execute the query and check that 20 results are returned.
     *     9. Verify that the index was trained by checking that the “Untrained index; queries may be slow”
     *       doesn’t exist in the log.
     *     10. Reset the custom logger.
     */
    @Test
    fun testCreateVectorIndex() {
        val config1 = VectorIndexConfiguration(expression = "vector", dimensions = 300, centroids = 8)
        createWordsIndex(config1)

        val rs = executeWordsQuery(limit = 20)
        assertEquals(20, rs.allResults().size)
    }

    /**
     * 6. TestUpdateVectorIndex
     * Description
     *     Test that the vector index created from the embedded vectors will be updated
     *     when documents are changed. The test also verifies that the created index can be
     *     used in the query.
     * Steps
     *     1. Copy database words_db.
     *     2. Register a custom logger to capture the INFO log.
     *     3. Create a vector index named "words_index" in _default.words collection.
     *         - expression: "vector"
     *         - dimensions: 300
     *         - centroids: 8
     *     4. Check that the index is created without an error returned.
     *     5. Create an SQL++ query:
     *         - SELECT meta().id, word
     *           FROM _default.words
     *           WHERE vector_match(words_index, <dinner vector>)
     *           LIMIT 350
     *     6. Check the explain() result of the query to ensure that the "words_index" is used.
     *     7. Execute the query and check that 300 results are returned.
     *     8. Verify that the index was trained by checking that the “Untrained index; queries may be slow”
     *       doesn’t exist in the log.
     *     9. Update the documents:
     *         - Create _default.words.word301 with the content from _default.extwords.word1
     *         - Create _default.words.word302 with the content from _default.extwords.word2
     *         - Update _default.words.word1 with the content from _default.extwords.word3
     *         - Delete _default.words.word2
     *     10. Execute the query again and check that 301 results are returned, and
     *         - word301 and word302 are included.
     *         - word1’s word is updated with the word from _default.extwords.word3
     *         - word2 is not included.
     *     11. Reset the custom logger.
     */
    @Test
    fun testUpdateVectorIndex() {
        val config = VectorIndexConfiguration(expression = "vector", dimensions = 300, centroids = 8)
        createWordsIndex(config)

        // Query:
        var rs = executeWordsQuery(limit = 350)
        assertEquals(300, rs.allResults().size)

        // Update docs:
        val extWord1 = extWordsCollection.getDocument("word1")!!
        val word301 = MutableDocument("word301")
        word301.setData(extWord1.toMap())
        wordsCollection.save(word301)

        val extWord2 = extWordsCollection.getDocument("word2")!!
        val word302 = MutableDocument("word302")
        word302.setData(extWord2.toMap())
        wordsCollection.save(word302)

        val extWord3 = extWordsCollection.getDocument("word3")!!
        val word1 = wordsCollection.getDocument("word1")!!.toMutable()
        word1.setData(extWord3.toMap())
        wordsCollection.save(word1)

        wordsCollection.delete(wordsCollection.getDocument("word2")!!)

        // Query:
        rs = executeWordsQuery(limit = 350)
        val wordMap = toDocIDWordMap(rs)
        assertEquals(301, wordMap.size)
        assertEquals(word301.getString("word"), wordMap["word301"])
        assertEquals(word302.getString("word"), wordMap["word302"])
        assertEquals(word1.getString("word"), wordMap["word1"])
        assertNull(wordMap["word2"])
    }

    /**
     * 7. TestCreateVectorIndexWithInvalidVectors
     * Description
     *     Using the default configuration, test that when creating the vector index with
     *     invalid vectors, the invalid vectors will be skipped from indexing.
     * Steps
     *     1. Copy database words_db.
     *     2. Register a custom logger to capture the INFO log.
     *     3. Update documents:
     *         - Update _default.words word1 with "vector" = null
     *         - Update _default.words word2 with "vector" = "string"
     *         - Update _default.words word3 by removing the "vector" key.
     *         - Update _default.words word4 by removing one number from the "vector" key.
     *     4. Create a vector index named "words_index" in _default.words collection.
     *         - expression: "vector"
     *         - dimensions: 300
     *         - centroids: 8
     *     5. Check that the index is created without an error returned.
     *     6. Create an SQL++ query.
     *         - SELECT meta().id, word
     *           FROM _default.words
     *           ORDER BY APPROX_VECTOR_DISTANCE(vector, $dinerVector)
     *           LIMIT 350
     *     7. Execute the query and check that 296 results are returned, and the results
     *        do not include document word1, word2, word3, and word4.
     *     8. Verify that the index was trained by checking that the “Untrained index; queries may be slow”
     *       doesn’t exist in the log.
     *     9. Update an already index vector with an invalid vector.
     *         - Update _default.words word5 with "vector" = null.
     *     10. Execute the query and check that 295 results are returned, and the results
     *        do not include document word5.
     *     11. Reset the custom logger.
     */
    @Test
    fun testCreateVectorIndexWithInvalidVectors() {
        // Update docs:
        var auxDoc = wordsCollection.getDocument("word1")!!.toMutable()
        auxDoc.setArray("vector", null)
        wordsCollection.save(auxDoc)

        auxDoc = wordsCollection.getDocument("word2")!!.toMutable()
        auxDoc.setString("vector", "string")
        wordsCollection.save(auxDoc)

        auxDoc = wordsCollection.getDocument("word3")!!.toMutable()
        auxDoc.remove("vector")
        wordsCollection.save(auxDoc)

        auxDoc = wordsCollection.getDocument("word4")!!.toMutable()
        val vector = auxDoc.getArray("vector")
        vector!!.remove(0)
        wordsCollection.save(auxDoc)

        val config = VectorIndexConfiguration(expression = "vector", dimensions = 300, centroids = 8)
        createWordsIndex(config)

        // Query:
        var rs = executeWordsQuery(limit = 350)
        var wordMap = toDocIDWordMap(rs)
        assertEquals(296, wordMap.size)
        assertNull(wordMap["word1"])
        assertNull(wordMap["word2"])
        assertNull(wordMap["word3"])
        assertNull(wordMap["word4"])

        // Update docs:
        auxDoc = wordsCollection.getDocument("word5")!!.toMutable()
        auxDoc.setString("vector", null)
        wordsCollection.save(auxDoc)

        // Query:
        rs = executeWordsQuery(limit = 350)
        wordMap = toDocIDWordMap(rs)
        assertEquals(295, wordMap.size)
        assertNull(wordMap["word5"])
    }

    /**
     * 8. TestCreateVectorIndexUsingPredictionModel
     * Description
     *     Using the default configuration, test that the vector index can be created from
     *     the vectors returned by a predictive model.
     * Steps
     *     1. Copy database words_db.
     *     2. Register a custom logger to capture the INFO log.
     *     3. Register  "WordEmbedding" predictive model defined in section 2.
     *     4. Create a vector index named "words_pred_index" in _default.words collection.
     *         - expression: "prediction(WordEmbedding, {"word": word}).vector"
     *         - dimensions: 300
     *         - centroids: 8
     *     5. Check that the index is created without an error returned.
     *     6. Create an SQL++ query:
     *         - SELECT meta().id, word
     *           FROM _default.words
     *           ORDER BY APPROX_VECTOR_DISTANCE(prediction(WordEmbedding, {'word': word}).vector, $dinerVector)
     *           LIMIT 350
     *     7. Check the explain() result of the query to ensure that the "words_pred_index" is used.
     *     8. Execute the query and check that 300 results are returned.
     *     9. Verify that the index was trained by checking that the “Untrained index; queries may be slow”
     *       doesn’t exist in the log.
     *     10. Update the vector index:
     *         - Create _default.words.word301 with the content from _default.extwords.word1
     *         - Create _default.words.word302 with the content from _default.extwords.word2
     *         - Update _default.words.word1 with the content from _default.extwords.word3
     *         - Delete _default.words.word2
     *     11. Execute the query and check that 301 results are returned.
     *         - word301 and word302 are included.
     *         - word1 is updated with the word from _default.extwords.word2.
     *         - word2 is not included.
     *     12. Reset the custom logger.
     */
    @Test
    fun testCreateVectorIndexUsingPredictionModel() {
        registerPredictiveModel()

        val expr = "prediction(WordEmbedding,{\"word\": word}).vector"
        val config = VectorIndexConfiguration(expression = expr, dimensions = 300, centroids = 8)
        createWordsIndex(config)

        // Query:
        var rs = executeWordsQuery(limit = 350, vectorExpression = expr)
        assertEquals(300, rs.allResults().size)
        assertTrue(checkIndexWasTrained())

        // Create words.word301 with extwords.word1 content
        val extWord1 = extWordsCollection.getDocument("word1")!!
        val word301 = MutableDocument("word301")
        word301.setData(extWord1.toMap())
        wordsCollection.save(word301)

        // Create words.word302 with extwords.word2 content
        val extWord2 = extWordsCollection.getDocument("word2")!!
        val word302 = MutableDocument("word302")
        word302.setData(extWord2.toMap())
        wordsCollection.save(word302)

        // Update words.word1 with extwords.word3 content
        val extWord3 = extWordsCollection.getDocument("word3")!!
        val word1 = wordsCollection.getDocument("word1")!!.toMutable()
        word1.setData(extWord3.toMap())
        wordsCollection.save(word1)

        // Delete words.word2
        wordsCollection.delete(wordsCollection.getDocument("word2")!!)

        rs = executeWordsQuery(limit = 350, vectorExpression = expr)
        val wordMap = toDocIDWordMap(rs)
        assertEquals(301, wordMap.size)
        assertEquals(word301.getString("word"), wordMap["word301"])
        assertEquals(word302.getString("word"), wordMap["word302"])
        assertEquals(word1.getString("word"), wordMap["word1"])
        assertNull(wordMap["word2"])

        Database.prediction.unregisterModel("WordEmbedding")
    }

    /**
     * 9. TestCreateVectorIndexUsingPredictiveModelWithInvalidVectors
     * Description
     *     Using the default configuration, test that when creating the vector index using
     *     a predictive model with invalid vectors, the invalid vectors will be skipped
     *     from indexing.
     * Steps
     *     1. Copy database words_db.
     *     2. Register a custom logger to capture the INFO log.
     *     3. Register  "WordEmbedding" predictive model defined in section 2.
     *     4. Update documents.
     *         - Update _default.words word1 with "vector" = null
     *         - Update _default.words word2 with "vector" = "string"
     *         - Update _default.words word3 by removing the "vector" key.
     *         - Update _default.words word4 by removing one number from the "vector" key.
     *     5. Create a vector index named "words_prediction_index" in _default.words collection.
     *         - expression: "prediction(WordEmbedding, {"word": word}).embedding"
     *         - dimensions: 300
     *         - centroids: 8
     *     6. Check that the index is created without an error returned.
     *     7. Create an SQL++ query.
     *         - SELECT meta().id, word
     *           FROM _default.words
     *           ORDER BY APPROX_VECTOR_DISTANCE(prediction(WordEmbedding, {'word': word}).vector, $dinerVector)
     *           LIMIT 350
     *     8. Check the explain() result of the query to ensure that the "words_predi_index" is used.
     *     9. Execute the query and check that 296 results are returned and the results
     *        do not include word1, word2, word3, and word4.
     *     10. Verify that the index was trained by checking that the “Untrained index; queries may be slow” doesn’t exist in the log.
     *     11. Update an already index vector with a non existing word in the database.
     *         - Update _default.words.word5 with “word” = “Fried Chicken”.
     *     12. Execute the query and check that 295 results are returned, and the results
     *         do not include document word5.
     *     13. Reset the custom logger.
     */
    @Test
    fun testCreateVectorIndexUsingPredictiveModelWithInvalidVectors() {
        registerPredictiveModel()

        // Update docs:
        var auxDoc = wordsCollection.getDocument("word1")!!.toMutable()
        auxDoc.setArray("vector", null)
        wordsCollection.save(auxDoc)

        auxDoc = wordsCollection.getDocument("word2")!!.toMutable()
        auxDoc.setString("vector", "string")
        wordsCollection.save(auxDoc)

        auxDoc = wordsCollection.getDocument("word3")!!.toMutable()
        auxDoc.remove("vector")
        wordsCollection.save(auxDoc)

        auxDoc = wordsCollection.getDocument("word4")!!.toMutable()
        val vector = auxDoc.getArray("vector")
        vector!!.remove(0)
        wordsCollection.save(auxDoc)

        val expr = "prediction(WordEmbedding,{\"word\": word}).vector"
        val config = VectorIndexConfiguration(expression = expr, dimensions = 300, centroids = 8)
        createWordsIndex(config)

        var rs = executeWordsQuery(limit = 350, vectorExpression = expr)
        var wordMap = toDocIDWordMap(rs)
        assertEquals(296, wordMap.size)
        assertNull(wordMap["word1"])
        assertNull(wordMap["word2"])
        assertNull(wordMap["word3"])
        assertNull(wordMap["word4"])
        assertTrue(checkIndexWasTrained())

        auxDoc = wordsCollection.getDocument("word5")!!.toMutable()
        auxDoc.setString("word", "Fried Chicken")
        wordsCollection.save(auxDoc)

        rs = executeWordsQuery(limit = 350, vectorExpression = expr)
        wordMap = toDocIDWordMap(rs)
        assertEquals(295, wordMap.size)
        assertNull(wordMap["word5"])
    }

    /**
     * 10. TestCreateVectorIndexWithSQ
     * Description
     *     Using different types of the Scalar Quantizer Encoding, test that the vector
     *     index can be created and used.
     * Steps
     *     1. Copy database words_db.
     *     2. Register a custom logger to capture the INFO log.
     *     3. Create a vector index named "words_index" in _default.words collection.
     *         - expression: "vector"
     *         - dimensions: 300
     *         - centroids: 8
     *         - encoding: ScalarQuantizer(type: SQ4)
     *     4. Check that the index is created without an error returned.
     *     5. Create an SQL++ query
     *         - SELECT meta().id, word
     *           FROM _default.words
     *           ORDER BY APPROX_VECTOR_DISTANCE(vector, $dinerVector)
     *           LIMIT 20
     *     6. Check the explain() result of the query to ensure that the "words_index" is used.
     *     7. Execute the query and check that 20 results are returned.
     *     8. Verify that the index was trained by checking that the “Untrained index; queries may be slow”
     *       doesn’t exist in the log.
     *     9. Delete the "words_index".
     *     10. Reset the custom logger.
     *     11. Repeat Step 2 – 10 by using SQ6 and SQ8 respectively.
     */
    @Test
    fun testCreateVectorIndexWithSQ() {
        val config = VectorIndexConfiguration(expression = "vector", dimensions = 300, centroids = 8)
        config.encoding = VectorEncoding.scalarQuantizer(VectorEncoding.ScalarQuantizerType.SQ4)
        createWordsIndex(config)

        // Query:
        var rs = executeWordsQuery(limit = 20)
        assertEquals(20, rs.allResults().size)

        // Repeat using SQ6
        resetIndexWasTrainedLog()
        deleteWordsIndex()
        config.encoding = VectorEncoding.scalarQuantizer(VectorEncoding.ScalarQuantizerType.SQ6)
        createWordsIndex(config)

        // Rerun query:
        rs = executeWordsQuery(limit = 20)
        assertEquals(20, rs.allResults().size)

        // Repeat using SQ8
        resetIndexWasTrainedLog()
        deleteWordsIndex()
        config.encoding = VectorEncoding.scalarQuantizer(VectorEncoding.ScalarQuantizerType.SQ8)
        createWordsIndex(config)

        // Rerun query:
        rs = executeWordsQuery(limit = 20)
        assertEquals(20, rs.allResults().size)
    }

    /**
     * 11. TestCreateVectorIndexWithNoneEncoding
     * Description
     *     Using the None Encoding, test that the vector index can be created and used.
     * Steps
     *     1. Copy database words_db.
     *     2. Register a custom logger to capture the INFO log.
     *     3. Create a vector index named "words_index" in _default.words collection.
     *         - expression: "vector"
     *         - dimensions: 300
     *         - centroids: 8
     *         - encoding: None
     *     4. Check that the index is created without an error returned.
     *     5. Create an SQL++ query.
     *         - SELECT meta().id, word
     *           FROM _default.words
     *           ORDER BY APPROX_VECTOR_DISTANCE(vector, $dinerVector)
     *           LIMIT 20
     *     6. Check the explain() result of the query to ensure that the "words_index" is used.
     *     7. Execute the query and check that 20 results are returned.
     *     8. Verify that the index was trained by checking that the “Untrained index; queries may be slow”
     *       doesn’t exist in the log.
     *     9. Reset the custom logger.
     */
    @Test
    fun testCreateVectorIndexWithNoneEncoding() {
        val config = VectorIndexConfiguration(expression = "vector", dimensions = 300, centroids = 8)
        config.encoding = VectorEncoding.none()
        createWordsIndex(config)

        val rs = executeWordsQuery(limit = 20)
        assertEquals(20, rs.allResults().size)
    }

    /**
     * 12. TestCreateVectorIndexWithPQ
     * Description
     *     Using the PQ Encoding, test that the vector index can be created and used. The
     *     test also tests the lower and upper bounds of the PQ’s bits.
     * Steps
     *     1. Copy database words_db.
     *     2. Register a custom logger to capture the INFO log.
     *     3. Create a vector index named "words_index" in _default.words collection.
     *         - expression: "vector"
     *         - dimensions: 300
     *         - centroids: 8
     *         - encoding : PQ(subquantizers: 5 bits: 8)
     *     4. Check that the index is created without an error returned.
     *     5. Create an SQL++ query.
     *         - SELECT meta().id, word
     *           FROM _default.words
     *           ORDER BY APPROX_VECTOR_DISTANCE(vector, $dinerVector)
     *           LIMIT 20
     *     6. Check the explain() result of the query to ensure that the "words_index" is used.
     *     7. Execute the query and check that 20 results are returned.
     *     8. Verify that the index was trained by checking that the “Untrained index; queries may be slow”
     *       doesn’t exist in the log.
     *     9. Delete the “words_index”.
     *     10. Reset the custom logger.
     *     11. Repeat steps 2 to 10 by changing the PQ’s bits to 4 and 12 respectively.
     */
    @Test
    fun testCreateVectorIndexWithPQ() {
        for (numberOfBits in arrayOf(8, 4, 12)) {
            // Create vector index
            val config = VectorIndexConfiguration(expression = "vector", dimensions = 300, centroids = 8)
            config.encoding = VectorEncoding.productQuantizer(subquantizers = 5, bits = numberOfBits.toLong())
            createWordsIndex(config)

            // Query:
            val rs = executeWordsQuery(limit = 20, checkTraining = false)
            assertEquals(20, rs.allResults().size)

            // Delete index
            deleteWordsIndex()

            // Reset log
            resetIndexWasTrainedLog()
        }
    }

    /**
     * 13. TestSubquantizersValidation
     * Description
     *     Test that the PQ’s subquantizers value is validated with dimensions correctly.
     *     The invalid argument exception should be thrown when the vector index is created
     *     with invalid subquantizers which are not a divisor of the dimensions or zero.
     * Steps
     *     1. Copy database words_db.
     *     2. Create a vector index named "words_index" in _default.words collection.
     *         - expression: "vector"
     *         - dimensions: 300
     *         - centroids: 8
     *         - PQ(subquantizers: 2, bits: 8)
     *     3. Check that the index is created without an error returned.
     *     4. Delete the "words_index".
     *     5. Repeat steps 2 to 4 by changing the subquantizers to
     *       3, 4, 5, 6, 10, 12, 15, 20, 25, 30, 50, 60, 75, 100, 150, and 300.
     *     6. Repeat step 2 to 4 by changing the subquantizers to 0 and 7.
     *     7. Check that an invalid argument exception is thrown.
     */
    @Test
    fun testSubquantizersValidation() {
        val config = VectorIndexConfiguration(expression = "vector", dimensions = 300, centroids = 8)
        config.encoding = VectorEncoding.productQuantizer(subquantizers = 2, bits = 8)
        createWordsIndex(config)

        // Step 5: Use valid subquantizer values
        for (numberOfSubq in arrayOf(3, 4, 5, 6, 10, 12, 15, 20, 25, 30, 50, 60, 75, 100, 150, 300)) {
            deleteWordsIndex()
            config.encoding = VectorEncoding.productQuantizer(subquantizers = numberOfSubq.toLong(), bits = 8)
            createWordsIndex(config)
        }

        // Step 7: Check if exception thrown for wrong subquantizers:
        for (numberOfSubq in arrayOf(0, 7)) {
            deleteWordsIndex()
            assertFailsWith<IllegalArgumentException> {
                config.encoding = VectorEncoding.productQuantizer(subquantizers = numberOfSubq.toLong(), bits = 8)
                createWordsIndex(config)
            }
        }
    }

    /**
     * The test will fail when using centroid = 20 as the number of vectors for training
     * the index is not low.
     *
     * 14. TestCreateVectorIndexWithFixedTrainingSize
     * Description
     *     Test that the vector index can be created and trained when minTrainingSize
     *     equals to maxTrainingSize.
     * Steps
     *     1. Copy database words_db.
     *     2. Register a custom logger to capture the INFO log.
     *     3. Create a vector index named "words_index" in _default.words collection.
     *         - expression: "vector"
     *         - dimensions: 300
     *         - centroids: 8
     *         - minTrainingSize: 100 and maxTrainingSize: 100
     *     4. Check that the index is created without an error returned.
     *     5. Create an SQL++ query.
     *         - SELECT meta().id, word
     *           FROM _default.words
     *           ORDER BY APPROX_VECTOR_DISTANCE(vector, $dinerVector)
     *           LIMIT 20
     *     5. Check the explain() result of the query to ensure that the "words_index" is used.
     *     6. Execute the query and check that 20 results are returned.
     *     7. Verify that the index was trained by checking that the “Untrained index; queries may be slow”
     *       doesn’t exist in the log.
     *     8. Reset the custom logger.
     */
    @Test
    fun testeCreateVectorIndexWithFixedTrainingSize() {
        val config = VectorIndexConfiguration(expression = "vector", dimensions = 300, centroids = 8)
        config.minTrainingSize = 100
        config.maxTrainingSize = 100
        createWordsIndex(config)

        val rs = executeWordsQuery(limit = 20)
        assertEquals(20, rs.allResults().size)
    }

    /**
     * 15. TestValidateMinMaxTrainingSize
     * Description
     *     Test that the minTrainingSize and maxTrainingSize values are validated
     *     correctly. The invalid argument exception should be thrown when the vector index
     *     is created with invalid minTrainingSize or maxTrainingSize.
     * Steps
     *     1. Copy database words_db.
     *     2. Create a vector index named "words_index" in _default.words collection.
     *         - expression: "vector"
     *         - dimensions: 300
     *         - centroids: 20
     *         - minTrainingSize: 1 and maxTrainingSize: 100
     *     3. Check that the index is created without an error returned.
     *     4. Delete the "words_index"
     *     5. Repeat Step 2 with the following case:
     *         - minTrainingSize = 10 and maxTrainingSize = 9
     *     6. Check that an invalid argument exception was thrown.
     */
    @Test
    fun testValidateMinMaxTrainingSize() {
        val config = VectorIndexConfiguration(expression = "vector", dimensions = 300, centroids = 20)
        config.minTrainingSize = 1
        config.maxTrainingSize = 100
        createWordsIndex(config)

        deleteWordsIndex()
        config.minTrainingSize = 10
        config.maxTrainingSize = 9
        assertFailsWith<IllegalArgumentException> {
            createWordsIndex(config)
        }
    }

    /**
     * 16. TestQueryUntrainedVectorIndex
     * Description
     *     Test that the untrained vector index can be used in queries.
     * Steps
     *     1. Copy database words_db.
     *     2. Register a custom logger to capture the INFO log.
     *     3. Create a vector index named "words_index" in _default.words collection.
     *         - expression: "vector"
     *         - dimensions: 300
     *         - centroids: 8
     *         - minTrainingSize: 400
     *         - maxTrainingSize: 500
     *     4. Check that the index is created without an error returned.
     *     5. Create an SQL++ query.
     *         - SELECT meta().id, word
     *           FROM _default.words
     *           ORDER BY APPROX_VECTOR_DISTANCE(vector, $dinerVector)
     *           LIMIT 20
     *     6. Check the explain() result of the query to ensure that the "words_index" is used.
     *     7. Execute the query and check that 20 results are returned.
     *     8. Verify that the index was not trained by checking that the “Untrained index;
     *       queries may be slow” message exists in the log.
     *     9. Reset the custom logger.
     */
    @Test
    fun testQueryUntrainedVectorIndex() {
        val config = VectorIndexConfiguration(expression = "vector", dimensions = 300, centroids = 8)
        // out of bounds (300 words in db)
        config.minTrainingSize = 400
        config.maxTrainingSize = 500
        createWordsIndex(config)

        val rs = executeWordsQuery(limit = 20, checkTraining = false)
        assertEquals(20, rs.allResults().size)
        assertFalse(checkIndexWasTrained())
    }

    /**
     * 17. TestCreateVectorIndexWithDistanceMetric
     * Description
     *    Test that the vector index can be created with all supported distance metrics.
     * Steps
     *    1. Copy database words_db.
     *    2. For each distance metric types : euclideanSquared, euclidean, cosine, and dot,
     *      create a vector index named "words_index" in _default.words collection:
     *       - expression: "vector"
     *       - dimensions: 300
     *       - centroids : 8
     *       - metric: <distance-metric>
     *     3. Check that the index is created without an error returned.
     *    4. Create an SQL++ query with the correspoding SQL++ metric name string:
     *      "EUCLIDEAN_SQUARED", "EUCLIDEAN", "COSINE", and "DOT"
     *        - SELECT meta().id, word
     *         FROM _default.words
     *         ORDER BY APPROX_VECTOR_DISTANCE(vector, $dinerVector, "<metric-name>")
     *         LIMIT 20
     *    5. Check the explain() result of the query to ensure that the "words_index" is used.
     *    6. Verify that the index was trained.
     *    7. Execute the query and check that 20 results are returned.
     */
    @Test
    fun testCreateVectorIndexWithDistanceMetric() {
        val metrics = VectorIndexConfiguration.DistanceMetric.entries
        for (metric in metrics) {
            val config = VectorIndexConfiguration(expression = "vector", dimensions = 300, centroids = 8)
            config.metric = metric
            createWordsIndex(config)

            val rs = executeWordsQuery(limit = 20, metric = metric.name)
            val results = rs.allResults()
            assertEquals(20, results.size)
        }
    }

    /**
     * 19. TestCreateVectorIndexWithExistingName
     * Description
     *     Test that creating a new vector index with an existing name is fine if the index
     *     configuration is the same or not.
     * Steps
     *     1. Copy database words_db.
     *     2. Create a vector index named "words_index" in _default.words collection.
     *         - expression: "vector"
     *         - dimensions: 300
     *         - centroids: 8
     *     3. Check that the index is created without an error returned.
     *     4. Repeat step 2 and check that the index is created without an error returned.
     *     5. Create a vector index named "words_index" in _default.words collection.
     *         - expression: "vectors"
     *         - dimensions: 300
     *         - centroids: 8
     *     6. Check that the index is created without an error returned.
     */
    @Test
    fun testCreateVectorIndexWithExistingName() {
        // Create and recreate vector index using the same config
        val config1 = VectorIndexConfiguration(expression = "vector", dimensions = 300, centroids = 8)
        createWordsIndex(config1)
        createWordsIndex(config1)

        // Recreate index with same name using different config
        val config2 = VectorIndexConfiguration(expression = "vectors", dimensions = 300, centroids = 8)
        createWordsIndex(config2)
    }

    /**
     * 20. TestDeleteVectorIndex
     * Description
     *     Test that creating a new vector index with an existing name is fine if the index
     *     configuration is the same. Otherwise, an error will be returned.
     * Steps
     *     1. Copy database words_db.
     *     2. Register a custom logger to capture the INFO log.
     *     3. Create a vector index named "words_index" in _default.words collection.
     *         - expression: "vectors"
     *         - dimensions: 300
     *         - centroids: 8
     *     4. Check that the index is created without an error returned.
     *     5. Create an SQL++ query.
     *         - SELECT meta().id, word
     *           FROM _default.words
     *           ORDER BY APPROX_VECTOR_DISTANCE(vector, $dinerVector)
     *           LIMIT 20
     *     6. Check the explain() result of the query to ensure that the "words_index" is used.
     *     7. Execute the query and check that 20 results are returned.
     *     8. Verify that the index was trained by checking that the “Untrained index; queries may be slow”
     *       doesn’t exist in the log.
     *     9. Delete index named "words_index".
     *     10. Check that getIndexes() does not contain "words_index".
     *     11. Create the same query again and check that a CouchbaseLiteException is returned
     *        as the index doesn’t exist.
     *     12. Reset the custom logger.
     */
    @Test
    fun testDeleteVectorIndex() {
        val config = VectorIndexConfiguration(expression = "vector", dimensions = 300, centroids = 8)
        createWordsIndex(config)

        val rs = executeWordsQuery(limit = 20)
        assertEquals(20, rs.allResults().size)

        deleteWordsIndex()
        val names = wordsCollection.indexes
        assertFalse(names.contains("words_index"))

        assertThrowsCBLException(CBLError.Domain.CBLITE, CBLError.Code.MISSING_INDEX) {
            executeWordsQuery(limit = 20)
        }
    }

    /**
     * 21. TestVectorMatchOnNonExistingIndex
     * Description
     *     Test that an error will be returned when creating a vector match query that uses
     *     a non existing index.
     * Steps
     *     1. Copy database words_db.
     *     2. Create an SQL++ query.
     *         - SELECT meta().id, word
     *           FROM _default.words
     *           ORDER BY APPROX_VECTOR_DISTANCE(vector, $dinerVector)
     *           LIMIT 20
     *     3. Check that a CouchbaseLiteException is returned as the index doesn’t exist.
     */
    @Test
    fun testVectorMatchOnNonExistingIndex() {
        assertThrowsCBLException(CBLError.Domain.CBLITE, CBLError.Code.MISSING_INDEX) {
            executeWordsQuery(limit = 20)
        }
    }

    /**
     * 23. TestVectorMatchLimitBoundary
     * Description
     *     Test vector_match’s limit boundary which is between 1 - 10000 inclusively. When
     *     creating vector_match queries with an out-out-bound limit, an error should be
     *     returned.
     * Steps
     *     1. Copy database words_db.
     *     2. Create a vector index named "words_index" in _default.words collection.
     *         - expression: "vector"
     *         - dimensions: 300
     *         - centroids: 8
     *     3. Check that the index is created without an error returned.
     *     4. Create an SQL++ query.
     *         - SELECT meta().id, word
     *           FROM _default.words
     *           ORDER BY APPROX_VECTOR_DISTANCE(vector, $dinerVector)
     *           LIMIT <limit>
     *         - limit: 1 and 10000
     *     5. Check that the query can be created without an error.
     *     6. Repeat step 4 with the limit: -1, 0, and 10001
     *     7. Check that a CouchbaseLiteException is returned when creating the query.
     */
    @Test
    fun testVectorMatchLimitBoundary() {
        val config = VectorIndexConfiguration(expression = "vector", dimensions = 300, centroids = 8)
        createWordsIndex(config)

        // Check valid query with -1, 0, 1 and 10000 set limit
        for (limit in arrayOf(-1, 0, 1, 10000)) {
            executeWordsQuery(limit = limit)
        }

        // Check if error thrown for wrong limit values
        assertThrowsCBLException(CBLError.Domain.CBLITE, CBLError.Code.INVALID_QUERY) {
            executeWordsQuery(limit = 10001)
        }
    }

    /**
     * 24. TestHybridVectorSearch
     * Description
     *     Test a simple hybrid search with WHERE clause.
     * Steps
     *     1. Copy database words_db.
     *     2. Register a custom logger to capture the INFO log.
     *     3. Create a vector index named "words_index" in _default.words collection.
     *         - expression: "vector"
     *         - dimensions: 300
     *         - centroids: 8
     *     4. Check that the index is created without an error returned.
     *     5. Create an SQL++ query.
     *         - SELECT word, catid
     *           FROM _default.words
     *           WHERE catid = "cat1"
     *           ORDER BY APPROX_VECTOR_DISTANCE(vector, $dinerVector)
     *           LIMIT 300
     *     6. Check that the query can be created without an error.
     *     7. Check the explain() result of the query to ensure that the "words_index" is used.
     *     8. Execute the query and check that the number of results returned is 50
     *       (there are 50 words in catid=1), and the results contain only catid == 'cat1'.
     *     9. Verify that the index was trained by checking that the “Untrained index; queries may be slow”
     *       doesn’t exist in the log.
     *     10. Reset the custom logger.
     */
    @Test
    fun testHybridVectorSearch() {
        val config = VectorIndexConfiguration(expression = "vector", dimensions = 300, centroids = 8)
        createWordsIndex(config)

        val rs = executeWordsQuery(limit = 300, whereExpression = "catid = 'cat1'")
        val results = rs.allResults()
        assertEquals(50, results.size)
        for (result in results) {
            assertEquals("cat1", result.getValue(2))
        }
    }

    /**
     * 25. TestHybridVectorSearchWithAND
     * Description
     *     Test hybrid search with multiple AND
     * Steps
     *     1. Copy database words_db.
     *     2. Register a custom logger to capture the INFO log.
     *     3. Create a vector index named "words_index" in _default.words collection.
     *         - expression: "vector"
     *         - dimensions: 300
     *         - centroids: 8
     *     4. Check that the index is created without an error returned.
     *     5. Create an SQL++ query.
     *         - SELECT word, catid
     *           FROM _default.words
     *           WHERE catid = "cat1" AND word is valued
     *           ORDER BY APPROX_VECTOR_DISTANCE(vector, $dinerVector)
     *           LIMIT 300
     *     6. Check that the query can be created without an error.
     *     7. Check the explain() result of the query to ensure that the "words_index" is used.
     *     8. Execute the query and check that the number of results returned is 50
     *       (there are 50 words in catid=1), and the results contain only catid == 'cat1'.
     *     9. Verify that the index was trained by checking that the “Untrained index; queries may be slow”
     *       doesn’t exist in the log.
     *     10. Reset the custom logger.
     */
    @Test
    fun testHybridVectorSearchWithAND() {
        val config = VectorIndexConfiguration(expression = "vector", dimensions = 300, centroids = 8)
        createWordsIndex(config)

        val rs = executeWordsQuery(limit = 300, whereExpression = "word is valued AND catid = 'cat1'")
        val results = rs.allResults()
        assertEquals(50, results.size)
        for (result in results) {
            assertEquals("cat1", result.getValue(2))
        }
    }

    /**
     * 26. TestInvalidHybridVectorSearchWithOR
     * Description
     *     Test that APPROX_VECTOR_DISTANCE cannot be used with OR expression.
     * Steps
     *     1. Copy database words_db.
     *     2. Create a vector index named "words_index" in _default.words collection.
     *         - expression: "vector"
     *         - dimensions: 300
     *         - centroids: 20
     *     3. Check that the index is created without an error returned.
     *     4. Create an SQL++ query.
     *         - SELECT word, catid
     *           FROM _default.words
     *           WHERE APPROX_VECTOR_DISTANCE(vector, $dinerVector) < 0.5 OR catid = 'cat1'
     *           ORDER BY APPROX_VECTOR_DISTANCE(vector, $dinerVector)
     *           LIMIT 20
     *     5. Check that a CouchbaseLiteException is returned when creating the query.
     */
    @Test
    fun testInvalidHybridVectorSearchWithOR() {
        val config = VectorIndexConfiguration(expression = "vector", dimensions = 300, centroids = 8)
        createWordsIndex(config)

        assertThrowsCBLException(CBLError.Domain.CBLITE, CBLError.Code.INVALID_QUERY) {
            executeWordsQuery(limit = 300, whereExpression = $$"APPROX_VECTOR_DISTANCE(vector, $vector) < 0.5 OR catid = 'cat1'")
        }
    }

    /**
     * 27. TestIndexVectorInBase64
     * Description
     *     Test that the vector in Base64 string can be indexed.
     * Steps
     *     1. Copy database words_db.
     *     2. Get the vector value from _default.words.word49's vector property as an array of floats.
     *     3. Convert the array of floats from Step 2 into binary data and then into Base64 string.
     *         - See "Vector in Base64 for Lunch" section for the pre-calculated base64 string
     *     4. Update _default.words.word49 with "vector" = Base64 string from Step 3.
     *     5. Create a vector index named "words_index" in _default.words collection.
     *         - expression: "vector"
     *         - dimensions: 300
     *         - centroids : 8
     *     6. Check that the index is created without an error returned.
     *     7. Create an SQL++ query:
     *         - SELECT meta().id, word,
     *            FROM _default.words
     *            WHERE vector_match(words_index, <dinner vector>)
     *            LIMIT 20
     *     8. Execute the query and check that 20 results are returned.
     *     9. Check that the result also contains doc id = word49.
     */
    @Test
    fun testIndexVectorInBase64() {
        val doc = wordsCollection.getDocument("word49")!!.toMutable()
        doc.setString("vector", lunchVectorBase64)
        wordsCollection.save(doc)

        val config = VectorIndexConfiguration(expression = "vector", dimensions = 300, centroids = 8)
        createWordsIndex(config)

        val rs = executeWordsQuery(limit = 20)
        val wordMap = toDocIDWordMap(rs)
        assertEquals(20, wordMap.size)
        assertNotNull(wordMap["word49"])
    }

    /**
     * 28. TestNumProbes
     * Description
     *     Test that the numProces specified is effective.
     * Steps
     *     1. Copy database words_db.
     *     2. Create a vector index named "words_index" in _default.words collection.
     *         - expression: "vector"
     *         - dimensions: 300
     *         - centroids : 8
     *         - numProbes: 5
     *     3. Check that the index is created without an error returned.
     *     4. Create an SQL++ query:
     *         - SELECT meta().id, word
     *           FROM _default.words
     *           ORDER BY APPROX_VECTOR_DISTANCE(vector, $dinerVector)
     *           LIMIT 300
     *     5. Execute the query and check that 20 results are returned.
     *     6. Repeat step 2 - 6 but change the numProbes to 1.
     *     7. Verify the number of results returned in Step 5 is larger than Step 6.
     */
    @Test
    fun testNumProbes() {
        val config = VectorIndexConfiguration(expression = "vector", dimensions = 300, centroids = 8)
        config.numProbes = 5
        createWordsIndex(config)
        var rs = executeWordsQuery(limit = 300)
        val numResultsFor5Probes = rs.allResults().size
        assertTrue(numResultsFor5Probes > 0)

        config.numProbes = 1
        createWordsIndex(config)
        rs = executeWordsQuery(limit = 300)
        val numResultsFor1Probes = rs.allResults().size
        assertTrue(numResultsFor1Probes > 0)

        assertTrue(numResultsFor5Probes > numResultsFor1Probes)
    }
}
