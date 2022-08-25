@file:Suppress("NO_ACTUAL_FOR_EXPECT") // https://youtrack.jetbrains.com/issue/KT-42466

package com.couchbase.lite.kmp

/**
 * A query data source, used for specifying the source of data for a query.
 */
public expect open class DataSource {

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
