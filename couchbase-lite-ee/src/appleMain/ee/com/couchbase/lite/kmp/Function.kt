package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.*

public actual fun Function.prediction(model: String, input: Expression): PredictionFunction =
    PredictionFunction(CBLQueryFunction.predictionUsingModel(model, input.actual))

public actual fun Function.euclideanDistance(
    expression1: Expression,
    expression2: Expression
): Expression {
    return Expression(
        CBLQueryFunction.euclideanDistanceBetween(
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
        CBLQueryFunction.squaredEuclideanDistanceBetween(
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
        CBLQueryFunction.cosineDistanceBetween(
            expression1.actual,
            expression2.actual
        )
    )
}