package kotbase

import cnames.structs.CBLAuthenticator
import kotlinx.cinterop.CPointer
import libcblite.CBLAuth_Free
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.createCleaner

internal actual class AuthenticatorPlatformState(
    internal val actual: CPointer<CBLAuthenticator>
)

public actual sealed class Authenticator(actual: CPointer<CBLAuthenticator>) {

    internal actual val platformState = AuthenticatorPlatformState(actual)

    @OptIn(ExperimentalNativeApi::class)
    @Suppress("unused")
    private val cleaner = createCleaner(actual) {
        CBLAuth_Free(it)
    }
}

internal val Authenticator.actual: CPointer<CBLAuthenticator>
    get() = platformState.actual
