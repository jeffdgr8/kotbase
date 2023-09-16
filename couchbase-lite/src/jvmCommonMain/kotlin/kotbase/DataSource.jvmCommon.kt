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

import com.couchbase.lite.DataSource as CBLDataSource

internal actual class DataSourcePlatformState(
    internal val actual: CBLDataSource
)

public actual open class DataSource
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
