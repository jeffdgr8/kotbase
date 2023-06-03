package kotbase

import com.couchbase.lite.Prediction
import kotbase.base.DelegatedClass

public actual class Prediction(actual: com.couchbase.lite.Prediction) :
    DelegatedClass<Prediction>(actual) {

    public actual fun registerModel(name: String, model: PredictiveModel) {
        actual.registerModel(name, model.convert())
    }

    public actual fun unregisterModel(name: String) {
        actual.unregisterModel(name)
    }
}
