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

abstract class BaseTest : PlatformBaseTest() {

//    protected var testSerialExecutor: CloseableExecutor? = null
//    private var testName: String? = null
    private var startTime: Instant = Instant.DISTANT_PAST

//    @org.junit.Rule
//    var watcher: TestRule = object : TestWatcher() {
//        protected override fun starting(description: org.junit.runner.Description) {
//            testName = description.getMethodName()
//        }
//    }

    @BeforeTest
    fun setUpBaseTest() {
//        Report.log(LogLevel.INFO, ">>>>>>>> Test started: $testName")
        Report.log(LogLevel.INFO, ">>>>>>>> Test started")
//        com.couchbase.lite.internal.support.Log.initLogging()
        setupPlatform()
//        testSerialExecutor = object : CloseableExecutor() {
//            val executor: java.util.concurrent.ExecutorService =
//                java.util.concurrent.Executors.newSingleThreadExecutor()
//
//            override fun execute(@NonNull task: Runnable) {
//                Report.log("task enqueued: $task")
//                executor.execute(Runnable {
//                    Report.log("task started: $task")
//                    task.run()
//                    Report.log("task finished: $task")
//                })
//            }
//
//            override fun stop(
//                timeout: Long,
//                @NonNull unit: java.util.concurrent.TimeUnit
//            ): Boolean {
//                executor.shutdownNow()
//                return true
//            }
//        }
        startTime = Clock.System.now()
    }

    @AfterTest
    fun tearDownBaseTest() {
//        var succeeded = false
//        if (testSerialExecutor != null) {
//            succeeded = testSerialExecutor.stop(2, java.util.concurrent.TimeUnit.SECONDS)
//        }
//        Report.log(LogLevel.DEBUG, "Executor stopped: $succeeded")
//        Report.log(
//            LogLevel.INFO,
//            "<<<<<<<< Test completed(%s): %s",
//            formatInterval(Clock.System.now() - startTime),
//            testName
//        )
        Report.log(
            LogLevel.INFO,
            "<<<<<<<< Test completed(%s)",
            formatInterval(Clock.System.now() - startTime)
        )
    }

//    protected fun skipTestWhen(tag: String) {
//        val exclusion = getExclusions(tag)
//        if (exclusion != null) {
//            Assume.assumeFalse(exclusion.msg, exclusion.test())
//        }
//    }

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
    @Throws(CouchbaseLiteException::class)
    protected fun createDb(
        name: String,
        config: DatabaseConfiguration = DatabaseConfiguration()
    ): Database {
        val dbName = getUniqueName(name)
        val dbDir = "${config.getDirectory()}/$dbName.cblite2" // C4Database.DB_EXTENSION
        println("dbDir = $dbDir")
        assertFalse(FileUtils.dirExists(dbDir))
        val db = Database(dbName, config)
        assertTrue(FileUtils.dirExists(dbDir))
        return db
    }

    @Throws(CouchbaseLiteException::class)
    protected fun duplicateDb(db: Database, config: DatabaseConfiguration? = null): Database {
        return Database(db.name, config ?: DatabaseConfiguration())
    }

    @Throws(CouchbaseLiteException::class)
    protected fun reopenDb(db: Database, config: DatabaseConfiguration? = null): Database {
        val dbName = db.name
        assertTrue(closeDb(db))
        return Database(dbName, config ?: DatabaseConfiguration())
    }

    @Throws(CouchbaseLiteException::class)
    protected fun recreateDb(db: Database, config: DatabaseConfiguration? = null): Database {
        val dbName = db.name
        assertTrue(deleteDb(db))
        return Database(dbName, config ?: DatabaseConfiguration())
    }

    protected fun closeDb(db: Database?): Boolean {
        if (db == null) {
            return true
        }
        return db.withLock {
            if (!db.isOpen) {
                true
            } else {
                doSafely("Close db " + db.name, db::close)
            }
        }
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
            Report.log(LogLevel.DEBUG, "$taskDesc succeeded")
            return true
        } catch (ex: CouchbaseLiteException) {
            Report.log(LogLevel.WARNING, "$taskDesc failed", ex)
        }
        return false
    }

    companion object {

        const val STD_TIMEOUT_SEC: Long = 10
        const val LONG_TIMEOUT_SEC: Long = 30
        val STD_TIMEOUT_MS = STD_TIMEOUT_SEC * 1000L
        val LONG_TIMEOUT_MS = LONG_TIMEOUT_SEC * 1000L
        private val SCRATCH_DIRS = mutableListOf<String>()

        @BeforeClass
        @JvmStatic
        fun setUpPlatformSuite() {
            Report.log(LogLevel.INFO, ">>>>>>>>>>>> Suite started")
        }

        @AfterClass
        @JvmStatic
        fun tearDownBaseTestSuite() {
            for (path in SCRATCH_DIRS) {
                FileUtils.eraseFileOrDir(path)
            }
            SCRATCH_DIRS.clear()
            Report.log(LogLevel.INFO, "<<<<<<<<<<<< Suite completed")
        }
    }
}
