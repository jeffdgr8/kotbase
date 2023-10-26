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

import cocoapods.CouchbaseLite.CBLQuerySelectResult
import kotbase.internal.DelegatedClass

public actual open class SelectResult
private constructor(actual: CBLQuerySelectResult) : DelegatedClass<CBLQuerySelectResult>(actual) {

    public actual class From
    internal constructor(private val all: (String?) -> CBLQuerySelectResult) : SelectResult(all(null)) {

        public actual fun from(alias: String): SelectResult =
            SelectResult(all(alias))
    }

    public actual class As
    private constructor(
        private val function: (Any, String?) -> CBLQuerySelectResult,
        private val param1: Any,
        param2: String? = null
    ) : SelectResult(function(param1, param2)) {

        internal companion object {

            @Suppress("UNCHECKED_CAST")
            internal operator fun <T : Any> invoke(
                function: (T, String?) -> CBLQuerySelectResult,
                param1: T
            ): As = As(function as (Any, String?) -> CBLQuerySelectResult, param1 as Any)
        }

        public actual fun `as`(alias: String): As {
            return As(function, param1, alias)
        }
    }

    public actual companion object {

        public actual fun property(property: String): As =
            As(CBLQuerySelectResult.Companion::property, property)

        public actual fun expression(expression: Expression): As =
            As(CBLQuerySelectResult.Companion::expression, expression.actual)

        public actual fun all(): From =
            From(CBLQuerySelectResult.Companion::allFrom)
    }
}
