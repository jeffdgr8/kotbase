package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.changeEncryptionKey
import com.couchbase.lite.kmp.ext.throwError

@Throws(CouchbaseLiteException::class)
public actual fun Database.changeEncryptionKey(encryptionKey: EncryptionKey?) {
    throwError { error ->
        changeEncryptionKey(encryptionKey?.actual, error)
    }
}
