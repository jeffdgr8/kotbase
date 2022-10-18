package com.couchbase.lite.kmp

public actual fun DatabaseConfiguration.setEncryptionKey(encryptionKey: EncryptionKey?): DatabaseConfiguration {
    actual.encryptionKey = encryptionKey
    return this
}

public actual var DatabaseConfiguration.encryptionKey: EncryptionKey?
    get() = actual.encryptionKey
    set(value) {
        actual.encryptionKey = value
    }
