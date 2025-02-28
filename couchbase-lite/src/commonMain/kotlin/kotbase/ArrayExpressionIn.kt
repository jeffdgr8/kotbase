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
 * The In class represents the IN clause object in a quantified operator (ANY/ANY AND EVERY/EVERY
 * &lt;variable name&gt; IN &lt;expr&gt; SATISFIES &lt;expr&gt;). The IN clause is used for specifying an array
 * object or an expression evaluated as an array object, each item of which will be evaluated
 * against the satisfies expression.
 */
public expect class ArrayExpressionIn {

    /**
     * Creates a Satisfies clause object with the given IN clause expression that could be an
     * array object or an expression evaluated as an array object.
     *
     * @param expression the expression evaluated as an array object.
     * @return A Satisfies object.
     */
    public fun `in`(expression: Expression): ArrayExpressionSatisfies
}
