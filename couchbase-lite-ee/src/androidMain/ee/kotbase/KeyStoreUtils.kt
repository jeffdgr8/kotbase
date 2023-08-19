package kotbase

import java.io.IOException
import java.io.InputStream
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.UnrecoverableEntryException
import java.security.cert.CertificateException
import com.couchbase.lite.KeyStoreUtils as CBLKeyStoreUtils

/**
 * Key Store Utilities
 */
public object KeyStoreUtils {

    /**
     * Imports the key entry including public key, private key, and certificates from the given
     * KeyStore input stream into the Android KeyStore. The imported key entry can be used as a
     * TLSIdentity by calling TLSIdentity.get(String alias) method.
     *
     * NOTE:
     * The key data including the private key data will be in memory, temporarily, during the import operation!
     * Android 9 (API 28) or higher has an alternative method that will import keys more securely.
     * Check the documentation
     * [here](https://developer.android.com/training/articles/keystore#ImportingEncryptedKeys)
     * for more info.
     *
     * @param storeType     KeyStore type, eg: "PKCS12"
     * @param storeStream   An InputStream from the keystore
     * @param storePassword The keystore password
     * @param extAlias      The alias, in the external keystore, of the entry to be imported.
     * @param extKeyPass    The key password
     * @param newAlias      The alias for the imported key
     * @throws KeyStoreException           on failure to create keystore
     * @throws CertificateException        on failure to load keystore
     * @throws NoSuchAlgorithmException    on failure to load keystore
     * @throws IOException                 on failure to load keystore
     * @throws UnrecoverableEntryException on failure to load keystore entry
     */
    @Throws(
        KeyStoreException::class,
        CertificateException::class,
        NoSuchAlgorithmException::class,
        IOException::class,
        UnrecoverableEntryException::class
    )
    public fun importEntry(
        storeType: String,
        storeStream: InputStream,
        storePassword: CharArray?,
        extAlias: String,
        extKeyPass: CharArray?,
        newAlias: String
    ) {
        CBLKeyStoreUtils.importEntry(
            storeType,
            storeStream,
            storePassword,
            extAlias,
            extKeyPass,
            newAlias
        )
    }
}
