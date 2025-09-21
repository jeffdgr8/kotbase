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

import kotlinx.cinterop.CValue
import kotlinx.cinterop.cValue
import libcblite.CBLFullTextIndexConfiguration
import libcblite.kCBLN1QLLanguage
import platform.posix.strdup
import platform.posix.strlen

public actual class FullTextIndexConfiguration
public actual constructor(
    expressions: List<String>,
    where: String?,
    ignoreAccents: Boolean,
    language: String?
) : IndexConfiguration(expressions) {

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
    // TODO: this should default to device's default locale, tests check for "en"
    public actual var language: String? = if (language == NOT_SPECIFIED) "en" else language

    @Suppress("DEPRECATION")
    @Deprecated("Use constructor parameter")
    public actual fun ignoreAccents(ignoreAccents: Boolean): FullTextIndexConfiguration {
        isIgnoringAccents = ignoreAccents
        return this
    }

    @set:Deprecated("Use constructor parameter")
    public actual var isIgnoringAccents: Boolean = ignoreAccents

    public actual val where: String? = where

    internal val actual: CValue<CBLFullTextIndexConfiguration>
        get() {
            val exp = expressions.joinToString(separator = ",")
            val lang = language
            val whr = where
            return cValue {
                expressionLanguage = kCBLN1QLLanguage
                expressions.buf = strdup(exp)
                expressions.size = strlen(exp)
                language.buf = lang?.let { strdup(it) }
                language.size = lang?.let { strlen(it) } ?: 0U
                where.buf = whr?.let { strdup(it) }
                where.size = whr?.let { strlen(it) } ?: 0U
                ignoreAccents = isIgnoringAccents
            }
        }
}
