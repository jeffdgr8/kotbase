package kotbase

import cocoapods.CouchbaseLite.CBLQueryPredictionFunction

public actual class PredictionFunction(
    override val actual: CBLQueryPredictionFunction
) : Expression(actual) {

    public actual fun propertyPath(path: String): Expression =
        Expression(actual.property(path))
}
