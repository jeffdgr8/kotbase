package com.couchbase.lite.kmp

import com.couchbase.lite.kmp.internal.utils.FileUtils
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
        } finally {
            deleteDb(db)
        }
    }

    @Test
    fun testConfigurationIsCopiedWhenGetSet() {
        val config = DatabaseConfiguration()
            .setDirectory(getScratchDirectoryPath(getUniqueName("copy-config-dir")))

        val db = createDb("config_copied_db", config)
        try {
            assertNotNull(db.config)
            assertNotSame(db.config, config)
        } finally {
            deleteDb(db)
        }
    }

    @Test
    fun testDatabaseConfigurationDefaultDirectory() {
        val expectedPath = DatabaseConfiguration().directory

        val config = DatabaseConfiguration()
        assertEquals(config.directory, expectedPath)

        val db = createDb("default_dir_db", config)
        try {
            assertTrue(FileUtils.getCanonicalPath(db.path!!).contains(expectedPath))
        } finally {
            db.delete()
        }
    }

    @Test
    fun testCreateWithDefaultConfiguration() {
        val db = createDb("default_config_db")
        try {
            assertNotNull(db)
            assertEquals(0, db.count)
        } finally {
            deleteDb(db)
        }
    }

    @Test
    fun testCreateWithEmptyDBNames() {
        assertFailsWith<IllegalArgumentException> {
            Database("")
        }
    }

    @Test
    fun testCreateWithSpecialCharacterDBNames() {
        val LEGAL_FILE_NAME_CHARS = "`~@#$%^&()_+{}][=-.,;'12345ABCDEabcde"
        val db = Database(LEGAL_FILE_NAME_CHARS)
        try {
            assertEquals(LEGAL_FILE_NAME_CHARS, db.name)
        } finally {
            deleteDb(db)
        }
    }

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
            deleteDb(db)
        }
    }
}
