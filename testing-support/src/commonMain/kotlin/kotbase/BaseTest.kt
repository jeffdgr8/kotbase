@file:Suppress("MemberVisibilityCanBePrivate")

package kotbase

import com.couchbase.lite.isOpen
import com.couchbase.lite.withDbLock
import kotbase.internal.utils.FileUtils
import kotbase.internal.utils.Report
import kotbase.internal.utils.StringUtils
import kotbase.internal.utils.paddedString
import kotbase.test.AfterClass
import kotbase.test.BeforeClass
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
        Report.log("<<<<<<<< Test completed(${formatInterval(Clock.System.now() - startTime)})")
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
        val dbDir = "${config.directory}/$dbName$DB_EXTENSION"
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
        if (db.withDbLock { !db.isOpen }) {
            return true
        }
        return doSafely("Close db " + db.name, db::close)
    }

    protected fun deleteDb(db: Database?): Boolean {
        if (db == null) {
            return true
        }
        val isOpen = db.withDbLock {
            db.isOpen
        }
        // there is a race here... probably small.
        return if (isOpen) {
            doSafely("Delete db " + db.name, db::delete)
        } else {
            //db.dbPath?.let { path ->
            //    FileUtils.eraseFileOrDir(path)
            //} ?: true

            // Use Database.delete() as eraseFileOrDir() may fail
            // to delete blobs right after database was closed
            try {
                Database.delete(db.name, db.config.directory)
                true
            } catch (e: CouchbaseLiteException) {
                // Already deleted
                e.domain == CBLError.Domain.CBLITE && e.code == CBLError.Code.NOT_FOUND
            }
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

    @Suppress("unused")
    companion object {

        const val STD_TIMEOUT_SEC = 10L
        const val LONG_TIMEOUT_SEC = 60L
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
