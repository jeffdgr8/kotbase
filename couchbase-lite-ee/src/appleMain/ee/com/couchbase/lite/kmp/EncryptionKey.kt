package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.CBLEncryptionKey
import com.udobny.kmp.DelegatedClass
import com.udobny.kmp.ext.toNSData

public actual class EncryptionKey
internal constructor(actual: CBLEncryptionKey) : DelegatedClass<CBLEncryptionKey>(actual) {

    public actual constructor(key: ByteArray) : this(CBLEncryptionKey(key.toNSData()))

    public actual constructor(password: String) : this(CBLEncryptionKey(password))
}

internal fun CBLEncryptionKey.asEncryptionKey(): EncryptionKey =
    EncryptionKey(this)
