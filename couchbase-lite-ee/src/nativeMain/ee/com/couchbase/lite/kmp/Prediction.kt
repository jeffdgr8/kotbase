package com.couchbase.lite.kmp

public actual class Prediction {

    public actual fun registerModel(name: String, model: PredictiveModel) {
        predictiveQueryUnsupported()
    }

    public actual fun unregisterModel(name: String) {
        predictiveQueryUnsupported()
    }
}
