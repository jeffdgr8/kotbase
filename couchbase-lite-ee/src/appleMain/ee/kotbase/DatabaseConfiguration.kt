package kotbase

import cocoapods.CouchbaseLite.encryptionKey
import cocoapods.CouchbaseLite.setEncryptionKey

public actual var DatabaseConfiguration.encryptionKey: EncryptionKey?
    get() = actual.encryptionKey?.asEncryptionKey()
    set(value) {
        actual.setEncryptionKey(value?.actual)
    }
