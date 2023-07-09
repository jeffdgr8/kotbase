package kotbase

import com.couchbase.lite.PredictiveModel as CBLPredictiveModel

internal fun PredictiveModel.convert(): CBLPredictiveModel {
    return CBLPredictiveModel { input ->
        predict(Dictionary(input))?.actual
    }
}
