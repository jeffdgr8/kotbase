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

import cocoapods.CouchbaseLite.CBLURLEndpointListenerConfiguration
import kotbase.internal.DelegatedClass

public actual class URLEndpointListenerConfiguration
private constructor(
    public actual val database: Database,
    identity: TLSIdentity?,
    authenticator: ListenerAuthenticator?,
    actual: CBLURLEndpointListenerConfiguration
) : DelegatedClass<CBLURLEndpointListenerConfiguration>(actual) {

    public actual constructor(
        database: Database,
        networkInterface: String?,
        port: Int,
        disableTls: Boolean,
        identity: TLSIdentity?,
        authenticator: ListenerAuthenticator?,
        readOnly: Boolean,
        enableDeltaSync: Boolean
    ) : this(
        database,
        identity,
        authenticator,
        CBLURLEndpointListenerConfiguration(database.actual).apply {
            this.networkInterface = networkInterface
            this.port = port.toUShort()
            this.disableTLS = disableTls
            this.tlsIdentity = identity?.actual
            this.authenticator = authenticator?.actual
            this.readOnly = readOnly
            this.enableDeltaSync = enableDeltaSync
        }
    )

    public actual constructor(config: URLEndpointListenerConfiguration) : this(
        config.database,
        config.tlsIdentity,
        config.authenticator,
        CBLURLEndpointListenerConfiguration(config.actual)
    )

    public actual var networkInterface: String?
        get() = actual.networkInterface
        set(value) {
            actual.networkInterface = value
        }

    public actual var port: Int?
        get() = actual.port.toInt()
        set(value) {
            actual.port = value?.toUShort() ?: 0U
        }

    public actual var isTlsDisabled: Boolean
        get() = actual.disableTLS
        set(value) {
            actual.disableTLS = value
        }

    public actual var tlsIdentity: TLSIdentity? = identity
        set(value) {
            field = value
            actual.tlsIdentity = value?.actual
        }

    public actual var authenticator: ListenerAuthenticator? = authenticator
        set(value) {
            field = value
            actual.authenticator = value?.actual
        }

    public actual var isReadOnly: Boolean
        get() = actual.readOnly
        set(value) {
            actual.readOnly = value
        }

    public actual var isDeltaSyncEnabled: Boolean
        get() = actual.enableDeltaSync
        set(value) {
            actual.enableDeltaSync = value
        }

    public actual companion object
}