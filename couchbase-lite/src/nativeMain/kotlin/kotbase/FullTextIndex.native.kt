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

import kotbase.internal.JsonUtils
import kotlinx.cinterop.CValue
import kotlinx.cinterop.cValue
import libcblite.CBLFullTextIndexConfiguration
import libcblite.kCBLJSONLanguage
import platform.posix.strdup
import platform.posix.strlen

public actual class FullTextIndex
internal constructor(private val items: List<FullTextIndexItem>) : Index() {

    public actual fun setLanguage(language: String?): FullTextIndex {
        this.language = language
        return this
    }

    // TODO: this should default to device's default locale, tests check for "en"
    public actual var language: String? = "en"

    public actual fun ignoreAccents(ignoreAccents: Boolean): FullTextIndex {
        isIgnoringAccents = ignoreAccents
        return this
    }

    public actual var isIgnoringAccents: Boolean = false

    private fun getJson(): String {
        val data = buildList {
            items.forEach {
                add(it.expression.asJSON())
            }
        }
        return JsonUtils.toJson(data)
    }

    internal val actual: CValue<CBLFullTextIndexConfiguration>
        get() {
            val json = getJson()
            val lang = language
            return cValue {
                expressionLanguage = kCBLJSONLanguage
                expressions.buf = strdup(json)
                expressions.size = strlen(json)
                language.buf = lang?.let { strdup(it) }
                language.size = lang?.let { strlen(it) } ?: 0U
                ignoreAccents = isIgnoringAccents
            }
        }
}
