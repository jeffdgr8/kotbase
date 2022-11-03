package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.CBLDatabase
import cocoapods.CouchbaseLite.changeEncryptionKey
import cocoapods.CouchbaseLite.prediction
import com.couchbase.lite.kmp.ext.wrapCBLError

@Throws(CouchbaseLiteException::class)
public actual fun Database.changeEncryptionKey(encryptionKey: EncryptionKey?) {
    wrapCBLError { error ->
        actual.changeEncryptionKey(encryptionKey?.actual, error)
    }
}

public actual val Database.Companion.prediction: Prediction
    get() = staticPrediction

private val staticPrediction by lazy { Prediction(CBLDatabase.prediction()) }
