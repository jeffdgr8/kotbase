package kotbase

internal expect class DataSourcePlatformState

/**
 * A query data source, used for specifying the source of data for a query.
 */
public expect sealed class DataSource {

    internal val platformState: DataSourcePlatformState

    /**
     * Database as a data source for query.
     */
    public class As : DataSource {

        /**
         * Set an alias to the database data source.
         *
         * @param alias the alias to set.
         * @return the data source object with the given alias set.
         */
        public fun `as`(alias: String): DataSource
    }

    public companion object {

        /**
         * Create a database as a data source.
         *
         * @param database the database used as a source of data for query.
         * @return `DataSource.Database` object.
         */
        public fun database(database: Database): As
    }
}
