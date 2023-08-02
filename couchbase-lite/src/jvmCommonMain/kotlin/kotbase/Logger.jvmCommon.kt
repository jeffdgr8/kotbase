package kotbase

import com.couchbase.lite.LogDomain as CBLLogDomain
import com.couchbase.lite.Logger as CBLLogger

internal fun Logger.convert() = object : CBLLogger {

    override fun getLevel(): LogLevel = this@convert.level

    override fun log(level: LogLevel, domain: CBLLogDomain, message: String) {
        this@convert.log(level, LogDomain.from(domain), message)
    }
}
