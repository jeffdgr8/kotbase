package kotbase

import com.couchbase.lite.PredictionFunction as CBLPredictionFunction

public actual class PredictionFunction(
    override val actual: CBLPredictionFunction
) : Expression(actual) {

    public actual fun propertyPath(path: String): Expression =
        Expression(actual.propertyPath(path))
}
