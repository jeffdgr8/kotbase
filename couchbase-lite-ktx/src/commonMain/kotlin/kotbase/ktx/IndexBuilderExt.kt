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
package kotbase.ktx

import kotbase.*

/**
 * Create a value index with the given properties to be indexed.
 *
 * @param properties The properties to be indexed
 * @return The value index
 */
@Deprecated(
    "Use ValueIndexConfiguration",
    ReplaceWith("ValueIndexConfiguration(*properties)")
)
public fun valueIndex(vararg properties: String): ValueIndex =
    IndexBuilder.valueIndex(
        *properties.map { ValueIndexItem.property(it) }.toTypedArray()
    )

/**
 * Create a full-text search index with the given properties to be
 * used to perform the match operation against with.
 *
 * @param properties Properties used to perform the match operation against with.
 * @return The full-text search index
 */
@Deprecated(
    "Use FullTextIndexConfiguration",
    ReplaceWith("FullTextIndexConfiguration(*properties)")
)
public fun fullTextIndex(vararg properties: String): FullTextIndex =
    IndexBuilder.fullTextIndex(
        *properties.map { FullTextIndexItem.property(it) }.toTypedArray()
    )
