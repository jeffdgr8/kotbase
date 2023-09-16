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
private constructor(expressions: List<String>) : IndexConfiguration(expressions) {

    public actual constructor(vararg expressions: String) : this(expressions.toList())

    internal fun getActual(): CValue<CBLValueIndexConfiguration> {
        val exp = expressions.joinToString(separator = ",")
        return cValue {
            expressionLanguage = kCBLN1QLLanguage
            expressions.buf = strdup(exp)
            expressions.size = strlen(exp)
        }
    }
}
