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

import com.couchbase.lite.ArrayIndexConfiguration as CBLArrayIndexConfiguration

public actual class ArrayIndexConfiguration
private constructor(override val actual: CBLArrayIndexConfiguration) : IndexConfiguration(actual) {

    public actual constructor(path: String) : this(CBLArrayIndexConfiguration(path))

    public actual constructor(path: String, expression: String, vararg expressions: String) : this(
        CBLArrayIndexConfiguration(path, expression, *expressions)
    )

    public actual constructor(path: String, expressions: List<String>?) : this(
        CBLArrayIndexConfiguration(path, expressions)
    )

    public actual val path: String
        get() = actual.path
}
