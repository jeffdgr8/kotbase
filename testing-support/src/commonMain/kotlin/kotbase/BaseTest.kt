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
@file:Suppress("MemberVisibilityCanBePrivate")

package kotbase

import com.couchbase.lite.dbPath
import com.couchbase.lite.isOpen
import kotbase.internal.utils.FileUtils
import kotbase.internal.utils.Report
import kotbase.internal.utils.StringUtils
import kotbase.internal.utils.paddedString
import kotbase.test.AfterClass
import kotbase.test.BeforeClass
import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.io.IOException
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.JvmStatic
import kotlin.test.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

abstract class BaseTest : PlatformTest() {

    protected lateinit var testSerialCoroutineContext: CoroutineContext
    private var startTime: Instant = Instant.DISTANT_PAST

    @BeforeTest
    fun setUpBaseTest() {
        Report.log(">>>>>>>> Test started")

        setupPlatform()

        @OptIn(ExperimentalCoroutinesApi::class)
        testSerialCoroutineContext = Dispatchers.Default.limitedParallelism(1)

        startTime = Clock.System.now()
    }

    @AfterTest
    fun tearDownBaseTest() {
        Report.log("<<<<<<<< Test completed(${formatInterval(Clock.System.now() - startTime)})")
    }

    protected fun getScratchDirectoryPath(name: String): String {
        return try {
            val path = FileUtils.getCanonicalPath(FileUtils.verifyDir("$tmpDir/$name"))
            SCRATCH_DIRS.add(path)
            path
        } catch (e: IOException) {
            throw AssertionError("Failed creating scratch directory: $name", e)
        }
    }

    // Prefer this method to any other way of creating a new database
    protected fun createDb(
        name: String,
        config: DatabaseConfiguration = DatabaseConfiguration()
    ): Database {
        val dbName = getUniqueName(name)
        val dbDir = "${config.directory}/$dbName$DB_EXTENSION"
        assertFalse(FileUtils.dirExists(dbDir))
        val db = try {
            Database(dbName, config)
        } catch (e: Exception) {
            throw AssertionError("Failed creating database $name", e)
        }
        assertTrue(FileUtils.dirExists(dbDir))
        return db
    }

    // Get a new instance of the db or fail.
    protected fun duplicateDb(db: Database, config: DatabaseConfiguration? = null): Database {
        try {
            return Database(db.name, config ?: DatabaseConfiguration())
        } catch (e: Exception) {
            throw AssertionError("Failed duplicating database $db", e)
        }
    }

    // Close and reopen the db or fail.
    protected fun reopenDb(db: Database, config: DatabaseConfiguration? = null): Database {
        val dbName = db.name
        closeDb(db)
        try {
            return Database(dbName, config ?: DatabaseConfiguration())
        } catch (e: Exception) {
            throw AssertionError("Failed reopening database $db", e)
        }
    }

    // Close the db or fail.
    protected fun closeDb(db: Database) {
        try {
            db.close()
        } catch (e: Exception) {
            throw AssertionError("Failed closing database $db", e)
        }
    }

    // Delete the db or fail.
    protected fun deleteDb(db: Database) {
        try {
            // there is a race here but probably small.
            if (db.isOpen) {
                db.delete()
                return
            }
            val dbPath = db.dbPath
            if (dbPath != null && FileUtils.dirExists(dbPath)) {
                FileUtils.eraseFileOrDir(dbPath)
            }
        } catch (e: Exception) {
            throw AssertionError("Failed deleting database $db", e)
        }
    }

    // Test cleanup: Best effort to delete the db.
    protected fun eraseDb(db: Database?) {
        if (db == null) return
        try {
            // there is a race here but probably small.
            if (db.isOpen) {
                db.delete()
                return
            }
            val dbPath = db.dbPath
            if (dbPath != null && FileUtils.dirExists(dbPath)) {
                FileUtils.eraseFileOrDir(dbPath)
            }
        } catch (e: Exception) {
            Report.log("Failed to delete database $db", e)
        }
    }

    protected fun createTestDoc(): MutableDocument {
        return createTestDoc(1, 1, getUniqueName("no-tag"))
    }

    protected fun createTestDoc(tag: String): MutableDocument {
        return createTestDoc(1, 1, tag)
    }

    protected fun createTestDocs(
        first: Int,
        n: Int,
        tag: String = getUniqueName("no-tag")
    ): List<MutableDocument> {
        return buildList {
            val last = first + n - 1
            for (i in first..last) {
                add(createTestDoc(i, last, tag))
            }
        }
    }

    protected fun createComplexTestDoc(tag: String = getUniqueName("tag")): MutableDocument {
        return addComplexData(createTestDoc(1, 1, tag))
    }

    protected fun createComplexTestDocs(n: Int, tag: String): List<MutableDocument> {
        return createComplexTestDocs(1000, n, tag)
    }

    protected fun createComplexTestDocs(first: Int, n: Int, tag: String): List<MutableDocument> {
        return buildList {
            val last = first + n - 1
            for (i in first..last) {
                add(addComplexData(createTestDoc(i, last, tag)))
            }
        }
    }

    // Comparing documents isn't trivial: Fleece
    // will compress numeric values into the smallest
    // type that can be used to represent them.
    // This doc is sufficiently complex to make simple
    // comparison interesting but uses only values/types
    // that are seen to survive the Fleece round-trip, unchanged
    private fun createTestDoc(id: Int, top: Int, tag: String): MutableDocument {
        return MutableDocument().apply {
            setValue("nullValue", null)
            setBoolean("booleanTrue", true)
            setBoolean("booleanFalse", false)
            setLong("longZero", 0)
            setLong("longBig", 4000000000L)
            setLong("longSmall", -4000000000L)
            setDouble("doubleBig", 1.0E200)
            setDouble("doubleSmall", -1.0E200)
            setString("stringNull", null)
            setString("stringPunk", "Jett")
            setDate("dateNull", null)
            setDate("dateCB", Instant.parse(TEST_DATE))
            setBlob("blobNull", null)
            setString(TEST_DOC_TAG_KEY, tag)
            setLong(TEST_DOC_SORT_KEY, id.toLong())
            setLong(TEST_DOC_REV_SORT_KEY, (top - id).toLong())
        }
    }

    private fun addComplexData(mDoc: MutableDocument): MutableDocument {
        // Dictionary:
        val address: MutableDictionary = MutableDictionary()
        address.setValue("street", "1 Main street")
        address.setValue("city", "Mountain View")
        address.setValue("state", "CA")
        mDoc.setValue("address", address)

        // Array:
        val phones: MutableArray = MutableArray()
        phones.addValue("650-123-0001")
        phones.addValue("650-123-0002")
        mDoc.setValue("phones", phones)
        return mDoc
    }

    private fun formatInterval(duration: Duration): String {
        return duration.toComponents { min, sec, nano ->
            val mil = nano / 1_000_000
            "${min.paddedString(2)}:${sec.paddedString(2)}.${mil.paddedString(3)}"
        }
    }

    @Suppress("unused")
    companion object {

        const val STD_TIMEOUT_SEC = 10L
        const val LONG_TIMEOUT_SEC = 60L

        const val STD_TIMEOUT_MS = STD_TIMEOUT_SEC * 1000L
        const val LONG_TIMEOUT_MS = LONG_TIMEOUT_SEC * 1000L

        const val TEST_DATE = "2019-02-21T05:37:22.014Z"
        const val BLOB_CONTENT = "Knox on fox in socks in box. Socks on Knox and Knox in box."

        const val TEST_DOC_SORT_KEY = "TEST_SORT_ASC"
        const val TEST_DOC_REV_SORT_KEY = "TEST_SORT_DESC"
        const val TEST_DOC_TAG_KEY = "TEST_TAG"

        private val SCRATCH_DIRS = mutableListOf<String>()

        const val DB_EXTENSION = ".cblite2" // C4Database.DB_EXTENSION

        @BeforeClass
        @JvmStatic
        fun setUpPlatformSuite() {
            Report.log(">>>>>>>>>>>> Suite started")
        }

        @AfterClass
        @JvmStatic
        fun tearDownBaseTestSuite() {
            for (path in SCRATCH_DIRS) {
                FileUtils.eraseFileOrDir(path)
            }
            SCRATCH_DIRS.clear()
            Report.log("<<<<<<<<<<<< Suite completed")
        }

        fun getUniqueName(prefix: String): String =
            StringUtils.getUniqueName(prefix, 8)

        // Run a boolean function every `waitMs` until it is true
        // If it is not true within `maxWaitMs` fail.
        @JvmStatic
        protected fun waitUntil(maxWaitMs: Long, test: () -> Boolean) {
            val waitMs = 100L
            val endTime = Clock.System.now() + (maxWaitMs - waitMs).milliseconds
            while (true) {
                if (test()) {
                    break
                }
                if (Clock.System.now() > endTime) {
                    throw AssertionError("Operation timed out")
                }
                try {
                    runBlocking {
                        delay(waitMs)
                    }
                } catch (e: CancellationException) {
                    throw AssertionError("Operation interrupted", e)
                }
            }
        }

        fun assertIsCBLException(e: Exception?, domain: String? = null, code: Int = 0) {
            assertNotNull(e)
            if (e !is CouchbaseLiteException) {
                throw AssertionError("Expected CBL exception ($domain, $code) but got:", e)
            }
            if (domain != null) {
                assertEquals(domain, e.domain)
            }
            if (code > 0) {
                assertEquals(code.toLong(), e.code.toLong())
            }
        }

        fun assertThrowsCBLException(domain: String?, code: Int, block: () -> Unit) {
            try {
                block()
                fail("Expected CBL exception ($domain, $code)")
            } catch (e: Exception) {
                assertIsCBLException(e, domain, code)
            }
        }
    }
}
