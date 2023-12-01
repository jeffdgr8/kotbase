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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.fail

@OptIn(ExperimentalStdlibApi::class)
class CollectionQueryTest : BaseQueryTest() {

    // Section 8.11

    // 8.11.1a: Test that query the default collection by using each default collection
    // identity works as expected.  Populate "names_100" data into the default collection.
    // Use the following SQL++ queries to create and execute the query:
    //     SELECT name.first FROM _ ORDER BY name.first LIMIT 1
    // Ensure that the result set has one result, and the data is { "first" : "Abe"}.
    @Test
    fun testQueryDefaultCollectionA() {
        val defaultCollection = testDatabase.getDefaultCollection()
        assertNotNull(defaultCollection!!)
        loadJSONResourceIntoCollection("names_100.json", collection = defaultCollection)

        // create with _
        verifyQuery(
            QueryBuilder.createQuery("SELECT name.first FROM _ ORDER BY name.first LIMIT 1", testDatabase),
            1
        ) { _, result ->
            assertEquals(1, result.count().toLong())
            assertEquals("{\"first\":\"Abe\"}", result.toJSON())
            assertEquals("Abe", result.getValue("first"))
            assertEquals(result.getValue(0), result.getValue("first"))
        }
    }

    // 8.11.1b: Test that query the default collection by using each default collection
    // identity works as expected.  Populate "names_100" data into the default collection.
    // Use the following SQL++ queries to create and execute the query:
    //     SELECT name.first FROM _default ORDER BY limit 1
    // Ensure that the result set has one result, and the data is { "first" : "Abe"}.
    @Test
    fun testQueryDefaultCollectionB() {
        val defaultCollection = testDatabase.getDefaultCollection()
        assertNotNull(defaultCollection!!)
        loadJSONResourceIntoCollection("names_100.json", collection = defaultCollection)
        verifyQuery(
            QueryBuilder.createQuery("SELECT name.first FROM _default ORDER BY name.first LIMIT 1", testDatabase),
            1
        ) { _, result ->
            assertEquals(1, result.count().toLong())
            assertEquals("{\"first\":\"Abe\"}", result.toJSON())
            assertEquals("Abe", result.getValue("first"))
            assertEquals(result.getValue(0), result.getValue("first"))
        }
    }

    // 8.11.1c: Test that query the default collection by using each default collection
    // identity works as expected.  Populate "names_100" data into the default collection.
    // Use the following SQL++ queries to create and execute the query:
    //     SELECT name.first FROM <DB-NAME> ORDER BY name.first limit 1
    // Ensure that the result set has one result, and the data is { "first" : "Abe"}.
    @Test
    fun testQueryDefaultCollectionC() {
        val defaultCollection = testDatabase.getDefaultCollection()
        assertNotNull(defaultCollection!!)
        loadJSONResourceIntoCollection("names_100.json", collection = defaultCollection)
        val query = QueryBuilder.createQuery(
            "SELECT name.first FROM " + testDatabase.name + " ORDER BY name.first LIMIT 1",
            testDatabase
        )
        verifyQuery(query, 1) { _, result ->
            assertEquals(1, result.count().toLong())
            assertEquals("{\"first\":\"Abe\"}", result.toJSON())
            assertEquals("Abe", result.getValue("first"))
            assertEquals(result.getValue(0), result.getValue("first"))
        }
    }

    // 8.11.2a: Test that query a collection in the default scope works as expected.
    // Create a "names" collection in the default scope.
    // Populate "names_100" data into the names collection.
    // Use the following SQL++ queries to create and execute the query:
    //     SELECT name.first FROM _default.names BY name.first LIMIT 1
    // Ensure that the result set has one result, and the data is { "first" : "Abe"}.
    @Test
    fun testSQLPPQueryDefaultScopeA() {
        val collection = testDatabase.createCollection("names")
        loadJSONResourceIntoCollection("names_100.json", collection = collection)
        verifyQuery(
            QueryBuilder.createQuery("SELECT name.first FROM _default.names ORDER BY name.first LIMIT 1", testDatabase),
            1
        ) { _, result ->
            assertEquals(1, result.count().toLong())
            assertEquals("{\"first\":\"Abe\"}", result.toJSON())
            assertEquals("Abe", result.getValue("first"))
            assertEquals(result.getValue(0), result.getValue("first"))
        }
    }

    // 8.11.2b: Test that query a collection in the default scope works as expected.
    // Create a "names" collection in the default scope.
    // Populate "names_100" data into the names collection.
    // Use the following SQL++ queries to create and execute the query:
    //     SELECT name.first FROM names BY name.first LIMIT 1
    // Ensure that the result set has one result, and the data is { "first" : "Abe"}.
    @Test
    fun testSQLPPQueryDefaultScopeB() {
        val collection = testDatabase.createCollection("names")
        loadJSONResourceIntoCollection("names_100.json", collection = collection)
        verifyQuery(
            QueryBuilder.createQuery("SELECT name.first FROM names ORDER BY name.first LIMIT 1", testDatabase),
            1
        ) { _, result ->
            assertEquals(1, result.count().toLong())
            assertEquals("{\"first\":\"Abe\"}", result.toJSON())
            assertEquals("Abe", result.getValue("first"))
            assertEquals(result.getValue(0), result.getValue("first"))
        }
    }

    // 8.11.3: Test that query a collection in non-default scope works as expected.
    // Create a "names" collection in the scope named "people".
    // Populate "names_100" data into the names collection.
    // Use the following SQL++ queries to create and execute the query:
    //     SELECT name.first FROM people.names BY name.first LIMIT 1
    // Ensure that the result set has one result, and the data is { "first" : "Abe"}.
    @Test
    fun testSQLPPQueryNamedCollection() {
        val collection = testDatabase.createCollection("names", "people")
        loadJSONResourceIntoCollection("names_100.json", collection = collection)
        verifyQuery(
            QueryBuilder.createQuery("SELECT name.first FROM people.names ORDER BY name.first LIMIT 1", testDatabase),
            1
        ) { _, result ->
            assertEquals(1, result.count().toLong())
            assertEquals("{\"first\":\"Abe\"}", result.toJSON())
            assertEquals("Abe", result.getValue("first"))
            assertEquals(result.getValue(0), result.getValue("first"))
        }
    }

    // 8.11.4: Test that query non-existing collection returns an error as expected.
    // Create a "names" collection in the scope named "people".
    // Populate "names_100" data into the names collection.
    // Use the following SQL++ query to create and execute the query:
    //     SELECT name.first FROM person.names BY name.first LIMIT 1
    // Ensure that an error is returned or thrown when executing the query.
    @Test
    fun testSQLPPQueryNonExistingCollection() {
        val collection = testDatabase.createCollection("names", "people")
        loadJSONResourceIntoCollection("names_100.json", collection = collection)
        val queryString = "SELECT name.first FROM person.names ORDER BY name.first LIMIT 1"
        val query = QueryBuilder.createQuery(queryString, testDatabase)
        assertThrowsCBLException(CBLError.Domain.CBLITE, CBLError.Code.INVALID_QUERY) {
            query.execute().use { }
        }
    }

    // 8.11.5a: Test that query by joining collections works as expected.
    // Create two collections named "flowers" and "colors" in the scope named "test".
    // Populate the following docs in the flowers collection:
    //     { “name”: "rose", “cid”: "c1" }
    //     { “name”: "hydrangea", “cid”: "c2" }
    // Populate the following docs in the colors collection:
    //     { “cid”: "c1", “color”: "red" }
    //     { “cid”: "c2", “color”: "blue" }
    //     { “cid”: "c3", “color”: "white" }
    // Use the following SQL++ queries to create and execute the query:
    //     SELECT .flowers.name, colors.color
    //         FROM test.flowers
    //         JOIN test.colors
    //         ON flowers.cid = colors.cid
    //         ORDER BY flowers.name
    // Ensure that the result set has two results as :
    //         { “name”: "hydrangea", “color”: "blue" }
    //         { “name”: "rose", “color”: "red" }
    @Test
    fun testSQLPPJoinWithCollectionsA() {
        val flowerCol = testDatabase.createCollection("flowers", "test")
        val colorCol = testDatabase.createCollection("colors", "test")
        var doc = MutableDocument()
        doc.setValue("name", "rose")
        doc.setValue("cid", "c1")
        saveDocInCollection(doc, flowerCol)
        doc = MutableDocument()
        doc.setValue("name", "hydrangea")
        doc.setValue("cid", "c2")
        saveDocInCollection(doc, flowerCol)
        doc = MutableDocument()
        doc.setValue("cid", "c1")
        doc.setValue("color", "red")
        saveDocInCollection(doc, colorCol)
        doc = MutableDocument()
        doc.setValue("cid", "c2")
        doc.setValue("color", "blue")
        saveDocInCollection(doc, colorCol)
        doc = MutableDocument()
        doc.setValue("cid", "c3")
        doc.setValue("color", "white")
        saveDocInCollection(doc, colorCol)

        // create with query string
        val query = QueryBuilder.createQuery(
            "SELECT flowers.name, colors.color"
                    + " FROM test.flowers"
                    + " JOIN test.colors"
                    + " ON flowers.cid = colors.cid"
                    + " ORDER BY flowers.name",
            testDatabase
        )
        verifyQuery(query, 2) { _, result ->
            when (val name = result.getString("name")) {
                "hydrangea" -> assertEquals("blue", result.getString("color"))
                "rose" -> assertEquals("red", result.getString("color"))
                else -> fail("unexpected name: $name")
            }
            assertEquals(2, result.count().toLong())
            assertEquals(result.getValue(0), result.getValue("name"))
            assertEquals(result.getValue(1), result.getValue("color"))
        }
    }

    // 8.11.5b: Test that query by joining collections works as expected.
    // Create two collections named "flowers" and "colors" in the scope named "test".
    // Populate the following docs in the flowers collection:
    //     { “name”: "rose", “cid”: "c1" }
    //     { “name”: "hydrangea", “cid”: "c2" }
    // Populate the following docs in the colors collection:
    //     { “cid”: "c1", “color”: "red" }
    //     { “cid”: "c2", “color”: "blue" }
    //     { “cid”: "c3", “color”: "white" }
    // Use the following SQL++ queries to create and execute the query:
    //     SELECT f.name, c.color
    //         FROM test.flowers f
    //         JOIN test.colors c
    //         ON f.cid = c.cid
    //         ORDER BY f.name
    // Ensure that the result set has two results as :
    //     { “name”: "hydrangea", “color”: "blue" }
    //     { “name”: "rose", “color”: "red" }
    //
    @Test
    fun testSQLPPJoinWithCollectionsB() {
        val flowerCol = testDatabase.createCollection("flowers", "test")
        val colorCol = testDatabase.createCollection("colors", "test")
        var doc = MutableDocument()
        doc.setValue("name", "rose")
        doc.setValue("cid", "c1")
        saveDocInCollection(doc, flowerCol)
        doc = MutableDocument()
        doc.setValue("name", "hydrangea")
        doc.setValue("cid", "c2")
        saveDocInCollection(doc, flowerCol)
        doc = MutableDocument()
        doc.setValue("cid", "c1")
        doc.setValue("color", "red")
        saveDocInCollection(doc, colorCol)
        doc = MutableDocument()
        doc.setValue("cid", "c2")
        doc.setValue("color", "blue")
        saveDocInCollection(doc, colorCol)
        doc = MutableDocument()
        doc.setValue("cid", "c3")
        doc.setValue("color", "white")
        saveDocInCollection(doc, colorCol)
        val query = QueryBuilder.createQuery(
            ("SELECT f.name, c.color"
                    + " FROM test.flowers AS f"
                    + " JOIN test.colors AS c"
                    + " ON f.cid = c.cid "
                    + " ORDER BY f.name"),
            testDatabase
        )
        verifyQuery(query, 2) { _, result ->
            when (val name = result.getString("name")) {
                "hydrangea" -> assertEquals("blue", result.getString("color"))
                "rose" -> assertEquals("red", result.getString("color"))
                else -> fail("unexpected name: $name")
            }
            assertEquals(2, result.count().toLong())
            assertEquals(result.getValue(0), result.getValue("name"))
            assertEquals(result.getValue(1), result.getValue("color"))
        }
    }

    // 8.11.6a: Test that query with match() and rank() function using the full-text index
    // created in the default collection returns the result as expected.
    // Use the following cases to reference the full-text index.
    //     <index-name>
    @Test
    fun testFTSQueryWithFullTextIndexInDefaultCollectionA() {
        val defaultCollection = testDatabase.getDefaultCollection()
        assertNotNull(defaultCollection!!)

        loadJSONResourceIntoCollection("sentences.json", collection = defaultCollection)

        defaultCollection.createIndex("sentence", IndexBuilder.fullTextIndex(FullTextIndexItem.property("sentence")))

        val idxName = "sentence"
        val query = QueryBuilder.createQuery(
            "SELECT _id, sentence" +
                    " FROM _" +
                    " WHERE MATCH(${idxName}, \"Dummie woman\")" +
                    " ORDER BY RANK(${idxName})",
            testDatabase
        )

        verifyQuery(query, 2) { _, result ->
            assertNotNull(result.getString(0))
            assertNotNull(result.getString(1))
        }
    }

    // 8.11.6b: Test that query with match() and rank() function using the full-text index
    // created in the default collection returns the result as expected.
    // Use the following cases to reference the full-text index.
    //     _.<index-name>
    @Test
    fun testFTSQueryWithFullTextIndexInDefaultCollectionB() {
        val defaultCollection = testDatabase.getDefaultCollection()
        assertNotNull(defaultCollection!!)

        loadJSONResourceIntoCollection("sentences.json", collection = defaultCollection)

        defaultCollection.createIndex("sentence", IndexBuilder.fullTextIndex(FullTextIndexItem.property("sentence")))

        val idxName = "_.sentence"
        val query = QueryBuilder.createQuery(
            "SELECT _id, sentence" +
                    " FROM _" +
                    " WHERE MATCH(${idxName}, \"Dummie woman\")" +
                    " ORDER BY RANK(${idxName})",
            testDatabase
        )

        verifyQuery(query, 2) { _, result ->
            assertNotNull(result.getString(0))
            assertNotNull(result.getString(1))
        }
    }

    // 8.11.6c: Test that query with match() and rank() function using the full-text index
    // created in the default collection returns the result as expected.
    // Use the following cases to reference the full-text index.
    //     _default.<index-name>
    @Test
    fun testFTSQueryWithFullTextIndexInDefaultCollectionC() {
        val defaultCollection = testDatabase.getDefaultCollection()
        assertNotNull(defaultCollection!!)

        loadJSONResourceIntoCollection("sentences.json", collection = defaultCollection)

        defaultCollection.createIndex("sentence", IndexBuilder.fullTextIndex(FullTextIndexItem.property("sentence")))

        val idxName = "_default.sentence"
        val query = QueryBuilder.createQuery(
            "SELECT _id, sentence" +
                    " FROM ${defaultCollection.name}" +
                    " WHERE MATCH(${idxName}, \"Dummie woman\")" +
                    " ORDER BY RANK(${idxName})",
            testDatabase
        )

        verifyQuery(query, 2) { _, result ->
            assertNotNull(result.getString(0))
            assertNotNull(result.getString(1))
        }
    }

    // 8.11.6d: Test that query with match() and rank() function using the full-text index
    // created in the default collection returns the result as expected.
    // Use the following cases to reference the full-text index.
    //     <database-name>.<index-name>
    @Test
    fun testFTSQueryWithFullTextIndexInDefaultCollectionD() {
        val defaultCollection = testDatabase.getDefaultCollection()
        assertNotNull(defaultCollection!!)

        loadJSONResourceIntoCollection("sentences.json", collection = defaultCollection)

        defaultCollection.createIndex("sentence", IndexBuilder.fullTextIndex(FullTextIndexItem.property("sentence")))

        val idxName = "${testDatabase.name}.sentence"
        val query = QueryBuilder.createQuery(
            "SELECT _id, sentence" +
                    " FROM ${testDatabase.name}" +
                    " WHERE MATCH(${idxName}, \"Dummie woman\")" +
                    " ORDER BY RANK(${idxName})",
            testDatabase
        )

        verifyQuery(query, 2) { _, result ->
            assertNotNull(result.getString(0))
            assertNotNull(result.getString(1))
        }
    }

    // 8.11.6e: Test that query with match() and rank() function using the full-text index
    // created in the default collection returns the result as expected.
    // Use the following cases to reference the full-text index.
    //     <collection-alias-name>.<index-name>
    @Test
    fun testFTSQueryWithFullTextIndexInDefaultCollectionE() {
        val defaultCollection = testDatabase.getDefaultCollection()
        assertNotNull(defaultCollection!!)

        loadJSONResourceIntoCollection("sentences.json", collection = defaultCollection)

        defaultCollection.createIndex("sentence", IndexBuilder.fullTextIndex(FullTextIndexItem.property("sentence")))

        val idxName = "foo.sentence"
        val query = QueryBuilder.createQuery(
            "SELECT _id, sentence" +
                    " FROM _ AS foo" +
                    " WHERE MATCH(${idxName}, \"Dummie woman\")" +
                    " ORDER BY RANK(${idxName})",
            testDatabase
        )

        verifyQuery(query, 2) { _, result ->
            assertNotNull(result.getString(0))
            assertNotNull(result.getString(1))
        }
    }

    // 8.11.7a: Test that query with match() and rank() function using the full-text index
    // created in a named collection returns the result as expected.
    // Use the following cases to reference the full-text index:
    //     <index-name>
    // Note: test by loading the “sentences” test data into a named collection.
    @Test
    fun testFTSQueryWithFullTextIndexInNamedCollectionA() {
        loadJSONResourceIntoCollection("sentences.json")

        testCollection.createIndex("sentence", IndexBuilder.fullTextIndex(FullTextIndexItem.property("sentence")))

        val idxName = "sentence"
        val query = QueryBuilder.createQuery(
            "SELECT _id, sentence" +
                    " FROM ${testCollection.fullName}" +
                    " WHERE MATCH(${idxName}, \"Dummie woman\")" +
                    " ORDER BY RANK(${idxName})",
            testDatabase
        )

        verifyQuery(query, 2) { _, result ->
            assertNotNull(result.getString(0))
            assertNotNull(result.getString(1))
        }
    }

    // 8.11.7b: Test that query with match() and rank() function using the full-text index
    // created in a named collection returns the result as expected.
    // Use the following cases to reference the full-text index:
    //     <collection>.<index-name> (For collection in a default scope>
    // Note: test by loading the “sentences” test data into a named collection.
    @Test
    fun testFTSQueryWithFullTextIndexInNamedCollectionB() {
        loadJSONResourceIntoCollection("sentences.json")

        testCollection.createIndex("sentence", IndexBuilder.fullTextIndex(FullTextIndexItem.property("sentence")))

        val idxName = "${testCollection.name}.sentence"
        val query = QueryBuilder.createQuery(
            "SELECT _id, sentence" +
                    " FROM ${testCollection.fullName}" +
                    " WHERE MATCH(${idxName}, \"Dummie woman\")" +
                    " ORDER BY RANK(${idxName})",
            testDatabase
        )

        verifyQuery(query, 2) { _, result ->
            assertNotNull(result.getString(0))
            assertNotNull(result.getString(1))
        }
    }

    // 8.11.7c: Test that query with match() and rank() function using the full-text index
    // created in a named collection returns the result as expected.
    // Use the following cases to reference the full-text index:
    //     <collection-alias name>.<index-name>
    // Note: test by loading the “sentences” test data into a named collection.
    @Test
    fun testFTSQueryWithFullTextIndexInNamedCollectionC() {
        loadJSONResourceIntoCollection("sentences.json")

        testCollection.createIndex("sentence", IndexBuilder.fullTextIndex(FullTextIndexItem.property("sentence")))

        val idxName = "foo.sentence"
        val query = QueryBuilder.createQuery(
            "SELECT _id, sentence" +
                    " FROM ${testCollection.fullName} as foo" +
                    " WHERE MATCH(${idxName}, \"Dummie woman\")" +
                    " ORDER BY RANK(${idxName})",
            testDatabase
        )

        verifyQuery(query, 2) { _, result ->
            assertNotNull(result.getString(0))
            assertNotNull(result.getString(1))
        }
    }

    // 8.11.8: Test that a multi-collection join query with match() function returns
    // the result as expected. Use the following cases to reference the full-text index.
    // <collection-alias-name>.<index-name>
    // The test case could be developed as follows:
    // Create two collections named "flowers" and "colors" in the scope named "test".
    // Create a FullText index named “DescIndex” for the “description” field in the “flowers” collection.
    // Populate the following docs in the flowers collection:
    //     { “name”: "rose", “description”: “Red flowers”,  “cid”: "c1" }
    //     { “name”: "hydrangea", “description”: “Blue flowers”, “cid”: "c2" }
    // Populate the following docs in the colors collection:
    //     { “cid”: "c1", “color”: "red" }
    //     { “cid”: "c2", “color”: "blue" }
    //     { “cid”: "c3", “color”: "white" }
    // Use the following SQL++ queries to create and execute the query:
    //     SELECT f.name, f.description, c.color
    //         FROM test.flowers AS f
    //         JOIN test.colors AS c
    //         ON f.cid = c.cid
    //         WHERE MATCH(f.DescIndex, "red")
    //         ORDER BY f.name
    // There should be one result returned as:
    //     { “name”: "rose", “description”: “Red flowers”, “color”: "red" }
    @Test
    fun testFTSJoinQuery() {
        val flowerCol = testDatabase.createCollection("flowers", "test")
        val colorCol = testDatabase.createCollection("colors", "test")

        flowerCol.createIndex("DescIndex", IndexBuilder.fullTextIndex(FullTextIndexItem.property("description")))

        var doc = MutableDocument()
        doc.setValue("name", "rose")
        doc.setValue("description", "Red flowers")
        doc.setValue("cid", "c1")
        saveDocInCollection(doc, flowerCol)
        doc = MutableDocument()
        doc.setValue("name", "hydrangea")
        doc.setValue("description", "Blue flowers")
        doc.setValue("cid", "c2")
        saveDocInCollection(doc, flowerCol)

        doc = MutableDocument()
        doc.setValue("cid", "c1")
        doc.setValue("color", "red")
        saveDocInCollection(doc, colorCol)
        doc = MutableDocument()
        doc.setValue("cid", "c2")
        doc.setValue("color", "blue")
        saveDocInCollection(doc, colorCol)
        doc = MutableDocument()
        doc.setValue("cid", "c3")
        doc.setValue("color", "white")
        saveDocInCollection(doc, colorCol)

        // create with query string
        val query = QueryBuilder.createQuery(
            """SELECT f.name, f.description, c.color
                FROM test.flowers AS f
                JOIN test.colors AS c
                ON f.cid = c.cid
                WHERE MATCH(f.DescIndex, "red")
                ORDER BY f.name"""
                .trimIndent(),
            testDatabase
        )
        verifyQuery(query, 1) { _, result ->
            assertEquals(3, result.count().toLong())
            assertEquals("rose", result.getValue("name"))
            assertEquals("Red flowers", result.getValue("description"))
            assertEquals("red", result.getValue("color"))
        }
    }

    // 8.11.9: Test that a multi-collection join query with match() function that
    // only uses index name without collection-alias-name prefix is failed to create.
    @Test
    fun testFTSJoinQueryError() {
        testDatabase.createCollection("colors", "test")

        val flowerCol = testDatabase.createCollection("flowers", "test")

        flowerCol.createIndex("DescIndex", IndexBuilder.fullTextIndex(FullTextIndexItem.property("description")))

        // create with query string
        val query = QueryBuilder.createQuery(
            """SELECT f.name, f.description, c.color
                FROM test.flowers AS f
                JOIN test.colors AS c
                ON f.cid = c.cid
                WHERE MATCH(DescIndex, "red")
                ORDER BY f.name"""
                .trimIndent(),
            testDatabase
        )
        assertThrowsCBLException(CBLError.Domain.CBLITE, CBLError.Code.INVALID_QUERY) {
            query.execute().use { }
        }
    }

    // 8.11.10a: Test that the result’s key names of the SELECT * are as follows.
    //     SELECT * FROM db => “db”
    @Test
    fun testSelectAllResultKeyA() {
        val defaultCollection = testDatabase.getDefaultCollection()
        assertNotNull(defaultCollection!!)

        var doc = MutableDocument()
        doc.setValue("name", "rose")
        doc.setValue("description", "Red flowers")
        doc.setValue("cid", "c1")
        saveDocInCollection(doc, defaultCollection)
        doc = MutableDocument()
        doc.setValue("name", "hydrangea")
        doc.setValue("description", "Blue flowers")
        doc.setValue("cid", "c2")
        saveDocInCollection(doc, defaultCollection)

        val query = QueryBuilder.createQuery("SELECT * FROM ${testDatabase.name}", testDatabase)
        query.execute().use { rs ->
            val colNames = rs.first().keys
            assertEquals(1, colNames.size)
            assertEquals(testDatabase.name, colNames[0])
        }
    }

    // 8.11.10b: Test that the result’s key names of the SELECT * are as follows.
    //     SELECT * FROM _ => “_”
    @Test
    fun testSelectAllResultKeyB() {
        val defaultCollection = testDatabase.getDefaultCollection()
        assertNotNull(defaultCollection!!)

        var doc = MutableDocument()
        doc.setValue("name", "rose")
        doc.setValue("description", "Red flowers")
        doc.setValue("cid", "c1")
        saveDocInCollection(doc, defaultCollection)
        doc = MutableDocument()
        doc.setValue("name", "hydrangea")
        doc.setValue("description", "Blue flowers")
        doc.setValue("cid", "c2")
        saveDocInCollection(doc, defaultCollection)

        val query = QueryBuilder.createQuery("SELECT * FROM _", testDatabase)
        query.execute().use { rs ->
            val colNames = rs.first().keys
            assertEquals(1, colNames.size)
            assertEquals("_", colNames[0])
        }
    }

    // 8.11.10c: Test that the result’s key names of the SELECT * are as follows.
    //     SELECT * FROM _default._default => “_default”
    @Test
    fun testSelectAllResultKeyC() {
        val defaultCollection = testDatabase.getDefaultCollection()
        assertNotNull(defaultCollection!!)

        var doc = MutableDocument()
        doc.setValue("name", "rose")
        doc.setValue("description", "Red flowers")
        doc.setValue("cid", "c1")
        saveDocInCollection(doc, defaultCollection)
        doc = MutableDocument()
        doc.setValue("name", "hydrangea")
        doc.setValue("description", "Blue flowers")
        doc.setValue("cid", "c2")
        saveDocInCollection(doc, defaultCollection)

        val query = QueryBuilder.createQuery("SELECT * FROM ${defaultCollection.fullName}", testDatabase)
        query.execute().use { rs ->
            val colNames = rs.first().keys
            assertEquals(1, colNames.size)
            assertEquals(defaultCollection.name, colNames[0])
        }
    }

    // 8.11.10d: Test that the result’s key names of the SELECT * are as follows.
    //     SELECT * FROM test.flowers => “flowers”
    @Test
    fun testSelectAllResultKeyD() {
        var doc = MutableDocument()
        doc.setValue("name", "rose")
        doc.setValue("description", "Red flowers")
        doc.setValue("cid", "c1")
        saveDocInCollection(doc, testCollection)
        doc = MutableDocument()
        doc.setValue("name", "hydrangea")
        doc.setValue("description", "Blue flowers")
        doc.setValue("cid", "c2")
        saveDocInCollection(doc, testCollection)

        val query = QueryBuilder.createQuery("SELECT * FROM ${testCollection.fullName}", testDatabase)
        query.execute().use { rs ->
            val colNames = rs.first().keys
            assertEquals(1, colNames.size)
            assertEquals(testCollection.name, colNames[0])
        }
    }

    // 8.11.10e: Test that the result’s key names of the SELECT * are as follows.
    //     SELECT * FROM test.flowers as f => “f”
    @Test
    fun testSelectAllResultKeyE() {
        var doc = MutableDocument()
        doc.setValue("name", "rose")
        doc.setValue("description", "Red flowers")
        doc.setValue("cid", "c1")
        saveDocInCollection(doc, testCollection)
        doc = MutableDocument()
        doc.setValue("name", "hydrangea")
        doc.setValue("description", "Blue flowers")
        doc.setValue("cid", "c2")
        saveDocInCollection(doc, testCollection)

        val query = QueryBuilder.createQuery("SELECT * FROM ${testCollection.fullName} as f", testDatabase)
        query.execute().use { rs ->
            val colNames = rs.first().keys
            assertEquals(1, colNames.size)
            assertEquals("f", colNames[0])
        }
    }

    // Section 8.12

    // 8.12.1: Test that query by using the default collection as data source works as expected.
    // Populate "names_100" data into the default collection.
    // Create a query using the QueryBuilder as
    // QueryBuilder
    //     .select(SelectResult.property("name.first"))
    //     .from(DataSource.collection(defaultCollection))
    //     .orderBy(Ordering.property("name.first"))
    //     .limit(Expression.intValue(1))
    // Ensure that the result set has one result, and the data is {"first" : "Abe"}.
    @Test
    fun testQueryBuilderWithDefaultCollectionAsDataSource() {
        val defaultCollection = testDatabase.getDefaultCollection()
        assertNotNull(defaultCollection!!)
        loadJSONResourceIntoCollection("names_100.json", collection = defaultCollection)

        // create with default collection
        val query = QueryBuilder
            .select(SelectResult.property("name.first"))
            .from(DataSource.collection((defaultCollection)))
            .orderBy(Ordering.property("name.first"))
            .limit(Expression.intValue(1))
        verifyQuery(query, 1) { _, result ->
            assertEquals(1, result.count().toLong())
            assertEquals("{\"first\":\"Abe\"}", result.toJSON())
            assertEquals("Abe", result.getValue("first"))
            assertEquals(result.getValue(0), result.getValue("first"))
        }
    }

    // 8.12.1x: Test that query in the default scope as data source works as expected.
    @Test
    fun testQueryBuilderDefaultScope() {
        val collection = testDatabase.createCollection("names")
        loadJSONResourceIntoCollection("names_100.json", collection = collection)
        val query = QueryBuilder.select(SelectResult.property("name.first"))
            .from(DataSource.collection(collection))
            .orderBy(Ordering.property("name.first"))
            .limit(Expression.intValue(1))
        verifyQuery(query, 1) { _, result ->
            assertEquals(1, result.count().toLong())
            assertEquals("{\"first\":\"Abe\"}", result.toJSON())
            assertEquals("Abe", result.getValue("first"))
            assertEquals(result.getValue(0), result.getValue("first"))
        }
    }

    // 8.12.2: Test that query by using a collection as data source works as expected
    // Create a "names" collection in the scope named "people".
    // Populate "names_100" data into the names collection.
    // Create a query using the QueryBuilder as
    // QueryBuilder
    //     .select(SelectResult.property("name.first"))
    //     .from(DataSource.collection(namesCollection))
    //     .orderBy(Ordering.property("name.first"))
    //     .limit(Expression.intValue(1))
    // Ensure that the result set has one result, and the data is {"first" : "Abe"}.
    @Test
    fun testQueryBuilderWithCollectionAsDataSource() {
        val namesCollection = testDatabase.createCollection("names", "people")
        loadJSONResourceIntoCollection("names_100.json", collection = namesCollection)
        val query = QueryBuilder
            .select(SelectResult.property("name.first"))
            .from(DataSource.collection(namesCollection))
            .orderBy(Ordering.property("name.first"))
            .limit(Expression.intValue(1))
        verifyQuery(query, 1) { _, result ->
            assertEquals(1, result.count().toLong())
            assertEquals("{\"first\":\"Abe\"}", result.toJSON())
            assertEquals("Abe", result.getValue("first"))
            assertEquals(result.getValue(0), result.getValue("first"))
        }
    }

    // 8.12.3a: Test that query by using the new match() and rank()
    // function with FullTextIndexExpression works as expected with
    // and without specifying the data source alias name.
    // Note: Reuse the existing match() and rank() test to create
    // this test by loading the “sentences” test data into a named collection.
    @Test
    fun testQueryBuilderMatchWithFullTextIndexExpression1() {
        loadJSONResourceIntoCollection("sentences.json")
        testCollection.createIndex("sentence", IndexBuilder.fullTextIndex(FullTextIndexItem.property("sentence")))
        val idx = Expression.fullTextIndex("sentence")
        val query = QueryBuilder
            .select(SelectResult.expression(Meta.id), SelectResult.property("sentence"))
            .from(DataSource.collection(testCollection))
            .where(FullTextFunction.match(idx, "'Dummie woman'"))
            .orderBy(Ordering.expression(FullTextFunction.rank(idx)).descending())
        verifyQuery(query, 2) { _, result ->
            assertNotNull(result.getString(0))
            assertNotNull(result.getString(1))
        }
    }

    // 8.12.3b: Test that query by using the new match() and rank()
    // function with FullTextIndexExpression works as expected with
    // and without specifying the data source alias name.
    // Note: Reuse the existing match() and rank() test to create
    // this test by loading the “sentences” test data into a named collection.
    @Test
    fun testQueryBuilderMatchWithFullTextIndexExpression2() {
        loadJSONResourceIntoCollection("sentences.json")
        testCollection.createIndex("sentence", IndexBuilder.fullTextIndex(FullTextIndexItem.property("sentence")))
        val idx = Expression.fullTextIndex("sentence").from("sentences")
        val query = QueryBuilder
            .select(SelectResult.expression(Meta.id), SelectResult.property("sentence"))
            .from(DataSource.collection(testCollection).`as`("sentences"))
            .where(FullTextFunction.match(idx, "'Dummie woman'"))
            .orderBy(Ordering.expression(FullTextFunction.rank(idx)).descending())
        verifyQuery(query, 2) { _, result ->
            assertNotNull(result.getString(0))
            assertNotNull(result.getString(1))
        }
    }

    // 8.12.4a: Test that the result’s key names of the SELECT * are as follows.
    //     SELECT * FROM db => “db”
    @Suppress("DEPRECATION")
    @Test
    fun testBuilderSelectAllResultKeyA() {
        var doc = MutableDocument()
        doc.setValue("name", "rose")
        doc.setValue("description", "Red flowers")
        doc.setValue("cid", "c1")
        testDatabase.save(doc)
        doc = MutableDocument()
        doc.setValue("name", "hydrangea")
        doc.setValue("description", "Blue flowers")
        doc.setValue("cid", "c2")
        testDatabase.save(doc)

        val query = QueryBuilder.select(SelectResult.all()).from(DataSource.database(testDatabase))
        query.execute().use { rs ->
            val colNames = rs.first().keys
            assertEquals(1, colNames.size)
            assertEquals(testDatabase.name, colNames[0])
        }
    }

    // 8.12.4b: Test that the result’s key names of the SELECT * are as follows.
    //     SELECT * FROM db AS foo => “db”
    @Suppress("DEPRECATION")
    @Test
    fun testBuilderSelectAllResultKeyB() {
        var doc = MutableDocument()
        doc.setValue("name", "rose")
        doc.setValue("description", "Red flowers")
        doc.setValue("cid", "c1")
        testDatabase.save(doc)
        doc = MutableDocument()
        doc.setValue("name", "hydrangea")
        doc.setValue("description", "Blue flowers")
        doc.setValue("cid", "c2")
        testDatabase.save(doc)

        val query = QueryBuilder.select(SelectResult.all()).from(DataSource.database(testDatabase).`as`("foo"))
        query.execute().use { rs ->
            val colNames = rs.first().keys
            assertEquals(1, colNames.size)
            assertEquals("foo", colNames[0])
        }
    }

    // 8.12.4c: Test that the result’s key names of the SELECT * are as follows.
    //     SELECT * FROM _default._default => “_default”
    @Test
    fun testBuilderSelectAllResultKeyC() {
        val defaultCollection = testDatabase.getDefaultCollection()
        assertNotNull(defaultCollection!!)

        var doc = MutableDocument()
        doc.setValue("name", "rose")
        doc.setValue("description", "Red flowers")
        doc.setValue("cid", "c1")
        saveDocInCollection(doc, defaultCollection)
        doc = MutableDocument()
        doc.setValue("name", "hydrangea")
        doc.setValue("description", "Blue flowers")
        doc.setValue("cid", "c2")
        saveDocInCollection(doc, defaultCollection)

        val query = QueryBuilder.select(SelectResult.all()).from(DataSource.collection(defaultCollection))
        query.execute().use { rs ->
            val colNames = rs.first().keys
            assertEquals(1, colNames.size)
            assertEquals(defaultCollection.name, colNames[0])
        }
    }

    // 8.12.4d: Test that the result’s key names of the SELECT * are as follows.
    //     SELECT * FROM test.flowers as f => “f”
    @Test
    fun testBuilderSelectAllResultKeyE() {
        var doc = MutableDocument()
        doc.setValue("name", "rose")
        doc.setValue("description", "Red flowers")
        doc.setValue("cid", "c1")
        saveDocInCollection(doc, testCollection)
        doc = MutableDocument()
        doc.setValue("name", "hydrangea")
        doc.setValue("description", "Blue flowers")
        doc.setValue("cid", "c2")
        saveDocInCollection(doc, testCollection)

        val query = QueryBuilder.select(SelectResult.all()).from(DataSource.collection(testCollection).`as`("foo"))
        query.execute().use { rs ->
            val colNames = rs.first().keys
            assertEquals(1, colNames.size)
            assertEquals("foo", colNames[0])
        }
    }

    @Test
    fun testBuilderQueryJoinWithCollections() {
        val flowerCol = testDatabase.createCollection("flowers", "test")
        val colorCol = testDatabase.createCollection("colors", "test")
        var doc = MutableDocument()
        doc.setValue("name", "rose")
        doc.setValue("cid", "c1")
        saveDocInCollection(doc, flowerCol)
        doc = MutableDocument()
        doc.setValue("name", "hydrangea")
        doc.setValue("cid", "c2")
        saveDocInCollection(doc, flowerCol)
        doc = MutableDocument()
        doc.setValue("cid", "c1")
        doc.setValue("color", "red")
        saveDocInCollection(doc, colorCol)
        doc = MutableDocument()
        doc.setValue("cid", "c2")
        doc.setValue("color", "blue")
        saveDocInCollection(doc, colorCol)
        doc = MutableDocument()
        doc.setValue("cid", "c3")
        doc.setValue("color", "white")
        saveDocInCollection(doc, colorCol)
        val query = QueryBuilder.select(
            SelectResult.expression(Expression.property("flowers.name")),
            SelectResult.expression(Expression.property("colors.color"))
        )
            .from(DataSource.collection(flowerCol)) // join flower and color where the cid field of color is equal to the cid field of flower
            .join(
                Join.join(DataSource.collection(colorCol))
                    .on(Expression.property("flowers.cid").equalTo(Expression.property("colors.cid")))
            )
            .orderBy(Ordering.expression(Expression.property("flowers.name")))
        verifyQuery(query, 2) { _, result ->
            assertEquals(2, result.count().toLong())
            when (val name = result.getString("name")) {
                "hydrangea" -> assertEquals("blue", result.getString("color"))
                "rose" -> assertEquals("red", result.getString("color"))
                else -> fail("unexpected name: $name")
            }
        }
    }

    @Test
    fun testBuilderQueryJoinWithAliasedCollections() {
        val flowerCol = testDatabase.createCollection("flowers", "test")
        val colorCol = testDatabase.createCollection("colors", "test")
        var doc = MutableDocument()
        doc.setValue("name", "rose")
        doc.setValue("cid", "c1")
        saveDocInCollection(doc, flowerCol)
        doc = MutableDocument()
        doc.setValue("name", "hydrangea")
        doc.setValue("cid", "c2")
        saveDocInCollection(doc, flowerCol)
        doc = MutableDocument()
        doc.setValue("cid", "c1")
        doc.setValue("color", "red")
        saveDocInCollection(doc, colorCol)
        doc = MutableDocument()
        doc.setValue("cid", "c2")
        doc.setValue("color", "blue")
        saveDocInCollection(doc, colorCol)
        doc = MutableDocument()
        doc.setValue("cid", "c3")
        doc.setValue("color", "white")
        saveDocInCollection(doc, colorCol)
        val query = QueryBuilder.select(
            SelectResult.expression(Expression.property("name").from("f")),
            SelectResult.expression(Expression.property("color").from("c"))
        )
            .from(DataSource.collection(flowerCol).`as`("f"))
            // join flower and color where the cid field of color is equal to the cid field of flower
            .join(
                Join.join(DataSource.collection(colorCol).`as`("c"))
                    .on(Expression.property("cid").from("f").equalTo(Expression.property("cid").from("c")))
            )
            .orderBy(Ordering.expression(Expression.property("f.name")))
        verifyQuery(query, 2) { _, result ->
            assertEquals(2, result.count().toLong())
            when (val name = result.getString("name")) {
                "hydrangea" -> assertEquals("blue", result.getString("color"))
                "rose" -> assertEquals("red", result.getString("color"))
                else -> fail("unexpected name: $name")
            }
        }
    }
}