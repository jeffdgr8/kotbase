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
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PartialIndexTest : BaseDbTest() {
    /**
     * 1.  TestCreatePartialValueIndex
     * Description
     *   Test that a partial value index is successfully created.
     *
     * Steps
     *     1. Create a partial value index named "numIndex" in the default collection.
     *         - expression: "num"
     *         - where: "type = 'number'"
     *     2. Check that the index is successfully created.
     *     3. Create a query object with an SQL++ string:
     *         - SELECT * FROM _ WHERE type = 'number' AND num > 1000
     *     4. Get the query plan from the query object and check that the plan contains "USING INDEX numIndex" string.
     *     5. Create a query object with an SQL++ string:
     *         - SELECT * FROM _ WHERE type = 'foo' AND num > 1000
     *     6. Get the query plan from the query object and check that the plan doesn't contain "USING INDEX numIndex" string.
     */
    @Test
    fun testCreatePartialValueIndex() {
        testCollection.createIndex("numIndex", ValueIndexConfiguration("num").setWhere("type = 'number'"))
        assertNotNull(testCollection.getIndex("numIndex"))

        assertTrue(
            testDatabase
                .createQuery("SELECT * FROM ${testCollection.fullName} WHERE type = 'number' AND num > 1000")
                .explain()
                .contains("USING INDEX numIndex")
        )

        assertFalse(
            testDatabase
                .createQuery("SELECT * FROM ${testCollection.fullName} WHERE type = 'foo' AND num > 1000")
                .explain()
                .contains("USING INDEX numIndex")
        )
    }

    /**
     * 2.  TestCreatePartialFullTextIndex
     * Description
     *   Test that a partial full text index is successfully created.
     *
     * Steps
     *     1. Create following two documents with the following bodies in the default collection.
     *         - { "content" : "Couchbase Lite is a database." }
     *         - { "content" : "Couchbase Lite is a NoSQL syncable database." }
     *     2. Create a partial full text index named "contentIndex" in the default collection.
     *         - expression: "content"
     *         - where: "length(content) > 30"
     *     3. Check that the index is successfully created.
     *     4. Create a query object with an SQL++ string:
     *         - SELECT content FROM _ WHERE match(contentIndex, "database")
     *     5. Execute the query and check that:
     *         - There is one result returned
     *         - The returned content is "Couchbase Lite is a NoSQL syncable database.".
     */
    @Test
    fun testCreatePartialFullTextIndex() {
        testCollection.save(MutableDocument().setString("content", "Couchbase Lite is a database."))
        testCollection.save(MutableDocument().setString("content", "Couchbase Lite is a NoSQL syncable database."))

        testCollection.createIndex(
            "contentIndex",
            FullTextIndexConfiguration("content").setWhere("length(content) > 30")
        )
        assertNotNull(testCollection.getIndex("contentIndex"))

        val results = testDatabase
            .createQuery("SELECT content FROM ${testCollection.fullName} WHERE match(contentIndex, 'database')")
            .execute()
            .allResults()
        assertEquals(1, results.size)
        assertEquals("Couchbase Lite is a NoSQL syncable database.", results[0].getString("content"))
    }
}
