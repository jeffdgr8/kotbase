package com.couchbase.lite.kmp

public actual fun IndexBuilder.predictiveIndex(
    model: String,
    input: Expression,
    properties: List<String>?
): PredictiveIndex {
    return PredictiveIndex(
        com.couchbase.lite.IndexBuilder.predictiveIndex(model, input.actual, properties)
    )
}
