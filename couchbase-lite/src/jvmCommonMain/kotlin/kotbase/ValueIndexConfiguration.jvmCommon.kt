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

import com.couchbase.lite.ValueIndexConfiguration as CBLValueIndexConfiguration

public actual class ValueIndexConfiguration
private constructor(override val actual: CBLValueIndexConfiguration) : IndexConfiguration(actual) {

    public actual constructor(
        expressions: List<String>,
        where: String?
    ) : this(CBLValueIndexConfiguration(expressions)) {
        actual.where = where
    }

    public actual constructor(vararg expressions: String, where: String?) : this(expressions.asList(), where)

    @Deprecated("For binary compatibility", level = DeprecationLevel.HIDDEN)
    public actual constructor(vararg expressions: String) : this(expressions.asList())

    @Deprecated("For binary compatibility", level = DeprecationLevel.HIDDEN)
    public actual constructor(expressions: List<String>) : this(expressions)

    public actual fun setWhere(where: String?): ValueIndexConfiguration {
        actual.where = where
        return this
    }

    public actual var where: String?
        get() = actual.where
        set(value) {
            actual.where = value
        }
}
