package com.couchbase.lite.kmp

public actual open class DataSource(
    internal val source: Database,
    internal val alias: String? = null
) {

    public actual class As
    internal constructor(database: Database) :
        DataSource(database) {

        public actual fun `as`(alias: String): DataSource =
            DataSource(source, alias)
    }

    private fun getColumnName(): String =
        alias ?: source.name

    internal fun asJSON(): Dictionary {
        return MutableDictionary().apply {
            setString("AS", getColumnName())
        }
    }

    public actual companion object {

        public actual fun database(database: Database): As =
            As(database)
    }
}
