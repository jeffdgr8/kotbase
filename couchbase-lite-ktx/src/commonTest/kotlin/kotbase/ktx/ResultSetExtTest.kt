/*
 * Copyright 2023 Jeff Lockhart
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
package kotbase.ktx

import kotbase.BaseDbTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class ResultSetExtTest : BaseDbTest() {

    class User(map: Map<String, Any?>) {
        val name: String by map
        val surname: String by map
        val age: Int by map
    }

    private fun addDocumentsToDatabase() {
        testCollection.save(MutableDocument("john_smith") {
            "name" to "John"
            "surname" to "Smith"
            "age" to 42
        })
        testCollection.save(MutableDocument {
            "name" to "Jane"
            "surname" to "Johnson"
            "age" to 35
        })
        testCollection.save(MutableDocument {
            "name" to "Sally"
            "surname" to "Smith"
            "age" to 54
        })
        testCollection.save(MutableDocument {
            "name" to "Jimmy"
            "surname" to "Johnson"
            "age" to 27
        })
    }

    @Test
    fun test_toObjects_singleUserAll() {
        addDocumentsToDatabase()

        val results = select(all())
            .from(testCollection)
            .where { "name" equalTo "John" }
            .execute()
            .toObjects(::User)

        assertEquals(1, results.size)
        assertEquals("John", results.first().name)
        assertEquals("Smith", results.first().surname)
        assertEquals(42, results.first().age)
    }

    @Test
    fun test_toObjects_multipleUsersAll() {
        addDocumentsToDatabase()

        val results = select(all())
            .from(testCollection)
            .where { "surname" equalTo "Smith" }
            .orderBy { "name".ascending() }
            .execute()
            .toObjects(::User)

        assertEquals(2, results.size)
        assertContentEquals(listOf("John", "Sally"), results.map { it.name })
        assertContentEquals(listOf("Smith", "Smith"), results.map { it.surname })
        assertContentEquals(listOf(42, 54), results.map { it.age })
    }

    @Test
    fun test_toObjects_singleUserProjection() {
        addDocumentsToDatabase()

        val results = select("name", "surname", "age")
            .from(testCollection)
            .where { "age" equalTo 27 }
            .execute()
            .toObjects(::User)

        assertEquals(1, results.size)
        assertEquals("Jimmy", results.first().name)
        assertEquals("Johnson", results.first().surname)
        assertEquals(27, results.first().age)
    }

    @Test
    fun test_toObjects_multipleUsersProjection() {
        addDocumentsToDatabase()

        val results = select("name", "surname", "age")
            .from(testCollection)
            .where { "surname" equalTo "Johnson" }
            .orderBy { "name".ascending() }
            .execute()
            .toObjects(::User)

        assertEquals(2, results.size)
        assertContentEquals(listOf("Jane", "Jimmy"), results.map { it.name })
        assertContentEquals(listOf("Johnson", "Johnson"), results.map { it.surname })
        assertContentEquals(listOf(35, 27), results.map { it.age })
    }
}
