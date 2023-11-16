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
 * Full-text functions.
 */
public expect object FullTextFunction {

    /**
     * Creates a full-text rank function with the given full-text index expression.
     * The rank function indicates how well the current query result matches
     * the full-text query when performing the match comparison.
     *
     * @param index The full-text index expression.
     * @return The full-text rank function.
     */
    public fun rank(index: IndexExpression): Expression

    /**
     * Creates a full-text match() function  with the given full-text index expression and the query text
     *
     * @param index  The full-text index expression.
     * @param query The query string.
     * @return The full-text match() function expression.
     */
    public fun match(index: IndexExpression, query: String): Expression

    /**
     * Creates a full-text rank function with the given full-text index name.
     * The rank function indicates how well the current query result matches
     * the full-text query when performing the match comparison.
     *
     * @param indexName The index name.
     * @return The full-text rank function.
     */
    @Deprecated("Use FullTextFunction.rank(IndexExpression)")
    public fun rank(indexName: String): Expression

    /**
     * Creates a full-text expression with the given full-text index name and search text.
     *
     * @param indexName The full-text index name.
     * @param query     The query string.
     * @return The full-text match expression
     */
    @Deprecated("Use FullTextFunction.match(IndexExpression)")
    public fun match(indexName: String, query: String): Expression
}
