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

import kotbase.internal.DelegatedClass
import com.couchbase.lite.SelectResult as CBLSelectResult

public actual open class SelectResult
private constructor(actual: CBLSelectResult) : DelegatedClass<CBLSelectResult>(actual) {

    public actual class From
    internal constructor(override val actual: CBLSelectResult.From) : SelectResult(actual) {

        public actual infix fun from(alias: String): SelectResult {
            actual.from(alias)
            return this
        }
    }

    public actual class As
    internal constructor(override val actual: CBLSelectResult.As) : SelectResult(actual) {

        public actual infix fun `as`(alias: String): As {
            actual.`as`(alias)
            return this
        }
    }

    public actual companion object {

        public actual fun property(property: String): As =
            As(CBLSelectResult.property(property))

        public actual fun expression(expression: Expression): As =
            As(CBLSelectResult.expression(expression.actual))

        public actual fun all(): From =
            From(CBLSelectResult.all())
    }
}
