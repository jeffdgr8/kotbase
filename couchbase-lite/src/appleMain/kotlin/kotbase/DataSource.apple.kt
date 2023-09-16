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

import cocoapods.CouchbaseLite.CBLDatabase
import cocoapods.CouchbaseLite.CBLQueryDataSource

internal actual class DataSourcePlatformState(
    internal val actual: CBLQueryDataSource
)

public actual open class DataSource
private constructor(actual: CBLQueryDataSource) {

    internal actual val platformState = DataSourcePlatformState(actual)

    public actual class As
    internal constructor(private val database: CBLDatabase) : DataSource(CBLQueryDataSource.database(database)) {

        public actual fun `as`(alias: String): DataSource =
            DataSource(CBLQueryDataSource.database(database, alias))
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
