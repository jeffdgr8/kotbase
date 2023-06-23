package kotbase

import kotbase.base.DelegatedClass
import com.couchbase.lite.Prediction as CBLPrediction

public actual class Prediction(actual: CBLPrediction) : DelegatedClass<CBLPrediction>(actual) {

    public actual fun registerModel(name: String, model: PredictiveModel) {
        actual.registerModel(name, model.convert())
    }

    public actual fun unregisterModel(name: String) {
        actual.unregisterModel(name)
    }
}
