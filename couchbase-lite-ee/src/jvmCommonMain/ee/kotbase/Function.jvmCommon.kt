package kotbase

import com.couchbase.lite.Function as CBLFunction

public actual fun Function.prediction(model: String, input: Expression): PredictionFunction =
    PredictionFunction(CBLFunction.prediction(model, input.actual))

public actual fun Function.euclideanDistance(
    expression1: Expression,
    expression2: Expression
): Expression {
    return ExpressionImpl(
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
    return ExpressionImpl(
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
    return ExpressionImpl(
        CBLFunction.cosineDistance(
            expression1.actual,
            expression2.actual
        )
    )
}
