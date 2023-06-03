package kotbase

public actual fun IndexBuilder.predictiveIndex(
    model: String,
    input: Expression,
    properties: List<String>?
): PredictiveIndex =
    predictiveQueryUnsupported()
