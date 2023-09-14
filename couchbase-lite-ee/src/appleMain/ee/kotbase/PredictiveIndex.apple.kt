package kotbase

import cocoapods.CouchbaseLite.CBLPredictiveIndex

public actual class PredictiveIndex(actual: CBLPredictiveIndex) : Index(actual)

internal val PredictiveIndex.actual: CBLPredictiveIndex
    get() = platformState!!.actual as CBLPredictiveIndex
