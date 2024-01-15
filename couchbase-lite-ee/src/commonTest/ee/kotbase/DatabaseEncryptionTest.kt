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

import kotbase.internal.utils.FileUtils
import kotlin.test.*

class DatabaseEncryptionTest : BaseDbTest() {

    // DatabaseEncryptionTest.swift

    private var seekrit: Database? = null

    @AfterTest
    fun tearDown() {
        eraseDb(seekrit)
        seekrit = null
    }

    private fun openSeekrit(password: String?): Database {
        val config = DatabaseConfiguration()
        config.encryptionKey = if (password != null) EncryptionKey(password) else null
        return Database("seekrit", config)
    }

    @Test
    fun testUnEncryptedDatabase() {
        seekrit = openSeekrit(null)

        val doc = MutableDocument(mapOf("answer" to 42))
        seekrit!!.defaultCollection.save(doc)
        seekrit!!.close()
        seekrit = null

        // Try to reopen with password (fails):
        assertThrowsCBLException(CBLError.Domain.CBLITE, CBLError.Code.NOT_A_DATABASE_FILE) {
            openSeekrit("wrong")
        }

        // Reopen with no password:
        seekrit = openSeekrit(null)
        assertEquals(1, seekrit!!.defaultCollection.count)
    }

    @Test
    fun testEncryptedDatabase() {
        // Create encrypted database:
        seekrit = openSeekrit("letmein")

        val doc = MutableDocument(mapOf("answer" to 42))
        seekrit!!.defaultCollection.save(doc)
        seekrit!!.close()
        seekrit = null

        // Reopen without password (fails):
        assertThrowsCBLException(CBLError.Domain.CBLITE, CBLError.Code.NOT_A_DATABASE_FILE) {
            openSeekrit(null)
        }

        // Reopen with wrong password (fails):
        assertThrowsCBLException(CBLError.Domain.CBLITE, CBLError.Code.NOT_A_DATABASE_FILE) {
            openSeekrit("wrong")
        }

        // Reopen with correct password:
        seekrit = openSeekrit("letmein")
    }

    @Test
    fun testDeleteEcnrypedDatabase() {
        // Create encrypted database:
        seekrit = openSeekrit("letmein")

        // Delete database:
        seekrit!!.delete()

        // Re-create database:
        seekrit = openSeekrit(null)
        assertEquals(0, seekrit!!.defaultCollection.count)
        seekrit!!.close()

        // Make sure it doesn't need a password now:
        seekrit = openSeekrit(null)
        assertEquals(0, seekrit!!.defaultCollection.count)
        seekrit!!.close()

        // Make sure old password doesn't work:
        assertThrowsCBLException(CBLError.Domain.CBLITE, CBLError.Code.NOT_A_DATABASE_FILE) {
            openSeekrit("letmein")
        }
    }

    @Test
    fun testEncryptedBlobs() {
        testEncryptedBlobs("letmein")
    }

    private fun testEncryptedBlobs(password: String?) {
        // Create database with the password:
        seekrit = openSeekrit(password)

        // Save a doc with a blob:
        val doc = MutableDocument("att")
        val body = "This is a blob!".encodeToByteArray()
        var blob = Blob("text/plain", body)
        doc.setValue("blob", blob)
        seekrit!!.defaultCollection.save(doc)

        // Read content from the raw blob file:
        blob = doc.getBlob("blob")!!
        val digest = blob.digest
        assertNotNull(digest)

        val fileName = digest.substring(5).replace("/", "_")
        val path = "${seekrit!!.path!!}Attachments/$fileName.blob"
        val raw = FileUtils.read(path)
        if (password != null) {
            assertNotEquals(body, raw)
        } else {
            assertEquals(body, raw)
        }

        // Check blob content:
        val savedDoc = seekrit!!.defaultCollection.getDocument("att")
        assertNotNull(savedDoc)
        blob = savedDoc.getBlob("blob")!!
        assertNotNull(blob.digest)
        assertNotNull(blob.content)
        val content = blob.content!!.decodeToString()
        assertEquals("This is a blob!", content)
    }

    @Test
    fun testMultipleDatabases() {
        // Create encrypted database:
        seekrit = openSeekrit("seekrit")

        // Get another instance of the database:
        val seekrit2 = openSeekrit("seekrit")
        seekrit2.close()

        // Try rekey:
        val newKey = EncryptionKey("foobar")
        seekrit!!.changeEncryptionKey(newKey)

        // Open with new password
        val seekrit3 = openSeekrit("foobar")
        seekrit3.close()
    }
}
