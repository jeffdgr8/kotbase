package kotbase

/**
 * **ENTERPRISE EDITION API**
 *
 * Set a key to encrypt the database with. If the database does not exist and is being created,
 * it will use this key, and the same key must be given every time it's opened
 *
 * @param encryptionKey the key
 * @return The self object.
 */
public fun DatabaseConfiguration.setEncryptionKey(encryptionKey: EncryptionKey?): DatabaseConfiguration {
    this.encryptionKey = encryptionKey
    return this
}

/**
 * **ENTERPRISE EDITION API**
 *
 * A key to encrypt the database with.
 */
public expect var DatabaseConfiguration.encryptionKey: EncryptionKey?

// TODO: provide update EE KTX creator function
