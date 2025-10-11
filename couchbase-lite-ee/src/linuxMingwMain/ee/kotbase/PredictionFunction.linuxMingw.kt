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

public actual class PredictionFunction
internal constructor(
    model: String,
    private val input: Expression
) : Expression() {

    private val model = string(model)

    public actual fun propertyPath(path: String): Expression =
        getPredictionFunction(model, input, string(".$path"))

    override fun asJSON(): Any {
        return getPredictionFunction(model, input).asJSON()
    }

    private fun getPredictionFunction(vararg params: Expression): FunctionExpression =
        FunctionExpression("PREDICTION()", params.asList())
}
