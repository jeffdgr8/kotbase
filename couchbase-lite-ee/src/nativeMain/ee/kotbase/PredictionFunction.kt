package kotbase

public actual class PredictionFunction : Expression() {

    public actual fun propertyPath(path: String): Expression =
        predictiveQueryUnsupported()
}
