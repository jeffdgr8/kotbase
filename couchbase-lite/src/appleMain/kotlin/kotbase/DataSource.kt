package kotbase

import cocoapods.CouchbaseLite.CBLDatabase
import cocoapods.CouchbaseLite.CBLQueryDataSource
import kotbase.base.DelegatedClass

public actual open class DataSource
private constructor(actual: CBLQueryDataSource) :
    DelegatedClass<CBLQueryDataSource>(actual) {

    public actual class As
    internal constructor(private val database: CBLDatabase) :
        DataSource(CBLQueryDataSource.database(database)) {

        public actual fun `as`(alias: String): DataSource =
            DataSource(CBLQueryDataSource.database(database, alias))
    }

    public actual companion object {

        public actual fun database(database: Database): As =
            As(database.actual)
    }
}
