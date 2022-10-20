package com.couchbase.lite.kmp

import com.udobny.kmp.DelegatedClass

public actual class Prediction(actual: com.couchbase.lite.Prediction) :
    DelegatedClass<com.couchbase.lite.Prediction>(actual) {

    public actual fun registerModel(name: String, model: PredictiveModel) {
        actual.registerModel(name, model.convert())
    }

    public actual fun unregisterModel(name: String) {
        actual.unregisterModel(name)
    }
}
