package com.couchbase.lite.kmm

import com.couchbase.lite.kmm.internal.utils.FileUtils
import com.couchbase.lite.kmm.internal.utils.PlatformUtils
import com.couchbase.lite.kmm.internal.utils.ZipUtils
import kotlin.test.*

class MigrationTest : BaseTest() {

    private lateinit var dbDir: String
    private var migrationTestDb: Database? = null

    @BeforeTest
    fun setUpMigrationTest() {
        dbDir = DatabaseConfiguration().getDirectory() + "/" + getUniqueName("migration-test-dir")
    }

    @AfterTest
    fun tearDownMigrationTest() {
        deleteDb(migrationTestDb)
        FileUtils.eraseFileOrDir(dbDir)
    }

    // TODO: 1.x DB's attachment is not automatically detected as blob
    // https://github.com/couchbase/couchbase-lite-android/issues/1237
    @Test
    @Throws(Exception::class)
    fun testOpenExistingDBv1x() {
        ZipUtils.unzip(PlatformUtils.getAsset("replacedb/android140-sqlite.cblite2.zip")!!, dbDir)

        migrationTestDb = openDatabase()
        assertEquals(2, migrationTestDb!!.count)
        for (i in 1..2) {
            val doc = migrationTestDb!!.getDocument("doc$i")
            assertNotNull(doc)
            assertEquals(i.toString(), doc.getString("key"))
            val attachments = doc.getDictionary("_attachments")
            assertNotNull(attachments)
            val key = "attach$i"
            val blob = attachments.getBlob(key)
            assertNotNull(blob)
            val attach = "attach$i".encodeToByteArray()
            assertContentEquals(attach, blob.content)
        }
    }

    // https://github.com/couchbase/couchbase-lite-android/issues/1237
    @Test
    @Throws(Exception::class)
    fun testOpenExistingDBv1xNoAttachment() {
        ZipUtils.unzip(
            PlatformUtils.getAsset("replacedb/android140-sqlite-noattachment.cblite2.zip")!!, dbDir
        )

        migrationTestDb = openDatabase()
        assertEquals(2, migrationTestDb!!.count)
        for (i in 1..2) {
            val doc = migrationTestDb!!.getDocument("doc$i")
            assertNotNull(doc)
            assertEquals(i.toString(), doc.getString("key"))
        }
    }

    @Test
    @Throws(Exception::class)
    fun testOpenExistingDB() {
        ZipUtils.unzip(PlatformUtils.getAsset("replacedb/android200-sqlite.cblite2.zip")!!, dbDir)

        migrationTestDb = openDatabase()
        assertEquals(2, migrationTestDb!!.count)
        for (i in 1..2) {
            val doc = migrationTestDb!!.getDocument("doc$i")
            assertNotNull(doc)
            assertEquals(i.toString(), doc.getString("key"))
            val blob = doc.getBlob("attach$i")
            assertNotNull(blob)
            val attach = "attach$i".encodeToByteArray()
            assertContentEquals(attach, blob.content)
        }
    }

    private fun openDatabase(): Database {
        val config = DatabaseConfiguration()
        config.setDirectory(FileUtils.getCanonicalPath(dbDir))
        return Database(DB_NAME, config)
    }

    companion object {
        private const val DB_NAME = "android-sqlite"
    }
}
