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
import kotbase.internal.actuals
import com.couchbase.lite.Joins as CBLJoins

public actual class Joins
internal constructor(actual: CBLJoins) :
    DelegatedClass<CBLJoins>(actual),
    Query by DelegatedQuery(actual),
    WhereRouter,
    OrderByRouter,
    LimitRouter {

    public actual override fun where(expression: Expression): Where =
        Where(actual.where(expression.actual))

    public actual override fun orderBy(vararg orderings: Ordering): OrderBy =
        OrderBy(actual.orderBy(*orderings.actuals()))

    public actual override fun limit(limit: Expression): Limit =
        Limit(actual.limit(limit.actual))

    public actual override fun limit(limit: Expression, offset: Expression?): Limit =
        Limit(actual.limit(limit.actual, offset?.actual))
}
