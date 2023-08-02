package kotbase

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import libcblite.CBLLog_ConsoleLevel
import libcblite.CBLLog_SetConsoleLevel
import platform.posix.pthread_self

public actual class ConsoleLogger internal constructor() : Logger {

    public actual var domains: Set<LogDomain> = LogDomain.ALL_DOMAINS

    public actual fun setDomains(vararg domains: LogDomain) {
        this.domains = domains.toSet()
    }

    actual override var level: LogLevel
        get() = LogLevel.from(CBLLog_ConsoleLevel())
        set(value) {
            CBLLog_SetConsoleLevel(value.actual)
        }

    override fun log(level: LogLevel, domain: LogDomain, message: String) {
        if (level < this.level || !domains.contains(domain)) return
        println(formatLog(level, domain.name, message))
    }

    private fun formatLog(level: LogLevel, domain: String, message: String): String {
        val thread = pthread_self().toString().padStart(THREAD_FIELD_LEN, ' ')
        val time = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .toString()
        return "$time$thread $level$LOG_TAG$domain: $message"
    }

    private companion object {
        private const val LOG_TAG = "/CouchbaseLite/"
        private const val THREAD_FIELD_LEN = 7
    }
}
