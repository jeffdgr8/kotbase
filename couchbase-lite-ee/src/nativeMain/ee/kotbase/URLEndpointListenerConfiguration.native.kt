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

public actual class URLEndpointListenerConfiguration private constructor() {

    init {
        urlEndpointListenerUnsupported()
    }

    public actual constructor(
        database: Database,
        networkInterface: String?,
        port: Int,
        disableTls: Boolean,
        identity: TLSIdentity?,
        authenticator: ListenerAuthenticator?,
        readOnly: Boolean,
        enableDeltaSync: Boolean
    ) : this()

    public actual constructor(config: URLEndpointListenerConfiguration) : this()

    public actual val database: Database

    public actual var networkInterface: String?

    public actual var port: Int?

    public actual var isTlsDisabled: Boolean

    public actual var tlsIdentity: TLSIdentity?

    public actual var authenticator: ListenerAuthenticator?

    public actual var isReadOnly: Boolean

    public actual var isDeltaSyncEnabled: Boolean
}
