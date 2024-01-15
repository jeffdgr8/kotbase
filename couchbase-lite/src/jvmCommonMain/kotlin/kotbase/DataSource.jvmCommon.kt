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

import kotbase.internal.DelegatedClass
import com.couchbase.lite.DataSource as CBLDataSource

public actual open class DataSource
private constructor(actual: CBLDataSource) : DelegatedClass<CBLDataSource>(actual) {

    public actual class As
    internal constructor(override val actual: CBLDataSource.As) : DataSource(actual) {

        public actual fun `as`(alias: String): DataSource {
            actual.`as`(alias)
            return this
        }
    }

    public actual companion object {

        @Suppress("DEPRECATION")
        @Deprecated(
            "Use DataSource.collection(Collection)",
            ReplaceWith("collection(database.defaultCollection)")
        )
        public actual fun database(database: Database): As =
            As(CBLDataSource.database(database.actual))

        public actual fun collection(collection: Collection): As =
            As(CBLDataSource.collection(collection.actual))
    }
}
