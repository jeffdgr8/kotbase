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

/**
 * The Satisfies class represents the SATISFIES clause object in a quantified operator
 * (ANY/ANY AND EVERY/EVERY &lt;variable name&gt; IN &lt;expr&gt; SATISFIES &lt;expr&gt;).
 * The SATISFIES clause is used for specifying an expression that will be used to evaluate
 * each item in the array.
 */
public expect class ArrayExpressionSatisfies {

    /**
     * Creates a complete quantified operator with the given satisfies expression.
     *
     * @param expression Parameter expression: The satisfies expression used for evaluating each item in the array.
     * @return The quantified expression.
     */
    public fun satisfies(expression: Expression): Expression
}
