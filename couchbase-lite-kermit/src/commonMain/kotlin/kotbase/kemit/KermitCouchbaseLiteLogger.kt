@file:Suppress("MemberVisibilityCanBePrivate")

package kotbase.kemit

import co.touchlab.kermit.Severity
import kotbase.LogDomain
import kotbase.LogLevel
import kotbase.Logger
import co.touchlab.kermit.Logger as Kermit

/**
 * Couchbase Lite custom Logger that logs to Kermit
 *
 * Disable default console logs and set as custom logger:
 *
 * ```
 * Database.log.console.level = LogLevel.NONE
 * Database.log.custom = KermitCouchbaseLiteLogger(kermit)
 * ```
 */
public class KermitCouchbaseLiteLogger(
    kermit: Kermit,
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

    private val LogLevel.severity: Severity
        get() = when (this) {
            // LogLevel.DEBUG is lowest CBL log level and only available in debug builds
            LogLevel.DEBUG -> Severity.Verbose
            LogLevel.VERBOSE -> Severity.Debug
            LogLevel.INFO -> Severity.Info
            LogLevel.WARNING -> Severity.Warn
            else -> Severity.Error
        }

    private val LogDomain.tag: String
        get() = when(this) {
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
