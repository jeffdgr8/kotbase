package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.CBLPrediction
import com.udobny.kmp.DelegatedClass

public actual class Prediction(actual: CBLPrediction) : DelegatedClass<CBLPrediction>(actual) {

    public actual fun registerModel(name: String, model: PredictiveModel) {
        actual.registerModel(model.convert(), name)
    }

    public actual fun unregisterModel(name: String) {
        actual.unregisterModelWithName(name)
    }
}
