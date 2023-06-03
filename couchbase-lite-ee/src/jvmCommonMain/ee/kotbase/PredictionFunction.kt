package kotbase

public actual class PredictionFunction(
    override val actual: com.couchbase.lite.PredictionFunction
) : Expression(actual) {

    public actual fun propertyPath(path: String): Expression =
        Expression(actual.propertyPath(path))
}
