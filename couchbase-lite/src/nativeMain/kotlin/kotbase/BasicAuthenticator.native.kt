package kotbase

import cnames.structs.CBLAuthenticator
import kotbase.internal.fleece.toFLString
import kotlinx.cinterop.CPointer
import libcblite.CBLAuth_CreatePassword
import libcblite.CBLAuth_Free
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.createCleaner

public actual class BasicAuthenticator
actual constructor(
    public actual val username: String,
    password: CharArray
) : Authenticator {

    override val actual: CPointer<CBLAuthenticator> =
        CBLAuth_CreatePassword(username.toFLString(), password.concatToString().toFLString())!!

    @OptIn(ExperimentalNativeApi::class)
    @Suppress("unused")
    private val cleaner = createCleaner(actual) {
        CBLAuth_Free(it)
    }

    public actual val passwordChars: CharArray = password
}
