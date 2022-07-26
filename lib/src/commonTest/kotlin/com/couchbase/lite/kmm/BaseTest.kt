package com.couchbase.lite.kmm

import com.couchbase.lite.dbPath
import com.couchbase.lite.isOpen
import com.couchbase.lite.kmm.internal.utils.FileUtils
import com.couchbase.lite.kmm.internal.utils.Report
import com.couchbase.lite.kmm.internal.utils.StringUtils
import com.couchbase.lite.kmm.internal.utils.paddedString
import com.couchbase.lite.withLock
import com.udobny.kmm.test.AfterClass
import com.udobny.kmm.test.BeforeClass
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import okio.IOException
import kotlin.jvm.JvmStatic
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

abstract class BaseTest : PlatformTest() {

    private var startTime: Instant = Instant.DISTANT_PAST

    @BeforeTest
    fun setUpBaseTest() {
        Report.log(">>>>>>>> Test started")

        setupPlatform()

        startTime = Clock.System.now()
    }

    @AfterTest
    fun tearDownBaseTest() {
        Report.log(
            "<<<<<<<< Test completed(%s)",
            formatInterval(Clock.System.now() - startTime)
        )
    }

    protected fun getUniqueName(prefix: String): String {
        return StringUtils.getUniqueName(prefix, 12)
    }

    protected fun waitUntil(maxTime: Long, test: () -> Boolean) {
        val delay = 100L
        if (maxTime <= delay) {
            assertTrue(test())
        }
        val endTimes = Clock.System.now() + (maxTime - delay).milliseconds
        do {
            try {
                runBlocking {
                    delay(delay)
                }
            } catch (e: CancellationException) {
                break
            }
            if (test()) {
                return
            }
        } while (Clock.System.now() < endTimes)

        // assertTrue() provides a more relevant message than fail()
        assertTrue(false)
    }

    protected fun getScratchDirectoryPath(name: String): String {
        return try {
            val path = FileUtils.getCanonicalPath(FileUtils.verifyDir("$tmpDir/$name"))
            SCRATCH_DIRS.add(path)
            path
        } catch (e: IOException) {
            throw IllegalStateException("Failed creating scratch directory: $name", e)
        }
    }

    // Prefer this method to any other way of creating a new database
    protected fun createDb(
        name: String,
        config: DatabaseConfiguration = DatabaseConfiguration()
    ): Database {
        val dbName = getUniqueName(name)
        val dbDir = "${config.getDirectory()}/$dbName$DB_EXTENSION"
        assertFalse(FileUtils.dirExists(dbDir))
        val db = Database(dbName, config)
        assertTrue(FileUtils.dirExists(dbDir))
        return db
    }

    protected fun duplicateDb(db: Database, config: DatabaseConfiguration? = null): Database {
        return Database(db.name, config ?: DatabaseConfiguration())
    }

    protected fun reopenDb(db: Database, config: DatabaseConfiguration? = null): Database {
        val dbName = db.name
        assertTrue(closeDb(db))
        return Database(dbName, config ?: DatabaseConfiguration())
    }

    protected fun recreateDb(db: Database, config: DatabaseConfiguration? = null): Database {
        val dbName = db.name
        assertTrue(deleteDb(db))
        return Database(dbName, config ?: DatabaseConfiguration())
    }

    protected fun closeDb(db: Database?): Boolean {
        if (db == null) {
            return true
        }
        if (db.withLock { !db.isOpen }) {
            return true
        }
        return doSafely("Close db " + db.name, db::close)
    }

    protected fun deleteDb(db: Database?): Boolean {
        if (db == null) {
            return true
        }
        val isOpen = db.withLock {
            db.isOpen
        }
        // there is a race here... probably small.
        return if (isOpen) {
            doSafely("Delete db " + db.name, db::delete)
        } else {
            db.dbPath?.let { path ->
                FileUtils.eraseFileOrDir(path)
            } ?: true
        }
    }

    protected fun formatInterval(duration: Duration): String {
        return duration.toComponents { min, sec, nano ->
            val mil = nano / 1_000_000
            "${min.paddedString(2)}:${sec.paddedString(2)}.${mil.paddedString(3)}"
        }
    }

    protected fun doSafely(
        taskDesc: String,
        task: () -> Unit
    ): Boolean {
        try {
            task()
            Report.log("$taskDesc succeeded")
            return true
        } catch (ex: CouchbaseLiteException) {
            Report.log("$taskDesc failed", ex)
        }
        return false
    }

    companion object {

        const val STD_TIMEOUT_SEC = 10L
        const val LONG_TIMEOUT_SEC = 30L
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
    }
}
