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

public actual open class SelectResult
private constructor(
    protected open val expression: Expression,
    protected var alias: String? = null
) {

    internal fun asJSON(): Any? {
        if (alias == null) {
            return expression.asJSON()
        }
        return listOf(
            "AS",
            expression.asJSON(),
            alias
        )
    }

    public actual class From
    internal constructor(override val expression: PropertyExpression) : SelectResult(expression) {

        public actual infix fun from(alias: String): SelectResult =
            SelectResult(expression.from(alias))
    }

    public actual class As
    internal constructor(expression: Expression) : SelectResult(expression) {

        public actual infix fun `as`(alias: String): As {
            this.alias = alias
            return this
        }
    }

    public actual companion object {

        public actual fun property(property: String): As =
            As(Expression.property(property))

        public actual fun expression(expression: Expression): As =
            As(expression)

        public actual fun all(): From =
            From(Expression.all())
    }
}
