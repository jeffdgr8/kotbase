package kotbase

import kotbase.base.DelegatedClass

public actual open class DataSource
private constructor(actual: com.couchbase.lite.DataSource) :
    DelegatedClass<com.couchbase.lite.DataSource>(actual) {

    public actual class As
    internal constructor(override val actual: com.couchbase.lite.DataSource.As) :
        DataSource(actual) {

        public actual fun `as`(alias: String): DataSource {
            actual.`as`(alias)
            return this
        }
    }

    public actual companion object {

        public actual fun database(database: Database): As =
            As(com.couchbase.lite.DataSource.database(database.actual))
    }
}
