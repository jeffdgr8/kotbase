package kotbase

public actual fun Function.prediction(model: String, input: Expression): PredictionFunction =
    predictiveQueryUnsupported()

public actual fun Function.euclideanDistance(
    expression1: Expression,
    expression2: Expression
): Expression =
    predictiveQueryUnsupported()

public actual fun Function.squaredEuclideanDistance(
    expression1: Expression,
    expression2: Expression
): Expression =
    predictiveQueryUnsupported()

public actual fun Function.cosineDistance(
    expression1: Expression,
    expression2: Expression
): Expression =
    predictiveQueryUnsupported()
