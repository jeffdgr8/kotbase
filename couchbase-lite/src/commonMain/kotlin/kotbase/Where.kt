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

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

/**
 * A Where represents the WHERE clause of the query for filtering the query result.
 */
public expect class Where : Query, GroupByRouter, OrderByRouter, LimitRouter {

    override var parameters: Parameters?

    override fun execute(): ResultSet

    override fun explain(): String

    override fun addChangeListener(listener: QueryChangeListener): ListenerToken

    override fun addChangeListener(context: CoroutineContext, listener: QueryChangeSuspendListener): ListenerToken

    override fun addChangeListener(scope: CoroutineScope, listener: QueryChangeSuspendListener)

    override fun removeChangeListener(token: ListenerToken)

    override fun groupBy(vararg expressions: Expression): GroupBy

    override fun orderBy(vararg orderings: Ordering): OrderBy

    override fun limit(limit: Expression): Limit

    override fun limit(limit: Expression, offset: Expression?): Limit
}