package kotbase

public actual open class DataSource
private constructor(
    internal val source: Database,
    private val alias: String? = null
) {

    public actual class As
    internal constructor(database: Database) :
        DataSource(database) {

        public actual fun `as`(alias: String): DataSource =
            DataSource(source, alias)
    }

    private fun getColumnName(): String =
        alias ?: source.name

    internal fun asJSON(): Map<String, Any?> =
        mapOf("AS" to getColumnName())

    public actual companion object {

        public actual fun database(database: Database): As =
            As(database)
    }
}
