/*
 * Copyright 2024 Jeff Lockhart
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
 * Configuration for indexing property values within nested arrays
 * in documents, intended for use with the UNNEST query.
 */
public expect class ArrayIndexConfiguration : IndexConfiguration {

    /**
     * Initializes the configuration with paths to the nested array with
     * no expressions constraining the values within the arrays to be indexed.
     *
     * @param path Path to the array, which can be nested to be indexed.
     *             Use "[]" to represent a property that is an array of each
     *             nested array level. For a single array or the last level
     *             array, the "[]" is optional. For instance, use
     *             "contacts[].phones" to specify an array of phones within each
     *             contact.
     */
    public constructor(path: String)

    /**
     * Initializes the configuration with paths to the nested array
     * and the expressions for the values within the arrays to be indexed.
     * A null expression will cause a runtime error.
     *
     * @param path        Path to the array, which can be nested to be indexed.
     *                    Use "[]" to represent a property that is an array of each
     *                    nested array level. For a single array or the last level
     *                    array, the "[]" is optional. For instance, use
     *                    "contacts[].phones" to specify an array of phones within each
     *                    contact.
     * @param expressions A list of strings, where each string represents an expression
     *                    defining the values within the array to be indexed. Expressions
     *                    may not be null.
     */
    public constructor(path: String, expression: String, vararg expressions: String)

    /**
     * Initializes the configuration with paths to the nested array
     * and the expressions for the values within the arrays to be indexed.
     *
     * @param path        Path to the array, which can be nested to be indexed.
     *                    Use "[]" to represent a property that is an array of each
     *                    nested array level. For a single array or the last level
     *                    array, the "[]" is optional. For instance, use
     *                    "contacts[].phones" to specify an array of phones within each
     *                    contact.
     * @param expressions An optional list of strings, where each string represents an expression
     *                    defining the values within the array to be indexed. If the array specified
     *                    by the path contains scalar values, this parameter can be null:
     *                    see <code>ArrayIndexConfiguration(String)</code>
     */
    public constructor(path: String, expressions: List<String>?)

    /**
     * Path to the array, which can be nested.
     */
    public val path: String
}
