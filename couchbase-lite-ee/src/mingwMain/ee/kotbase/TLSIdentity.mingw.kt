/*
 * Copyright 2022-2023 Jeff Lockhart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kotbase

import kotbase.internal.fleece.toFLString
import kotbase.internal.wrapCBLError
import kotlinx.cinterop.memScoped
import libcblite.CBLTLSIdentity_DeleteIdentityWithLabel
import libcblite.CBLTLSIdentity_IdentityWithLabel

// On Windows, identities are persisted in the CNG Key Storage Provider.

internal actual fun getTLSIdentity(label: String): TLSIdentity? {
    return wrapCBLError { error ->
        memScoped {
            CBLTLSIdentity_IdentityWithLabel(label.toFLString(this), error)
        }
    }?.asTLSIdentity()
}

internal actual fun deleteTLSIdentity(label: String) {
    wrapCBLError { error ->
        memScoped {
            CBLTLSIdentity_DeleteIdentityWithLabel(label.toFLString(this), error)
        }
    }
}
