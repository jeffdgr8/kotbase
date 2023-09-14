package kotbase

import com.couchbase.lite.PredictionFunction as CBLPredictionFunction

public actual class PredictionFunction(actual: CBLPredictionFunction) : Expression(actual) {

    public actual fun propertyPath(path: String): Expression =
        DelegatedExpression(actual.propertyPath(path))
}

internal val PredictionFunction.actual: CBLPredictionFunction
    get() = platformState!!.actual as CBLPredictionFunction
