/*
 * Copyright 2025 Jeff Lockhart
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
package kotbase.kermit

import co.touchlab.kermit.CommonWriter
import co.touchlab.kermit.DefaultFormatter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Message
import co.touchlab.kermit.Severity
import co.touchlab.kermit.Tag
import co.touchlab.kermit.loggerConfigInit
import co.touchlab.stately.collections.ConcurrentMutableList
import kotbase.BaseTest
import kotbase.Database
import kotbase.LogDomain
import kotbase.LogLevel
import kotbase.MutableDocument
import kotbase.logging.ConsoleLogSink
import kotbase.logging.CustomLogSink
import kotbase.logging.LogSinks
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KermitCouchbaseLiteLogSinkTest : BaseTest() {

    class TestLogWriter(
        private val minSeverity: Severity,
        private val tags: Set<String>
    ) : CommonWriter() {

        private data class Log(
            val severity: Severity,
            val message: String,
            val tag: String
        )

        private val logs = ConcurrentMutableList<Log>()

        override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
            super.log(severity, message, tag, throwable)
            logs += Log(severity, message, tag)
        }

        fun clearCache() {
            logs.clear()
        }

        fun checkLog(severity: Severity, tag: String, vararg messageContent: String) {
            val logged = logs.block {
                it.any { log ->
                    log.severity == severity &&
                    log.tag == tag &&
                    messageContent.all { log.message.contains(it) }
                }
            }

            val concatMessage = messageContent.joinToString("...")
            val formatted = DefaultFormatter.formatMessage(severity, Tag(tag), Message(concatMessage))

            if (minSeverity <= severity && tags.contains(tag)) {
                assertTrue(logged, "\"$formatted\" not logged")
            } else {
                assertFalse(logged, "\"$formatted\" logged, when it shouldn't")
            }
        }
    }

    private var database: Database? = null

    @BeforeTest
    fun setup() {
        LogSinks.console = ConsoleLogSink(LogLevel.NONE)
    }

    @AfterTest
    fun cleanup() {
        database?.delete()
    }

    @Test
    fun testLogsWritten() {
        testKermitLogger(Severity.Verbose, LogLevel.VERBOSE)
    }

    @Test
    fun testLogsBelowSeverityNotWritten() {
        testKermitLogger(Severity.Info, LogLevel.VERBOSE)
    }

    @Test
    fun testLogsBelowLevelNotWritten() {
        testKermitLogger(Severity.Verbose, LogLevel.INFO)
    }

    @Test
    fun testOmittedDomainsNotWritten() {
        testKermitLogger(Severity.Verbose, LogLevel.VERBOSE, LogDomain.QUERY)
    }

    private fun testKermitLogger(
        minSeverity: Severity,
        minLevel: LogLevel,
        vararg domains: LogDomain
    ) {
        val logWriter = TestLogWriter(maxOf(minSeverity, minLevel.severity), domains.toTags())
        val kermit = Logger(loggerConfigInit(logWriter, minSeverity = minSeverity))
        LogSinks.custom = CustomLogSink(minLevel, *domains, logSink = KermitCouchbaseLiteLogSink(kermit))

        val database = createDb("log-test-db")
        this.database = database
        allowLogsToWrite()
        logWriter.checkLog(
            Severity.Info,
            CBL_DATABASE,
            "{DB#", "}==>", "litecore", "SQLiteDataFile", "log-test-db", ".cblite2", "db.sqlite3"
        )
        logWriter.checkLog(Severity.Info, CBL_DATABASE, "Obj=/DB#", "File=Shared#", "Opening database")
        logWriter.checkLog(Severity.Info, CBL_DATABASE, "Instantiated")
        logWriter.clearCache()

        database.defaultCollection.save(MutableDocument("doc-1", """{"foo":"bar","baz":42}"""))
        allowLogsToWrite()
        logWriter.checkLog(Severity.Verbose, CBL_DATABASE, "Obj=/DB#", "begin transaction")
        logWriter.checkLog(Severity.Verbose, CBL_DATABASE, "Obj=/DB#", "Saved 'doc-1' rev #1-", "as seq 1")
        logWriter.checkLog(Severity.Verbose, CBL_DATABASE, "Obj=/DB#", "commit transaction")
        logWriter.clearCache()

        database.createQuery("""SELECT * FROM _ WHERE foo = "bar"""").execute().use { rs ->
            rs.allResults()
        }
        allowLogsToWrite()
        logWriter.checkLog(
            Severity.Info,
            CBL_QUERY,
            "Obj=/Query#", """Compiling N1QL query: SELECT * FROM _ WHERE foo = "bar""""
        )
        logWriter.checkLog(
            Severity.Info,
            CBL_QUERY,
            "Obj=/Query#",
            "Compiled as SELECT fl_result(fl_root(_.body)) FROM kv_default AS _ WHERE (fl_value(_.body, 'foo') = 'bar') AND (_.flags & 1 = 0)"
        )
        logWriter.clearCache()

        database.delete()
        this.database = null
        allowLogsToWrite()
        logWriter.checkLog(Severity.Verbose, CBL_DATABASE, "Obj=/DB#", "Closed SQLite database")
        logWriter.checkLog(Severity.Info, CBL_DATABASE, "Deleting database file", "log-test-db", ".cblite2", "db.sqlite3 (with -wal and -shm)")
    }

    private fun allowLogsToWrite() = runBlocking {
        delay(20)
    }

    private fun Array<out LogDomain>.toTags() = ifEmpty { LogDomain.ALL.toTypedArray() }
        .map { "CBL-${it.name}" }.toSet()

    companion object {
        const val CBL_DATABASE = "CBL-DATABASE"
        const val CBL_QUERY = "CBL-QUERY"
    }
}
