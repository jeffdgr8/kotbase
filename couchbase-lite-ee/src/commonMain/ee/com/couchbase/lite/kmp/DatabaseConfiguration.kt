package com.couchbase.lite.kmp

/**
 * **ENTERPRISE EDITION API**
 *
 * Set a key to encrypt the database with. If the database does not exist and is being created,
 * it will use this key, and the same key must be given every time it's opened
 *
 * @param encryptionKey the key
 * @return The self object.
 */
public expect fun DatabaseConfiguration.setEncryptionKey(encryptionKey: EncryptionKey?): DatabaseConfiguration

/**
 * **ENTERPRISE EDITION API**
 *
 * Returns a key to encrypt the database with.
 *
 * @return the key
 */
public expect var DatabaseConfiguration.encryptionKey: EncryptionKey?
