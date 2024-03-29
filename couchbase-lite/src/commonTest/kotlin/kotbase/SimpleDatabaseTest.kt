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

class SimpleDatabaseTest : BaseTest() {

    @Test
    fun testCreateConfiguration() {
        // Default:
        val config1 = DatabaseConfiguration()
        assertNotNull(config1.directory)
        assertFalse(config1.directory.isEmpty())

        // Custom
        val config2 = DatabaseConfiguration()
        val dbDir = getScratchDirectoryPath(getUniqueName("tmp"))
        config2.setDirectory(dbDir)
        assertEquals(dbDir, config2.directory)
    }

    @Test
    fun testGetSetConfiguration() {
        val config = DatabaseConfiguration()
            .setDirectory(getScratchDirectoryPath(getUniqueName("get-set-config-dir")))

        val db = createDb("get_set_config_db", config)
        try {
            val newConfig = db.config
            assertNotNull(newConfig)
            assertEquals(config.directory, newConfig.directory)
        } finally { eraseDb(db) }
    }

    @Test
    fun testConfigurationIsCopiedWhenGetSet() {
        val config = DatabaseConfiguration()
            .setDirectory(getScratchDirectoryPath(getUniqueName("copy-config-dir")))

        val db = createDb("config_copied_db", config)
        try {
            assertNotNull(db.config)
            assertNotSame(db.config, config)
        } finally { eraseDb(db) }
    }

    @Test
    fun testDatabaseConfigurationDefaultDirectory() {
        val expectedPath = DatabaseConfiguration().directory

        val config = DatabaseConfiguration()
        assertEquals(config.directory, expectedPath)

        val db = createDb("default_dir_db", config)
        try {
            assertTrue(FileUtils.getCanonicalPath(db.path!!).contains(expectedPath))
        } finally { db.delete() }
    }

    @Suppress("DEPRECATION")
    @Test
    fun testCreateWithDefaultConfiguration() {
        val db = createDb("default_config_db")
        try {
            assertNotNull(db)
            assertEquals(0, db.count)
        } finally { eraseDb(db) }
    }

    @Test
    fun testCreateWithEmptyDBNames() {
        assertFailsWith<IllegalArgumentException> { Database("") }
    }

    @Test
    fun testCreateWithSpecialCharacterDBNames() {
        val db = Database(LEGAL_FILE_NAME_CHARS)
        try { assertEquals(LEGAL_FILE_NAME_CHARS, db.name) }
        finally { eraseDb(db) }
    }

    @Suppress("DEPRECATION")
    @Test
    fun testCreateWithCustomDirectory() {
        val dir = getScratchDirectoryPath(getUniqueName("create-custom-dir"))

        val dbName = getUniqueName("create_custom_db")

        // create db with custom directory
        val config = DatabaseConfiguration().setDirectory(dir)
        val db = Database(dbName, config)

        try {
            assertNotNull(db)
            assertTrue(Database.exists(dbName, dir))

            assertEquals(dbName, db.name)

            val path = FileUtils.getCanonicalPath(db.path!!)
            assertTrue(path.endsWith(DB_EXTENSION))
            assertTrue(path.contains(dir))

            assertEquals(0, db.count)
        } finally {
            eraseDb(db)
        }
    }
}
