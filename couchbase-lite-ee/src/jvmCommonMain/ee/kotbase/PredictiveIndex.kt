package kotbase

import com.couchbase.lite.PredictiveIndex as CBLPredictiveIndex

public actual class PredictiveIndex(actual: CBLPredictiveIndex) : Index(actual)
