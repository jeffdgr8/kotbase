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
 * A Where represents the WHERE clause of the query for filtering the query result.
 */
public expect class Where : Query {

    /**
     * Create and chain a GROUP BY component to group the query result.
     *
     * @param expressions The expression objects.
     * @return The GroupBy object.
     */
    public fun groupBy(vararg expressions: Expression): GroupBy

    /**
     * Create and chain an ORDER BY component for specifying the ORDER BY clause of the query.
     *
     * @param orderings an array of the ORDER BY expressions.
     * @return the ORDER BY component.
     */
    public fun orderBy(vararg orderings: Ordering): OrderBy

    /**
     * Create and chain a LIMIT component to limit the number query results.
     *
     * @param limit The limit Expression object
     * @return The Limit object.
     */
    public fun limit(limit: Expression): Limit

    /**
     * Create and chain a LIMIT component to skip the returned results for the given offset
     * position and to limit the number of results to not more than the given limit value.
     *
     * @param limit  The limit Expression object
     * @param offset The offset Expression object
     * @return The Limit object.
     */
    public fun limit(limit: Expression, offset: Expression?): Limit
}
