package kotbase

import cnames.structs.CBLAuthenticator
import kotlinx.cinterop.CPointer

@OptIn(ExperimentalMultiplatform::class)
@AllowDifferentMembersInActual
public actual interface Authenticator {

    public val actual: CPointer<CBLAuthenticator>
}
