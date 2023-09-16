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

import cocoapods.CouchbaseLite.CBLQueryArrayExpression

public actual object ArrayExpression {

    public actual fun any(variable: VariableExpression): ArrayExpressionIn =
        ArrayExpressionIn(CBLQueryArrayExpression.Companion::any, variable.actual)

    public actual fun every(variable: VariableExpression): ArrayExpressionIn =
        ArrayExpressionIn(CBLQueryArrayExpression.Companion::every, variable.actual)

    public actual fun anyAndEvery(variable: VariableExpression): ArrayExpressionIn =
        ArrayExpressionIn(CBLQueryArrayExpression.Companion::anyAndEvery, variable.actual)

    public actual fun variable(name: String): VariableExpression =
        VariableExpression(CBLQueryArrayExpression.variableWithName(name))
}
