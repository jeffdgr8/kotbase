package kotbase

import kotbase.internal.utils.FileUtils
import kotbase.internal.utils.TestUtils.assertThrowsCBL
import kotlin.test.*

class DatabaseEncryptionTest : BaseDbTest() {

    // DatabaseEncryptionTest.swift

    private var seekrit: Database? = null

    @AfterTest
    fun tearDown() {
        seekrit?.close()
        seekrit = null
        Database.delete(SEEKRIT)
    }

    private fun openSeekrit(password: String?): Database {
        val config = DatabaseConfiguration()
        config.encryptionKey = if (password != null) EncryptionKey(password) else null
        return Database(SEEKRIT, config)
    }

    @Test
    fun testUnEncryptedDatabase() {
        seekrit = openSeekrit(null)

        val doc = MutableDocument(mapOf("answer" to 42))
        seekrit!!.save(doc)
        seekrit!!.close()
        seekrit = null

        // Try to reopen with password (fails):
        assertThrowsCBL(CBLError.Domain.CBLITE, CBLError.Code.NOT_A_DATABASE_FILE) {
            openSeekrit("wrong")
        }

        // Reopen with no password:
        seekrit = openSeekrit(null)
        assertEquals(1, seekrit!!.count)
    }

    @Test
    fun testEncryptedDatabase() {
        // Create encrypted database:
        seekrit = openSeekrit("letmein")

        val doc = MutableDocument(mapOf("answer" to 42))
        seekrit!!.save(doc)
        seekrit!!.close()
        seekrit = null

        // Reopen without password (fails):
        assertThrowsCBL(CBLError.Domain.CBLITE, CBLError.Code.NOT_A_DATABASE_FILE) {
            openSeekrit(null)
        }

        // Reopen with wrong password (fails):
        assertThrowsCBL(CBLError.Domain.CBLITE, CBLError.Code.NOT_A_DATABASE_FILE) {
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
        assertEquals(0, seekrit!!.count)
        seekrit!!.close()
        seekrit = null

        // Make sure it doesn't need a password now:
        seekrit = openSeekrit(null)
        assertEquals(0, seekrit!!.count)
        seekrit!!.close()
        seekrit = null

        // Make sure old password doesn't work:
        assertThrowsCBL(CBLError.Domain.CBLITE, CBLError.Code.NOT_A_DATABASE_FILE) {
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
        seekrit!!.save(doc)

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
        val savedDoc = seekrit!!.getDocument("att")
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
        seekrit = openSeekrit(SEEKRIT)

        // Get another instance of the database:
        val seekrit2 = openSeekrit(SEEKRIT)
        seekrit2.close()

        // Try rekey:
        val newKey = EncryptionKey("foobar")
        seekrit!!.changeEncryptionKey(newKey)
    }

    fun rekey(oldPassword: String?, newPassword: String?) {
        // First run the encryped blobs test to populate the database:
        testEncryptedBlobs(oldPassword)

        // Create some documents:
        seekrit!!.inBatch {
            for (i in 0..99) {
                val doc = MutableDocument(mapOf("seq" to i))
                seekrit!!.save(doc)
            }
        }

        // Rekey:
        val newKey = if (newPassword != null) EncryptionKey(newPassword) else null
        seekrit!!.changeEncryptionKey(newKey)

        // Close & reopen seekrit:
        seekrit!!.close()
        seekrit = null

        // Reopen the database with the new key:
        val seekrit2 = openSeekrit(newPassword)
        seekrit = seekrit2

        // Check the document and its attachment
        val doc = seekrit!!.getDocument("att")
        assertNotNull(doc)
        val blob = doc.getBlob("blob")!!
        assertNotNull(blob.digest)
        assertNotNull(blob.content)
        val content = blob.content!!.decodeToString()
        assertEquals("This is a blob!", content)

        // Query documents:
        val seq = Expression.property("seq")
        val query = QueryBuilder
            .select(SelectResult.expression(seq))
            .from(DataSource.database(seekrit!!))
            .where(seq.isValued())
            .orderBy(Ordering.expression(seq))
        val rs = query.execute()
        assertEquals(100, rs.count())

        for ((i, r) in rs.withIndex()) {
            assertEquals(i, r.getInt(0))
        }
    }

    companion object {
        private const val SEEKRIT = "seekrit"
    }
}
