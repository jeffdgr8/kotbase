@file:JvmName("DatabaseEEJvm") // https://youtrack.jetbrains.com/issue/KT-21186

package kotbase

import com.couchbase.lite.Database as CBLDatabase

@Throws(CouchbaseLiteException::class)
public actual fun Database.changeEncryptionKey(encryptionKey: EncryptionKey?) {
    actual.changeEncryptionKey(encryptionKey)
}

public actual val Database.Companion.prediction: Prediction
    get() = staticPrediction

private val staticPrediction by lazy { Prediction(CBLDatabase.prediction) }
