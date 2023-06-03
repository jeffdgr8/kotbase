package kotbase

public actual fun Function.prediction(model: String, input: Expression): PredictionFunction =
    PredictionFunction(com.couchbase.lite.Function.prediction(model, input.actual))

public actual fun Function.euclideanDistance(
    expression1: Expression,
    expression2: Expression
): Expression {
    return Expression(
        com.couchbase.lite.Function.euclideanDistance(
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
        com.couchbase.lite.Function.squaredEuclideanDistance(
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
        com.couchbase.lite.Function.cosineDistance(
            expression1.actual,
            expression2.actual
        )
    )
}
