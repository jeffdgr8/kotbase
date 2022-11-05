@file:JvmName("DatabaseEEJvm") // https://youtrack.jetbrains.com/issue/KT-21186

package com.couchbase.lite.kmp

@Throws(CouchbaseLiteException::class)
public actual fun Database.changeEncryptionKey(encryptionKey: EncryptionKey?) {
    actual.changeEncryptionKey(encryptionKey)
}

public actual val Database.Companion.prediction: Prediction
    get() = staticPrediction

private val staticPrediction by lazy { Prediction(com.couchbase.lite.Database.prediction) }
