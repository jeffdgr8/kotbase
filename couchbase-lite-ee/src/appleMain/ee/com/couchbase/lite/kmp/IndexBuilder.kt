package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.CBLIndexBuilder
import cocoapods.CouchbaseLite.predictiveIndexWithModel

public actual fun IndexBuilder.predictiveIndex(
    model: String,
    input: Expression,
    properties: List<String>?
): PredictiveIndex {
    return PredictiveIndex(
        CBLIndexBuilder.predictiveIndexWithModel(model, input.actual, properties)
    )
}
