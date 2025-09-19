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

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import kotbase.LogDomain
import kotbase.LogLevel
import kotbase.logging.LogSink

/**
 * Couchbase Lite custom LogSink that logs to Kermit
 *
 * Disable default console logs and set as custom logger:
 *
 * ```
 * LogSinks.console = ConsoleLogSink(LogLevel.NONE)
 * LogSinks.custom = CustomLogSink(LogLevel.WARNING, logSink = KermitCouchbaseLiteLogSink(kermit))
 * ```
 *
 * Note Couchbase Lite `LogLevel.DEBUG` is lower than
 * `LogLevel.VERBOSE`, while Kermit `Severity.Verbose` is lower than
 * `Severity.Debug`. `LogLevel.Verbose` still maps to `Severity.Verbose`
 * and `LogLevel.DEBUG` to `Severity.Debug` for consistency reading log
 * prefixes. But these logs will filter differently based on the `level`
 * filter in this class and Kermit's own `minSeverity` filter. Since
 * `LogLevel.DEBUG` logs are only logged in debug builds of Couchbase
 * Lite, this generally isn't an issue.
 */
public class KermitCouchbaseLiteLogSink(kermit: Logger) : LogSink {

    private val log = kermit.withTag(TAG)

    override fun writeLog(level: LogLevel, domain: LogDomain, message: String) {
        log.log(level.severity, domain.tag, null, message)
    }

    private val LogDomain.tag: String
        @Suppress("REDUNDANT_ELSE_IN_WHEN")
        get() = when (this) {
            LogDomain.DATABASE -> TAG_DATABASE
            LogDomain.QUERY -> TAG_QUERY
            LogDomain.REPLICATOR -> TAG_REPLICATOR
            LogDomain.NETWORK -> TAG_NETWORK
            LogDomain.LISTENER -> TAG_LISTENER
            else -> TAG
        }

    private companion object {
        const val TAG = "CBL"
        const val TAG_DATABASE = "$TAG-DATABASE"
        const val TAG_QUERY = "$TAG-QUERY"
        const val TAG_REPLICATOR = "$TAG-REPLICATOR"
        const val TAG_NETWORK = "$TAG-NETWORK"
        const val TAG_LISTENER = "$TAG-LISTENER"
    }
}

internal val LogLevel.severity: Severity
    get() = when (this) {
        // LogLevel.DEBUG is lowest CBL log level and only available in debug builds
        LogLevel.DEBUG -> Severity.Debug
        LogLevel.VERBOSE -> Severity.Verbose
        LogLevel.INFO -> Severity.Info
        LogLevel.WARNING -> Severity.Warn
        LogLevel.ERROR -> Severity.Error
        else -> Severity.Assert
    }
