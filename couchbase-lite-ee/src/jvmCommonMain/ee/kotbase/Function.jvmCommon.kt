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

import com.couchbase.lite.Function as CBLFunction

public actual fun Function.prediction(model: String, input: Expression): PredictionFunction =
    PredictionFunction(CBLFunction.prediction(model, input.actual))

public actual fun Function.euclideanDistance(
    expression1: Expression,
    expression2: Expression
): Expression {
    return Expression(
        CBLFunction.euclideanDistance(
            expression1.actual,
            expression2.actual
        )
    )
}

public actual fun Function.squaredEuclideanDistance(
    expression1: Expression,
    expression2: Expression
): Expression {
    return Expression(
        CBLFunction.squaredEuclideanDistance(
            expression1.actual,
            expression2.actual
        )
    )
}

public actual fun Function.cosineDistance(
    expression1: Expression,
    expression2: Expression
): Expression {
    return Expression(
        CBLFunction.cosineDistance(
            expression1.actual,
            expression2.actual
        )
    )
}
