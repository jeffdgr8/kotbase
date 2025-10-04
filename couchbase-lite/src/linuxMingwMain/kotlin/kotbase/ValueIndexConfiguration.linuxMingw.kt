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
import libcblite.CBLValueIndexConfiguration
import libcblite.kCBLN1QLLanguage
import platform.posix.strdup
import platform.posix.strlen

public actual class ValueIndexConfiguration
public actual constructor(
    expressions: List<String>,
    where: String?
) : IndexConfiguration(expressions) {

    public actual constructor(vararg expressions: String, where: String?) : this(expressions.asList(), where)

    @Deprecated("For binary compatibility", level = DeprecationLevel.HIDDEN)
    public actual constructor(vararg expressions: String) : this(expressions.asList())

    @Deprecated("For binary compatibility", level = DeprecationLevel.HIDDEN)
    public actual constructor(expressions: List<String>) : this(expressions)

    public actual fun setWhere(where: String?): ValueIndexConfiguration {
        this.where = where
        return this
    }

    public actual var where: String? = null

    internal val actual: CValue<CBLValueIndexConfiguration>
        get() {
            val exp = expressions.joinToString(separator = ",")
            val whr = where
            return cValue {
                expressionLanguage = kCBLN1QLLanguage
                expressions.buf = strdup(exp)
                expressions.size = strlen(exp)
                where.buf = whr?.let { strdup(whr) }
                where.size = whr?.let { strlen(whr) } ?: 0U
            }
        }
}
