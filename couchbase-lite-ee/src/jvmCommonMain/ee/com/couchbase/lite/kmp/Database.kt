package com.couchbase.lite.kmp

@Throws(CouchbaseLiteException::class)
public actual fun Database.changeEncryptionKey(encryptionKey: EncryptionKey?) {
    actual.changeEncryptionKey(encryptionKey)
}
