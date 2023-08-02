package kotbase

import kotbase.base.DelegatedClass
import com.couchbase.lite.DataSource as CBLDataSource

public actual open class DataSource
private constructor(actual: CBLDataSource) : DelegatedClass<CBLDataSource>(actual) {

    public actual class As
    internal constructor(override val actual: CBLDataSource.As) :
        DataSource(actual) {

        public actual fun `as`(alias: String): DataSource {
            actual.`as`(alias)
            return this
        }
    }

    public actual companion object {

        public actual fun database(database: Database): As =
            As(CBLDataSource.database(database.actual))
    }
}
