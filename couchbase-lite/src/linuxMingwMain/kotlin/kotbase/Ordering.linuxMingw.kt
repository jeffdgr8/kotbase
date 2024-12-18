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

public actual sealed class Ordering
private constructor(protected val expression: Expression) {

    internal abstract fun asJSON(): Any?

    public actual class SortOrder
    internal constructor(
        expression: Expression,
        private var isAscending: Boolean = true
    ) : Ordering(expression) {

        public actual fun ascending(): Ordering {
            isAscending = true
            return this
        }

        public actual fun descending(): Ordering {
            isAscending = false
            return this
        }

        override fun asJSON(): Any? {
            if (isAscending) {
                return expression.asJSON()
            }

            return listOf(
                "DESC",
                expression.asJSON()
            )
        }
    }

    public actual companion object {

        public actual fun property(property: String): SortOrder =
            SortOrder(Expression.property(property))

        public actual fun expression(expression: Expression): SortOrder =
            SortOrder(expression)
    }
}
