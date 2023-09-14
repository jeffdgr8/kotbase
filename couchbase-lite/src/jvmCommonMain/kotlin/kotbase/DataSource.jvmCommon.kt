package kotbase

import com.couchbase.lite.DataSource as CBLDataSource

internal actual class DataSourcePlatformState(
    internal val actual: CBLDataSource
)

public actual sealed class DataSource
private constructor(actual: CBLDataSource) {

    internal actual val platformState = DataSourcePlatformState(actual)

    public actual class As
    internal constructor(actual: CBLDataSource.As) : DataSource(actual) {

        public actual fun `as`(alias: String): DataSource {
            actual.`as`(alias)
            return this
        }
    }

    override fun equals(other: Any?): Boolean =
        actual == (other as? DataSource)?.actual

    override fun hashCode(): Int =
        actual.hashCode()

    override fun toString(): String =
        actual.toString()

    public actual companion object {

        public actual fun database(database: Database): As =
            As(CBLDataSource.database(database.actual))
    }
}

internal val DataSource.actual: CBLDataSource
    get() = platformState.actual

internal val DataSource.As.actual: CBLDataSource.As
    get() = platformState.actual as CBLDataSource.As
