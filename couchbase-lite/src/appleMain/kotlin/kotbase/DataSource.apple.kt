package kotbase

import cocoapods.CouchbaseLite.CBLDatabase
import cocoapods.CouchbaseLite.CBLQueryDataSource

internal actual class DataSourcePlatformState(
    internal val actual: CBLQueryDataSource
)

public actual sealed class DataSource
private constructor(actual: CBLQueryDataSource) {

    internal actual val platformState = DataSourcePlatformState(actual)

    private class DataSourceImpl(actual: CBLQueryDataSource) : DataSource(actual)

    public actual class As
    internal constructor(private val database: CBLDatabase) : DataSource(CBLQueryDataSource.database(database)) {

        public actual fun `as`(alias: String): DataSource =
            DataSourceImpl(CBLQueryDataSource.database(database, alias))
    }

    override fun equals(other: Any?): Boolean =
        actual.isEqual((other as? DataSource)?.actual)

    override fun hashCode(): Int =
        actual.hash.toInt()

    override fun toString(): String =
        actual.description ?: super.toString()

    public actual companion object {

        public actual fun database(database: Database): As =
            As(database.actual)
    }
}

internal val DataSource.actual: CBLQueryDataSource
    get() = platformState.actual
