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
import com.couchbase.lite.From as CBLFrom

public actual class From
internal constructor(actual: CBLFrom) :
    DelegatedClass<CBLFrom>(actual),
    Query by DelegatedQuery(actual),
    JoinRouter,
    WhereRouter,
    GroupByRouter,
    OrderByRouter,
    LimitRouter {

    public actual override fun join(vararg joins: Join): Joins =
        Joins(actual.join(*joins.actuals()))

    public actual override fun where(expression: Expression): Where =
        Where(actual.where(expression.actual))

    public actual override fun groupBy(vararg expressions: Expression): GroupBy =
        GroupBy(actual.groupBy(*expressions.actuals()))

    public actual override fun orderBy(vararg orderings: Ordering): OrderBy =
        OrderBy(actual.orderBy(*orderings.actuals()))

    public actual override fun limit(limit: Expression): Limit =
        Limit(actual.limit(limit.actual))

    public actual override fun limit(limit: Expression, offset: Expression?): Limit =
        Limit(actual.limit(limit.actual, offset?.actual))
}