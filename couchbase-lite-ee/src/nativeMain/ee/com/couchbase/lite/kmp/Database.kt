package com.couchbase.lite.kmp

import com.couchbase.lite.kmp.internal.wrapCBLError
import libcblite.CBLDatabase_ChangeEncryptionKey

@Throws(CouchbaseLiteException::class)
public actual fun Database.changeEncryptionKey(encryptionKey: EncryptionKey?) {
    wrapCBLError { error ->
        CBLDatabase_ChangeEncryptionKey(actual, encryptionKey?.actual, error)
    }
}
