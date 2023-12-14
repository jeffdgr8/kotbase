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
import libcblite.CBLValueIndexConfiguration
import libcblite.kCBLJSONLanguage
import platform.posix.strdup
import platform.posix.strlen

public actual class ValueIndex
internal constructor(private val items: List<ValueIndexItem>) : Index() {

    private fun getJson(): String {
        val data = buildList {
            items.forEach {
                add(it.expression.asJSON())
            }
        }
        return JsonUtils.toJson(data)
    }

    internal val actual: CValue<CBLValueIndexConfiguration>
        get() {
            val json = getJson()
            return cValue {
                expressionLanguage = kCBLJSONLanguage
                expressions.buf = strdup(json)
                expressions.size = strlen(json)
            }
        }
}
