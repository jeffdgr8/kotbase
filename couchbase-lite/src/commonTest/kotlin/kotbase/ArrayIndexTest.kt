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

import com.couchbase.lite.INDEX_KEY_EXPR
import com.couchbase.lite.INDEX_KEY_NAME
import com.couchbase.lite.getIndexInfo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

// Implements test spec version 1.0.2
class ArrayIndexTest : BaseDbTest() {

    private val IndexConfiguration.indexSpec
        get() = expressions.joinToString(",")

    @Test
    fun testArrayIndexConfigValidExpressions1() = assertEquals("", ArrayIndexConfiguration("foo").indexSpec)

//    @Test
//    fun testArrayIndexConfigValidExpressions3() =
//        assertEquals("", ArrayIndexConfiguration("foo", null).indexSpec)

    @Test
    fun testArrayIndexConfigValidExpressions4() =
        assertEquals("bar", ArrayIndexConfiguration("foo", listOf("bar")).indexSpec)

//    @Test
//    fun testArrayIndexConfigValidExpressions5() =
//        assertEquals("bar,baz", ArrayIndexConfiguration("foo", "bar", "baz").indexSpec)

    @Test
    fun testArrayIndexConfigValidExpressions7() =
        assertEquals("bar,baz", ArrayIndexConfiguration("foo", listOf("bar", "baz")).indexSpec)

    /**
     * 1. TestArrayIndexConfigInvalidExpressions
     *
     * Description
     *     Test that creating an ArrayIndexConfiguration with invalid
     *     expressions which are an empty expressions or contain null.
     *
     * Steps
     * 1. Create a ArrayIndexConfiguration object.
     *     - path: "contacts"
     *     - expressions: []
     * 2. Check that an invalid argument exception is thrown.
     * 3. Create a ArrayIndexConfiguration object.
     *     - path: "contacts"
     *     - expressions: [""]
     * 4. Check that an invalid argument exception is thrown.
     * 5. Create a ArrayIndexConfiguration object. This case can be ignored if the platform doesn't allow null.
     *     - path: "contacts"
     *     - expressions: ["address.state", null, "address.city"]
     * 6. Check that an invalid argument exception is thrown.
     */
    @Test
    fun testArrayIndexConfigInvalidExpressions1() {
        assertFailsWith<IllegalArgumentException> {
            ArrayIndexConfiguration("contacts", emptyList())
        }
    }

    @Test
    fun testArrayIndexConfigInvalidExpressions3a() {
        assertFailsWith<IllegalArgumentException> {
            ArrayIndexConfiguration("contacts", listOf(""))
        }
    }

//    @Test
//    fun testArrayIndexConfigInvalidExpressions3b() {
//        assertFailsWith<IllegalArgumentException> {
//            ArrayIndexConfiguration("contacts", "")
//        }
//    }

//    @Test
//    fun testArrayIndexConfigInvalidExpressions5() {
//        assertFailsWith<IllegalArgumentException> {
//            ArrayIndexConfiguration("contacts", listOf("address.state", null, "address.city"))
//        }
//    }

    /**
     * 2. TestCreateArrayIndexWithPath
     *
     * Description
     *     Test that creating an array index with only path works as expected.
     *
     * Steps
     *     1. Load profiles.json into the collection named "_default.profiles".
     *     2. Create a ArrayIndexConfiguration object.
     *         - path: "contacts"
     *         - expressions: null
     *     3. Create an array index named "contacts" in the profiles collection.
     *     4. Get index names from the profiles collection and check that the index named "contacts" exists.
     *     5. Get info of the index named "contacts" using an internal API and check that the
     *        index has path and expressions as configured.
     */
    @Test
    fun testCreateArrayIndexWithPath() {
        val profilesCollection = testDatabase.createCollection("profiles")
        loadJSONResourceIntoCollection("profiles_100.json", collection = profilesCollection)

        profilesCollection.createIndex("contacts", ArrayIndexConfiguration("contacts"))

        val idx = profilesCollection.getIndexExpressions("contacts")
        assertNotNull(idx)
        assertEquals(1, idx.size)
        assertEquals("", idx[0])

        assertEquals("contacts", profilesCollection.getPathForIndex("contacts"))
    }

    /**
     * 3. TestCreateArrayIndexWithPathAndExpressions
     *
     * Description
     *     Test that creating an array index with path and expressions works as expected.
     *
     * Steps
     *     1. Load profiles.json into the collection named "_default.profiles".
     *     2. Create a ArrayIndexConfiguration object.
     *         - path: "contacts"
     *         - expressions: ["address.city", "address.state"]
     *     3. Create an array index named "contacts" in the profiles collection.
     *     4. Get index names from the profiles collection and check that the index named "contacts" exists.
     *     5. Get info of the index named "contacts" using an internal API and check that the
     *        index has path and expressions as configured.
     */
    @Test
    fun testCreateArrayIndexWithPathAndExpressions() {
        val profilesCollection = testDatabase.createCollection("profiles")
        loadJSONResourceIntoCollection("profiles_100.json", collection = profilesCollection)

        val exprs = listOf("address.city", "address.state")

        profilesCollection.createIndex("contacts", ArrayIndexConfiguration("contacts", exprs))

        assertEquals("contacts", profilesCollection.getPathForIndex("contacts"))

        val idx = profilesCollection.getIndexExpressions("contacts")
        assertNotNull(idx)
        assertEquals(2, idx.size)
        assertTrue(idx.contains(exprs[0]))
        assertTrue(idx.contains(exprs[0]))
    }

    private fun Collection.getIndexExpressions(indexName: String) =
        this.getIndexInfo()
            .firstOrNull { it[Collection.INDEX_KEY_NAME] == indexName }
            ?.let { (it[Collection.INDEX_KEY_EXPR] as? String)?.split(",") } ?: emptyList()

    private fun Collection.getPathForIndex(indexName: String): String {
        assertNotNull(getIndex(indexName))
        // the best we can do without C4 internal access
        return indexName
    }
}
