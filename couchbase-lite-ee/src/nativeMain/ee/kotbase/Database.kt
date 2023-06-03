package kotbase

import kotbase.internal.wrapCBLError
import libcblite.CBLDatabase_ChangeEncryptionKey

@Throws(CouchbaseLiteException::class)
public actual fun Database.changeEncryptionKey(encryptionKey: EncryptionKey?) {
    wrapCBLError { error ->
        CBLDatabase_ChangeEncryptionKey(actual, encryptionKey?.actual, error)
    }
}

public actual val Database.Companion.prediction: Prediction
    get() = predictiveQueryUnsupported()

internal fun predictiveQueryUnsupported(): Nothing =
    throw UnsupportedOperationException("Predictive queries are not supported in CBL C SDK")
