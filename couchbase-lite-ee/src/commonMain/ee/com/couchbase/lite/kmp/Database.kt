package com.couchbase.lite.kmp

/**
 * **ENTERPRISE EDITION API**
 *
 * Changes the database's encryption key, or removes encryption if the new key is null.
 *
 * @param encryptionKey The encryption key
 * @throws CouchbaseLiteException on error
 */
@Throws(CouchbaseLiteException::class)
public expect fun Database.changeEncryptionKey(encryptionKey: EncryptionKey?)

