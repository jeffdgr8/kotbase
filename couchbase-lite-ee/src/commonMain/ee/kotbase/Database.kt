package kotbase

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

/**
 * **ENTERPRISE EDITION API**
 *
 * The predictive model manager for registering and unregistering predictive models.
 * This is part of the Public API.
 */
public expect val Database.Companion.prediction: Prediction
