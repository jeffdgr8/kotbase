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

import kotlinx.cinterop.CValue
import kotlinx.cinterop.cValue
import libcblite.CBLArrayIndexConfiguration
import libcblite.kCBLN1QLLanguage
import platform.posix.strdup
import platform.posix.strlen

public actual class ArrayIndexConfiguration
public actual constructor(
    public actual val path: String,
    expressions: List<String>?
) : IndexConfiguration(expressions ?: emptyList()) {

    public actual constructor(path: String) : this(path, null)

    public actual constructor(path: String, expression: String, vararg expressions: String) : this(
        path,
        listOf(expression) + expressions
    )

    internal val actual: CValue<CBLArrayIndexConfiguration>
        get() {
            val exp = expressions.joinToString(separator = ",")
            val path = path
            return cValue {
                expressionLanguage = kCBLN1QLLanguage
                expressions.buf = strdup(exp)
                expressions.size = strlen(exp)
                this.path.buf = strdup(path)
                this.path.size = strlen(path)
            }
        }
}