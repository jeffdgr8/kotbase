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

internal const val NOT_SPECIFIED = "NOT_SPECIFIED"

/**
 * Configuration for creating full-text indexes.
 *
 * @constructor Initializes a full-text index using an array of SQL++ expression
 * strings, with an optional where clause for partial indexing.
 */
public expect class FullTextIndexConfiguration(
    expressions: List<String>,
    where: String? = null,
    ignoreAccents: Boolean = Defaults.FullTextIndex.IGNORE_ACCENTS,
    language: String? = NOT_SPECIFIED
) : IndexConfiguration {

    @Deprecated(
        "Use FullTextIndexConfiguration(List<String>)",
        ReplaceWith("FullTextIndexConfiguration(listOf(*expressions))")
    )
    public constructor(vararg expressions: String)

    @Deprecated("For binary compatibility", level = DeprecationLevel.HIDDEN)
    public constructor(expressions: List<String>)

    /**
     * The language code which is an ISO-639 language such as "en", "fr", etc.
     * Setting the language code affects how word breaks and word stems are parsed.
     * If not explicitly set, the current locale's language will be used. Setting
     * a null, empty, or unrecognized value will disable the language features.
     */
    @Deprecated("Use constructor parameter")
    public fun setLanguage(language: String?): FullTextIndexConfiguration

    /**
     * The language code which is an ISO-639 language such as "en", "fr", etc.
     * Setting the language code affects how word breaks and word stems are parsed.
     * If not explicitly set, the current locale's language will be used. Setting
     * a null, empty, or unrecognized value will disable the language features.
     */
    @set:Deprecated("Use constructor parameter")
    public var language: String?

    /**
     * Whether to ignore accents/diacritical marks.
     * The default value is [Defaults.FullTextIndex.IGNORE_ACCENTS].
     */
    @Deprecated("Use constructor parameter")
    public fun ignoreAccents(ignoreAccents: Boolean): FullTextIndexConfiguration

    /**
     * Whether to ignore accents/diacritical marks.
     * The default value is [Defaults.FullTextIndex.IGNORE_ACCENTS].
     */
    @set:Deprecated("Use constructor parameter")
    public var isIgnoringAccents: Boolean

    /**
     * A predicate expression defining conditions for indexing documents.
     * Only documents satisfying the predicate are included, enabling partial indexes.
     */
    public val where: String?
}
