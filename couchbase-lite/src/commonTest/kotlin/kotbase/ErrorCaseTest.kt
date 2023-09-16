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

import com.couchbase.lite.generation
import kotlin.test.*

class ErrorCaseTest : BaseDbTest() {

    internal class CustomClass {
        var text = "custom"
    }

    // -- DatabaseTest
    @Test
    fun testDeleteSameDocTwice() {
        // Store doc:
        val docID = "doc1"
        val doc = createSingleDocInBaseTestDb(docID)

        // First time deletion:
        baseTestDb.delete(doc)
        assertEquals(0, baseTestDb.count)
        assertNull(baseTestDb.getDocument(docID))

        // Second time deletion:
        // NOTE: doc is pointing to old revision. this cause conflict but this generate same revision
        baseTestDb.delete(doc)
        assertNull(baseTestDb.getDocument(docID))
    }

    // -- DatabaseTest
    @Test
    fun testDeleteUnsavedDocument() {
        val doc = MutableDocument("doc1")
        doc.setValue("name", "Scott Tiger")
        try {
            baseTestDb.delete(doc)
            fail()
        } catch (e: CouchbaseLiteException) {
            if (e.code != CBLError.Code.NOT_FOUND) {
                fail()
            }
        }
        assertEquals("Scott Tiger", doc.getValue("name"))
    }

    @Test
    fun testSaveSavedMutableDocument() {
        val doc = MutableDocument("doc1")
        doc.setValue("name", "Scott Tiger")
        @Suppress("VARIABLE_WITH_REDUNDANT_INITIALIZER")
        var saved = saveDocInBaseTestDb(doc)
        doc.setValue("age", 20)
        saved = saveDocInBaseTestDb(doc)
        assertEquals(2, saved.generation)
        assertEquals(20, saved.getInt("age").toLong())
        assertEquals("Scott Tiger", saved.getString("name"))
    }

    @Test
    fun testDeleteSavedMutableDocument() {
        val doc = MutableDocument("doc1")
        doc.setValue("name", "Scott Tiger")
        saveDocInBaseTestDb(doc)
        baseTestDb.delete(doc)
        assertNull(baseTestDb.getDocument("doc1"))
    }

    @Test
    fun testDeleteDocAfterPurgeDoc() {
        val doc = MutableDocument("doc1")
        doc.setValue("name", "Scott Tiger")
        val saved = saveDocInBaseTestDb(doc)

        // purge doc
        baseTestDb.purge(saved)
        try {
            baseTestDb.delete(saved)
            fail()
        } catch (e: CouchbaseLiteException) {
            if (e.code != CBLError.Code.NOT_FOUND) {
                fail()
            }
        }
    }

    @Test
    fun testDeleteDocAfterDeleteDoc() {
        val doc = MutableDocument("doc1")
        doc.setValue("name", "Scott Tiger")
        val saved = saveDocInBaseTestDb(doc)

        // delete doc
        baseTestDb.delete(saved)

        // delete doc -> conflict resolver -> no-op
        baseTestDb.delete(saved)
    }

    @Test
    fun testPurgeDocAfterDeleteDoc() {
        val doc = MutableDocument("doc1")
        doc.setValue("name", "Scott Tiger")
        val saved = saveDocInBaseTestDb(doc)

        // delete doc
        baseTestDb.delete(saved)

        // purge doc
        baseTestDb.purge(saved)
    }

    @Test
    fun testPurgeDocAfterPurgeDoc() {
        val doc = MutableDocument("doc1")
        doc.setValue("name", "Scott Tiger")
        val saved = saveDocInBaseTestDb(doc)

        // purge doc
        baseTestDb.purge(saved)
        try {
            baseTestDb.purge(saved)
            fail()
        } catch (e: CouchbaseLiteException) {
            if (e.code != CBLError.Code.NOT_FOUND) {
                fail()
            }
        }
    }

    // -- ArrayTest
    @Test
    fun testAddValueUnExpectedObject() {
        assertFailsWith<IllegalArgumentException> {
            MutableArray().addValue(CustomClass())
        }
    }

    @Test
    fun testSetValueUnExpectedObject() {
        assertFailsWith<IllegalArgumentException> {
            MutableArray().setValue(0, CustomClass())
        }
    }
}
