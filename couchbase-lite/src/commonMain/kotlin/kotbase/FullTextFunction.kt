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
 * Full-text function.
 */
public expect object FullTextFunction {

    /**
     * Creates a full-text expression with the given full-text index name and search text.
     *
     * @param indexName The full-text index name.
     * @param text The search text
     * @return The full-text match expression
     */
    public fun match(indexName: String, text: String): Expression

    /**
     * Creates a full-text rank function with the given full-text index name.
     * The rank function indicates how well the current query result matches
     * the full-text query when performing the match comparison.
     *
     * @param indexName The index name.
     * @return The full-text rank function.
     */
    public fun rank(indexName: String): Expression
}
