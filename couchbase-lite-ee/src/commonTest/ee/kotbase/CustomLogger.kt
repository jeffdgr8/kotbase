package kotbase

class CustomLogger : Logger {

    var lines = mutableListOf<String>()

    override var level = LogLevel.NONE

    fun reset() {
        lines.clear()
    }

    override fun log(level: LogLevel, domain: LogDomain, message: String) {
        lines.add(message)
    }
}
