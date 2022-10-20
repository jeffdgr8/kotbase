package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.CBLDictionary
import cocoapods.CouchbaseLite.CBLPredictiveModelProtocol
import platform.darwin.NSObject

internal fun PredictiveModel.convert(): CBLPredictiveModelProtocol {
    return object : NSObject(), CBLPredictiveModelProtocol {

        override fun predict(input: CBLDictionary): CBLDictionary? =
            predict(Dictionary(input))?.actual
    }
}
