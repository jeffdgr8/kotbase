/*
 * Copyright 2022-2023 Jeff Lockhart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kotbase

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
         * @return `DataSource` object.
         */
        @Deprecated(
            "Use DataSource.collection(Collection)",
            ReplaceWith("collection(database.getDefaultCollection()!!)")
        )
        public fun database(database: Database): As

        /**
         * Create a collection as a data source.
         *
         * @param collection the collection used as a source of data for query.
         * @return `DataSource` object.
         */
        public fun collection(collection: Collection): As
    }
}
