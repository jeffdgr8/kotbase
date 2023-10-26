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

import cocoapods.CouchbaseLite.CBLQueryOrdering
import cocoapods.CouchbaseLite.CBLQuerySortOrder
import kotbase.internal.DelegatedClass

public actual sealed class Ordering
private constructor(actual: CBLQueryOrdering) : DelegatedClass<CBLQueryOrdering>(actual) {

    public actual class SortOrder
    internal constructor(override val actual: CBLQuerySortOrder) : Ordering(actual) {

        public actual fun ascending(): Ordering {
            actual.ascending()
            return this
        }

        public actual fun descending(): Ordering {
            actual.descending()
            return this
        }
    }

    public actual companion object {

        public actual fun property(property: String): SortOrder =
            SortOrder(CBLQueryOrdering.property(property))

        public actual fun expression(expression: Expression): SortOrder =
            SortOrder(CBLQueryOrdering.expression(expression.actual))
    }
}
