package com.couchbase.lite.kmp

public actual var DatabaseConfiguration.encryptionKey: EncryptionKey?
    get() = actual.encryptionKey
    set(value) {
        actual.encryptionKey = value
    }
