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
// TODO: uses some internal APIs
//  https://forums.couchbase.com/t/objc-sdk-doesnt-set-length-after-database-getblob/34077/12
package kotbase

import kotbase.internal.utils.FileUtils
import kotbase.logging.ConsoleLogSink
import kotbase.logging.CustomLogSink
import kotbase.logging.FileLogSink
import kotbase.logging.LogSinks
import kotlinx.coroutines.Runnable
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import kotlinx.io.readLine
import kotlin.Array
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

//private class TestDeprecatedConsoleLogger : ConsoleLogger() {
//    private val buf = StringBuilder()
//    val content
//        get() = buf.toString()
//
//    override fun shimFactory(level: LogLevel, domain: Set<LogDomain>): ShimLogger {
//        return object : ShimLogger(level, domain) {
//            override fun doWriteLog(level: LogLevel, domain: LogDomain, message: String) {
//                buf.append(message)
//            }
//        }
//    }
//
//    fun clearContent() = buf.clear()
//}

// This test class can only run by itself, as it requires legacy logging
// Some tests fail on non-JVM platforms
@Ignore
@Suppress("DEPRECATION")
@OptIn(ExperimentalUuidApi::class)
class LegacyLogTest : BaseDbTest(useLegacyLogging = true) {
    private var scratchDirPath: String? = null

    private val tempDir: Path?
        get() {
            val dir = scratchDirPath
            return dir?.let { Path(it) }
        }

    private val logFiles: Array<Path>
        get() = SystemFileSystem.list(tempDir!!).toTypedArray()

    @BeforeTest
    fun setUpLogTest() {
        scratchDirPath = getScratchDirectoryPath(getUniqueName("log-dir"))
    }

//    @AfterTest
//    fun tearDownLogTest() = LogSinksImpl.initLogging()

//    @Test
//    fun testConsoleLoggerLevel() {
//        val consoleLogger = TestDeprecatedConsoleLogger()
//
//        consoleLogger.setDomains(LogDomain.DATABASE)
//        for (level in LogLevel.entries) {
//            if (level == LogLevel.NONE) {
//                continue
//            }
//
//            consoleLogger.level = level
//            consoleLogger.log(LogLevel.DEBUG, LogDomain.DATABASE, "D")
//            consoleLogger.log(LogLevel.VERBOSE, LogDomain.DATABASE, "V")
//            consoleLogger.log(LogLevel.INFO, LogDomain.DATABASE, "I")
//            consoleLogger.log(LogLevel.WARNING, LogDomain.DATABASE, "W")
//            consoleLogger.log(LogLevel.ERROR, LogDomain.DATABASE, "E")
//        }
//
//        assertEquals("DVIWEVIWEIWEWEE", consoleLogger.content)
//    }

//    @Test
//    fun testConsoleLoggerDomains() {
//        val consoleLogger = TestDeprecatedConsoleLogger()
//
//        consoleLogger.setDomains()
//        for (level in LogLevel.entries) {
//            if (level == LogLevel.NONE) {
//                continue
//            }
//
//            consoleLogger.level = level
//            consoleLogger.log(LogLevel.DEBUG, LogDomain.DATABASE, "D")
//            consoleLogger.log(LogLevel.VERBOSE, LogDomain.DATABASE, "V")
//            consoleLogger.log(LogLevel.INFO, LogDomain.DATABASE, "I")
//            consoleLogger.log(LogLevel.WARNING, LogDomain.DATABASE, "W")
//            consoleLogger.log(LogLevel.ERROR, LogDomain.DATABASE, "E")
//        }
//        assertEquals("", consoleLogger.content)
//        consoleLogger.clearContent()
//
//        consoleLogger.setDomains(LogDomain.NETWORK, LogDomain.QUERY)
//        for (level in LogLevel.entries) {
//            if (level == LogLevel.NONE) {
//                continue
//            }
//
//            consoleLogger.level = level
//            consoleLogger.log(LogLevel.DEBUG, LogDomain.DATABASE, "D")
//            consoleLogger.log(LogLevel.VERBOSE, LogDomain.DATABASE, "V")
//            consoleLogger.log(LogLevel.INFO, LogDomain.DATABASE, "I")
//            consoleLogger.log(LogLevel.WARNING, LogDomain.DATABASE, "W")
//            consoleLogger.log(LogLevel.ERROR, LogDomain.DATABASE, "E")
//        }
//        assertEquals("", consoleLogger.content)
//
//        consoleLogger.domains = LogDomain.ALL_DOMAINS
//        consoleLogger.level = LogLevel.DEBUG
//        consoleLogger.log(LogLevel.DEBUG, LogDomain.NETWORK, "N")
//        consoleLogger.log(LogLevel.DEBUG, LogDomain.QUERY, "Q")
//        consoleLogger.log(LogLevel.DEBUG, LogDomain.DATABASE, "D")
//        assertEquals("NQD", consoleLogger.content)
//    }

    @Test
    fun testFileLoggerDefaults() {
        val config = LogFileConfiguration("up/down")
        assertEquals(Defaults.LogFile.MAX_SIZE, config.maxSize)
        assertEquals(Defaults.LogFile.MAX_ROTATE_COUNT, config.maxRotateCount)
        assertEquals(Defaults.LogFile.USE_PLAINTEXT, config.usesPlaintext)
    }

//    @Test
//    fun testFileLoggingLevels() {
//        val mark = "$$$$ ${Uuid.random()}"
//        testWithConfiguration(
//            LogLevel.DEBUG,
//            LogFileConfiguration(scratchDirPath!!).setUsePlaintext(true).setMaxRotateCount(0)
//        ) {
//            for (level in LogLevel.entries) {
//                if (level == LogLevel.NONE) {
//                    continue
//                }
//                Database.log.file.level = level
//
//                Log.d(LogDomain.DATABASE, mark)
//                Log.i(LogDomain.DATABASE, mark)
//                Log.w(LogDomain.DATABASE, mark)
//                Log.e(LogDomain.DATABASE, mark)
//            }
//
//            for (log in logFiles) {
//                var lineCount = 0
//                SystemFileSystem.source(log).buffered().use {
//                    while (true) {
//                        val l = it.readLine() ?: break
//                        if (l.contains(mark)) {
//                            lineCount++
//                        }
//                    }
//                }
//
//                val logPath = SystemFileSystem.resolve(log).toString()
//                when {
//                    logPath.contains("error") -> assertEquals(5, lineCount)
//                    logPath.contains("warning") -> assertEquals(4, lineCount)
//                    logPath.contains("info") -> assertEquals(3, lineCount)
//                    logPath.contains("debug") -> assertEquals(1, lineCount)
//                    logPath.contains("verbose") -> assertEquals(0, lineCount)
//                }
//            }
//        }
//    }

//    @Test
//    fun testFileLoggingDefaultBinaryFormat() {
//        testWithConfiguration(LogLevel.INFO, LogFileConfiguration(scratchDirPath!!)) {
//            Log.i(LogDomain.DATABASE, "TEST INFO")
//
//            val files = logFiles
//            assertTrue(files.isNotEmpty())
//
//            val lastModifiedFile = getMostRecent(files)
//            assertNotNull(lastModifiedFile)
//
//            val bytes = ByteArray(4)
//            SystemFileSystem.source(lastModifiedFile).buffered().use { inStr -> assertEquals(4, inStr.readAtMostTo(bytes)) }
//            assertEquals(0xCF.toByte(), bytes[0])
//            assertEquals(0xB2.toByte(), bytes[1])
//            assertEquals(0xAB.toByte(), bytes[2])
//            assertEquals(0x1B.toByte(), bytes[3])
//        }
//    }

//    @Test
//    fun testFileLoggingUsePlainText() {
//        testWithConfiguration(LogLevel.INFO, LogFileConfiguration(scratchDirPath!!).setUsePlaintext(true)) {
//            val uuidString = Uuid.random().toString()
//            Log.i(LogDomain.DATABASE, uuidString)
//            val files = SystemFileSystem.list(tempDir!!).filter { file ->
//                file.name.lowercase().startsWith("cbl_info_")
//            }.toTypedArray()
//
//            assertNotNull(files)
//            assertEquals(1, files.size)
//
//            val file = getMostRecent(files)
//            assertNotNull(file)
//            assertTrue(getLogContents(file).contains(uuidString))
//        }
//    }

//    @Test
//    fun testFileLoggingLogFilename() {
//        testWithConfiguration(LogLevel.DEBUG, LogFileConfiguration(scratchDirPath!!)) {
//            Log.e(LogDomain.DATABASE, $$$$"$$$TEST MESSAGE")
//
//            val files = logFiles
//            assertTrue(files.size >= 4)
//
//            val rex = Regex("cbl_(debug|verbose|info|warning|error)_\\d+\\.cbllog")
//            for (file in files) {
//                assertTrue(file.name.matches(rex))
//            }
//        }
//    }

//    @Test
//    fun testFileLoggingMaxSize() {
//        val config = LogFileConfiguration(scratchDirPath!!)
//            .setUsePlaintext(true)
//            .setMaxSize(1024)
//            .setMaxRotateCount(10)
//        testWithConfiguration(LogLevel.DEBUG, config) {
//            // This should create two files for each of the 5 levels except verbose (debug, info, warning, error):
//            // 1k of logs plus .5k headers. There should be only one file at the verbose level (just the headers)
//            write1KBToLog()
//            assertEquals((4 * 2) + 1, logFiles.size)
//        }
//    }

//    @Test
//    fun testFileLoggingDisableLogging() {
//        val uuidString = Uuid.random().toString()
//
//        testWithConfiguration(LogLevel.NONE, LogFileConfiguration(scratchDirPath!!).setUsePlaintext(true)) {
//            writeAllLogs(uuidString)
//            for (log in logFiles) {
//                assertFalse(getLogContents(log).contains(uuidString))
//            }
//        }
//    }

//    @Test
//    fun testFileLoggingReEnableLogging() {
//        val uuidString = Uuid.random().toString()
//
//        testWithConfiguration(LogLevel.NONE, LogFileConfiguration(scratchDirPath!!).setUsePlaintext(true)) {
//            writeAllLogs(uuidString)
//
//            for (log in logFiles) {
//                assertFalse(getLogContents(log).contains(uuidString))
//            }
//
//            Database.log.file.level = LogLevel.INFO
//            writeAllLogs(uuidString)
//
//            for (log in logFiles) {
//                val fn = log.name.lowercase()
//                if (fn.startsWith("cbl_debug_") || fn.startsWith("cbl_verbose_")) {
//                    assertFalse(getLogContents(log).contains(uuidString))
//                } else {
//                    assertTrue(getLogContents(log).contains(uuidString))
//                }
//            }
//        }
//    }

//    @Test
//    fun testFileLoggingHeader() {
//        testWithConfiguration(LogLevel.VERBOSE, LogFileConfiguration(scratchDirPath!!).setUsePlaintext(true)) {
//            write1KBToLog()
//            for (log in logFiles) {
//                var logLine: String?
//                SystemFileSystem.source(log).buffered().use {
//                    logLine = it.readLine()
//                    logLine = it.readLine() // skip the LiteCore log line...
//                }
//                assertNotNull(logLine)
//                assertTrue(logLine.contains("CouchbaseLite")) //$PRODUCT"))
//                assertTrue(logLine.contains("Core/"))
//                //assertTrue(logLine.contains(CBLVersion.getSysInfo()))
//            }
//        }
//    }

//    @Test
//    fun testWriteLogWithError() {
//        val message = "test message"
//        val uuid = Uuid.random().toString()
//        val error = CouchbaseLiteException(uuid)
//
//        testWithConfiguration(LogLevel.DEBUG, LogFileConfiguration(scratchDirPath!!).setUsePlaintext(true)) {
//            Log.d(LogDomain.DATABASE, message, error)
//            Log.i(LogDomain.DATABASE, message, error)
//            Log.w(LogDomain.DATABASE, message, error)
//            Log.e(LogDomain.DATABASE, message, error)
//
//            for (log in logFiles) {
//                if (!log.name.contains("verbose")) {
//                    assertTrue(getLogContents(log).contains(uuid))
//                }
//            }
//        }
//    }

//    @Test
//    fun testWriteLogWithErrorAndArgs() {
//        val uuid1 = Uuid.random().toString()
//        val uuid2 = Uuid.random().toString()
//        val message = "test message %s"
//        val error = CouchbaseLiteException(uuid1)
//
//        testWithConfiguration(LogLevel.DEBUG, LogFileConfiguration(scratchDirPath!!).setUsePlaintext(true)) {
//            Log.d(LogDomain.DATABASE, message, error, uuid2)
//            Log.i(LogDomain.DATABASE, message, error, uuid2)
//            Log.w(LogDomain.DATABASE, message, error, uuid2)
//            Log.e(LogDomain.DATABASE, message, error, uuid2)
//
//            for (log in logFiles) {
//                if (!log.name.contains("verbose")) {
//                    val content = getLogContents(log)
//                    assertTrue(content.contains(uuid1))
//                    assertTrue(content.contains(uuid2))
//                }
//            }
//        }
//    }

    @Test
    fun testLogFileConfigurationConstructors() {
        val rotateCount = 4
        val maxSize = 2048L
        val usePlainText = true

//        assertFailsWith<IllegalArgumentException> {
//            KotlinHelpers.createLogFileConfigWithNullConfig()
//        }

//        assertFailsWith<IllegalArgumentException> {
//            KotlinHelpers.createLogFileConfigWithNullDir()
//        }

        val config = LogFileConfiguration(scratchDirPath!!)
            .setMaxRotateCount(rotateCount)
            .setMaxSize(maxSize)
            .setUsePlaintext(usePlainText)

        assertEquals(rotateCount, config.maxRotateCount)
        assertEquals(maxSize, config.maxSize)
        assertEquals(usePlainText, config.usesPlaintext)
        assertEquals(scratchDirPath, config.directory)

        val tempDir2 = getScratchDirectoryPath(getUniqueName("logtest2"))
        val newConfig = LogFileConfiguration(tempDir2, config)
        assertEquals(rotateCount, newConfig.maxRotateCount)
        assertEquals(maxSize, newConfig.maxSize)
        assertEquals(usePlainText, newConfig.usesPlaintext)
        assertEquals(tempDir2, newConfig.directory)
    }

    @Test
    fun testEditReadOnlyLogFileConfiguration() {
        testWithConfiguration(LogLevel.DEBUG, LogFileConfiguration(scratchDirPath!!)) {
            assertFailsWith<CouchbaseLiteError> { Database.log.file.config!!.maxSize = 1024 }
            assertFailsWith<CouchbaseLiteError> { Database.log.file.config!!.maxRotateCount = 3 }
            assertFailsWith<CouchbaseLiteError> { Database.log.file.config!!.setUsePlaintext(true) }
        }
    }

    @Test
    fun testSetNewLogFileConfiguration() {
        val config = LogFileConfiguration(scratchDirPath!!)
        val fileLogger = Database.log.file
        fileLogger.config = config
        assertEquals(config, fileLogger.config)
        fileLogger.config = null
        assertNull(fileLogger.config)
        fileLogger.config = config
        assertEquals(config, fileLogger.config)
        fileLogger.config = LogFileConfiguration("$scratchDirPath/legacyLogs")
        assertEquals(LogFileConfiguration("$scratchDirPath/legacyLogs"), fileLogger.config)
    }

    @Test
    fun testMixLegacyAndNewAPIs1() {
        assertFailsWith<CouchbaseLiteError> {
            val fileLogger = Database.log.file
            fileLogger.config = LogFileConfiguration(scratchDirPath!!)
            fileLogger.level = LogLevel.VERBOSE
            LogSinks.file = FileLogSink(directory = scratchDirPath!!, level = LogLevel.ERROR)
        }
    }

    @Test
    fun testMixLegacyAndNewAPIs2() {
        assertFailsWith<CouchbaseLiteError> {
            LogSinks.file = FileLogSink(directory = scratchDirPath!!, level = LogLevel.ERROR)
            val fileLogger = Database.log.file
            fileLogger.config = LogFileConfiguration(scratchDirPath!!)
            fileLogger.level = LogLevel.VERBOSE
        }
    }

    @Test
    fun testMixLegacyAndNewAPIs3() {
        assertFailsWith<CouchbaseLiteError> {
            Database.log.console.level = LogLevel.VERBOSE
            LogSinks.console = ConsoleLogSink(LogLevel.ERROR, LogDomain.ALL)
        }
    }

    @Test
    fun testMixLegacyAndNewAPIs4() {
        assertFailsWith<CouchbaseLiteError> {
            LogSinks.console = ConsoleLogSink(LogLevel.VERBOSE, LogDomain.ALL)
            Database.log.console.level = LogLevel.ERROR
        }
    }

    @Test
    fun testMixLegacyAndNewAPIs5() {
        assertFailsWith<CouchbaseLiteError> {
            Database.log.custom = object : Logger {
                override val level = LogLevel.VERBOSE
                override fun log(level: LogLevel, domain: LogDomain, message: String) {}
            }
            LogSinks.custom = CustomLogSink(LogLevel.ERROR, LogDomain.ALL) { _, _, _ -> }
        }
    }

    @Test
    fun testMixLegacyAndNewAPIs6() {
        assertFailsWith<CouchbaseLiteError> {
            LogSinks.custom = CustomLogSink(LogLevel.ERROR, LogDomain.ALL) { _, _, _ -> }
            Database.log.custom = object : Logger {
                override val level = LogLevel.VERBOSE
                override fun log(level: LogLevel, domain: LogDomain, message: String) {}
            }
        }
    }

    @Test
    fun testMixLegacyAndNewAPIs7() {
        assertFailsWith<CouchbaseLiteError> {
            Database.log.custom = object : Logger {
                override val level = LogLevel.VERBOSE
                override fun log(level: LogLevel, domain: LogDomain, message: String) {}
            }
            LogSinks.file = FileLogSink(directory = scratchDirPath!!, level = LogLevel.ERROR)
        }
    }

    @Test
    fun testMixLegacyAndNewAPIs8() {
        assertFailsWith<CouchbaseLiteError> {
            LogSinks.file = FileLogSink(directory = scratchDirPath!!, level = LogLevel.ERROR)
            Database.log.custom = object : Logger {
                override val level = LogLevel.VERBOSE
                override fun log(level: LogLevel, domain: LogDomain, message: String) {}
            }
        }
    }

    private fun testWithConfiguration(level: LogLevel, config: LogFileConfiguration, task: Runnable) {
        val logger = Database.log
        val consoleLogger = logger.console
        consoleLogger.level = level

        val fileLogger = logger.file
        fileLogger.config = config
        fileLogger.level = level

        task.run()
    }

//    private fun write1KBToLog() {
//        val message = "11223344556677889900" // ~65 bytes including the line headers
//        // 16 * 65 ~= 1024.
//        for (i in 0..15) {
//            writeAllLogs(message)
//        }
//    }

//    private fun writeAllLogs(message: String) {
//        Log.d(LogDomain.DATABASE, message)
//        Log.i(LogDomain.DATABASE, message)
//        Log.w(LogDomain.DATABASE, message)
//        Log.e(LogDomain.DATABASE, message)
//    }

    private fun getLogContents(log: Path): String {
        val b = SystemFileSystem.source(log).buffered().use { it.readByteArray() }
        return b.decodeToString()
    }

    private fun getMostRecent(files: Array<Path>?) = files?.maxByOrNull { FileUtils.lastModified(it.toString()) }
}
