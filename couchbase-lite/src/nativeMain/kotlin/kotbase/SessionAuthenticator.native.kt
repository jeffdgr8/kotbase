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

import cnames.structs.CBLAuthenticator
import kotbase.internal.fleece.toFLString
import kotlinx.cinterop.CPointer
import libcblite.CBLAuth_CreateSession

public actual class SessionAuthenticator
private constructor(
    public actual val sessionID: String,
    public actual val cookieName: String,
    actual: CPointer<CBLAuthenticator>
) : Authenticator(actual) {

    public actual constructor(sessionID: String) : this(sessionID, null)

    public actual constructor(sessionID: String, cookieName: String?) : this(
        sessionID,
        cookieName ?: DEFAULT_SYNC_GATEWAY_SESSION_ID_NAME,
        CBLAuth_CreateSession(
            sessionID.toFLString(),
            (cookieName ?: DEFAULT_SYNC_GATEWAY_SESSION_ID_NAME).toFLString()
        )!!
    )

    private companion object {
        private const val DEFAULT_SYNC_GATEWAY_SESSION_ID_NAME = "SyncGatewaySession"
    }
}
