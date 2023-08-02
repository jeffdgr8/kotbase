package kotbase

import com.couchbase.lite.IndexBuilder as CBLIndexBuilder

public actual fun IndexBuilder.predictiveIndex(
    model: String,
    input: Expression,
    properties: List<String>?
): PredictiveIndex {
    return PredictiveIndex(
        CBLIndexBuilder.predictiveIndex(model, input.actual, properties)
    )
}
