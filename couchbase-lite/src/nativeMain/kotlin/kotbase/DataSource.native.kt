package kotbase

internal actual class DataSourcePlatformState(
    internal val source: Database,
    private val alias: String?
) {

    private fun getColumnName(): String =
        alias ?: source.name

    internal fun asJSON(): Map<String, Any?> =
        mapOf("AS" to getColumnName())
}

public actual sealed class DataSource
private constructor(
    source: Database,
    alias: String? = null
) {

    internal actual val platformState = DataSourcePlatformState(source, alias)

    private class DataSourceImpl(source: Database, alias: String?) : DataSource(source, alias)

    public actual class As
    internal constructor(database: Database) : DataSource(database) {

        public actual fun `as`(alias: String): DataSource =
            DataSourceImpl(source, alias)
    }

    public actual companion object {

        public actual fun database(database: Database): As =
            As(database)
    }
}

internal val DataSource.source: Database
    get() = platformState.source

internal fun DataSource.asJSON(): Map<String, Any?> =
    platformState.asJSON()
