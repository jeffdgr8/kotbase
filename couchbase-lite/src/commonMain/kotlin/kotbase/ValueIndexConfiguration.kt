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
 * Configuration for a standard database index.
 *
 * @constructor Initializes a value index using an array of SQL++ expression
 * strings, with an optional where clause for partial indexing.
 */
public expect class ValueIndexConfiguration(
    expressions: List<String>,
    where: String? = null
) : IndexConfiguration {

    /**
     * Initializes a value index using an array of SQL++ expression
     * strings, with an optional where clause for partial indexing.
     */
    public constructor(vararg expressions: String, where: String? = null)

    @Deprecated("For binary compatibility", level = DeprecationLevel.HIDDEN)
    public constructor(vararg expressions: String)

    @Deprecated("For binary compatibility", level = DeprecationLevel.HIDDEN)
    public constructor(expressions: List<String>)

    /**
     * A predicate expression defining conditions for indexing documents.
     * Only documents satisfying the predicate are included, enabling partial indexes.
     */
    public fun setWhere(where: String?): ValueIndexConfiguration

    /**
     * A predicate expression defining conditions for indexing documents.
     * Only documents satisfying the predicate are included, enabling partial indexes.
     */
    public var where: String?
}
