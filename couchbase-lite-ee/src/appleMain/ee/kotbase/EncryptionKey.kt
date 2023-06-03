package kotbase

import cocoapods.CouchbaseLite.CBLEncryptionKey
import kotbase.base.DelegatedClass
import kotbase.ext.toNSData

public actual class EncryptionKey
internal constructor(actual: CBLEncryptionKey) : DelegatedClass<CBLEncryptionKey>(actual) {

    public actual constructor(key: ByteArray) : this(CBLEncryptionKey(key.toNSData()))

    public actual constructor(password: String) : this(CBLEncryptionKey(password))
}

internal fun CBLEncryptionKey.asEncryptionKey(): EncryptionKey =
    EncryptionKey(this)
