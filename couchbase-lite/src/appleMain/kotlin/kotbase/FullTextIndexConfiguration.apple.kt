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

import cocoapods.CouchbaseLite.CBLFullTextIndexConfiguration
import platform.Foundation.NSLocale
import platform.Foundation.NSLocaleLanguageCode
import platform.Foundation.currentLocale

public actual class FullTextIndexConfiguration
private constructor(override var actual: CBLFullTextIndexConfiguration) : IndexConfiguration(actual) {

    public actual constructor(
        expressions: List<String>,
        where: String?,
        ignoreAccents: Boolean,
        language: String?
    ) : this(
        CBLFullTextIndexConfiguration(
            expressions,
            where,
            ignoreAccents,
            if (language == NOT_SPECIFIED) {
                NSLocale.currentLocale.objectForKey(NSLocaleLanguageCode) as String?
            } else {
                language
            }
        )
    )

    @Deprecated(
        "Use FullTextIndexConfiguration(List<String>)",
        ReplaceWith("FullTextIndexConfiguration(listOf(*expressions))")
    )
    public actual constructor(vararg expressions: String) : this(expressions.asList())

    @Deprecated("For binary compatibility", level = DeprecationLevel.HIDDEN)
    public actual constructor(expressions: List<String>) : this(expressions)

    @Suppress("DEPRECATION")
    @Deprecated("Use constructor parameter")
    public actual fun setLanguage(language: String?): FullTextIndexConfiguration {
        this.language = language
        return this
    }

    @set:Deprecated("Use constructor parameter")
    public actual var language: String?
        get() = actual.language
        set(value) {
            actual = CBLFullTextIndexConfiguration(
                actual.expressions,
                actual.where,
                actual.ignoreAccents,
                value
            )
        }

    @Suppress("DEPRECATION")
    @Deprecated("Use constructor parameter")
    public actual fun ignoreAccents(ignoreAccents: Boolean): FullTextIndexConfiguration {
        isIgnoringAccents = ignoreAccents
        return this
    }

    @set:Deprecated("Use constructor parameter")
    public actual var isIgnoringAccents: Boolean
        get() = actual.ignoreAccents
        set(value) {
            actual = CBLFullTextIndexConfiguration(
                actual.expressions,
                actual.where,
                value,
                actual.language
            )
        }

    public actual val where: String?
        get() = actual.where
}
