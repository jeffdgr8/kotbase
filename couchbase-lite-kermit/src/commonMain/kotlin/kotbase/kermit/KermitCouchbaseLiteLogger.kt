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

package kotbase.kermit

import co.touchlab.kermit.Severity
import kotbase.LogDomain
import kotbase.LogLevel
import kotbase.Logger
import co.touchlab.kermit.Logger as KermitLogger

/**
 * Couchbase Lite custom Logger that logs to Kermit
 *
 * Disable default console logs and set as custom logger:
 *
 * ```
 * Database.log.console.level = LogLevel.NONE
 * Database.log.custom = KermitCouchbaseLiteLogger(kermit)
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
public class KermitCouchbaseLiteLogger(
    kermit: KermitLogger,
    override val level: LogLevel = LogLevel.WARNING
) : Logger {

    private val log = kermit.withTag(TAG)

    override fun log(level: LogLevel, domain: LogDomain, message: String) {
        if (level < this.level || !domains.contains(domain)) return
        log.log(level.severity, domain.tag, null, message)
    }

    /**
     * The domains that will be considered for writing to the console log.
     */
    public var domains: Set<LogDomain> = LogDomain.ALL_DOMAINS

    /**
     * Sets the domains that will be considered for writing to the console log.
     *
     * @param domains The domains to make active (vararg)
     */
    public fun setDomains(vararg domains: LogDomain) { this.domains = domains.toSet() }

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
