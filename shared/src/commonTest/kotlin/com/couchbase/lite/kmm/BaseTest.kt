package com.couchbase.lite.kmm

import com.couchbase.lite.kmm.internal.utils.Report
import com.couchbase.lite.kmm.internal.utils.StringUtils
import com.udobny.kmm.test.AfterClass
import com.udobny.kmm.test.BeforeClass
import kotlin.jvm.JvmStatic
import kotlin.test.assertFalse
import kotlin.test.assertTrue

abstract class BaseTest : PlatformBaseTest() {

//    protected var testSerialExecutor: CloseableExecutor? = null
//    private var testName: String? = null
//    private var startTime: Long = 0
//
//    @org.junit.Rule
//    var watcher: TestRule = object : TestWatcher() {
//        protected override fun starting(description: org.junit.runner.Description) {
//            testName = description.getMethodName()
//        }
//    }
//
//    @BeforeTest
//    fun setUpBaseTest() {
//        Report.log(LogLevel.INFO, ">>>>>>>> Test started: $testName")
//        com.couchbase.lite.internal.support.Log.initLogging()
//        setupPlatform()
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
//        startTime = System.currentTimeMillis()
//    }
//
//    @AfterTest
//    fun tearDownBaseTest() {
//        var succeeded = false
//        if (testSerialExecutor != null) {
//            succeeded = testSerialExecutor.stop(2, java.util.concurrent.TimeUnit.SECONDS)
//        }
//        Report.log(LogLevel.DEBUG, "Executor stopped: $succeeded")
//        Report.log(
//            LogLevel.INFO,
//            "<<<<<<<< Test completed(%s): %s",
//            formatInterval(System.currentTimeMillis() - startTime),
//            testName
//        )
//    }
//
//    protected fun skipTestWhen(tag: String) {
//        val exclusion: Exclusion = getExclusions(tag)
//        if (exclusion != null) {
//            Assume.assumeFalse(exclusion.msg, exclusion.test.get())
//        }
//    }

    protected fun getUniqueName(prefix: String): String {
        return StringUtils.getUniqueName(prefix, 12)
    }

//    protected fun waitUntil(
//        maxTime: Long,
//        test: com.couchbase.lite.internal.utils.Fn.Provider<Boolean?>
//    ) {
//        val delay: Long = 100
//        if (maxTime <= delay) {
//            assertTrue(test.get())
//        }
//        val endTimes: Long = System.currentTimeMillis() + maxTime - delay
//        do {
//            try {
//                Thread.sleep(delay)
//            } catch (e: InterruptedException) {
//                break
//            }
//            if (test.get()) {
//                return
//            }
//        } while (System.currentTimeMillis() < endTimes)
//
//        // assertTrue() provides a more relevant message than fail()
//        assertTrue(false)
//    }
//
//    protected fun getScratchDirectoryPath(name: String): String {
//        return try {
//            val path: String = com.couchbase.lite.internal.utils.FileUtils.verifyDir(
//                File(
//                    getTmpDir(),
//                    name
//                )
//            ).getCanonicalPath()
//            SCRATCH_DIRS.add(path)
//            path
//        } catch (e: IOException) {
//            throw IllegalStateException("Failed creating scratch directory: $name", e)
//        }
//    }

    // Prefer this method to any other way of creating a new database
    @Throws(CouchbaseLiteException::class)
    protected fun createDb(
        name: String,
        config: DatabaseConfiguration = DatabaseConfiguration()
    ): Database {
        val dbName = getUniqueName(name)
        val dbDir = "${config.getDirectory()}/$dbName.cblite2" // C4Database.DB_EXTENSION
        assertFalse(dirExists(dbDir))
        val db = Database(dbName, config)
        assertTrue(dirExists(dbDir))
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
        // TODO:
        if (db == null) {
            return true
        }
        return doSafely("Close db " + db.name) {
            db.close()
        }
//        synchronized(db.getDbLock()) {
//            if (db == null || !db.isOpen()) {
//                return true
//            }
//        }
//        return doSafely("Close db " + db!!.name, TaskThrows<CouchbaseLiteException> { db.close() })
    }

    protected fun deleteDb(db: Database?): Boolean {
        // TODO:
        if (db == null) {
            return true
        }
        return doSafely("Delete db " + db.name) {
            db.delete()
        }
//        if (db == null) {
//            return true
//        }
//        var isOpen: Boolean
//        synchronized(db.getDbLock()) { isOpen = db.isOpen() }
//        // there is a race here... probably small.
//        return if (isOpen) doSafely(
//            "Delete db " + db.name,
//            TaskThrows<CouchbaseLiteException> { db.delete() }) else com.couchbase.lite.internal.utils.FileUtils.eraseFileOrDir(
//            db.getDbFile()
//        )
    }

//    protected fun formatInterval(ms: Long): String {
//        var ms = ms
//        val min: Long = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(ms)
//        ms -= java.util.concurrent.TimeUnit.MINUTES.toMillis(min)
//        val sec: Long = java.util.concurrent.TimeUnit.MILLISECONDS.toSeconds(ms)
//        ms -= java.util.concurrent.TimeUnit.SECONDS.toMillis(sec)
//        return String.format("%02d:%02d.%03d", min, sec, ms)
//    }

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
                // TODO:
                //com.couchbase.lite.internal.utils.FileUtils.eraseFileOrDir(path)
            }
            SCRATCH_DIRS.clear()
            Report.log(LogLevel.INFO, "<<<<<<<<<<<< Suite completed")
        }
    }
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect fun dirExists(dir: String): Boolean
