package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.encryptionKey
import cocoapods.CouchbaseLite.setEncryptionKey

public actual fun DatabaseConfiguration.setEncryptionKey(encryptionKey: EncryptionKey?): DatabaseConfiguration {
    actual.setEncryptionKey(encryptionKey?.actual)
    return this
}

public actual var DatabaseConfiguration.encryptionKey: EncryptionKey?
    get() = actual.encryptionKey?.asEncryptionKey()
    set(value) {
        setEncryptionKey(value)
    }
