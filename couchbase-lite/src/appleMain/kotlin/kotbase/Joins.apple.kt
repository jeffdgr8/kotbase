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

import cocoapods.CouchbaseLite.CBLQuery
import cocoapods.CouchbaseLite.CBLQueryLimit
import kotbase.base.AbstractDelegatedClass

public actual class Joins
internal constructor(private val state: QueryState) : AbstractDelegatedClass<CBLQuery>(), Query by state {

    public actual fun where(expression: Expression): Where {
        return Where(state.copy(where = expression.actual))
    }

    public actual fun orderBy(vararg orderings: Ordering): OrderBy {
        return OrderBy(state.copy(orderBy = orderings.actuals()))
    }

    public actual fun limit(limit: Expression): Limit {
        return Limit(state.copy(limit = CBLQueryLimit.limit(limit.actual)))
    }

    public actual fun limit(limit: Expression, offset: Expression?): Limit {
        return Limit(state.copy(limit = CBLQueryLimit.limit(limit.actual, offset?.actual)))
    }

    override val actual: CBLQuery
        get() = state.actual
}
