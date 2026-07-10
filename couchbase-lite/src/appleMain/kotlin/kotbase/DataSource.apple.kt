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

import cocoapods.CouchbaseLite.CBLCollection
import cocoapods.CouchbaseLite.CBLQueryDataSource
import kotbase.internal.DelegatedClass

public actual open class DataSource
private constructor(override var actual: CBLQueryDataSource) : DelegatedClass<CBLQueryDataSource>(actual) {

    public actual class As
    internal constructor(
        private val collection: CBLCollection
    ) : DataSource(CBLQueryDataSource.collection(collection)) {

        public actual infix fun `as`(alias: String): DataSource {
            actual = CBLQueryDataSource.collection(collection, alias)
            return this
        }
    }

    public actual companion object {

        public actual fun collection(collection: Collection): As =
            As(collection.actual)
    }
}
