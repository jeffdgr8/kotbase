package kotbase

import com.couchbase.lite.ConsoleLogger
import kotbase.base.DelegatedClass
import java.util.*

public actual class ConsoleLogger
internal constructor(override val actual: com.couchbase.lite.ConsoleLogger) :
    DelegatedClass<ConsoleLogger>(actual), Logger {

    public actual var domains: Set<LogDomain>
        get() = actual.domains.map { LogDomain.from(it) }.toSet()
        set(value) {
            actual.domains = EnumSet.copyOf(value.map { it.actual })
        }

    public actual fun setDomains(vararg domains: LogDomain) {
        this.domains = domains.toSet()
    }

    actual override var level: LogLevel
        get() = actual.level
        set(value) {
            actual.level = value
        }

    override fun log(level: LogLevel, domain: LogDomain, message: String) {
        actual.log(level, domain.actual, message)
    }
}
