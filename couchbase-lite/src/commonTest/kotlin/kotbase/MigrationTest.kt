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
import kotbase.internal.utils.PlatformUtils
import kotbase.internal.utils.ZipUtils
import kotbase.test.IgnoreNative
import kotlin.test.*

class MigrationTest : BaseTest() {

    private lateinit var dbDir: String
    private var migrationTestDb: Database? = null

    @BeforeTest
    fun setUpMigrationTest() {
        dbDir = DatabaseConfiguration().directory + "/" + getUniqueName("migration-test-dir")
    }

    @AfterTest
    fun tearDownMigrationTest() {
        deleteDb(migrationTestDb)
        FileUtils.eraseFileOrDir(dbDir)
    }

    // Native C doesn't support legacy 1.x blob
    @IgnoreNative
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