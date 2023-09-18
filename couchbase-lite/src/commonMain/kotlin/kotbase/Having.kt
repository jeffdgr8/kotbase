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
 * Having represents a HAVING clause of the query statement used for filtering the aggregated values
 * from the GROUP BY clause.
 */
public expect class Having : Query, LimitRouter {

    /**
     * Create and chain an ORDER BY component for specifying the orderings of the query result.
     *
     * @param orderings an array of the ORDER BY expressions.
     * @return the ORDER BY component.
     */
    public fun orderBy(vararg orderings: Ordering): OrderBy

    public override fun limit(limit: Expression): Limit

    public override fun limit(limit: Expression, offset: Expression?): Limit
}
