package kotbase

import cocoapods.CouchbaseLite.CBLDatabase
import cocoapods.CouchbaseLite.changeEncryptionKey
import cocoapods.CouchbaseLite.prediction
import kotbase.ext.wrapCBLError

@Throws(CouchbaseLiteException::class)
public actual fun Database.changeEncryptionKey(encryptionKey: EncryptionKey?) {
    wrapCBLError { error ->
        actual.changeEncryptionKey(encryptionKey?.actual, error)
    }
}

public actual val Database.Companion.prediction: Prediction
    get() = staticPrediction

private val staticPrediction by lazy { Prediction(CBLDatabase.prediction()) }
