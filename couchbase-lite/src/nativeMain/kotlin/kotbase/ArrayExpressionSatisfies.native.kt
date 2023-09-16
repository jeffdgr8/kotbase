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

public actual class ArrayExpressionSatisfies
internal constructor(
    private val type: ArrayExpression.QuantifiesType,
    private val variable: VariableExpression,
    private val inExpression: Expression
) {

    private class QuantifiedExpression(
        val type: ArrayExpression.QuantifiesType,
        val variable: VariableExpression,
        val inExpression: Expression,
        val satisfiesExpression: Expression
    ) : Expression() {

        override fun asJSON(): Any {
            return buildList {
                when (type) {
                    ArrayExpression.QuantifiesType.ANY -> add("ANY")
                    ArrayExpression.QuantifiesType.ANY_AND_EVERY -> add("ANY AND EVERY")
                    ArrayExpression.QuantifiesType.EVERY -> add("EVERY")
                }
                add(variable.name)
                add(inExpression.asJSON())
                add(satisfiesExpression.asJSON())
            }
        }
    }

    public actual fun satisfies(expression: Expression): Expression =
        QuantifiedExpression(type, variable, inExpression, expression)
}
