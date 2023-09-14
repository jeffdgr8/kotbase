package kotbase

import com.couchbase.lite.PredictiveIndex as CBLPredictiveIndex

public actual class PredictiveIndex(actual: CBLPredictiveIndex) : Index(actual)

internal val PredictiveIndex.actual: CBLPredictiveIndex
    get() = platformState!!.actual as CBLPredictiveIndex
