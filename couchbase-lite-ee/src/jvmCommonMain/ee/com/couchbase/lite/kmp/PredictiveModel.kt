package com.couchbase.lite.kmp

internal fun PredictiveModel.convert(): com.couchbase.lite.PredictiveModel {
    return com.couchbase.lite.PredictiveModel { input ->
        predict(Dictionary(input))?.actual
    }
}
