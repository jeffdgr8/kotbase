package kotbase

public actual var DatabaseConfiguration.encryptionKey: EncryptionKey?
    get() = actual.encryptionKey
    set(value) {
        actual.encryptionKey = value
    }
