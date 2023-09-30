/*
 * Copyright 2023 Jeff Lockhart
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

import co.touchlab.kermit.*
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Message
import kotbase.*
import kotlin.test.*

@OptIn(ExperimentalStdlibApi::class)
class KermitCouchbaseLiteLoggerTest : PlatformTest() {

    class TestLogWriter(
        private val minSeverity: Severity,
        private val tags: Set<String>
    ) : CommonWriter() {

        private data class Log(
            val severity: Severity,
            val message: String,
            val tag: String
        )

        private val logs = mutableListOf<Log>()

        override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
            super.log(severity, message, tag, throwable)
            logs += Log(severity, message, tag)
        }

        fun clearCache() {
            logs.clear()
        }

        fun checkLog(severity: Severity, tag: String, vararg messageContent: String) {
            val logged = logs.any { log ->
                log.severity == severity &&
                log.tag == tag &&
                messageContent.all { log.message.contains(it) }
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
        Database.log.console.level = LogLevel.NONE
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
        testKermitLogger(Severity.Verbose, LogLevel.VERBOSE, setOf(LogDomain.QUERY))
    }

    private fun testKermitLogger(
        minSeverity: Severity,
        minLevel: LogLevel,
        domains: Set<LogDomain> = LogDomain.ALL_DOMAINS
    ) {
        val logWriter = TestLogWriter(maxOf(minSeverity, minLevel.severity), domains.toTags())
        val kermit = Logger(loggerConfigInit(logWriter, minSeverity = minSeverity))
        Database.log.custom = KermitCouchbaseLiteLogger(kermit, minLevel).apply {
            this.domains = domains
        }

        val database = Database("log-test-db")
        this.database = database
        logWriter.checkLog(
            Severity.Info,
            CBL_DATABASE,
            "{DB#", "}==>", "litecore", "SQLiteDataFile", "log-test-db.cblite2", "db.sqlite3"
        )
        logWriter.checkLog(Severity.Info, CBL_DATABASE, "{DB#", "Opening database")
        logWriter.checkLog(Severity.Info, CBL_DATABASE, "Instantiated")
        logWriter.clearCache()

        database.save(MutableDocument("doc-1", """{"foo":"bar","baz":42}"""))
        logWriter.checkLog(Severity.Debug, CBL_DATABASE, "{DB#", "begin transaction")
        logWriter.checkLog(Severity.Debug, CBL_DATABASE, "{DB#", "Saved 'doc-1' rev #1-", "as seq 1")
        logWriter.checkLog(Severity.Debug, CBL_DATABASE, "{DB#", "commit transaction")
        logWriter.clearCache()

        database.createQuery("""SELECT * FROM _ WHERE foo = "bar"""").execute().use { rs ->
            rs.allResults()
        }
        logWriter.checkLog(
            Severity.Info,
            CBL_QUERY,
            "{Query#", """Compiling N1QL query: SELECT * FROM _ WHERE foo = "bar""""
        )
        logWriter.checkLog(
            Severity.Info,
            CBL_QUERY,
            "{Query#",
            "Compiled as SELECT fl_result(fl_root(_.body)) FROM kv_default AS _ WHERE (fl_value(_.body, 'foo') = 'bar') AND (_.flags & 1 = 0)"
        )
        logWriter.clearCache()

        database.delete()
        this.database = null
        logWriter.checkLog(Severity.Debug, CBL_DATABASE, "{DB#", "Closed SQLite database")
        logWriter.checkLog(Severity.Info, CBL_DATABASE, "{DB#", "Closing database")
    }

    private fun Set<LogDomain>.toTags() = map { "CBL-${it.name}" }.toSet()

    companion object {
        const val CBL_DATABASE = "CBL-DATABASE"
        const val CBL_QUERY = "CBL-QUERY"
    }
}
